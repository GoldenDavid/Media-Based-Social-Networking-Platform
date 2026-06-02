import { useCallback, useEffect, useState } from 'react';
import { Settings, Grid, Bookmark, X } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { api, type ProfileDto, type PostDto } from '../services/api';
import PostCard from '../components/PostCard';
import './Profile.css';

const Profile = () => {
  const { user, loading: authLoading } = useAuth();
  const [profile, setProfile] = useState<ProfileDto | null>(null);
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedPost, setSelectedPost] = useState<PostDto | null>(null);



  const handlePostUpdated = useCallback((updated: PostDto) => {
    setSelectedPost(updated);
    setPosts(prev => prev.map(p => p.id === updated.id ? updated : p));
  }, []);

  useEffect(() => {
    if (authLoading || !user) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    const fetchProfileData = async () => {
      try {
        const profileRes = await api.getMyProfile();
        if (cancelled) return;
        setProfile(profileRes.profile);
        
        if (profileRes.profile?.id) {
          const postsRes = await api.getUserPosts(profileRes.profile.id);
          if (!cancelled) setPosts(postsRes.posts || []);
        }
      } catch (error) {
        console.error("Failed to fetch profile data:", error);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchProfileData();
    return () => { cancelled = true; };
  }, [authLoading, user]);

  if (loading) {
    return <div className="loading-spinner">Loading...</div>;
  }



  if (!profile) {
    return <div className="empty-state">Failed to load profile.</div>;
  }

  return (
    <div className="profile-container animate-fade-in">
      <header className="profile-header glass-panel">
        <div className="profile-avatar-container">
          <div className="avatar-ring-large">
            <img src={profile.profileImageUrl || 'https://i.pravatar.cc/150'} alt="Avatar" className="profile-avatar" />
          </div>
        </div>
        
        <div className="profile-info">
          <div className="profile-actions">
            <h2 className="profile-username">{profile.username}</h2>
            <button className="btn-primary">Edit Profile</button>
            <button className="btn-icon">
              <Settings size={24} />
            </button>
          </div>
          
          <div className="profile-stats">
            <span><strong className="text-primary">{posts.length}</strong> posts</span>
            <span><strong className="text-primary">-</strong> followers</span>
            <span><strong className="text-primary">-</strong> following</span>
          </div>
          
          <div className="profile-bio">
            <h3 className="font-bold">{profile.displayName}</h3>
            <p>{profile.bio}</p>
          </div>
        </div>
      </header>
      
      <div className="profile-tabs">
        <button className="tab active">
          <Grid size={18} /> POSTS
        </button>
        <button className="tab">
          <Bookmark size={18} /> SAVED
        </button>
      </div>
      
      <div className="profile-grid">
        {posts.length === 0 ? (
          <div className="empty-state">No posts yet.</div>
        ) : (
          posts.map((post) => (
            <div key={post.id} className="grid-item" onClick={() => setSelectedPost(post)} style={{ cursor: 'pointer' }}>
              <img src={post.imageUrl} alt={`Post ${post.id}`} />
              <div className="grid-item-overlay">
                <span>❤️ {post.userLikes?.length || 0}</span>
                <span>💬 {post.comments?.length || 0}</span>
              </div>
            </div>
          ))
        )}
      </div>

      {selectedPost && (
        <div className="zoom-modal-overlay" onClick={() => setSelectedPost(null)}>
          <button className="zoom-close-btn" onClick={() => setSelectedPost(null)}>
            <X size={32} />
          </button>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{ width: '100%', maxWidth: '500px' }}>
            <PostCard 
              post={selectedPost} 
              currentUser={user} 
              onPostUpdated={handlePostUpdated} 
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;
