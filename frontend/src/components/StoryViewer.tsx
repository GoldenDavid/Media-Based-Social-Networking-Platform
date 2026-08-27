import React, { useState, useEffect } from 'react';
import { type StoryFeedDto } from '../services/api';
import './StoryViewer.css';

interface StoryViewerProps {
  storyFeeds: StoryFeedDto[];
  initialAuthorId: number | null;
  onClose: () => void;
}

const StoryViewer: React.FC<StoryViewerProps> = ({ storyFeeds, initialAuthorId, onClose }) => {
  const [currentFeedIndex, setCurrentFeedIndex] = useState(() => {
    if (initialAuthorId) {
      const index = storyFeeds.findIndex(feed => feed.author.id === initialAuthorId);
      return index !== -1 ? index : 0;
    }
    return 0;
  });
  const [currentStoryIndex, setCurrentStoryIndex] = useState(0);
  const handleNext = React.useCallback(() => {
    const currentFeed = storyFeeds[currentFeedIndex];
    if (currentStoryIndex < currentFeed.stories.length - 1) {
      // Next story in current feed
      setCurrentStoryIndex(prev => prev + 1);
    } else if (currentFeedIndex < storyFeeds.length - 1) {
      // Next feed
      setCurrentFeedIndex(prev => prev + 1);
      setCurrentStoryIndex(0);
    } else {
      // End of all stories
      onClose();
    }
  }, [currentFeedIndex, currentStoryIndex, storyFeeds, onClose]);

  const handlePrev = React.useCallback(() => {
    if (currentStoryIndex > 0) {
      // Prev story in current feed
      setCurrentStoryIndex(prev => prev - 1);
    } else if (currentFeedIndex > 0) {
      // Prev feed, last story
      setCurrentFeedIndex(prev => prev - 1);
      setCurrentStoryIndex(storyFeeds[currentFeedIndex - 1].stories.length - 1);
    } else {
      // Beginning of all stories
      onClose();
    }
  }, [currentFeedIndex, currentStoryIndex, storyFeeds, onClose]);

  // Auto advance story every 5 seconds
  useEffect(() => {
    const timer = setTimeout(() => {
      handleNext();
    }, 5000);
    return () => clearTimeout(timer);
  }, [handleNext]);

  if (storyFeeds.length === 0) return null;

  const currentFeed = storyFeeds[currentFeedIndex];
  if (!currentFeed || !currentFeed.stories || currentFeed.stories.length === 0) {
    onClose();
    return null;
  }
  
  const currentStory = currentFeed.stories[currentStoryIndex];

  return (
    <div className="story-viewer-overlay">
      <button className="story-close-btn" onClick={onClose}>&times;</button>
      
      <div className="story-viewer-container">
        {/* Progress bars */}
        <div className="story-progress-container">
          {currentFeed.stories.map((s, idx) => (
            <div key={s.id} className="story-progress-bar-bg">
              <div 
                className={`story-progress-bar-fill ${idx < currentStoryIndex ? 'completed' : ''} ${idx === currentStoryIndex ? 'active' : ''}`}
              />
            </div>
          ))}
        </div>

        {/* Header */}
        <div className="story-header">
          <img 
            src={currentFeed.author.profileImageUrl || '/default-avatar.svg'} 
            alt="author" 
            className="story-header-avatar"
          />
          <span className="story-header-name">{currentFeed.author.displayName}</span>
          <span className="story-header-time">
            {new Date(currentStory.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
          </span>
        </div>

        {/* Image */}
        <div className="story-image-container">
          <img src={currentStory.imageUrl} alt="story" className="story-image" />
        </div>

        {/* Navigation areas (invisible) */}
        <div className="story-nav-area prev-area" onClick={handlePrev} />
        <div className="story-nav-area next-area" onClick={handleNext} />
      </div>
    </div>
  );
};

export default StoryViewer;
