import { useEffect, useState } from 'react';
import { Heart, MessageCircle, Image as ImageIcon } from 'lucide-react';
import { api, type NotificationDto } from '../services/api';

const Notifications = () => {
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const fetchHistory = async () => {
      try {
        const resp = await api.getMyNotifications(1, 50);
        if (!cancelled) {
          setNotifications(resp.notifications || []);
        }
      } catch (err) {
        if (!cancelled) {
          setError((err as Error).message);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };
    void fetchHistory();
    return () => { cancelled = true; };
  }, []);

  const renderIcon = (type: string) => {
    switch (type) {
      case 'LIKE_YOUR_POST': return <Heart size={16} className="notif-icon heart" />;
      case 'COMMENT_YOUR_POST': return <MessageCircle size={16} className="notif-icon comment" />;
      case 'NEW_POST': return <ImageIcon size={16} className="notif-icon follow" />;
      default: return null;
    }
  };

  const renderMessage = (notif: NotificationDto) => {
    const username = notif.fromUser?.username || 'Someone';
    switch (notif.notificationType) {
      case 'LIKE_YOUR_POST': return <span><strong className="text-primary">{username}</strong> liked your post.</span>;
      case 'COMMENT_YOUR_POST': return <span><strong className="text-primary">{username}</strong> commented on your post.</span>;
      case 'NEW_POST': return <span><strong className="text-primary">{username}</strong> published a new post.</span>;
      default: return <span>New notification</span>;
    }
  };

  return (
    <div className="home-container animate-fade-in">
      <header className="home-header">
        <h1 className="text-gradient">Notifications</h1>
      </header>

      <div className="feed-container">
        {loading ? (
          <div className="loading-spinner">Loading...</div>
        ) : error ? (
          <div className="error-message">Could not load notifications: {error}</div>
        ) : notifications.length === 0 ? (
          <div className="empty-state">No new notifications</div>
        ) : (
          notifications.map(notif => (
            <div key={notif.id} className="glass-panel" style={{ padding: '16px', display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' }}>
              <div className="avatar-ring notif-avatar-ring">
                <img src={notif.fromUser?.profileImageUrl || 'https://i.pravatar.cc/150'} alt={notif.fromUser?.username} style={{ width: '48px', height: '48px', borderRadius: '50%', objectFit: 'cover' }} />
                <div style={{ position: 'absolute', bottom: '-4px', right: '-4px', background: 'var(--card-bg)', borderRadius: '50%', padding: '2px' }}>
                  {renderIcon(notif.notificationType)}
                </div>
              </div>
              <div style={{ flex: 1 }}>
                <p style={{ margin: 0, fontSize: '1rem' }}>{renderMessage(notif)}</p>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{notif.createdAt}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default Notifications;
