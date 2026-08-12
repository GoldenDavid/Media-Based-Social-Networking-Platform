import { memo, useState } from 'react';
import { Heart, MessageCircle, Share2, MoreHorizontal, Bookmark, X, Send, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { api, type PostDto, type UserPrincipal } from '../services/api';
import './PostCard.css';

interface PostCardProps {
  post: PostDto;
  currentUser: UserPrincipal | null;
  onPostUpdated?: (updatedPost: PostDto) => void;
  onPostDeleted?: (postId: number) => void;
}

const PostCard = memo(({ post: initialPost, currentUser, onPostUpdated, onPostDeleted }: PostCardProps) => {
  const [post, setPost] = useState<PostDto>(initialPost);
  const [isZoomed, setIsZoomed] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [commentText, setCommentText] = useState('');
  const [isLiking, setIsLiking] = useState(false);
  const [isCommenting, setIsCommenting] = useState(false);
  const navigate = useNavigate();

  const hasLiked = currentUser ? post.userLikes?.some(u => u.username === currentUser.username) : false;
  const isPostOwner = currentUser && post.createdBy?.username === currentUser.username;

  const handleLikeToggle = async () => {
    if (!currentUser || isLiking) return;
    setIsLiking(true);
    try {
      const updatedPost = hasLiked 
        ? await api.unlikePost(post.id)
        : await api.likePost(post.id);
      setPost(updatedPost);
      if (onPostUpdated) onPostUpdated(updatedPost);
    } catch (error) {
      console.error('Failed to toggle like', error);
    } finally {
      setIsLiking(false);
    }
  };

  const submitComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim() || !currentUser || isCommenting) return;
    
    setIsCommenting(true);
    try {
      const updatedPost = await api.commentOnPost(post.id, commentText.trim());
      setPost(updatedPost);
      setCommentText('');
      if (onPostUpdated) onPostUpdated(updatedPost);
    } catch (error) {
      console.error('Failed to post comment', error);
    } finally {
      setIsCommenting(false);
    }
  };

  const handleDeletePost = async () => {
    if (!window.confirm("Are you sure you want to delete this post?")) return;
    try {
      await api.deletePost(post.id);
      if (onPostDeleted) onPostDeleted(post.id);
    } catch (error) {
      console.error('Failed to delete post', error);
      alert('Failed to delete post');
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    if (!window.confirm("Are you sure you want to delete this comment?")) return;
    try {
      await api.deleteComment(commentId);
      // Remove comment locally
      const updatedPost = { ...post, comments: post.comments.filter(c => c.id !== commentId) };
      setPost(updatedPost);
      if (onPostUpdated) onPostUpdated(updatedPost);
    } catch (error) {
      console.error('Failed to delete comment', error);
      alert('Failed to delete comment');
    }
  };

  const navigateToProfile = (userId?: number) => {
    if (userId) {
      navigate(`/profile/${userId}`);
    }
  };

  const username = post.createdBy?.username || 'Unknown';
  const avatarUrl = post.createdBy?.profileImageUrl || '/default-avatar.svg';
  const timeAgo = new Date(post.createdAt).toLocaleDateString();

  return (
    <>
      <article className="post-card glass-panel animate-fade-in">
        <div className="post-header">
          <div className="post-user-info" onClick={() => navigateToProfile(post.createdBy?.id)} style={{ cursor: 'pointer' }}>
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
              <button className="btn-icon" onClick={handleDeletePost} title="Delete Post">
                <Trash2 size={20} className="text-danger" />
              </button>
            )}
            <button className="btn-icon">
              <MoreHorizontal size={20} />
            </button>
          </div>
        </div>
        
        <div className="post-image-container" onClick={() => setIsZoomed(true)}>
          <img src={post.imageUrl} alt="Post content" className="post-image" />
        </div>
        
        <div className="post-actions">
          <div className="action-group">
            <button 
              className={`btn-icon action-btn like-btn ${hasLiked ? 'liked' : ''}`}
              onClick={handleLikeToggle}
              disabled={!currentUser || isLiking}
            >
              <Heart size={24} fill={hasLiked ? 'currentColor' : 'none'} />
            </button>
            <button className="btn-icon action-btn" onClick={() => setShowComments(!showComments)}>
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
            <span className="text-gradient font-bold">{post.userLikes?.length || 0} likes</span>
          </div>
          <div className="caption-container">
            <span className="username font-bold" onClick={() => navigateToProfile(post.createdBy?.id)} style={{ cursor: 'pointer' }}>{username}</span>
            <span className="caption">{post.caption}</span>
          </div>
          
          {(post.comments?.length || 0) > 0 && !showComments && (
            <button className="view-comments" onClick={() => setShowComments(true)}>
              View all {post.comments?.length} comments
            </button>
          )}

          {showComments && (
            <div className="comments-section animate-fade-in">
              <div className="comments-list">
                {post.comments?.map(c => (
                  <div key={c.id} className="comment-item" style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                    <div>
                      <span className="username font-bold" onClick={() => navigateToProfile(c.createdBy?.id)} style={{ cursor: 'pointer' }}>{c.createdBy?.username || 'Unknown'}</span>
                      <span className="comment-text" style={{ marginLeft: '8px' }}>{c.comment}</span>
                    </div>
                    {currentUser && c.createdBy?.username === currentUser.username && (
                      <button className="btn-icon" onClick={() => handleDeleteComment(c.id)} title="Delete Comment" style={{ padding: '0 4px' }}>
                        <X size={14} className="text-danger" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
              {currentUser && (
                <form className="comment-form" onSubmit={submitComment}>
                  <input
                    type="text"
                    placeholder="Add a comment..."
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
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
          )}
        </div>
      </article>

      {isZoomed && (
        <div className="zoom-modal-overlay" onClick={() => setIsZoomed(false)}>
          <button className="zoom-close-btn" onClick={() => setIsZoomed(false)}>
            <X size={32} />
          </button>
          <img src={post.imageUrl} alt="Zoomed content" className="zoomed-image animate-fade-in" onClick={e => e.stopPropagation()} />
        </div>
      )}
    </>
  );
});

export default PostCard;
PostCard.displayName = 'PostCard';
