import { Heart, MessageCircle, Share2, Bookmark } from 'lucide-react';
import { type UserPrincipal } from '../services/api';

interface PostActionsProps {
  hasLiked: boolean;
  hasSaved: boolean;
  currentUser: UserPrincipal | null;
  isLiking: boolean;
  isSaving: boolean;
  onLikeToggle: () => void;
  onSaveToggle: () => void;
  onToggleComments: () => void;
}

export const PostActions = ({
  hasLiked,
  hasSaved,
  currentUser,
  isLiking,
  isSaving,
  onLikeToggle,
  onSaveToggle,
  onToggleComments
}: PostActionsProps) => {
  return (
    <div className="post-actions">
      <div className="action-group">
        <button 
          className={`btn-icon action-btn like-btn ${hasLiked ? 'liked' : ''}`}
          onClick={onLikeToggle}
          disabled={!currentUser || isLiking}
        >
          <Heart size={24} fill={hasLiked ? 'currentColor' : 'none'} />
        </button>
        <button className="btn-icon action-btn" onClick={onToggleComments}>
          <MessageCircle size={24} />
        </button>
        <button className="btn-icon action-btn">
          <Share2 size={24} />
        </button>
      </div>
      <button 
        className={`btn-icon action-btn save-btn ${hasSaved ? 'saved' : ''}`}
        onClick={onSaveToggle}
        disabled={!currentUser || isSaving}
      >
        <Bookmark size={24} fill={hasSaved ? 'currentColor' : 'none'} />
      </button>
    </div>
  );
};
