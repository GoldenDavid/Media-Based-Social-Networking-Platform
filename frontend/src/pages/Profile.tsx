import { Settings, Grid, Bookmark } from 'lucide-react';
import './Profile.css';

const MOCK_PROFILE = {
  username: 'neon_dreamer',
  name: 'Neon Dreamer',
  bio: 'Digital artist & cyberpunk enthusiast. Creating worlds out of pixels. 🎮✨',
  avatarUrl: 'https://i.pravatar.cc/150?img=32',
  posts: 42,
  followers: '12.4K',
  following: 890
};

const MOCK_GRID = [
  'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=500&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=500&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1605806616949-1e87b487cb2a?q=80&w=500&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1542831371-29b0f74f9713?q=80&w=500&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=500&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=500&auto=format&fit=crop',
];

const Profile = () => {
  return (
    <div className="profile-container animate-fade-in">
      <header className="profile-header glass-panel">
        <div className="profile-avatar-container">
          <div className="avatar-ring-large">
            <img src={MOCK_PROFILE.avatarUrl} alt="Avatar" className="profile-avatar" />
          </div>
        </div>
        
        <div className="profile-info">
          <div className="profile-actions">
            <h2 className="profile-username">{MOCK_PROFILE.username}</h2>
            <button className="btn-primary">Edit Profile</button>
            <button className="btn-icon">
              <Settings size={24} />
            </button>
          </div>
          
          <div className="profile-stats">
            <span><strong className="text-primary">{MOCK_PROFILE.posts}</strong> posts</span>
            <span><strong className="text-primary">{MOCK_PROFILE.followers}</strong> followers</span>
            <span><strong className="text-primary">{MOCK_PROFILE.following}</strong> following</span>
          </div>
          
          <div className="profile-bio">
            <h3 className="font-bold">{MOCK_PROFILE.name}</h3>
            <p>{MOCK_PROFILE.bio}</p>
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
        {MOCK_GRID.map((url, i) => (
          <div key={i} className="grid-item">
            <img src={url} alt={`Post ${i}`} />
            <div className="grid-item-overlay">
              <span>❤️ 1.2k</span>
              <span>💬 45</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Profile;
