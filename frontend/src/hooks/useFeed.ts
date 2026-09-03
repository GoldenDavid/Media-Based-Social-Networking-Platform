import { useState, useEffect, useCallback } from 'react';
import { api, type PostDto } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

export type FeedSource = 'dynamic' | 'precomputed';
const FEED_SOURCE_KEY = 'app:feedSource';

const readPersistedFeedSource = (): FeedSource => {
  try {
    const stored = localStorage.getItem(FEED_SOURCE_KEY);
    if (stored === 'dynamic' || stored === 'precomputed') return stored;
  } catch {
    // fallback
  }
  return 'dynamic';
};

export const useFeed = () => {
  const { user, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [feedSource, setFeedSource] = useState<FeedSource>(readPersistedFeedSource);

  const fetchFeed = useCallback(async () => {
    if (authLoading) return;
    if (!user) {
      setPosts([]);
      setLoading(false);
      return;
    }
    
    setLoading(true);
    try {
      const response = feedSource === 'precomputed'
        ? await api.getPrecomputedFeed(1, 10)
        : await api.getFeed(1, 10);
      setPosts(response.posts || []);
    } catch (error) {
      console.error("Failed to fetch feed:", error);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  }, [user, authLoading, feedSource]);

  useEffect(() => {
    let cancelled = false;
    if (!cancelled) {
      void Promise.resolve().then(fetchFeed);
    }
    return () => { cancelled = true; };
  }, [fetchFeed]);

  const handlePostUpdated = useCallback((updated: PostDto) => {
    setPosts(prev => prev.map(p => p.id === updated.id ? updated : p));
  }, []);

  const handleSwitchSource = (next: FeedSource) => {
    if (next === feedSource) return;
    setFeedSource(next);
    try { localStorage.setItem(FEED_SOURCE_KEY, next); } catch { /* ignore */ }
  };

  return {
    posts,
    loading,
    feedSource,
    handleSwitchSource,
    handlePostUpdated
  };
};
