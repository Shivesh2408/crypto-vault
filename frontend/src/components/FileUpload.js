import React, { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { toast } from 'react-hot-toast';
import apiService from '../services/apiService';

export default function FileUpload({ onUploadSuccess }) {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  const onDrop = useCallback(async (acceptedFiles) => {
    if (acceptedFiles.length === 0) return;

    setUploading(true);
    setProgress(0);
    let successCount = 0;

    for (const file of acceptedFiles) {
      const formData = new FormData();
      formData.append('file', file);
      try {
        await apiService.uploadFile(formData, (e) => {
          if (e.total) setProgress(Math.round((e.loaded / e.total) * 100));
        });
        successCount++;
      } catch {
        toast.error(`Failed to upload: ${file.name}`);
      }
    }

    if (successCount > 0) {
      toast.success(`${successCount} file(s) uploaded and encrypted`);
      onUploadSuccess?.();
    }
    setUploading(false);
    setProgress(0);
  }, [onUploadSuccess]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: true,
    maxSize: 50 * 1024 * 1024, // 50MB
  });

  return (
    <div>
      <div
        {...getRootProps()}
        style={{
          border: `2px dashed ${isDragActive ? '#1e40af' : '#cbd5e1'}`,
          borderRadius: '0.5rem',
          padding: '2rem',
          textAlign: 'center',
          cursor: 'pointer',
          background: isDragActive ? 'rgba(30,64,175,0.05)' : 'transparent',
          transition: 'all 0.2s',
        }}
      >
        <input {...getInputProps()} />
        <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📁</div>
        {isDragActive ? (
          <p style={{ color: '#1e40af' }}>Drop files here to upload</p>
        ) : (
          <p style={{ color: '#64748b' }}>
            Drag & drop files here, or <span style={{ color: '#1e40af' }}>click to select</span>
            <br />
            <small>Files are encrypted with AES-256 before storage • Max 50MB</small>
          </p>
        )}
      </div>

      {uploading && (
        <div style={{ marginTop: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem', fontSize: '0.875rem' }}>
            <span>Encrypting & uploading...</span>
            <span>{progress}%</span>
          </div>
          <div style={{ height: '8px', background: '#e2e8f0', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{
              height: '100%',
              width: `${progress}%`,
              background: '#1e40af',
              transition: 'width 0.3s',
            }} />
          </div>
        </div>
      )}
    </div>
  );
}
