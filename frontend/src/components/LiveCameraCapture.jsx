import { useCallback, useEffect, useRef, useState } from 'react';
import { attendanceAPI } from '../services/api';

export default function LiveCameraCapture({
  imageFile,
  onCapture,
  onWorkerDetected,
  mode = 'identify',
  showDetectedWorkerDetails = true,
}) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const previewUrlRef = useRef(null);
  const detectionIntervalRef = useRef(null);
  const detectionTimeoutRef = useRef(null);
  const cameraRequestRef = useRef(0);

  const [cameraError, setCameraError] = useState('');
  const [isCameraReady, setIsCameraReady] = useState(false);
  const [previewUrl, setPreviewUrl] = useState('');
  const [detectionStatus, setDetectionStatus] = useState('');
  const [detectedWorker, setDetectedWorker] = useState(null);
  const [isDetecting, setIsDetecting] = useState(false);
  const isRegistrationMode = mode === 'register';

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
      detectionIntervalRef.current = null;
    }
    if (detectionTimeoutRef.current) {
      clearTimeout(detectionTimeoutRef.current);
      detectionTimeoutRef.current = null;
    }
    setIsCameraReady(false);
    setIsDetecting(false);
  }, []);

  const startCamera = useCallback(async () => {
    const requestId = ++cameraRequestRef.current;

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

      if (requestId !== cameraRequestRef.current) {
        stream.getTracks().forEach((track) => track.stop());
        return;
      }

      streamRef.current = stream;

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
      }

      setIsCameraReady(true);
      if (isRegistrationMode) {
        setDetectionStatus('Camera ready. Capture the worker photo to continue registration.');
      } else {
        setDetectionStatus('📸 Preparing for face detection...');
        
        // Wait a bit for video to stabilize, then mark detection as active.
        detectionTimeoutRef.current = setTimeout(() => {
          if (requestId !== cameraRequestRef.current) {
            return;
          }
          setIsDetecting(true);
          setDetectionStatus('�️ Face detection active - Auto-capturing in 2.5 seconds...');
        }, 300);
      }
    } catch (error) {
      if (requestId !== cameraRequestRef.current) {
        return;
      }
      setCameraError(
        error.name === 'NotAllowedError'
          ? 'Camera access was denied. Please allow camera access and try again.'
          : error.name === 'NotReadableError' || error.name === 'AbortError'
            ? 'Camera is busy or was interrupted. Please close other camera tabs/apps and try again.'
            : 'Unable to access the camera right now.'
      );
    }
  }, [isRegistrationMode, stopCamera]);

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
            console.log('Face identification response:', response);
            
            // Check both data.worker and worker in response
            const worker = response?.data?.worker || response?.worker;
            
            if (worker && worker.employeeId) {
              setDetectedWorker(worker);
              setDetectionStatus(`✓ Recognized: ${worker.fullName}`);
              console.log('Worker detected:', worker);
              
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
            } else {
              console.warn('No worker data in response:', response);
              setDetectionStatus('Face detected but worker not identified. Try "Capture Photo Manually"');
              
              // Still capture the image even if worker not identified
              const file = new File([blob], `face-capture-unidentified-${Date.now()}.jpg`, {
                type: 'image/jpeg',
              });
              onCapture(file);
              updatePreview(URL.createObjectURL(file));
              stopCamera();
            }
          } catch (error) {
            console.error('Error identifying worker:', error);
            
            // Still capture the image even on error
            const file = new File([blob], `face-capture-error-${Date.now()}.jpg`, {
              type: 'image/jpeg',
            });
            onCapture(file);
            updatePreview(URL.createObjectURL(file));
            
            setDetectionStatus(`Face captured but identification failed. ${error.response?.data?.message || 'Please try manual capture.'}`);
            stopCamera();
          }
        },
        'image/jpeg',
        0.92
      );
    } catch (error) {
      console.error('Error in face detection:', error);
      setDetectionStatus('Error capturing face. Please try again.');
    }
  }, [onCapture, onWorkerDetected, stopCamera, updatePreview]);

  const startFaceDetection = useCallback(() => {
    if (detectionIntervalRef.current) {
      clearInterval(detectionIntervalRef.current);
    }

    const QUICK_CAPTURE_TIMEOUT = 2500; // 2.5 seconds quick capture (reduced from 3s)
    const MAX_TIMEOUT = 8000; // 8 seconds max timeout (reduced from 10s)
    const startTime = Date.now();
    let captureAttempted = false;
    let updateCounter = 0;

    detectionIntervalRef.current = setInterval(() => {
      if (!videoRef.current || !isDetecting) return;

      const elapsedTime = Date.now() - startTime;
      updateCounter++;

      // Quick auto-capture after 2.5 seconds if no manual capture
      if (elapsedTime > QUICK_CAPTURE_TIMEOUT && !captureAttempted) {
        captureAttempted = true;
        console.log('Auto-capturing face after 2.5 seconds...');
        const canvas = captureFrame();
        if (canvas) {
          setDetectionStatus('📸 Capturing face...');
          detectFaceAndIdentifyWorker(canvas);
          if (detectionIntervalRef.current) {
            clearInterval(detectionIntervalRef.current);
            detectionIntervalRef.current = null;
          }
        }
        return;
      }

      // Force capture after max timeout
      if (elapsedTime > MAX_TIMEOUT) {
        console.log('Force capturing face after 8 second timeout...');
        const canvas = captureFrame();
        if (canvas) {
          setDetectionStatus('📸 Force capturing (timeout)...');
          detectFaceAndIdentifyWorker(canvas);
        }
        if (detectionIntervalRef.current) {
          clearInterval(detectionIntervalRef.current);
          detectionIntervalRef.current = null;
        }
        return;
      }

      // Update UI every ~500ms
      if (updateCounter % 1 === 0 && elapsedTime < QUICK_CAPTURE_TIMEOUT) {
        const remaining = Math.ceil((QUICK_CAPTURE_TIMEOUT - elapsedTime) / 1000);
        setDetectionStatus(`📸 Auto-capturing in ${remaining} second${remaining !== 1 ? 's' : ''}...`);
      }
    }, 250); // More frequent interval for better tracking
  }, [captureFrame, detectFaceAndIdentifyWorker, isDetecting]);

  useEffect(() => {
    if (isDetecting && !isRegistrationMode) {
      startFaceDetection();
    }
  }, [isDetecting, isRegistrationMode, startFaceDetection]);

  useEffect(() => {
    startCamera();
    return () => {
      cameraRequestRef.current += 1;
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

        console.log('Manual capture: attempting face identification');
        
        // For manual capture in identify mode, also try to identify the worker
        if (!isRegistrationMode) {
          detectFaceAndIdentifyWorker(canvas);
        } else {
          // For registration mode, just capture without identification
          onCapture(file);
          updatePreview(URL.createObjectURL(file));
          stopCamera();
        }
      },
      'image/jpeg',
      0.92
    );
  }, [isCameraReady, onCapture, stopCamera, updatePreview, isRegistrationMode, detectFaceAndIdentifyWorker]);

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

      {detectedWorker && showDetectedWorkerDetails && (
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
            disabled={!isCameraReady}
          >
            {isRegistrationMode
              ? (isCameraReady ? 'Capture Registration Photo' : 'Starting Camera...')
              : (isCameraReady ? '📷 Capture Photo Manually' : 'Starting Camera...')}
          </button>
        )}
      </div>

      {imageFile && <p className="file-name">{imageFile.name}</p>}
      {cameraError && <p className="camera-error">{cameraError}</p>}
      {!imageFile && !cameraError && (
        <>
          <p className="camera-help">
            {isRegistrationMode 
              ? 'Allow camera access, then capture a photo to register.'
              : '✓ Allow camera access\n✓ Face will be auto-captured in 3 seconds\n✓ Or click "Capture Photo Manually" to capture now'}
          </p>
          {isDetecting && !isRegistrationMode && (
            <p className="camera-help" style={{ color: '#27ae60', fontWeight: '600' }}>
              ⏳ Auto-detecting... Stand still (3-second auto-capture)
            </p>
          )}
        </>
      )}
    </div>
  );
}
