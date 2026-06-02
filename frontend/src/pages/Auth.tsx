import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';

const Auth = () => {
  const { login, register } = useAuth();
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [authError, setAuthError] = useState('');

  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError('');
    try {
      if (authMode === 'login') {
        await login(username, password);
      } else {
        await register(name, username, password);
        setAuthMode('login');
        setAuthError('Registration successful! Please log in.');
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Authentication failed';
      setAuthError(message);
    }
  };

  const inputStyle = {
    background: 'rgba(255,255,255,0.05)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    color: 'var(--text-primary)',
    padding: '0.8rem',
    borderRadius: 'var(--radius-sm)',
    outline: 'none',
    width: '100%'
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center' }}>
      <div className="auth-container glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '400px', padding: '2rem', textAlign: 'center' }}>
        <h2 className="text-gradient" style={{ marginBottom: '1.5rem', fontSize: '2rem' }}>Nova</h2>
        <h3 style={{ marginBottom: '1.5rem', fontSize: '1.25rem', fontWeight: 500 }}>
          {authMode === 'login' ? 'Welcome Back' : 'Create Account'}
        </h3>
        
        {authError && <p style={{ color: 'var(--accent-pink)', marginBottom: '1rem', fontSize: '0.9rem' }}>{authError}</p>}
        
        <form onSubmit={handleAuth} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {authMode === 'register' && (
            <input 
              type="text" 
              placeholder="Full Name" 
              value={name} 
              onChange={e => setName(e.target.value)} 
              style={inputStyle}
              required 
            />
          )}
          <input 
            type="text" 
            placeholder="Username" 
            value={username} 
            onChange={e => setUsername(e.target.value)} 
            style={inputStyle}
            required 
          />
          <input 
            type="password" 
            placeholder="Password" 
            value={password} 
            onChange={e => setPassword(e.target.value)} 
            style={inputStyle}
            required 
          />
          <button type="submit" className="btn-primary" style={{ marginTop: '0.5rem', width: '100%' }}>
            {authMode === 'login' ? 'Sign In' : 'Register'}
          </button>
        </form>
        
        <p 
          style={{ marginTop: '1.5rem', cursor: 'pointer', color: 'var(--text-secondary)', fontSize: '0.9rem', transition: 'color 0.2s' }} 
          onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}
          onMouseOver={(e) => e.currentTarget.style.color = '#fff'}
          onMouseOut={(e) => e.currentTarget.style.color = 'var(--text-secondary)'}
        >
          {authMode === 'login' ? "Don't have an account? Register" : "Already have an account? Log in"}
        </p>
      </div>
    </div>
  );
};

export default Auth;
