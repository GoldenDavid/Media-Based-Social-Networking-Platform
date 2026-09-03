import { type PostDto } from '../services/api';

interface ProfileGridProps {
  posts: PostDto[];
  onSelectPost: (post: PostDto) => void;
}

export const ProfileGrid = ({ posts, onSelectPost }: ProfileGridProps) => {
  if (posts.length === 0) {
    return <div className="empty-state">No posts yet.</div>;
  }

  return (
    <div className="profile-grid">
      {posts.map((post) => (
        <div key={post.id} className="grid-item" onClick={() => onSelectPost(post)} style={{ cursor: 'pointer' }}>
          <img src={post.imageUrl} alt={`Post ${post.id}`} />
          <div className="grid-item-overlay">
            <span>❤️ {post.userLikes?.length || 0}</span>
            <span>💬 {post.comments?.length || 0}</span>
          </div>
        </div>
      ))}
    </div>
  );
};
