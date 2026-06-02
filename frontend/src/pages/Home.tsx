import { useCallback, useEffect, useState } from 'react';
import { Zap, Layers } from 'lucide-react';
import PostCard from '../components/PostCard';
import { useAuth } from '../contexts/AuthContext';
import { api, type PostDto } from '../services/api';
import './Home.css';

type FeedSource = 'dynamic' | 'precomputed';

const FEED_SOURCE_KEY = 'app:feedSource';

const readPersistedFeedSource = (): FeedSource => {
  try {
    const stored = localStorage.getItem(FEED_SOURCE_KEY);
    if (stored === 'dynamic' || stored === 'precomputed') return stored;
  } catch {
    // localStorage may be unavailable (private mode, quota, etc.) — fall through.
  }
  return 'dynamic';
};

const Home = () => {
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [feedSource, setFeedSource] = useState<FeedSource>(readPersistedFeedSource);

  const handlePostUpdated = useCallback((updated: PostDto) => {
    setPosts(prev => prev.map(p => p.id === updated.id ? updated : p));
  }, []);

  useEffect(() => {
    if (authLoading) return;
    let cancelled = false;
    const fetchFeed = async () => {
      if (!user) {
        // Not signed in — clear any stale posts from a previous session
        // and clear the loading spinner. The setState calls are
        // intentional (one-shot bootstrap, not a render-time derivation)
        // and the effect's effect runs once per auth-state change.
        if (!cancelled) {
          setPosts([]);
          setLoading(false);
        }
        return;
      }
      if (!cancelled) setLoading(true);
      try {
        // Per ADR-018: dynamic is the source of truth; precomputed is
        // opt-in. Both endpoints return a flat GetFeedResponse (no
        // BaseResponse envelope).
        const response = feedSource === 'precomputed'
          ? await api.getPrecomputedFeed(1, 10)
          : await api.getFeed(1, 10);
        if (!cancelled) setPosts(response.posts || []);
      } catch (error) {
        console.error("Failed to fetch feed:", error);
        if (!cancelled) setPosts([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    // Defer to a microtask so the effect body itself does not call
    // setState synchronously (React 19 anti-pattern flagged by
    // react-hooks/set-state-in-effect).
    void Promise.resolve().then(fetchFeed);
    return () => { cancelled = true; };
  }, [authLoading, user, feedSource]);

  const handleSwitch = (next: FeedSource) => {
    if (next === feedSource) return;
    setFeedSource(next);
    try { localStorage.setItem(FEED_SOURCE_KEY, next); } catch { /* ignore */ }
  };

  return (
    <div className="home-container">
      <header className="home-header">
        <h1 className="text-gradient">For You</h1>
        <div className="feed-source-toggle" role="tablist" aria-label="Feed source">
          <button
            type="button"
            role="tab"
            aria-selected={feedSource === 'dynamic'}
            className={`feed-source-btn ${feedSource === 'dynamic' ? 'active' : ''}`}
            onClick={() => handleSwitch('dynamic')}
            title="Live feed (source of truth)"
          >
            <Zap size={14} /> Dynamic
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={feedSource === 'precomputed'}
            className={`feed-source-btn ${feedSource === 'precomputed' ? 'active' : ''}`}
            onClick={() => handleSwitch('precomputed')}
            title="Fan-out cache (Redis, may lag by seconds)"
          >
            <Layers size={14} /> Precomputed
          </button>
        </div>
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
