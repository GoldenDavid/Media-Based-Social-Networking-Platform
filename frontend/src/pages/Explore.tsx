import { useState, useEffect } from 'react';
import { Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { api, type ProfileDto } from '../services/api';
import FollowButton from '../components/FollowButton';
import { useAuth } from '../contexts/AuthContext';

const Explore = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<ProfileDto[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { profileId } = useAuth();

  const search = React.useCallback(async (searchQuery: string) => {
    setLoading(true);
    try {
      const profiles = await api.searchUsers(searchQuery);
      setResults(profiles);
    } catch (error) {
      console.error('Failed to search users', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      if (query.trim()) {
        search(query.trim());
      } else {
        setResults([]);
      }
    }, 300); // 300ms debounce

    return () => clearTimeout(delayDebounceFn);
  }, [query, search]);



  return (
    <div className="explore-container animate-fade-in" style={{ padding: '24px', maxWidth: '600px', margin: '0 auto' }}>
      <header className="explore-header" style={{ marginBottom: '24px' }}>
        <h1 className="text-gradient" style={{ fontSize: '2rem', marginBottom: '16px' }}>Explore</h1>
        
        <div className="search-bar" style={{ 
          position: 'relative', 
          display: 'flex', 
          alignItems: 'center',
          backgroundColor: 'var(--surface-color)',
          borderRadius: '12px',
          padding: '8px 16px',
          border: '1px solid var(--border-color)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
        }}>
          <Search size={20} style={{ color: 'var(--text-secondary)', marginRight: '12px' }} />
          <input
            type="text"
            placeholder="Search users..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            style={{ 
              border: 'none', 
              background: 'transparent', 
              outline: 'none', 
              width: '100%', 
              color: 'var(--text-primary)',
              fontSize: '1rem'
            }}
          />
        </div>
      </header>

      <div className="search-results">
        {loading ? (
          <div className="loading-spinner" style={{ minHeight: '100px' }}>Searching...</div>
        ) : query.trim() && results.length === 0 ? (
          <div className="empty-state">No users found for "{query}"</div>
        ) : (
          <div className="user-list" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {results.map((u) => (
              <div 
                key={u.id} 
                className="user-card glass-panel" 
                onClick={() => navigate(`/profile/${u.id}`)}
                style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  padding: '16px', 
                  borderRadius: '16px',
                  cursor: 'pointer',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  gap: '16px'
                }}
                onMouseOver={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
                onMouseOut={(e) => e.currentTarget.style.transform = 'translateY(0)'}
              >
                <img 
                  src={u.profileImageUrl || 'https://i.pravatar.cc/150'} 
                  alt={u.username} 
                  style={{ width: '56px', height: '56px', borderRadius: '50%', objectFit: 'cover' }} 
                />
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>{u.username}</div>
                  <div style={{ color: 'var(--text-secondary)' }}>{u.displayName}</div>
                </div>
                {profileId !== u.id && (
                  <FollowButton userId={u.id} />
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Explore;
