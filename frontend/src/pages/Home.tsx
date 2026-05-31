import PostCard from '../components/PostCard';
import './Home.css';

// Mock data to demonstrate the UI until API integration
const MOCK_POSTS = [
  {
    id: 1,
    username: 'alex_cyber',
    avatarUrl: 'https://i.pravatar.cc/150?u=alex',
    imageUrl: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=1000&auto=format&fit=crop',
    caption: 'Exploring the neon streets tonight. The vibes are immaculate! 🌃✨ #cyberpunk #citylights',
    likes: 1243,
    timeAgo: '2 hours ago'
  },
  {
    id: 2,
    username: 'nova_designs',
    avatarUrl: 'https://i.pravatar.cc/150?u=nova',
    imageUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1000&auto=format&fit=crop',
    caption: 'Abstract fluid gradients are my new obsession. What do you guys think? 🎨',
    likes: 892,
    timeAgo: '5 hours ago'
  }
];

const Home = () => {
  return (
    <div className="home-container">
      <header className="home-header">
        <h1 className="text-gradient">For You</h1>
      </header>
      
      <div className="feed-container">
        {MOCK_POSTS.map(post => (
          <PostCard 
            key={post.id}
            username={post.username}
            avatarUrl={post.avatarUrl}
            imageUrl={post.imageUrl}
            caption={post.caption}
            likes={post.likes}
            timeAgo={post.timeAgo}
          />
        ))}
      </div>
    </div>
  );
};

export default Home;
