import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle2, AlertCircle } from 'lucide-react';
import { api, type PostDto } from '../services/api';
import './CreatePost.css';

interface ToastState {
  kind: 'success' | 'error';
  message: string;
}

const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

const CreatePost = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [caption, setCaption] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [toast, setToast] = useState<ToastState | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3500);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      setError('Please choose an image file.');
      return;
    }
    if (file.size > MAX_IMAGE_BYTES) {
      setError('Image is larger than 5MB. Please pick a smaller file.');
      return;
    }
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setError('');
    setSelectedFile(file);
    setPreviewUrl(URL.createObjectURL(file));
  };

  const clearImage = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setSelectedFile(null);
    setPreviewUrl(null);
  };

  /**
   * Reads the file as a data URI (e.g. `data:image/png;base64,iVBORw0K...`).
   * The data URI prefix MUST be preserved so the backend `parseExtension`
   * helper can detect the MIME type. See ADR-003.
   */
  const toDataUri = (file: File): Promise<string> =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = () => reject(reader.error ?? new Error('Failed to read file'));
    });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setError('Please select an image to upload.');
      return;
    }
    if (!caption.trim()) {
      setError('Please write a caption.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const base64ImageString = await toDataUri(selectedFile);
      const created: PostDto = await api.createPost(base64ImageString, caption.trim());
      setToast({ kind: 'success', message: 'Post shared to your feed.' });
      window.setTimeout(() => navigate('/'), 600);
      void created;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to create post. Please try again.';
      setError(message);
      setToast({ kind: 'error', message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-post-container animate-fade-in">
      <div className="glass-panel create-post-card">
        <h2 className="text-gradient">Create New Post</h2>

        {error && (
          <div className="error-message" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="create-post-form">
          <div className="image-upload-section">
            {previewUrl ? (
              <div className="image-preview">
                <img src={previewUrl} alt="Selected preview" />
                <button
                  type="button"
                  className="btn-remove-image"
                  onClick={clearImage}
                  disabled={loading}
                >
                  Remove Image
                </button>
              </div>
            ) : (
              <label className="image-upload-label">
                <div className="upload-placeholder">
                  <span>Click to select an image</span>
                  <p>JPG, PNG, GIF, WebP up to 5MB</p>
                </div>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleFileChange}
                  className="hidden-file-input"
                />
              </label>
            )}
          </div>

          <div className="caption-section">
            <textarea
              placeholder="Write a caption..."
              value={caption}
              onChange={e => setCaption(e.target.value)}
              className="caption-input"
              rows={4}
              maxLength={2200}
            />
            <div className="caption-counter">{caption.length} / 2200</div>
          </div>

          <button
            type="submit"
            className="btn-primary submit-btn"
            disabled={loading || !selectedFile || !caption.trim()}
          >
            {loading ? 'Sharing...' : 'Share Post'}
          </button>
        </form>
      </div>

      {toast && (
        <div
          className={`toast ${toast.kind === 'success' ? 'toast-success' : 'toast-error'}`}
          role="status"
          aria-live="polite"
        >
          {toast.kind === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <span>{toast.message}</span>
        </div>
      )}
    </div>
  );
};

export default CreatePost;
