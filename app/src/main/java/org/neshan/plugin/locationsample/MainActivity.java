package org.neshan.plugin.locationsample;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.neshan.plugin.location.LocationFailed;
import org.neshan.plugin.location.NeshanLocation;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.neshan.core.LngLat;
import org.neshan.services.NeshanMapStyle;
import org.neshan.services.NeshanServices;
import org.neshan.ui.MapView;

public class MainActivity extends AppCompatActivity implements NeshanLocation.OnLocationEventListener {

    private MapView map;
    private NeshanLocation neshanLocation;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkPermission();
        initMap();
    }

    @Override
    protected void onPause() {
        super.onPause();
        neshanLocation.stopAutoLocationUpdate();
    }

    private void checkPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            String message = "اجازه دسترسی به لوکیشن را ندارید\n\nمیخواهید اجازه دسترسی به لوکیشن را دریافت کنید؟";
                            showFailDialog(message, 0);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void initViews() {
        map = findViewById(R.id.map);
    }

    private void initMap() {
        map.getLayers().insert(0, NeshanServices.createBaseMap(NeshanMapStyle.STANDARD_DAY));
        map.setFocalPointPosition(new LngLat(59.5435513, 36.3137498), 0);
        map.setZoom(15f, 0);
        neshanLocation = new NeshanLocation(this, map);
    }


    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void openLocationSetting() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void showFailDialog(String message, final int id) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("بله", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (id == 0) {
                    openAppSettings();
                } else {
                    openLocationSetting();
                }
            }
        });
        alertDialog.setNegativeButton("خیر", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();

    }


    public void updateLocationClick(View view) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    @RequiresPermission(anyOf = {"android.permission.ACCESS_FINE_LOCATION"})
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                       // neshanLocation.startAutoLocationUpdate(6000);
                        neshanLocation.startAutoLocationUpdate(6000);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            String message = "اجازه دسترسی به لوکیشن را ندارید\n\nمیخواهید اجازه دسترسی به لوکیشن را دریافت کنید؟";
                            showFailDialog(message, 0);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    @Override
    public void onLocationReceived(Location location) {
        Log.i(TAG, "onLocationReceived: ");
         map.setFocalPointPosition(new LngLat(location.getLongitude(), location.getLatitude()), 0.5f);
    }

    @Override
    public void onLocationFailed(LocationFailed failed) {
        if (failed == LocationFailed.NO_PERMISSION) {
            Log.i(TAG, "onLocationFailed: no permission");
        } else {
            Log.i(TAG, "onLocationFailed: no location");
            showFailDialog("لوکیشن گوشی فعال نیست!\n\nمیخواهید لوکیشن گوشی را فعال کنید ؟", 1);
        }


    }

}