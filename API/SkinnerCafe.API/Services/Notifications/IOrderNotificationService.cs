namespace SkinnerCafe.API.Services.Notifications;

public interface IOrderNotificationService
{
    Task SendOrderStatusNotificationAsync(
        string identityUserId,
        string orderId,
        string orderNumber,
        string status,
        CancellationToken cancellationToken = default);
}