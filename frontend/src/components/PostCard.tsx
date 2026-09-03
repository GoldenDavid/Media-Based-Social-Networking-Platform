import { memo, useState } from 'react';
import { X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { api, type PostDto, type UserPrincipal } from '../services/api';
import { PostHeader } from './PostHeader';
import { PostActions } from './PostActions';
import { PostComments } from './PostComments';
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
  const [isSaving, setIsSaving] = useState(false);
  const [isCommenting, setIsCommenting] = useState(false);
  const navigate = useNavigate();

  const hasLiked = currentUser ? post.userLikes?.some(u => u.username === currentUser.username) : false;
  const hasSaved = currentUser ? post.userSaves?.some(u => u.username === currentUser.username) : false;
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

  const handleSaveToggle = async () => {
    if (!currentUser || isSaving) return;
    setIsSaving(true);
    try {
      const updatedPost = hasSaved 
        ? await api.unsavePost(post.id)
        : await api.savePost(post.id);
      setPost(updatedPost);
      if (onPostUpdated) onPostUpdated(updatedPost);
    } catch (error) {
      console.error('Failed to toggle save', error);
    } finally {
      setIsSaving(false);
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
        <PostHeader
          username={username}
          avatarUrl={avatarUrl}
          timeAgo={timeAgo}
          isPostOwner={isPostOwner ?? false}
          onNavigateToProfile={() => navigateToProfile(post.createdBy?.id)}
          onDeletePost={handleDeletePost}
        />
        
        <div className="post-image-container" onClick={() => setIsZoomed(true)}>
          <img src={post.imageUrl} alt="Post content" className="post-image" />
        </div>
        
        <PostActions
          hasLiked={hasLiked ?? false}
          hasSaved={hasSaved ?? false}
          currentUser={currentUser}
          isLiking={isLiking}
          isSaving={isSaving}
          onLikeToggle={handleLikeToggle}
          onSaveToggle={handleSaveToggle}
          onToggleComments={() => setShowComments(!showComments)}
        />
        
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
            <PostComments
              comments={post.comments}
              currentUser={currentUser}
              commentText={commentText}
              isCommenting={isCommenting}
              onCommentTextChange={setCommentText}
              onSubmitComment={submitComment}
              onDeleteComment={handleDeleteComment}
              onNavigateToProfile={navigateToProfile}
            />
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
