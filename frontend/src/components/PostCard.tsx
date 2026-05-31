import { Heart, MessageCircle, Share2, MoreHorizontal, Bookmark } from 'lucide-react';
import './PostCard.css';

interface PostCardProps {
  username: string;
  avatarUrl: string;
  imageUrl: string;
  caption: string;
  likes: number;
  timeAgo: string;
}

const PostCard = ({ username, avatarUrl, imageUrl, caption, likes, timeAgo }: PostCardProps) => {
  return (
    <article className="post-card glass-panel animate-fade-in">
      <div className="post-header">
        <div className="post-user-info">
          <div className="avatar-ring">
            <img src={avatarUrl} alt={username} className="avatar-img" />
          </div>
          <div className="user-meta">
            <span className="username">{username}</span>
            <span className="time-ago">{timeAgo}</span>
          </div>
        </div>
        <button className="btn-icon">
          <MoreHorizontal size={20} />
        </button>
      </div>
      
      <div className="post-image-container">
        <img src={imageUrl} alt="Post content" className="post-image" />
      </div>
      
      <div className="post-actions">
        <div className="action-group">
          <button className="btn-icon action-btn like-btn">
            <Heart size={24} />
          </button>
          <button className="btn-icon action-btn">
            <MessageCircle size={24} />
          </button>
          <button className="btn-icon action-btn">
            <Share2 size={24} />
          </button>
        </div>
        <button className="btn-icon action-btn">
          <Bookmark size={24} />
        </button>
      </div>
      
      <div className="post-content">
        <div className="likes-count">
          <span className="text-gradient font-bold">{likes} likes</span>
        </div>
        <div className="caption-container">
          <span className="username font-bold">{username}</span>
          <span className="caption">{caption}</span>
        </div>
        <button className="view-comments">View all comments</button>
      </div>
    </article>
  );
};

export default PostCard;
