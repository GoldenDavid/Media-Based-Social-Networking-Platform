import { MoreHorizontal, Trash2 } from 'lucide-react';

interface PostHeaderProps {
  username: string;
  avatarUrl: string;
  timeAgo: string;
  isPostOwner: boolean;
  onNavigateToProfile: () => void;
  onDeletePost: () => void;
}

export const PostHeader = ({
  username,
  avatarUrl,
  timeAgo,
  isPostOwner,
  onNavigateToProfile,
  onDeletePost
}: PostHeaderProps) => {
  return (
    <div className="post-header">
      <div className="post-user-info" onClick={onNavigateToProfile} style={{ cursor: 'pointer' }}>
        <div className="avatar-ring">
          <img src={avatarUrl} alt={username} className="avatar-img" />
        </div>
        <div className="user-meta">
          <span className="username">{username}</span>
          <span className="time-ago">{timeAgo}</span>
        </div>
      </div>
      <div className="action-group">
        {isPostOwner && (
          <button className="btn-icon" onClick={onDeletePost} title="Delete Post">
            <Trash2 size={20} className="text-danger" />
          </button>
        )}
        <button className="btn-icon">
          <MoreHorizontal size={20} />
        </button>
      </div>
    </div>
  );
};
