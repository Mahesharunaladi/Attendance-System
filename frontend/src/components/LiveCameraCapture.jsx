import { useCallback, useEffect, useRef, useState } from 'react';
import { attendanceAPI } from '../services/api';

export default function LiveCameraCapture({ imageFile, onCapture, onWorkerDetected }) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const previewUrlRef = useRef(null);
  const detectionIntervalRef = useRef(null);
  const lastBlinkTimeRef = useRef(0);
  const blinkThresholdRef = useRef(500); // milliseconds

  const [cameraError, setCameraError] = useState('');
  const [isCameraReady, setIsCameraReady] = useState(false);
  const [previewUrl, setPreviewUrl] = useState('');
  const [detectionStatus, setDetectionStatus] = useState('');
  const [detectedWorker, setDetectedWorker] = useState(null);
  const [isDetecting, setIsDetecting] = useState(false);

  const updatePreview = useCallback((nextUrl) => {
    if (previewUrlRef.current) {
      URL.revokeObjectURL(previewUrlRef.current);
    }

    previewUrlRef.current = nextUrl || null;
    setPreviewUrl(nextUrl || '');
  }, []);

  const stopCamera = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
    if (detectionIntervalRef.current) {
      clearInterval(detectionIntervalRef.current);
    }
    setIsCameraReady(false);
    setIsDetecting(false);
  }, []);

  const startCamera = useCallback(async () => {
    if (!navigator.mediaDevices?.getUserMedia) {
      setCameraError('Live camera capture is not supported in this browser.');
      return;
    }

    stopCamera();
    setCameraError('');
    setDetectionStatus('Starting camera...');

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
      setDetectionStatus('Camera ready - loading models...');
      
      // Wait a bit for video to stabilize, then mark detection as active.
      setTimeout(() => {
        setIsDetecting(true);
        setDetectionStatus('Detecting face... Please blink to capture');
      }, 500);
    } catch (error) {
      setCameraError(
        error.name === 'NotAllowedError'
          ? 'Camera access was denied. Please allow camera access and try again.'
          : 'Unable to access the camera right now.'
      );
    }
  }, [stopCamera]);

  const captureFrame = useCallback(() => {
    if (!videoRef.current || !canvasRef.current || !isCameraReady) {
      return null;
    }

    const video = videoRef.current;
    const canvas = canvasRef.current;
    canvas.width = video.videoWidth || 1280;
    canvas.height = video.videoHeight || 720;

    const context = canvas.getContext('2d');
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    return canvas;
  }, [isCameraReady]);

  const detectFaceAndIdentifyWorker = useCallback(async (canvas) => {
    if (!canvas) return;

    try {
      // Convert canvas to blob for sending to backend
      canvas.toBlob(
        async (blob) => {
          if (!blob) return;

          const formData = new FormData();
          formData.append('image', blob, `face-${Date.now()}.jpg`);

          try {
            // Call backend API to identify worker from face
            const response = await attendanceAPI.identifyWorkerFromFace(formData);
            const worker = response?.data?.worker;
            
            if (worker) {
              setDetectedWorker(worker);
              setDetectionStatus(`Detected: ${worker.fullName}`);
              
              // Call the parent callback with worker details
              if (onWorkerDetected) {
                onWorkerDetected(worker, blob);
              }

              // Create a file object and pass it to onCapture
              const file = new File([blob], `face-capture-${Date.now()}.jpg`, {
                type: 'image/jpeg',
              });
              onCapture(file);
              updatePreview(URL.createObjectURL(file));

              // Auto-stop camera after successful detection
              setTimeout(() => {
                stopCamera();
              }, 1500);
            }
          } catch (error) {
            console.error('Error identifying worker:', error);
            setDetectionStatus('Face detected but could not identify worker');
          }
        },
        'image/jpeg',
        0.92
      );
    } catch (error) {
      console.error('Error in face detection:', error);
    }
  }, [onCapture, onWorkerDetected, stopCamera, updatePreview]);

  const startFaceDetection = useCallback(() => {
    if (detectionIntervalRef.current) {
      clearInterval(detectionIntervalRef.current);
    }

    // Simulate face detection with simple eye blink detection
    // In a real implementation, you'd use a library like face-api.js or ml5.js
    let frameCount = 0;
    let previousBrightness = 0;

    detectionIntervalRef.current = setInterval(() => {
      if (!videoRef.current || !isDetecting) return;

      frameCount++;

      // Capture frame every 5 frames (reduce processing overhead)
      if (frameCount % 5 === 0) {
        const canvas = captureFrame();
        if (!canvas) return;

        const context = canvas.getContext('2d');
        const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
        const data = imageData.data;

        // Calculate average brightness
        let brightness = 0;
        for (let i = 0; i < data.length; i += 4) {
          brightness += (data[i] + data[i + 1] + data[i + 2]) / 3;
        }
        brightness = brightness / (data.length / 4);

        // Detect blink by sudden brightness change (eyes closing)
        const brightnessDiff = Math.abs(brightness - previousBrightness);
        
        if (brightnessDiff > 30 && Date.now() - lastBlinkTimeRef.current > blinkThresholdRef.current) {
          lastBlinkTimeRef.current = Date.now();
          setDetectionStatus('Blink detected! Capturing...');
          
          // Capture the current frame
          detectFaceAndIdentifyWorker(canvas);
        }

        previousBrightness = brightness;
      }
    }, 100); // Check every 100ms
  }, [captureFrame, detectFaceAndIdentifyWorker, isDetecting]);

  useEffect(() => {
    if (isDetecting) {
      startFaceDetection();
    }
  }, [isDetecting, startFaceDetection]);

  useEffect(() => {
    startCamera();
    return () => {
      stopCamera();
      if (previewUrlRef.current) {
        URL.revokeObjectURL(previewUrlRef.current);
      }
    };
  }, [startCamera, stopCamera]);

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
    setDetectedWorker(null);
    setDetectionStatus('');
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

      {detectionStatus && (
        <div className={`detection-status ${detectedWorker ? 'success' : 'detecting'}`}>
          {detectionStatus}
        </div>
      )}

      {detectedWorker && (
        <div className="worker-details">
          <h3>Worker Identified</h3>
          <div className="details-grid">
            <div className="detail-item">
              <label>Name:</label>
              <span>{detectedWorker.fullName}</span>
            </div>
            <div className="detail-item">
              <label>Employee ID:</label>
              <span>{detectedWorker.employeeId}</span>
            </div>
            <div className="detail-item">
              <label>Aadhar Number:</label>
              <span>{detectedWorker.aadharNumber || 'N/A'}</span>
            </div>
            <div className="detail-item">
              <label>Phone Number:</label>
              <span>{detectedWorker.phoneNumber || 'N/A'}</span>
            </div>
            <div className="detail-item">
              <label>Gender:</label>
              <span>{detectedWorker.gender || 'N/A'}</span>
            </div>
            <div className="detail-item">
              <label>Caste:</label>
              <span>{detectedWorker.caste || 'N/A'}</span>
            </div>
            <div className="detail-item">
              <label>Role:</label>
              <span>{detectedWorker.role || 'N/A'}</span>
            </div>
            <div className="detail-item">
              <label>Department:</label>
              <span>{detectedWorker.department || 'N/A'}</span>
            </div>
          </div>
        </div>
      )}

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
            disabled={!isCameraReady || isDetecting}
          >
            {isDetecting ? 'Auto-detecting... Blink to capture' : isCameraReady ? 'Capture Photo Manually' : 'Starting Camera...'}
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
