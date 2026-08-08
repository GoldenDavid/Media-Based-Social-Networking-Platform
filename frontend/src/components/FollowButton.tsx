import { useState } from 'react';
import { UserPlus, UserMinus } from 'lucide-react';
import { api } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

interface FollowButtonProps {
  userId: number;
}

const FollowButton = ({ userId }: FollowButtonProps) => {
  const { user, followingIds, toggleFollowingId } = useAuth();
  const [loading, setLoading] = useState(false);

  // If not logged in or viewing oneself, don't show the button
  // Note: user.id is a string (UUID) and userId is a number (auto-increment). 
  // We can't strictly compare them here, but we could pass username or assume Explore doesn't show current user.
  // Actually, we can just hide it if user is null.
  if (!user) return null;

  const isFollowing = followingIds.includes(userId);

  const handleToggle = async (e: React.MouseEvent) => {
    e.stopPropagation(); // prevent navigating to profile if inside a clickable card
    if (loading) return;
    setLoading(true);
    try {
      if (isFollowing) {
        await api.unfollowUser(userId);
        toggleFollowingId(userId, false);
      } else {
        await api.followUser(userId);
        toggleFollowingId(userId, true);
      }
    } catch (error) {
      console.error("Failed to toggle follow status", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <button 
      className={isFollowing ? "btn-secondary" : "btn-primary"} 
      onClick={handleToggle}
      disabled={loading}
      style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '6px 12px', fontSize: '0.85rem' }}
    >
      {isFollowing ? (
        <><UserMinus size={16} /> Unfollow</>
      ) : (
        <><UserPlus size={16} /> Follow</>
      )}
    </button>
  );
};

export default FollowButton;
