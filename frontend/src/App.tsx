import { useState } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Sidebar from './layouts/Sidebar';
import Home from './pages/Home';
import Profile from './pages/Profile';
import CreatePost from './pages/CreatePost';
import Notifications from './pages/Notifications';
import NotificationDrawer from './components/NotificationDrawer';
import { useAuth } from './contexts/AuthContext';
import Auth from './pages/Auth';
import './App.css';

function App() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center' }}>
        Loading...
      </div>
    );
  }

  // /login is the only public route while unauthenticated.
  if (!user && location.pathname !== '/login') {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<Auth />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
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
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/create" element={<CreatePost />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/login" element={<Navigate to="/" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
