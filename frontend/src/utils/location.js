export function getCurrentLocation() {
  if (!navigator.geolocation) {
    return Promise.reject(
      new Error('Geolocation is not supported in this browser.')
    );
  }

  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude.toFixed(6),
          longitude: position.coords.longitude.toFixed(6),
        });
      },
      (error) => {
        const message =
          error.code === error.PERMISSION_DENIED
            ? 'Location access was denied. Please allow location access and try again.'
            : 'Unable to fetch your current location.';

        reject(new Error(message));
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  });
}
