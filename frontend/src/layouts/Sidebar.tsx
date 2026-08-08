import { NavLink } from 'react-router-dom';
import { Home, Compass, Bell, PlusSquare, User, Menu, Sun, Moon, LogOut } from 'lucide-react';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import { api } from '../services/api';
import './Sidebar.css';

interface SidebarProps {
  onOpenNotifications: () => void;
}

const Sidebar = ({ onOpenNotifications }: SidebarProps) => {
  const { theme, toggleTheme } = useTheme();
  const { user, logout } = useAuth();
  const googleLoginUrl = api.getGoogleLoginUrl();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.error('Logout failed:', err);
    }
  };

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
        {user ? (
          <button className="nav-item" onClick={handleLogout}>
            <LogOut size={24} />
            <span>Sign Out</span>
          </button>
        ) : (
          <a className="nav-item" href={googleLoginUrl}>
            <LogOut size={24} />
            <span>Login with Google</span>
          </a>
        )}
        <button className="nav-item" onClick={toggleTheme}>
          {theme === 'dark' ? <Sun size={24} /> : <Moon size={24} />}
          <span>{theme === 'dark' ? 'Light Mode' : 'Dark Mode'}</span>
        </button>
        <button className="nav-item">
          <Menu size={24} />
          <span>More</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
