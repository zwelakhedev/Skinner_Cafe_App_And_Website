using System.Collections.Concurrent;
using SkinnerCafe.API.Services.Notifications;

namespace SkinnerCafe.API.Tests;

public sealed record CapturedOrderNotification(
    string IdentityUserId,
    string OrderId,
    string OrderNumber,
    string Status
);

public sealed class TestOrderNotificationService : IOrderNotificationService
{
    private readonly ConcurrentQueue<CapturedOrderNotification> _notifications = new();

    public IReadOnlyCollection<CapturedOrderNotification> Notifications =>
        _notifications.ToArray();

    public void Clear()
    {
        while (_notifications.TryDequeue(out _))
        {
        }
    }

    public Task SendOrderStatusNotificationAsync(
        string identityUserId,
        string orderId,
        string orderNumber,
        string status,
        CancellationToken cancellationToken = default)
    {
        _notifications.Enqueue(
            new CapturedOrderNotification(
                identityUserId,
                orderId,
                orderNumber,
                status
            )
        );

        return Task.CompletedTask;
    }
}