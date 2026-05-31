import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { X, Heart, MessageCircle, UserPlus } from 'lucide-react';
import './NotificationDrawer.css';

interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  username: string; // The logged-in user's username
}

interface Notification {
  id: number;
  fromUser: string;
  avatarUrl: string;
  type: 'LIKE' | 'COMMENT' | 'FOLLOW';
  postId?: number;
  createdAt: string;
}

const NotificationDrawer = ({ isOpen, onClose, username }: NotificationDrawerProps) => {
  const [notifications, setNotifications] = useState<Notification[]>([
    {
      id: 1,
      fromUser: 'neon_dreamer',
      avatarUrl: 'https://i.pravatar.cc/150?img=32',
      type: 'LIKE',
      createdAt: 'Just now'
    },
    {
      id: 2,
      fromUser: 'cyber_ninja',
      avatarUrl: 'https://i.pravatar.cc/150?img=11',
      type: 'COMMENT',
      createdAt: '2 hours ago'
    }
  ]);

  useEffect(() => {
    // Only connect if the drawer is open or if we want background notifications
    const socket = new SockJS('/api/gs-guide-websocket');
    const client = new Client({
      webSocketFactory: () => socket as any,
      debug: function (str) {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('Connected to WebSocket');
      // Subscribe to personal notifications
      client.subscribe(`/topic/notifications/${username}`, (message) => {
        if (message.body) {
          const newNotif = JSON.parse(message.body);
          setNotifications(prev => [newNotif, ...prev]);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [username]);

  const renderIcon = (type: string) => {
    switch (type) {
      case 'LIKE': return <Heart size={16} className="notif-icon heart" />;
      case 'COMMENT': return <MessageCircle size={16} className="notif-icon comment" />;
      case 'FOLLOW': return <UserPlus size={16} className="notif-icon follow" />;
      default: return null;
    }
  };

  const renderMessage = (notif: Notification) => {
    switch (notif.type) {
      case 'LIKE': return <span><strong className="text-primary">{notif.fromUser}</strong> liked your post.</span>;
      case 'COMMENT': return <span><strong className="text-primary">{notif.fromUser}</strong> commented on your post.</span>;
      case 'FOLLOW': return <span><strong className="text-primary">{notif.fromUser}</strong> started following you.</span>;
      default: return <span>New notification</span>;
    }
  };

  return (
    <>
      {/* Overlay */}
      <div 
        className={`drawer-overlay ${isOpen ? 'open' : ''}`} 
        onClick={onClose}
      />
      
      {/* Drawer */}
      <div className={`notification-drawer glass-panel ${isOpen ? 'open' : ''}`}>
        <div className="drawer-header">
          <h2>Notifications</h2>
          <button className="btn-icon" onClick={onClose}>
            <X size={24} />
          </button>
        </div>
        
        <div className="drawer-content">
          {notifications.length === 0 ? (
            <div className="empty-state">No new notifications</div>
          ) : (
            notifications.map(notif => (
              <div key={notif.id} className="notification-item animate-fade-in">
                <div className="avatar-ring notif-avatar-ring">
                  <img src={notif.avatarUrl || 'https://i.pravatar.cc/150'} alt={notif.fromUser} className="avatar-img notif-avatar" />
                  <div className="icon-badge">
                    {renderIcon(notif.type)}
                  </div>
                </div>
                <div className="notif-text">
                  <p>{renderMessage(notif)}</p>
                  <span className="time-ago">{notif.createdAt}</span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </>
  );
};

export default NotificationDrawer;
