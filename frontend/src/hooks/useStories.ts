import { useState, useEffect, useCallback } from 'react';
import { api, type StoryFeedDto } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

export const useStories = () => {
  const { user } = useAuth();
  const [storyFeeds, setStoryFeeds] = useState<StoryFeedDto[]>([]);
  const [selectedStoryAuthorId, setSelectedStoryAuthorId] = useState<number | null>(null);

  const fetchStories = useCallback(async () => {
    if (!user) return;
    try {
      const feeds = await api.getStoryFeeds();
      setStoryFeeds(feeds);
    } catch (error) {
      console.error("Failed to fetch stories:", error);
    }
  }, [user]);

  useEffect(() => {
    void fetchStories();
  }, [fetchStories]);

  return {
    storyFeeds,
    selectedStoryAuthorId,
    setSelectedStoryAuthorId,
    refreshStories: fetchStories
  };
};
