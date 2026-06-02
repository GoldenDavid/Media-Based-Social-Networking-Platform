import { describe, expect, it } from 'vitest';
import { unwrap, type BaseResponse, type PostDto } from './api';

describe('api.unwrap', () => {
  it('returns the inner data of a BaseResponse envelope', () => {
    const posts: PostDto[] = [];
    const wrapped: BaseResponse<{ posts: PostDto[] }> = {
      success: true,
      message: 'ok',
      timestamp: '2026-06-02T00:00:00Z',
      data: { posts },
    };
    const result = unwrap(wrapped);
    expect(result).toEqual({ posts });
    expect(result.posts).toBe(posts);
  });

  it('returns the profile payload from a nested envelope', () => {
    const wrapped: BaseResponse<{ profile: { id: number; username: string } }> = {
      success: true,
      message: 'ok',
      timestamp: '2026-06-02T00:00:00Z',
      data: { profile: { id: 1, username: 'alice_dev' } },
    };
    const { profile } = unwrap(wrapped);
    expect(profile.username).toBe('alice_dev');
  });
});
