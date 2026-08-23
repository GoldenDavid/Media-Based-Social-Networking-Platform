const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '/api';
const MEDIA_BASE_URL = (import.meta.env.VITE_MEDIA_BASE_URL as string | undefined) ?? '/api/media';
const GOOGLE_OAUTH_PATH =
  (import.meta.env.VITE_GOOGLE_OAUTH_PATH as string | undefined) ?? '/oauth2/authorization/google';

export interface ProfileDto {
  id: number;
  displayName: string;
  username: string;
  bio: string;
  profileImageUrl: string;
}

export interface CommentDto {
  id: number;
  createdBy: ProfileDto;
  comment: string;
  createdAt: string;
}

export interface PostDto {
  id: number;
  createdBy: ProfileDto;
  imageUrl: string;
  caption: string;
  createdAt: string;
  comments: CommentDto[];
  userLikes: ProfileDto[];
  userSaves?: ProfileDto[];
}

export interface FeedResponse {
  posts: PostDto[];
  totalPage: number;
}

export interface StoryDto {
  id: number;
  createdBy: ProfileDto;
  imageUrl: string;
  createdAt: string;
}

export interface StoryFeedDto {
  author: ProfileDto;
  stories: StoryDto[];
}

export interface BaseResponse<T> {
  success: boolean;
  message: string;
  timestamp: string;
  data: T;
}

export interface UserPrincipal {
  id: string;
  username: string;
  name?: string;
  token?: string;
}

export interface CreatePostResponse {
  post: PostDto;
}

export interface FollowersResponse {
  totalPage: number;
  totalCount: number;
  followers: ProfileDto[];
}

export interface FollowingsResponse {
  totalPage: number;
  totalCount: number;
  followings: ProfileDto[];
}

export interface NotificationDto {
  id: number;
  fromUser: ProfileDto;
  notificationType: 'NEW_POST' | 'LIKE_YOUR_POST' | 'COMMENT_YOUR_POST';
  postId?: number;
  createdAt: string;
}

export interface NotificationsResponse {
  totalPage: number;
  totalCount: number;
  notifications: NotificationDto[];
}

/**
 * Unwraps a `BaseResponse<T>` envelope. Backend wraps most REST endpoints in
 * `{ success, message, timestamp, data }`; this helper exposes the inner `T`.
 *
 * NOTE: feed endpoints (`GET /dynamic-feeds`, `GET /precomputed-feeds`) return
 * a flat `GetFeedResponse` (no envelope) by contract — that is the shape
 * produced by the gRPC feed service and serialized directly by the controller.
 * Do not wrap feed responses with `unwrap()`.
 */
export function unwrap<T>(resp: BaseResponse<T>): T {
  return resp.data;
}

const resolveProfileImages = (profile?: ProfileDto) => {
  if (profile && profile.profileImageUrl && !profile.profileImageUrl.startsWith('http')) {
    profile.profileImageUrl = `${MEDIA_BASE_URL}/${profile.profileImageUrl}`;
  }
  return profile;
};

const resolvePostImages = (post: PostDto) => {
  if (post.imageUrl && !post.imageUrl.startsWith('http')) {
    post.imageUrl = `${MEDIA_BASE_URL}/${post.imageUrl}`;
  }
  resolveProfileImages(post.createdBy);
  post.userLikes?.forEach(resolveProfileImages);
  post.userSaves?.forEach(resolveProfileImages);
  post.comments?.forEach(c => resolveProfileImages(c.createdBy));
  return post;
};

const resolveStoryImages = (story: StoryDto) => {
  if (story.imageUrl && !story.imageUrl.startsWith('http')) {
    story.imageUrl = `${MEDIA_BASE_URL}/${story.imageUrl}`;
  }
  resolveProfileImages(story.createdBy);
  return story;
};

async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${path}`;
  const token = localStorage.getItem('jwtToken');
  const headers = { Accept: 'application/json', ...init?.headers } as any;
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  const res = await fetch(url, {
    headers,
    ...init,
  });
  if (!res.ok) {
    const errorBody = await res.text().catch(() => '');
    throw new Error(`Request failed: ${res.status} ${path}${errorBody ? ` - ${errorBody}` : ''}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  inspectAuth: async (): Promise<UserPrincipal | null> => {
    const url = `${API_BASE_URL}/auth/inspect`;
    const token = localStorage.getItem('jwtToken');
    const headers = { Accept: 'application/json' } as any;
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    const res = await fetch(url, {
      headers,
    });
    if (res.status === 401) {
      return null;
    }
    if (!res.ok) {
      throw new Error(`Auth inspect failed: ${res.status}`);
    }
    return res.json() as Promise<UserPrincipal>;
  },

  getGoogleLoginUrl: (): string => GOOGLE_OAUTH_PATH,

  getFeed: async (page: number = 1, limit: number = 10): Promise<FeedResponse> => {
    // Flat contract: see `unwrap` JSDoc.
    const raw = await apiFetch<FeedResponse>(`/dynamic-feeds?page=${page}&limit=${limit}`);
    raw.posts?.forEach(resolvePostImages);
    return raw;
  },

  getPrecomputedFeed: async (page: number = 1, limit: number = 10): Promise<FeedResponse> => {
    const raw = await apiFetch<FeedResponse>(`/precomputed-feeds?page=${page}&limit=${limit}`);
    raw.posts?.forEach(resolvePostImages);
    return raw;
  },

  getMyProfile: async (): Promise<ProfileDto> => {
    const wrapped = await apiFetch<BaseResponse<{ profile: ProfileDto }>>('/profiles/me');
    const profile = unwrap(wrapped).profile;
    resolveProfileImages(profile);
    return profile;
  },

  getUserProfile: async (userId: number): Promise<ProfileDto> => {
    const wrapped = await apiFetch<BaseResponse<{ profile: ProfileDto }>>(`/profiles/${userId}`);
    const profile = unwrap(wrapped).profile;
    resolveProfileImages(profile);
    return profile;
  },

  searchUsers: async (query: string): Promise<ProfileDto[]> => {
    const wrapped = await apiFetch<BaseResponse<{ profiles: ProfileDto[] }>>(`/profiles/search?query=${encodeURIComponent(query)}`);
    const profiles = unwrap(wrapped).profiles || [];
    profiles.forEach(resolveProfileImages);
    return profiles;
  },

  updateProfile: async (displayName: string, username: string, bio: string): Promise<ProfileDto> => {
    const wrapped = await apiFetch<BaseResponse<{ profile: ProfileDto }>>('/profiles', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ displayName, username, bio }),
    });
    if (!wrapped.success) throw new Error(wrapped.message);
    const profile = unwrap(wrapped).profile;
    resolveProfileImages(profile);
    return profile;
  },

  updateProfileImage: async (base64ImageString: string): Promise<string> => {
    const wrapped = await apiFetch<BaseResponse<{ url: string }>>('/profiles/profile-image', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ base64ImageString }),
    });
    if (!wrapped.success) throw new Error(wrapped.message);
    let url = unwrap(wrapped).url;
    if (url && !url.startsWith('http')) {
      url = `${MEDIA_BASE_URL}/${url}`;
    }
    return url;
  },

  getUserPosts: async (userId: number): Promise<PostDto[]> => {
    const wrapped = await apiFetch<BaseResponse<{ posts: PostDto[] }>>(`/posts/user/${userId}`);
    const posts = unwrap(wrapped).posts ?? [];
    posts.forEach(resolvePostImages);
    return posts;
  },

  login: async (username: string, password: string): Promise<UserPrincipal> => {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) {
      throw new Error(`Login failed: ${res.status}`);
    }
    const data = await res.json() as UserPrincipal;
    if (data.token) {
      localStorage.setItem('jwtToken', data.token);
    }
    return data;
  },

  logout: async (): Promise<void> => {
    localStorage.removeItem('jwtToken');
    const res = await fetch(`${API_BASE_URL}/auth/logout`, {
      method: 'POST',
    });
    if (!res.ok) {
      throw new Error(`Logout failed: ${res.status}`);
    }
  },

  register: async (name: string, username: string, password: string): Promise<void> => {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ name, username, password }),
    });
    if (!res.ok) {
      const errorText = await res.text().catch(() => 'Unknown error');
      throw new Error(`Registration failed: ${errorText}`);
    }
  },

  createPost: async (base64ImageString: string, caption: string): Promise<PostDto> => {
    // Per ADR-003, the data URI prefix MUST be preserved so the backend
    // can detect the MIME type via `parseExtension`.
    const wrapped = await apiFetch<BaseResponse<CreatePostResponse>>('/posts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ base64ImageString, caption }),
    });
    if (!wrapped.success) {
      throw new Error(wrapped.message || 'Failed to create post');
    }
    return resolvePostImages(unwrap(wrapped).post);
  },

  createStory: async (base64ImageString: string): Promise<StoryDto> => {
    const wrapped = await apiFetch<BaseResponse<{ story: StoryDto }>>('/stories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ base64ImageString }),
    });
    if (!wrapped.success) {
      throw new Error(wrapped.message || 'Failed to create story');
    }
    return resolveStoryImages(unwrap(wrapped).story);
  },

  getStoryFeeds: async (): Promise<StoryFeedDto[]> => {
    const wrapped = await apiFetch<BaseResponse<{ storyFeeds: StoryFeedDto[] }>>('/story-feeds');
    const storyFeeds = unwrap(wrapped).storyFeeds ?? [];
    storyFeeds.forEach(sf => {
      resolveProfileImages(sf.author);
      sf.stories?.forEach(resolveStoryImages);
    });
    return storyFeeds;
  },

  deletePost: async (postId: number): Promise<void> => {
    const wrapped = await apiFetch<BaseResponse<any>>(`/posts/${postId}`, {
      method: 'DELETE',
    });
    if (wrapped.success === false) {
      throw new Error(wrapped.message || 'Failed to delete post');
    }
  },

  likePost: async (postId: number): Promise<PostDto> => {
    const wrapped = await apiFetch<BaseResponse<{ post: PostDto }>>(`/posts/like/${postId}`, {
      method: 'POST',
    });
    if (!wrapped.success) {
      throw new Error(wrapped.message || 'Failed to like post');
    }
    return resolvePostImages(unwrap(wrapped).post);
  },

  unlikePost: async (postId: number): Promise<PostDto> => {
    const wrapped = await apiFetch<BaseResponse<{ post: PostDto }>>(`/posts/like/${postId}`, {
      method: 'DELETE',
    });
    if (!wrapped.success) {
      throw new Error(wrapped.message || 'Failed to unlike post');
    }
    return resolvePostImages(unwrap(wrapped).post);
  },

  savePost: async (postId: number): Promise<PostDto> => {
    const wrapped = await apiFetch<BaseResponse<{ post: PostDto }>>(`/posts/save/${postId}`, {
      method: 'POST',
    });
    if (!wrapped.success) {
      throw new Error(wrapped.message || 'Failed to save post');
    }
    return resolvePostImages(unwrap(wrapped).post);
  },

  unsavePost: async (postId: number): Promise<PostDto> => {
    const wrapped = await apiFetch<BaseResponse<{ post: PostDto }>>(`/posts/save/${postId}`, {
      method: 'DELETE',
    });
    if (!wrapped.success) {
      throw new Error(wrapped.message || 'Failed to unsave post');
    }
    return resolvePostImages(unwrap(wrapped).post);
  },

  getSavedPosts: async (): Promise<PostDto[]> => {
    const wrapped = await apiFetch<BaseResponse<{ posts: PostDto[] }>>('/posts/saved');
    const posts = unwrap(wrapped).posts ?? [];
    posts.forEach(resolvePostImages);
    return posts;
  },

  commentOnPost: async (postId: number, comment: string): Promise<PostDto> => {
    const wrapped = await apiFetch<BaseResponse<{ post: PostDto }>>('/comments', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ postId, comment }),
    });
    if (!wrapped.success) {
      throw new Error(wrapped.message || 'Failed to add comment');
    }
    return resolvePostImages(unwrap(wrapped).post);
  },

  deleteComment: async (commentId: number): Promise<void> => {
    const wrapped = await apiFetch<BaseResponse<any>>(`/comments/${commentId}`, {
      method: 'DELETE',
    });
    if (wrapped.success === false) {
      throw new Error(wrapped.message || 'Failed to delete comment');
    }
  },

  // ── Follow graph ──────────────────────────────────────────────────────────

  getFollowers: async (userId: number, page: number = 1, limit: number = 20): Promise<FollowersResponse> => {
    const wrapped = await apiFetch<BaseResponse<FollowersResponse>>(
      `/follow/user/followers/${userId}?page=${page}&limit=${limit}`
    );
    const body = unwrap(wrapped);
    body.followers?.forEach(resolveProfileImages);
    return body;
  },

  getFollowings: async (userId: number, page: number = 1, limit: number = 20): Promise<FollowingsResponse> => {
    const wrapped = await apiFetch<BaseResponse<FollowingsResponse>>(
      `/follow/user/followings/${userId}?page=${page}&limit=${limit}`
    );
    const body = unwrap(wrapped);
    body.followings?.forEach(resolveProfileImages);
    return body;
  },

  followUser: async (profileId: number): Promise<void> => {
    const wrapped = await apiFetch<BaseResponse<{ followed: boolean; profileId: number }>>(
      '/follow',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ profileId }),
      }
    );
    const body = unwrap(wrapped);
    if (!body?.followed) {
      throw new Error('Follow request did not confirm');
    }
  },

  unfollowUser: async (profileId: number): Promise<void> => {
    const wrapped = await apiFetch<BaseResponse<{ followed: boolean; profileId: number }>>(
      '/follow',
      {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ profileId }),
      }
    );
    const body = unwrap(wrapped);
    if (body?.followed) {
      throw new Error('Unfollow request did not confirm');
    }
  },

  // ── Notifications ────────────────────────────────────────────────────────

  getMyNotifications: async (page: number = 1, limit: number = 20): Promise<NotificationsResponse> => {
    const wrapped = await apiFetch<BaseResponse<NotificationsResponse>>(
      `/notifications/me?page=${page}&limit=${limit}`
    );
    const body = unwrap(wrapped);
    body.notifications?.forEach((n) => resolveProfileImages(n.fromUser));
    return body;
  }
};
