/* eslint-disable react-refresh/only-export-components */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react';
import { api, type UserPrincipal } from '../services/api';

interface AuthContextValue {
  user: UserPrincipal | null;
  profileId: number | null;
  loading: boolean;
  followingIds: number[];
  toggleFollowingId: (userId: number, isFollowing: boolean) => void;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  register: (name: string, username: string, password: string) => Promise<void>;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserPrincipal | null>(null);
  const [profileId, setProfileId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [followingIds, setFollowingIds] = useState<number[]>([]);

  const fetchFollowings = async () => {
    try {
      const profile = await api.getMyProfile();
      if (profile && profile.id) {
        setProfileId(profile.id);
        const followingsResp = await api.getFollowings(profile.id, 1, 1000);
        setFollowingIds(followingsResp.followings.map((f: { id: number }) => f.id));
      }
    } catch (e) {
      console.error("Failed to fetch followings", e);
      setProfileId(null);
    }
  };

  const toggleFollowingId = useCallback((userId: number, isFollowing: boolean) => {
    setFollowingIds(prev => {
      if (isFollowing) {
        return prev.includes(userId) ? prev : [...prev, userId];
      } else {
        return prev.filter(id => id !== userId);
      }
    });
  }, []);

  const refresh = useCallback(async () => {
    try {
      const u = await api.inspectAuth();
      setUser(u);
      if (u) {
        await fetchFollowings();
      } else {
        setFollowingIds([]);
        setProfileId(null);
      }
    } catch (err) {
      console.error('Auth refresh failed:', err);
      setUser(null);
      setFollowingIds([]);
      setProfileId(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Initial auth probe on mount. The setState calls inside `refresh`
    // are intentional: this is a one-shot bootstrap, not a render-time
    // derivation. We schedule it as a microtask so the effect body
    // itself does not call setState synchronously.
    void Promise.resolve().then(refresh);
  }, [refresh]);

  const login = async (username: string, password: string) => {
    const user = await api.login(username, password);
    setUser(user);
    if (user) await fetchFollowings();
  };

  const logout = async () => {
    await api.logout();
    setUser(null);
    setFollowingIds([]);
    setProfileId(null);
  };

  const register = async (name: string, username: string, password: string) => {
    await api.register(name, username, password);
  };

  return (
    <AuthContext.Provider value={{ user, profileId, loading, followingIds, toggleFollowingId, login, logout, register, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
