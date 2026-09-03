import { Zap, Layers } from 'lucide-react';
import PostCard from '../components/PostCard';
import { useAuth } from '../contexts/AuthContext';
import StoriesBar from '../components/StoriesBar';
import StoryViewer from '../components/StoryViewer';
import { useFeed } from '../hooks/useFeed';
import { useStories } from '../hooks/useStories';
import './Home.css';

const Home = () => {
  const { user } = useAuth();
  
  const {
    posts,
    loading,
    feedSource,
    handleSwitchSource,
    handlePostUpdated
  } = useFeed();

  const {
    storyFeeds,
    selectedStoryAuthorId,
    setSelectedStoryAuthorId,
    refreshStories
  } = useStories();

  return (
    <main className="home-container">
      <header className="home-header">
        <h1 className="text-gradient">For You</h1>
        <nav className="feed-source-toggle" role="tablist" aria-label="Feed source">
          <button
            type="button"
            role="tab"
            aria-selected={feedSource === 'dynamic'}
            className={`feed-source-btn ${feedSource === 'dynamic' ? 'active' : ''}`}
            onClick={() => handleSwitchSource('dynamic')}
            title="Live feed (source of truth)"
          >
            <Zap size={14} /> Dynamic
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={feedSource === 'precomputed'}
            className={`feed-source-btn ${feedSource === 'precomputed' ? 'active' : ''}`}
            onClick={() => handleSwitchSource('precomputed')}
            title="Fan-out cache (Redis, may lag by seconds)"
          >
            <Layers size={14} /> Precomputed
          </button>
        </nav>
      </header>

      {user && (
        <section style={{ marginBottom: '20px' }} aria-label="Stories">
          <StoriesBar 
            storyFeeds={storyFeeds} 
            onStoryClick={setSelectedStoryAuthorId} 
            onStoryCreated={refreshStories} 
          />
        </section>
      )}

      <section className="feed-container" aria-label="Feed Posts">
        {loading ? (
          <div className="skeleton" style={{ height: '200px', margin: '20px 0' }}></div>
        ) : posts.length === 0 ? (
          <div className="empty-state animate-fade-in">
            <h2>No posts in your feed yet!</h2>
            <p className="text-secondary">Follow people to see their updates here.</p>
          </div>
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
      </section>

      {selectedStoryAuthorId !== null && (
        <StoryViewer 
          storyFeeds={storyFeeds}
          initialAuthorId={selectedStoryAuthorId}
          onClose={() => setSelectedStoryAuthorId(null)}
        />
      )}
    </main>
  );
};

export default Home;
