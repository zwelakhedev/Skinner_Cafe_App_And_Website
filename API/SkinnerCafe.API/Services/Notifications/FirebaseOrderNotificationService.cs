using FirebaseAdmin.Messaging;
using Microsoft.EntityFrameworkCore;
using SkinnerCafe.API.Data;

namespace SkinnerCafe.API.Services.Notifications;

public class FirebaseOrderNotificationService : IOrderNotificationService
{
    private readonly SkinnerCafeDbContext _dbContext;
    private readonly ILogger<FirebaseOrderNotificationService> _logger;

    public FirebaseOrderNotificationService(
        SkinnerCafeDbContext dbContext,
        ILogger<FirebaseOrderNotificationService> logger)
    {
        _dbContext = dbContext;
        _logger = logger;
    }

    public async Task SendOrderStatusNotificationAsync(
        string identityUserId,
        string orderId,
        string orderNumber,
        string status,
        CancellationToken cancellationToken = default)
    {
        // Only notify customers about these staff-driven status changes.
        if (status is not ("Confirmed" or "Ready"))
        {
            return;
        }

        var deviceTokens = await _dbContext.DeviceTokens
            .Where(device =>
                device.IdentityUserId == identityUserId &&
                device.IsActive)
            .ToListAsync(cancellationToken);

        if (deviceTokens.Count == 0)
        {
            _logger.LogInformation(
                "No active devices found for customer {IdentityUserId}.",
                identityUserId);

            return;
        }

        var title = status == "Confirmed"
            ? "Order confirmed!"
            : "Order ready for collection!";

        var body = status == "Confirmed"
            ? $"Your order {orderNumber} has been confirmed."
            : $"Your order {orderNumber} is ready for collection.";

        try
        {
            // Firebase supports a maximum of 500 registration tokens per batch.
            foreach (var batch in deviceTokens.Chunk(500))
            {
                var message = new MulticastMessage
                {
#pragma warning disable CS0618
                    Tokens = batch.Select(device => device.Token).ToList(),
#pragma warning restore CS0618

                    // Data-only payload: Android controls notification display.
                    Data = new Dictionary<string, string>
                    {
                        ["title"] = title,
                        ["body"] = body,
                        ["orderId"] = orderId,
                        ["orderNumber"] = orderNumber,
                        ["status"] = status
                    }
                };

                var response = await FirebaseMessaging.DefaultInstance
                    .SendEachForMulticastAsync(
                        message,
                        cancellationToken);

                for (var i = 0; i < response.Responses.Count; i++)
                {
                    var sendResult = response.Responses[i];

                    if (
                        !sendResult.IsSuccess &&
                        sendResult.Exception is FirebaseMessagingException exception &&
                        exception.MessagingErrorCode == MessagingErrorCode.Unregistered
                    )
                    {
                        batch[i].IsActive = false;
                        batch[i].UpdatedAt = DateTime.UtcNow;

                        _logger.LogInformation(
                            "Deactivated an unregistered FCM device token.");
                    }
                }
            }

            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (Exception exception) when (
            exception is not OperationCanceledException)
        {
            _logger.LogError(
                exception,
                "Failed to send order status notification for order {OrderNumber}.",
                orderNumber);
        }
    }
}
