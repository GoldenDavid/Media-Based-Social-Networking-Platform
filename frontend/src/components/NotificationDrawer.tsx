import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { X, Heart, MessageCircle, Image as ImageIcon } from 'lucide-react';
import { type ProfileDto } from '../services/api';
import './NotificationDrawer.css';

interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  username: string; // The logged-in user's username
}

interface Notification {
  id: number;
  fromUser: ProfileDto;
  notificationType: 'NEW_POST' | 'LIKE_YOUR_POST' | 'COMMENT_YOUR_POST';
  postId?: number;
  createdAt: string;
}

const NotificationDrawer = ({ isOpen, onClose, username }: NotificationDrawerProps) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    // WebSocket path: matches gateway route `/gs-guide-websocket/**` exposed
    // by `notification-service` in api-gateway/application.yml. The frontend
    // nginx strips `/api` before forwarding to the gateway on :8080, so the
    // client uses `/api/gs-guide-websocket`.
    // SockJS runtime supports `withCredentials`; the bundled @types
    // declaration omits it, so cast the options bag. This sends the session
    // cookie on the initial SockJS info/XHR handshake.
    const socket = new SockJS('/api/gs-guide-websocket', null, {
      withCredentials: true,
    } as SockJS.Options);
    const client = new Client({
      webSocketFactory: () => socket as any,
      connectHeaders: {},
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
      case 'LIKE_YOUR_POST': return <Heart size={16} className="notif-icon heart" />;
      case 'COMMENT_YOUR_POST': return <MessageCircle size={16} className="notif-icon comment" />;
      case 'NEW_POST': return <ImageIcon size={16} className="notif-icon follow" />;
      default: return null;
    }
  };

  const renderMessage = (notif: Notification) => {
    const username = notif.fromUser?.username || 'Someone';
    switch (notif.notificationType) {
      case 'LIKE_YOUR_POST': return <span><strong className="text-primary">{username}</strong> liked your post.</span>;
      case 'COMMENT_YOUR_POST': return <span><strong className="text-primary">{username}</strong> commented on your post.</span>;
      case 'NEW_POST': return <span><strong className="text-primary">{username}</strong> published a new post.</span>;
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
                  <img src={notif.fromUser?.profileImageUrl || 'https://i.pravatar.cc/150'} alt={notif.fromUser?.username} className="avatar-img notif-avatar" />
                  <div className="icon-badge">
                    {renderIcon(notif.notificationType)}
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
