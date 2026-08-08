import { useCallback, useEffect, useState } from 'react';
import { Grid, X } from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { api, type ProfileDto, type PostDto } from '../services/api';
import PostCard from '../components/PostCard';
import FollowButton from '../components/FollowButton';
import './Profile.css';

const UserProfile = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, profileId, loading: authLoading } = useAuth();
  const [profile, setProfile] = useState<ProfileDto | null>(null);
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [followerCount, setFollowerCount] = useState<number | null>(null);
  const [followingCount, setFollowingCount] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedPost, setSelectedPost] = useState<PostDto | null>(null);

  const handlePostUpdated = useCallback((updated: PostDto) => {
    setSelectedPost(updated);
    setPosts(prev => prev.map(p => p.id === updated.id ? updated : p));
  }, []);

  const handlePostDeleted = useCallback((postId: number) => {
    setSelectedPost(null);
    setPosts(prev => prev.filter(p => p.id !== postId));
  }, []);

  useEffect(() => {
    if (authLoading || !id) return;
    let cancelled = false;
    const fetchProfileData = async () => {
      try {
        const userId = parseInt(id, 10);
        const profileData = await api.getUserProfile(userId);
        if (cancelled) return;
        setProfile(profileData);

        if (profileData?.id) {
          const userPosts = await api.getUserPosts(profileData.id);
          if (!cancelled) setPosts(userPosts || []);

          try {
            const followers = await api.getFollowers(profileData.id, 1, 1000);
            if (!cancelled) {
              setFollowerCount(followers.totalCount);
            }
          } catch {
            if (!cancelled) setFollowerCount(0);
          }

          try {
            const followings = await api.getFollowings(profileData.id, 1, 1);
            if (!cancelled) setFollowingCount(followings.totalCount);
          } catch {
            if (!cancelled) setFollowingCount(0);
          }
        }
      } catch (error) {
        console.error("Failed to fetch profile data:", error);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    void Promise.resolve().then(fetchProfileData);
    return () => { cancelled = true; };
  }, [authLoading, id, user]);

  if (loading) {
    return <div className="loading-spinner">Loading...</div>;
  }

  if (!profile) {
    return <div className="empty-state">User not found.</div>;
  }

  const isCurrentUser = profileId === profile.id;

  return (
    <>
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
            {isCurrentUser ? (
              <button className="btn-primary" onClick={() => navigate('/profile')}>Edit Profile</button>
            ) : (
              <FollowButton userId={profile.id} />
            )}
          </div>
          
          <div className="profile-stats">
            <span><strong className="text-primary">{posts.length}</strong> posts</span>
            <span><strong className="text-primary">{followerCount ?? '-'}</strong> followers</span>
            <span><strong className="text-primary">{followingCount ?? '-'}</strong> following</span>
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
              onPostDeleted={handlePostDeleted}
            />
          </div>
        </div>
      )}
    </>
  );
};

export default UserProfile;
