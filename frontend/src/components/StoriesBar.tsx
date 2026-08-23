import React, { useRef, useState } from 'react';
import { type StoryFeedDto, api } from '../services/api';
import './StoriesBar.css';

interface StoriesBarProps {
  storyFeeds: StoryFeedDto[];
  onStoryClick: (authorId: number) => void;
  onStoryCreated: () => void;
}

const StoriesBar: React.FC<StoriesBarProps> = ({ storyFeeds, onStoryClick, onStoryCreated }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    try {
      const reader = new FileReader();
      reader.onloadend = async () => {
        const base64String = reader.result as string;
        await api.createStory(base64String);
        onStoryCreated();
      };
      reader.readAsDataURL(file);
    } catch (error) {
      console.error('Failed to create story:', error);
      alert('Failed to upload story');
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  return (
    <div className="stories-bar-container">
      <div className="stories-bar">
        {/* Create Story Button */}
        <div className="story-item create-story" onClick={() => fileInputRef.current?.click()}>
          <div className="story-avatar-container create-story-avatar">
            <span className="plus-icon">+</span>
          </div>
          <span className="story-author-name">Your Story</span>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="image/*"
            style={{ display: 'none' }}
          />
          {isUploading && <div className="story-uploading-overlay" />}
        </div>

        {/* Existing Stories */}
        {storyFeeds.map(feed => (
          <div
            key={feed.author.id}
            className="story-item"
            onClick={() => onStoryClick(feed.author.id)}
          >
            <div className="story-avatar-container has-unseen-stories">
              <img
                src={feed.author.profileImageUrl || '/default-avatar.svg'}
                alt={feed.author.displayName}
                className="story-avatar"
              />
            </div>
            <span className="story-author-name">{feed.author.displayName}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default StoriesBar;
