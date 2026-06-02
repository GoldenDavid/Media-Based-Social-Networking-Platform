import { useCallback, useEffect, useState } from 'react';
import PostCard from '../components/PostCard';
import { useAuth } from '../contexts/AuthContext';
import { api, type PostDto } from '../services/api';
import './Home.css';

const Home = () => {
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [loading, setLoading] = useState(true);

  const handlePostUpdated = useCallback((updated: PostDto) => {
    setPosts(prev => prev.map(p => p.id === updated.id ? updated : p));
  }, []);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    const fetchFeed = async () => {
      try {
        const response = await api.getFeed(1, 10);
        if (!cancelled) setPosts(response.posts || []);
      } catch (error) {
        console.error("Failed to fetch feed:", error);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchFeed();
    return () => { cancelled = true; };
  }, [authLoading, user]);

  return (
    <div className="home-container">
      <header className="home-header">
        <h1 className="text-gradient">For You</h1>
      </header>
      
      <div className="feed-container">
        {loading ? (
          <div className="loading-spinner">Loading...</div>
        ) : posts.length === 0 ? (
          <div className="empty-state animate-fade-in">No posts in your feed yet!</div>
        ) : (
          posts.map(post => (
            <PostCard 
              key={post.id}
              post={post}
              currentUser={user}
              onPostUpdated={handlePostUpdated}
            />
          ))
        )}
      </div>
    </div>
  );
};

export default Home;
