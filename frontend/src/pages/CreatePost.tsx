import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import './CreatePost.css';

const CreatePost = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [caption, setCaption] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
      const file = e.target.files[0];
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const toBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        let result = reader.result as string;
        // The backend expects just the base64 string without the data URI prefix (e.g. data:image/jpeg;base64,...)
        // Actually, it usually expects the pure base64 string. Let's strip the prefix just in case, 
        // OR the backend might handle it. Let's send the pure base64 string.
        const base64String = result.replace(/^data:image\/[a-z]+;base64,/, "");
        resolve(base64String);
      };
      reader.onerror = error => reject(error);
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
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
      const base64ImageString = await toBase64(selectedFile);
      await api.createPost(base64ImageString, caption);
      navigate('/'); // Redirect to feed on success
    } catch (err: any) {
      setError(err.message || 'Failed to create post. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-post-container animate-fade-in">
      <div className="glass-panel create-post-card">
        <h2 className="text-gradient">Create New Post</h2>
        
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="create-post-form">
          <div className="image-upload-section">
            {previewUrl ? (
              <div className="image-preview">
                <img src={previewUrl} alt="Preview" />
                <button type="button" className="btn-remove-image" onClick={() => { if (previewUrl) URL.revokeObjectURL(previewUrl); setSelectedFile(null); setPreviewUrl(null); }}>
                  Remove Image
                </button>
              </div>
            ) : (
              <label className="image-upload-label">
                <div className="upload-placeholder">
                  <span>Click to select an image</span>
                  <p>JPG, PNG, GIF up to 5MB</p>
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
              onChange={(e) => setCaption(e.target.value)}
              className="caption-input"
              rows={4}
            />
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
    </div>
  );
};

export default CreatePost;
