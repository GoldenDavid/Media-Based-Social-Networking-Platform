import { useCallback, useEffect, useState } from 'react';
import { Settings, Grid, Bookmark, X, Camera } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { api, type ProfileDto, type PostDto } from '../services/api';
import PostCard from '../components/PostCard';
import './Profile.css';

const Profile = () => {
  const { user, loading: authLoading } = useAuth();
  const [profile, setProfile] = useState<ProfileDto | null>(null);
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [followerCount, setFollowerCount] = useState<number | null>(null);
  const [followingCount, setFollowingCount] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedPost, setSelectedPost] = useState<PostDto | null>(null);

  const [showFollowers, setShowFollowers] = useState(false);
  const [followersList, setFollowersList] = useState<ProfileDto[]>([]);
  const [showFollowing, setShowFollowing] = useState(false);
  const [followingList, setFollowingList] = useState<ProfileDto[]>([]);
  const [isSocialLoading, setIsSocialLoading] = useState(false);

  const openFollowers = async () => {
    if (!profile) return;
    setIsSocialLoading(true);
    setShowFollowers(true);
    try {
      const resp = await api.getFollowers(profile.id, 1, 50);
      setFollowersList(resp.followers || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsSocialLoading(false);
    }
  };

  const openFollowing = async () => {
    if (!profile) return;
    setIsSocialLoading(true);
    setShowFollowing(true);
    try {
      const resp = await api.getFollowings(profile.id, 1, 50);
      setFollowingList(resp.followings || []);
    } catch (e) {
      console.error(e);
    } finally {
      setIsSocialLoading(false);
    }
  };

  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({ displayName: '', username: '', bio: '' });
  const [editImage, setEditImage] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  const openEditModal = () => {
    if (profile) {
      setEditForm({ displayName: profile.displayName || '', username: profile.username || '', bio: profile.bio || '' });
      setEditImage(null);
      setIsEditing(true);
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setEditImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const saveProfile = async () => {
    try {
      setIsSaving(true);
      let updatedProfile = profile;
      if (editImage) {
        const newUrl = await api.updateProfileImage(editImage);
        updatedProfile = { ...updatedProfile!, profileImageUrl: newUrl };
      }
      updatedProfile = await api.updateProfile(editForm.displayName, editForm.username, editForm.bio);
      setProfile(updatedProfile);
      setIsEditing(false);
    } catch (err: unknown) {
      alert("Failed to save profile: " + (err instanceof Error ? err.message : String(err)));
    } finally {
      setIsSaving(false);
    }
  };  const handlePostUpdated = useCallback((updated: PostDto) => {
    setSelectedPost(updated);
    setPosts(prev => prev.map(p => p.id === updated.id ? updated : p));
  }, []);

  const handlePostDeleted = useCallback((postId: number) => {
    setSelectedPost(null);
    setPosts(prev => prev.filter(p => p.id !== postId));
  }, []);

  useEffect(() => {
    if (authLoading) return;
    let cancelled = false;
    const fetchProfileData = async () => {
      if (!user) {
        // Not signed in — clear any stale profile data and the spinner.
        if (!cancelled) {
          setProfile(null);
          setPosts([]);
          setFollowerCount(null);
          setFollowingCount(null);
          setLoading(false);
        }
        return;
      }
      try {
        const profileData = await api.getMyProfile();
        if (cancelled) return;
        setProfile(profileData);

        if (profileData?.id) {
          const userPosts = await api.getUserPosts(profileData.id);
          if (!cancelled) setPosts(userPosts || []);

          // Best-effort: fetch follower/following counts. The endpoints may 404
          // on a brand-new account with no social graph yet, or fail if the
          // FollowerController isn't deployed — show "0" rather than "-" then.
          try {
            const followers = await api.getFollowers(profileData.id, 1, 1);
            if (!cancelled) setFollowerCount(followers.totalCount);
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
    // Defer to a microtask so the effect body itself does not call
    // setState synchronously (React 19 anti-pattern flagged by
    // react-hooks/set-state-in-effect).
    void Promise.resolve().then(fetchProfileData);
    return () => { cancelled = true; };
  }, [authLoading, user]);

  if (loading) {
    return <div className="loading-spinner">Loading...</div>;
  }



  if (!profile) {
    if (!user) {
      return <div className="empty-state">Please sign in to view your profile.</div>;
    }
    return <div className="empty-state">Failed to load profile.</div>;
  }

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
            <button className="btn-primary" onClick={openEditModal}>Edit Profile</button>
            <button className="btn-icon">
              <Settings size={24} />
            </button>
          </div>
          
          <div className="profile-stats">
            <span><strong className="text-primary">{posts.length}</strong> posts</span>
            <span onClick={openFollowers} style={{ cursor: 'pointer' }}><strong className="text-primary">{followerCount ?? '-'}</strong> followers</span>
            <span onClick={openFollowing} style={{ cursor: 'pointer' }}><strong className="text-primary">{followingCount ?? '-'}</strong> following</span>
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

      {showFollowers && (
        <div className="zoom-modal-overlay" onClick={() => setShowFollowers(false)}>
          <div className="modal-content profile-edit-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Followers</h2>
              <button className="zoom-close-btn" onClick={() => setShowFollowers(false)}>
                <X size={24} />
              </button>
            </div>
            <div className="modal-body" style={{ maxHeight: '400px', overflowY: 'auto' }}>
              {isSocialLoading ? (
                <div className="loading-spinner" style={{ minHeight: '100px' }}>Loading...</div>
              ) : followersList.length === 0 ? (
                <div className="empty-state">No followers yet.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {followersList.map(u => (
                    <div key={u.id} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <img src={u.profileImageUrl || 'https://i.pravatar.cc/150'} alt={u.username} style={{ width: '40px', height: '40px', borderRadius: '50%' }} />
                      <div>
                        <div style={{ fontWeight: 'bold' }}>{u.username}</div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{u.displayName}</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {showFollowing && (
        <div className="zoom-modal-overlay" onClick={() => setShowFollowing(false)}>
          <div className="modal-content profile-edit-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Following</h2>
              <button className="zoom-close-btn" onClick={() => setShowFollowing(false)}>
                <X size={24} />
              </button>
            </div>
            <div className="modal-body" style={{ maxHeight: '400px', overflowY: 'auto' }}>
              {isSocialLoading ? (
                <div className="loading-spinner" style={{ minHeight: '100px' }}>Loading...</div>
              ) : followingList.length === 0 ? (
                <div className="empty-state">Not following anyone yet.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {followingList.map(u => (
                    <div key={u.id} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <img src={u.profileImageUrl || 'https://i.pravatar.cc/150'} alt={u.username} style={{ width: '40px', height: '40px', borderRadius: '50%' }} />
                      <div>
                        <div style={{ fontWeight: 'bold' }}>{u.username}</div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{u.displayName}</div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {isEditing && (
        <div className="zoom-modal-overlay" onClick={() => !isSaving && setIsEditing(false)}>
          <div className="modal-content profile-edit-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Edit Profile</h2>
              <button className="zoom-close-btn" onClick={() => !isSaving && setIsEditing(false)} disabled={isSaving}>
                <X size={24} />
              </button>
            </div>
            <div className="modal-body">
              <div className="edit-avatar-container">
                <img src={editImage || profile?.profileImageUrl || 'https://i.pravatar.cc/150'} alt="Avatar" className="edit-avatar" />
                <label className="edit-avatar-overlay">
                  <Camera size={24} />
                  <input type="file" accept="image/*" onChange={handleImageChange} hidden disabled={isSaving} />
                </label>
              </div>
              <div className="form-group">
                <label>Name</label>
                <input type="text" value={editForm.displayName} onChange={e => setEditForm({...editForm, displayName: e.target.value})} disabled={isSaving} />
              </div>
              <div className="form-group">
                <label>Username</label>
                <input type="text" value={editForm.username} onChange={e => setEditForm({...editForm, username: e.target.value})} disabled={isSaving} />
              </div>
              <div className="form-group">
                <label>Bio</label>
                <textarea value={editForm.bio} onChange={e => setEditForm({...editForm, bio: e.target.value})} disabled={isSaving} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-primary" onClick={saveProfile} disabled={isSaving}>
                {isSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Profile;
