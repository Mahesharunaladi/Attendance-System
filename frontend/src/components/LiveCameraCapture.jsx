import { useCallback, useEffect, useRef, useState } from 'react';

export default function LiveCameraCapture({ imageFile, onCapture }) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const previewUrlRef = useRef(null);

  const [cameraError, setCameraError] = useState('');
  const [isCameraReady, setIsCameraReady] = useState(false);
  const [previewUrl, setPreviewUrl] = useState('');

  const stopCamera = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
    setIsCameraReady(false);
  }, []);

  const startCamera = useCallback(async () => {
    if (!navigator.mediaDevices?.getUserMedia) {
      setCameraError('Live camera capture is not supported in this browser.');
      return;
    }

    stopCamera();
    setCameraError('');

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: 'user',
          width: { ideal: 1280 },
          height: { ideal: 720 },
        },
        audio: false,
      });

      streamRef.current = stream;

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
      }

      setIsCameraReady(true);
    } catch (error) {
      setCameraError(
        error.name === 'NotAllowedError'
          ? 'Camera access was denied. Please allow camera access and try again.'
          : 'Unable to access the camera right now.'
      );
    }
  }, [stopCamera]);

  useEffect(() => {
    startCamera();
    return () => {
      stopCamera();
      if (previewUrlRef.current) {
        URL.revokeObjectURL(previewUrlRef.current);
      }
    };
  }, [startCamera, stopCamera]);

  const updatePreview = useCallback((nextUrl) => {
    if (previewUrlRef.current) {
      URL.revokeObjectURL(previewUrlRef.current);
    }

    previewUrlRef.current = nextUrl || null;
    setPreviewUrl(nextUrl || '');
  }, []);

  const handleCapture = useCallback(() => {
    if (!videoRef.current || !canvasRef.current || !isCameraReady) {
      return;
    }

    const video = videoRef.current;
    const canvas = canvasRef.current;
    canvas.width = video.videoWidth || 1280;
    canvas.height = video.videoHeight || 720;

    const context = canvas.getContext('2d');
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    canvas.toBlob(
      (blob) => {
        if (!blob) {
          setCameraError('Unable to capture image. Please try again.');
          return;
        }

        const file = new File([blob], `live-capture-${Date.now()}.jpg`, {
          type: 'image/jpeg',
        });

        onCapture(file);
        updatePreview(URL.createObjectURL(file));
        stopCamera();
      },
      'image/jpeg',
      0.92
    );
  }, [isCameraReady, onCapture, stopCamera, updatePreview]);

  const handleRetake = useCallback(() => {
    onCapture(null);
    updatePreview('');
    startCamera();
  }, [onCapture, startCamera, updatePreview]);

  return (
    <div className="camera-section">
      <div className="camera-frame">
        {previewUrl ? (
          <img src={previewUrl} alt="Captured preview" className="camera-preview" />
        ) : (
          <video
            ref={videoRef}
            className="camera-preview"
            muted
            playsInline
            autoPlay
          />
        )}
      </div>

      <canvas ref={canvasRef} className="camera-canvas" />

      <div className="camera-actions">
        {imageFile ? (
          <button type="button" className="secondary-btn" onClick={handleRetake}>
            Retake Photo
          </button>
        ) : (
          <button
            type="button"
            className="secondary-btn"
            onClick={handleCapture}
            disabled={!isCameraReady}
          >
            {isCameraReady ? 'Capture Photo' : 'Starting Camera...'}
          </button>
        )}
      </div>

      {imageFile && <p className="file-name">{imageFile.name}</p>}
      {cameraError && <p className="camera-error">{cameraError}</p>}
      {!imageFile && !cameraError && (
        <p className="camera-help">Allow camera access, then capture a live image to continue.</p>
      )}
    </div>
  );
}
