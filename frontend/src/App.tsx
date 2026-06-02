import { useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from './layouts/Sidebar';
import Home from './pages/Home';
import Profile from './pages/Profile';
import CreatePost from './pages/CreatePost';
import NotificationDrawer from './components/NotificationDrawer';
import { useAuth } from './contexts/AuthContext';
import Auth from './pages/Auth';
import './App.css';

function App() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const { user, loading } = useAuth();

  if (loading) {
    return <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center' }}>Loading...</div>;
  }

  if (!user) {
    return <Auth />;
  }

  return (
    <div className="app-container">
      <Sidebar onOpenNotifications={() => setIsDrawerOpen(true)} />

      <NotificationDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        username={user.username}
      />

      <main className="main-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/explore" element={<div className="page-placeholder animate-fade-in"><h2>Explore (Coming Soon)</h2></div>} />
          <Route path="/notifications" element={<div className="page-placeholder animate-fade-in"><h2>Notifications (Coming Soon)</h2></div>} />
          <Route path="/create" element={<CreatePost />} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
