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
}

export interface FeedResponse {
  posts: PostDto[];
  totalPage: number;
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
}

export interface CreatePostResponse {
  post: PostDto;
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
  post.comments?.forEach(c => resolveProfileImages(c.createdBy));
  return post;
};

async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${path}`;
  const res = await fetch(url, {
    credentials: 'include',
    headers: { Accept: 'application/json', ...init?.headers },
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
    const res = await fetch(url, {
      credentials: 'include',
      headers: { Accept: 'application/json' },
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
      credentials: 'include',
    });
    if (!res.ok) {
      throw new Error(`Login failed: ${res.status}`);
    }
    return res.json() as Promise<UserPrincipal>;
  },

  register: async (name: string, username: string, password: string): Promise<void> => {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify({ name, username, password }),
      credentials: 'include',
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
  }
};
