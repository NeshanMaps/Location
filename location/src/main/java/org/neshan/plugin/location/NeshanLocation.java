package org.neshan.plugin.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.neshan.styles.MarkerOrientation;
import org.neshan.ui.MapView;

import androidx.annotation.RequiresPermission;

import static org.neshan.plugin.location.LocationFailed.NO_LOCATION;
import static org.neshan.plugin.location.LocationFailed.NO_PERMISSION;

public class NeshanLocation {
    private static final String TAG = "NeshanLocation";

    private ViewLocation viewLocation;
    private Location mLocation;
    private Context context;
    private LocationManager locationManager;
    private boolean networkEnabled;
    private boolean gpsEnabled;

    private LocationCallback currentLocationCallback;
    private LocationCallback updateLocationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private OnLocationEventListener eventListener;

    private LocationListener currentLocationListener;
    private LocationListener updateGpsLocationListener;
    private LocationListener updateNetworkLocationListener;
    private boolean updateOnlyGps;
    private boolean onFailedCalled;


    public NeshanLocation(Context context, MapView mapView) {
        this.context = context;
        viewLocation = ViewLocation.getViewLocation(context, mapView);
        this.eventListener = (OnLocationEventListener) context;
        initLocationServices();
    }


    private void initLocationServices() {
        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        if (isPlayServiceOK()) {
            initGoogleLocationApi();
        } else {
            initAndroidLocationApi();
        }
    }

    private void initGoogleLocationApi() {
        try {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            currentLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        mLocation = locationResult.getLastLocation();
                        viewLocation.updateUI(mLocation);
                        fusedLocationProviderClient.removeLocationUpdates(currentLocationCallback);
                    }
                }
            };

            updateLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        mLocation = locationResult.getLastLocation();
                        viewLocation.updateUI(mLocation);
                        onFailedCalled = false;
                    }

                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (!isLocationEnabled()) {
                        if (eventListener != null) {
                            eventListener.onLocationFailed(NO_LOCATION);
                        }
                    }
                }
            };
        } catch (Exception e) {
            initAndroidLocationApi();
        }
    }


    private void initAndroidLocationApi() {
        currentLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(currentLocationListener);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        updateGpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
                viewLocation.updateUI(mLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        updateNetworkLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
                viewLocation.updateUI(mLocation);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };


    }


    @RequiresPermission(anyOf = {"android.permission.ACCESS_FINE_LOCATION"})
    private void autoLocationUpdateFromGoogleAPI(long interval) {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(interval);
        request.setFastestInterval(1000);
        fusedLocationProviderClient.requestLocationUpdates(request, updateLocationCallback, null);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    viewLocation.updateUI(location);
                }
            }
        });
    }

    @RequiresPermission(anyOf = {"android.permission.ACCESS_FINE_LOCATION"})
    private void autoLocationUpdateFromAndroidAPI(long interval) {
        if (gpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    interval, 10, updateGpsLocationListener);
            locationManager.addGpsStatusListener(new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {
                    if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                        updateOnlyGps = true;
                    }
                }
            });

            if (updateOnlyGps) {
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLocation != null) {
                    viewLocation.updateUI(mLocation);
                    locationManager.removeUpdates(updateNetworkLocationListener);
                    return;
                }
            }
        }
        if (networkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    interval, 10, updateNetworkLocationListener);
            mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (mLocation != null) {
                viewLocation.updateUI(mLocation);
            }
        }
    }


    @RequiresPermission(anyOf = {"android.permission.ACCESS_FINE_LOCATION"})
    private void getCurrentLocationFromGoogleAPI() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(6000);
        request.setFastestInterval(1000);
        fusedLocationProviderClient.requestLocationUpdates(request, currentLocationCallback, null);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    viewLocation.updateUI(location);
                }
            }
        });
    }

    @RequiresPermission(anyOf = {"android.permission.ACCESS_FINE_LOCATION"})
    private void getCurrentLocationFromAndroidAPI() {
        if (gpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    6000, 10, currentLocationListener);
            locationManager.addGpsStatusListener(new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {
                    if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                        updateOnlyGps = true;
                    }
                }
            });

            if (updateOnlyGps) {
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mLocation != null) {
                    viewLocation.updateUI(mLocation);
                    return;
                }
            }
        }

        if (networkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    6000, 10, currentLocationListener);
            mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (mLocation != null) {
                viewLocation.updateUI(mLocation);
            }
        }
    }


    private boolean isPlayServiceOK() {
        boolean isOK = false;
        try {
            GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
            int availableResult = googleApi.isGooglePlayServicesAvailable(context);
            if (availableResult == ConnectionResult.SUCCESS) {
                isOK = true;
            }
        } catch (Exception e) {

        }

        return isOK;
    }


    private boolean isLocationEnabled() {
        boolean locationEnabled = true;
        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!networkEnabled && !gpsEnabled) {
            locationEnabled = false;
            if (eventListener != null) {
                eventListener.onLocationFailed(NO_LOCATION);
            }
        }

        return locationEnabled;
    }

    private boolean isPermissionGranted() {
        boolean permissionGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                if (eventListener != null) {
                    eventListener.onLocationFailed(NO_PERMISSION);
                }
            }
        }

        return permissionGranted;

    }


    @RequiresPermission(anyOf = {"android.permission.ACCESS_FINE_LOCATION"})
    public void startAutoLocationUpdate(long interval) {
        if (!isLocationEnabled()) {
            return;
        }

        if (!isPermissionGranted()) {
            return;
        }
        if (fusedLocationProviderClient != null) {
            autoLocationUpdateFromGoogleAPI(interval);
        } else {
            autoLocationUpdateFromAndroidAPI(interval);
        }
    }


    @RequiresPermission(anyOf = {"android.permission.ACCESS_FINE_LOCATION"})
    public void getCurrentLocation() {
        if (!isLocationEnabled()) {
            return;
        }

        if (!isPermissionGranted()) {
            return;
        }


        if (fusedLocationProviderClient != null) {
            getCurrentLocationFromGoogleAPI();
        } else {
            getCurrentLocationFromAndroidAPI();
        }
    }

    public void stopAutoLocationUpdate() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(updateLocationCallback);
            fusedLocationProviderClient.removeLocationUpdates(currentLocationCallback);
        } else {
            locationManager.removeUpdates(currentLocationListener);
            locationManager.removeUpdates(updateGpsLocationListener);
            locationManager.removeUpdates(updateNetworkLocationListener);
        }
    }


    public void setMarkerIcon(int markerIcon) {
        viewLocation.setMarkerIcon(markerIcon);
    }

    public void setMarkerSize(float markerSize) {
        viewLocation.setMarkerSize(markerSize);
    }

    public void setCircleFillColor(int color) {
        viewLocation.setCircleFillColor(color);
    }

    public void setRippleOneFillColor(int color) {
        viewLocation.setRippleOneFillColor(color);
    }

    public void setRippleTwoFillColor(int color) {
        viewLocation.setRippleTwoFillColor(color);
    }

    public void setCircleStrokeColor(int color) {
        viewLocation.setCircleStrokeColor(color);
    }

    public void setCircleOpacity(float opacity) {
        viewLocation.setCircleOpacity(opacity);
    }

    public void setCircleStrokeWidth(float width) {
        viewLocation.setCircleStrokeWidth(width);
    }

    public void setCircleVisible(boolean visible) {
        viewLocation.setCircleVisible(visible);
    }

    public void setMarkerMode(MarkerOrientation markerMode) {
        viewLocation.setMarkerMode(markerMode);
    }

    public void setRippleEnable(boolean enable) {
        viewLocation.setRippleEnable(enable);
    }

    public int getMarkerIcon() {
        return viewLocation.getMarkerIcon();
    }

    public float getMarkerSize() {
        return viewLocation.getMarkerSize();
    }


    public String getCircleFillColor() {
        return viewLocation.getCircleFillColor();
    }

    public String getCircleStrokeColor() {
        return viewLocation.getCircleStrokeColor();
    }


    public float getCircleOpacity() {
        return viewLocation.getCircleOpacity();
    }

    public boolean isCircleVisible() {
        return viewLocation.isCircleVisible();
    }

    public float getCircleStrokeWidth() {
        return viewLocation.getCircleStrokeWidth();
    }

    public MarkerOrientation getMarkerMode() {
        return viewLocation.getMarkerMode();
    }

    public boolean isRippleEnable() {
        return viewLocation.isRippleEnable();
    }

    public interface OnLocationEventListener {
        void onLocationReceived(Location location);

        void onLocationFailed(LocationFailed failed);
    }


}


