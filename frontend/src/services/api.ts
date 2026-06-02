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

const BASE_URL = '/api';
const MINIO_URL = 'http://localhost:9000/spring-boot';

const resolveProfileImages = (profile?: ProfileDto) => {
  if (profile && profile.profileImageUrl && !profile.profileImageUrl.startsWith('http')) {
    profile.profileImageUrl = `${MINIO_URL}/${profile.profileImageUrl}`;
  }
  return profile;
};

const resolvePostImages = (post: PostDto) => {
  if (post.imageUrl && !post.imageUrl.startsWith('http')) {
    post.imageUrl = `${MINIO_URL}/${post.imageUrl}`;
  }
  resolveProfileImages(post.createdBy);
  post.userLikes?.forEach(resolveProfileImages);
  post.comments?.forEach(c => resolveProfileImages(c.createdBy));
  return post;
};

async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${BASE_URL}${path}`;
  const res = await fetch(url, {
    credentials: 'include',
    headers: { Accept: 'application/json', ...init?.headers },
    ...init,
  });
  if (!res.ok) {
    throw new Error(`Request failed: ${res.status} ${path}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  inspectAuth: async (): Promise<UserPrincipal | null> => {
    const url = `${BASE_URL}/auth/inspect`;
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

  getFeed: async (page: number = 1, limit: number = 10): Promise<FeedResponse> => {
    const raw = await apiFetch<FeedResponse>(`/dynamic-feeds?page=${page}&limit=${limit}`);
    raw.posts?.forEach(resolvePostImages);
    return raw;
  },

  getMyProfile: async (): Promise<{ profile: ProfileDto }> => {
    const raw = await apiFetch<{ profile: ProfileDto }>('/profiles/me');
    resolveProfileImages(raw.profile);
    return raw;
  },

  getUserPosts: async (userId: number): Promise<{ posts: PostDto[] }> => {
    const wrapped = await apiFetch<BaseResponse<{ posts: PostDto[] }>>(`/posts/user/${userId}`);
    const posts = wrapped.data?.posts ?? [];
    posts.forEach(resolvePostImages);
    return { posts };
  },

  login: async (username: string, password: string): Promise<UserPrincipal> => {
    const res = await fetch(`${BASE_URL}/auth/login`, {
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
    const res = await fetch(`${BASE_URL}/auth/register`, {
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
    const raw = await apiFetch<BaseResponse<{ post: PostDto }>>('/posts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ base64ImageString, caption }),
    });
    if (!raw.success) {
      throw new Error(raw.message || 'Failed to create post');
    }
    return resolvePostImages(raw.data.post);
  },

  likePost: async (postId: number): Promise<PostDto> => {
    const raw = await apiFetch<BaseResponse<{ post: PostDto }>>(`/posts/like/${postId}`, {
      method: 'POST',
    });
    if (!raw.success) {
      throw new Error(raw.message || 'Failed to like post');
    }
    return resolvePostImages(raw.data.post);
  },

  unlikePost: async (postId: number): Promise<PostDto> => {
    const raw = await apiFetch<BaseResponse<{ post: PostDto }>>(`/posts/like/${postId}`, {
      method: 'DELETE',
    });
    if (!raw.success) {
      throw new Error(raw.message || 'Failed to unlike post');
    }
    return resolvePostImages(raw.data.post);
  },

  commentOnPost: async (postId: number, comment: string): Promise<PostDto> => {
    const raw = await apiFetch<BaseResponse<{ post: PostDto }>>('/comments', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ postId, comment }),
    });
    if (!raw.success) {
      throw new Error(raw.message || 'Failed to add comment');
    }
    return resolvePostImages(raw.data.post);
  }
};
