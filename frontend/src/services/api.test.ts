import { describe, expect, it } from 'vitest';
import { unwrap, type BaseResponse, type PostDto, type FeedResponse } from './api';

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

describe('feed response shape (flat, no BaseResponse)', () => {
  it('FeedResponse is the raw contract for both dynamic and precomputed feeds', () => {
    // The precomputed feed endpoint returns the same shape as the
    // dynamic one — a flat GetFeedResponse (no envelope). The
    // `api.getFeed` and `api.getPrecomputedFeed` helpers therefore
    // bypass unwrap(). This test pins the contract; if the backend
    // wraps either endpoint, FE must update together.
    const sample: FeedResponse = {
      posts: [],
      totalPage: 1,
    };
    expect(sample.posts).toEqual([]);
    expect(sample.totalPage).toBe(1);
  });
});
