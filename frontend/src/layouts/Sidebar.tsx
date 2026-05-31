import React from 'react';
import { NavLink } from 'react-router-dom';
import { Home, Compass, Bell, PlusSquare, User, Menu } from 'lucide-react';
import './Sidebar.css';

interface SidebarProps {
  onOpenNotifications: () => void;
}

const Sidebar = ({ onOpenNotifications }: SidebarProps) => {
  return (
    <aside className="sidebar glass-panel">
      <div className="sidebar-logo">
        <h2 className="text-gradient">Nova</h2>
      </div>
      
      <nav className="sidebar-nav">
        <NavLink to="/" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <Home size={24} />
          <span>Home</span>
        </NavLink>
        
        <NavLink to="/explore" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <Compass size={24} />
          <span>Explore</span>
        </NavLink>
        
        <button className="nav-item" onClick={onOpenNotifications}>
          <Bell size={24} />
          <span>Notifications</span>
          <span className="badge">New</span>
        </button>
        
        <NavLink to="/create" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <PlusSquare size={24} />
          <span>Create</span>
        </NavLink>
        
        <NavLink to="/profile" className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}>
          <User size={24} />
          <span>Profile</span>
        </NavLink>
      </nav>
      
      <div className="sidebar-footer">
        <button className="nav-item">
          <Menu size={24} />
          <span>More</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
