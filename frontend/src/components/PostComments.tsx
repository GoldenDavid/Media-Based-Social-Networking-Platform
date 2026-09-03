import { X, Send } from 'lucide-react';
import { type UserPrincipal, type CommentDto } from '../services/api';

interface PostCommentsProps {
  comments: CommentDto[];
  currentUser: UserPrincipal | null;
  commentText: string;
  isCommenting: boolean;
  onCommentTextChange: (text: string) => void;
  onSubmitComment: (e: React.FormEvent) => void;
  onDeleteComment: (commentId: number) => void;
  onNavigateToProfile: (userId?: number) => void;
}

export const PostComments = ({
  comments,
  currentUser,
  commentText,
  isCommenting,
  onCommentTextChange,
  onSubmitComment,
  onDeleteComment,
  onNavigateToProfile
}: PostCommentsProps) => {
  return (
    <div className="comments-section animate-fade-in">
      <div className="comments-list">
        {comments?.map(c => (
          <div key={c.id} className="comment-item" style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
            <div>
              <span className="username font-bold" onClick={() => onNavigateToProfile(c.createdBy?.id)} style={{ cursor: 'pointer' }}>{c.createdBy?.username || 'Unknown'}</span>
              <span className="comment-text" style={{ marginLeft: '8px' }}>{c.comment}</span>
            </div>
            {currentUser && c.createdBy?.username === currentUser.username && (
              <button className="btn-icon" onClick={() => onDeleteComment(c.id)} title="Delete Comment" style={{ padding: '0 4px' }}>
                <X size={14} className="text-danger" />
              </button>
            )}
          </div>
        ))}
      </div>
      {currentUser && (
        <form className="comment-form" onSubmit={onSubmitComment}>
          <input
            type="text"
            placeholder="Add a comment..."
            value={commentText}
            onChange={(e) => onCommentTextChange(e.target.value)}
            disabled={isCommenting}
            maxLength={2000}
          />
          <button 
            type="submit" 
            className="post-comment-btn" 
            disabled={!commentText.trim() || isCommenting}
          >
            <Send size={18} />
          </button>
        </form>
      )}
    </div>
  );
};
