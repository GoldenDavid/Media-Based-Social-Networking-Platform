import { useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from './layouts/Sidebar';
import Home from './pages/Home';
import Profile from './pages/Profile';
import NotificationDrawer from './components/NotificationDrawer';
import './App.css';

function App() {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  return (
    <div className="app-container">
      <Sidebar onOpenNotifications={() => setIsDrawerOpen(true)} />
      
      <NotificationDrawer 
        isOpen={isDrawerOpen} 
        onClose={() => setIsDrawerOpen(false)} 
        username="alex_cyber" 
      />

      <main className="main-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/explore" element={<div className="page-placeholder animate-fade-in"><h2>Explore (Coming Soon)</h2></div>} />
          <Route path="/notifications" element={<div className="page-placeholder animate-fade-in"><h2>Notifications (Coming Soon)</h2></div>} />
          <Route path="/create" element={<div className="page-placeholder animate-fade-in"><h2>Create Post (Coming Soon)</h2></div>} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
