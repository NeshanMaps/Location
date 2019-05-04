package org.neshan.plugin.location;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Handler;
import android.view.animation.LinearInterpolator;

import org.neshan.core.LngLat;
import org.neshan.core.LngLatVector;
import org.neshan.geometry.PolygonGeom;
import org.neshan.graphics.ARGB;
import org.neshan.layers.VectorElementLayer;
import org.neshan.services.NeshanServices;
import org.neshan.styles.LineStyle;
import org.neshan.styles.LineStyleCreator;
import org.neshan.styles.MarkerOrientation;
import org.neshan.styles.MarkerStyle;
import org.neshan.styles.MarkerStyleCreator;
import org.neshan.styles.PolygonStyle;
import org.neshan.styles.PolygonStyleCreator;
import org.neshan.ui.MapView;
import org.neshan.utils.BitmapUtils;
import org.neshan.vectorelements.Marker;
import org.neshan.vectorelements.Polygon;

import static java.lang.Math.PI;

public class ViewLocation {
    private static ViewLocation viewLocation;
    private static final String TAG = "ViewLocation";
    private static final double PI_RAD = PI / 180.0;
    private static final double PI_DEG = 180.0 / PI;
    private static final double R = 6371.01;

    private VectorElementLayer markerLayer;
    private VectorElementLayer circleLayer;
    private Context context;
    private MapView map;
    private Marker marker;

    private ValueAnimator animatorRipple;
    private ValueAnimator animatorRippleTwo;

    private LngLatVector lngLatVector;
    private LngLatVector lngLatVectorRipple;
    private LngLatVector lngLatVectorRippleTwo;
    private Polygon polygon;
    private Polygon polygonRipple;
    private Polygon polygonRippleTwo;
    private Location mLocation;


    private int markerIcon = org.neshan.plugin.location.R.drawable.ic_neshan_current_marker;
    private float markerSize = 16f;
    private ARGB circleFillColor = new ARGB((short) 2, (short) 119, (short) 189, (short) 50);
    private ARGB circleStrokeColor = new ARGB((short) 2, (short) 119, (short) 189, (short) 80);

    private NeshanLocation.OnLocationEventListener eventListener;
    private float lineWidth = 0.5f;
    private MarkerOrientation markerMode = MarkerOrientation.GROUND;
    private float circleOpacity;
    private ARGB circlerippleFillColor = new ARGB((short) 2, (short) 119, (short) 189, (short) 60);
    private ARGB circlerippleFillColorTwo = new ARGB((short) 2, (short) 119, (short) 189, (short) 60);
    private boolean rippleEnable;


    protected ViewLocation(Context context, MapView mapView) {
        this.context = context;
        this.map = mapView;
        this.eventListener = (NeshanLocation.OnLocationEventListener) context;
        initLayers();
        intitAnimator();
    }

    protected static ViewLocation getViewLocation(Context context , MapView mapView){
        if (viewLocation == null){
            viewLocation = new ViewLocation(context , mapView);
        }
        return viewLocation;
    }

    private void intitAnimator() {
        animatorRipple = ValueAnimator.ofFloat(0, 0);
        animatorRipple.setRepeatCount(ValueAnimator.INFINITE);
        animatorRipple.setRepeatMode(ValueAnimator.RESTART);
        animatorRipple.setDuration(5000);
        animatorRipple.setEvaluator(new FloatEvaluator());
        animatorRipple.setInterpolator(new LinearInterpolator());
        animatorRipple.start();

        animatorRippleTwo = ValueAnimator.ofFloat(0, 0);
        animatorRippleTwo.setRepeatCount(ValueAnimator.INFINITE);
        animatorRippleTwo.setRepeatMode(ValueAnimator.RESTART);
        animatorRippleTwo.setDuration(5000);
        animatorRippleTwo.setEvaluator(new FloatEvaluator());
        animatorRippleTwo.setInterpolator(new LinearInterpolator());
        animatorRippleTwo.start();
    }


    private void initLayers() {
        initMarkerLayer();
        initCircleLayer();
    }


    private void initMarkerLayer() {
        markerLayer = NeshanServices.createVectorElementLayer();
        map.getLayers().add(markerLayer);
        marker = new Marker(new LngLat(-144.037474, -59.658040), getMarkerStyle());
        markerLayer.add(marker);
    }

    private void initCircleLayer() {
        circleLayer = NeshanServices.createVectorElementLayer();
        map.getLayers().add(circleLayer);
        lngLatVector = new LngLatVector();
        PolygonGeom polygonGeom = new PolygonGeom(lngLatVector);
        polygon = new Polygon(polygonGeom, getPolygonStyle());
        circleLayer.add(polygon);
    }


    private LngLat getDestinationPoint(double bearing, double d) {
        LngLat srcLngLat = new LngLat(mLocation.getLongitude(), mLocation.getLatitude());
        double srcLat = srcLngLat.getY() * PI_RAD;
        double srcLng = srcLngLat.getX() * PI_RAD;
        bearing = bearing * PI_RAD;
        d = d / 1000.0;
        double destLat = Math.asin(Math.sin(srcLat) * Math.cos(d / R) +
                Math.cos(srcLat) * Math.sin(d / R) * Math.cos(bearing));
        double destLng = srcLng + Math.atan2(Math.sin(bearing) * Math.sin(d / R) * Math.cos(srcLat),
                Math.cos(d / R) - Math.sin(srcLat) * Math.sin(destLat));
        LngLat destLngLat = new LngLat(destLng * PI_DEG, destLat * PI_DEG);
        return destLngLat;
    }



    private PolygonStyle getPolygonStyle() {
        PolygonStyleCreator styleCreator = new PolygonStyleCreator();
        styleCreator.setColor(circleFillColor);
        styleCreator.setLineStyle(getLineStyle());
        return styleCreator.buildStyle();
    }

    private PolygonStyle getPolygonRippleStyle() {
        PolygonStyleCreator styleCreator = new PolygonStyleCreator();
        styleCreator.setColor(circlerippleFillColor);
        styleCreator.setLineStyle(getLineStyle());
        return styleCreator.buildStyle();
    }
    private PolygonStyle getPolygonRippleStyleTwo() {
        PolygonStyleCreator styleCreator = new PolygonStyleCreator();
        styleCreator.setColor(circlerippleFillColorTwo);
        styleCreator.setLineStyle(getLineStyle());
        return styleCreator.buildStyle();
    }


    private LineStyle getLineStyle() {
        LineStyleCreator styleCreator = new LineStyleCreator();
        styleCreator.setColor(circleStrokeColor);
        styleCreator.setWidth(lineWidth);
        return styleCreator.buildStyle();
    }


    private MarkerStyle getMarkerStyle() {
        MarkerStyleCreator styleCreator = new MarkerStyleCreator();
        styleCreator.setOrientationMode(markerMode);
        styleCreator.setSize(markerSize);
        styleCreator.setAnchorPoint(0f, 0f);
        styleCreator.setBitmap(
                BitmapUtils.createBitmapFromAndroidBitmap(
                        BitmapFactory.decodeResource(context.getResources(), markerIcon)));
        return styleCreator.buildStyle();
    }


    protected void updateUI(Location location) {
        mLocation = location;
        LngLat lngLat = new LngLat(mLocation.getLongitude(), mLocation.getLatitude());
        lngLatVector.clear();
        for (int bearing = 0; bearing <= 360; bearing = bearing + 2) {
            lngLatVector.add(getDestinationPoint(bearing, mLocation.getAccuracy()));
        }
        marker.setPos(lngLat);
        polygon.setPoses(lngLatVector);

        if (rippleEnable) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doRipple();
                    doRippleTwo();
                }
            } , 2000);
        }


        if (eventListener != null) {
            eventListener.onLocationReceived(location);
        }


    }

    private void doRipple() {
        animatorRipple.cancel();
        animatorRipple = ValueAnimator.ofFloat(0, mLocation.getAccuracy());
        animatorRipple.setRepeatCount(ValueAnimator.INFINITE);
        animatorRipple.setRepeatMode(ValueAnimator.RESTART);
        animatorRipple.setDuration(8000);
        animatorRipple.setEvaluator(new FloatEvaluator());
        animatorRipple.setInterpolator(new LinearInterpolator());
        animatorRipple.start();
        animatorRipple.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                lngLatVectorRipple.clear();
                for (int bearing = 0; bearing <= 360; bearing = bearing + 15) {
                    lngLatVectorRipple.add(getDestinationPoint(bearing, value));
                }
                polygonRipple.setPoses(lngLatVectorRipple);
            }

        });
    }

    private void doRippleTwo() {
        animatorRippleTwo.cancel();
        animatorRippleTwo = ValueAnimator.ofFloat(0, (mLocation.getAccuracy()));
        animatorRippleTwo.setRepeatCount(ValueAnimator.INFINITE);
        animatorRippleTwo.setRepeatMode(ValueAnimator.RESTART);
        animatorRippleTwo.setDuration(6000);
        animatorRippleTwo.setEvaluator(new FloatEvaluator());
        animatorRippleTwo.setInterpolator(new LinearInterpolator());
        animatorRippleTwo.start();
        animatorRippleTwo.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                lngLatVectorRippleTwo.clear();
                for (int bearing = 0; bearing <= 360; bearing = bearing + 15) {
                    lngLatVectorRippleTwo.add(getDestinationPoint(bearing, value));
                }
                polygonRippleTwo.setPoses(lngLatVectorRippleTwo);
            }

        });
    }

    protected void setMarkerIcon(int markerIcon) {
        this.markerIcon = markerIcon;
        marker.setStyle(getMarkerStyle());
    }

    protected void setMarkerSize(float markerSize) {
        this.markerSize = markerSize;
        marker.setStyle(getMarkerStyle());
    }

    protected void setCircleFillColor(int color) {
        String hexColor = Integer.toHexString(color);
        short a = (short) Integer.parseInt(hexColor.substring(0, 2), 16);
        short r = (short) Integer.parseInt(hexColor.substring(2, 4), 16);
        short g = (short) Integer.parseInt(hexColor.substring(4, 6), 16);
        short b = (short) Integer.parseInt(hexColor.substring(6, 8), 16);
        circleFillColor = new ARGB(r, g, b, a);
        polygon.setStyle(getPolygonStyle());
    }

    protected void setCircleStrokeColor(int color) {
        String hexColor = Integer.toHexString(color);
        short a = (short) Integer.parseInt(hexColor.substring(0, 2), 16);
        short r = (short) Integer.parseInt(hexColor.substring(2, 4), 16);
        short g = (short) Integer.parseInt(hexColor.substring(4, 6), 16);
        short b = (short) Integer.parseInt(hexColor.substring(6, 8), 16);
        circleStrokeColor = new ARGB(r, g, b, a);
        polygon.setStyle(getPolygonStyle());
    }
    protected void setRippleOneFillColor(int color) {
        String hexColor = Integer.toHexString(color);
        short a = (short) Integer.parseInt(hexColor.substring(0, 2), 16);
        short r = (short) Integer.parseInt(hexColor.substring(2, 4), 16);
        short g = (short) Integer.parseInt(hexColor.substring(4, 6), 16);
        short b = (short) Integer.parseInt(hexColor.substring(6, 8), 16);
        circlerippleFillColor = new ARGB(r, g, b, a);
        polygonRipple.setStyle(getPolygonRippleStyle());
    }
    protected void setRippleTwoFillColor(int color) {
        String hexColor = Integer.toHexString(color);
        short a = (short) Integer.parseInt(hexColor.substring(0, 2), 16);
        short r = (short) Integer.parseInt(hexColor.substring(2, 4), 16);
        short g = (short) Integer.parseInt(hexColor.substring(4, 6), 16);
        short b = (short) Integer.parseInt(hexColor.substring(6, 8), 16);
        circlerippleFillColorTwo = new ARGB(r, g, b, a);
        polygonRippleTwo.setStyle(getPolygonRippleStyleTwo());
    }

    protected void setCircleOpacity(float opacity) {
        this.circleOpacity = opacity;
        circleLayer.setOpacity(opacity);
    }

    protected void setCircleVisible(boolean visible) {
        polygon.setVisible(visible);
    }

    protected void setCircleStrokeWidth(float width) {
        this.lineWidth = width;
        polygon.setStyle(getPolygonStyle());
    }

    protected void setMarkerMode(MarkerOrientation markerMode) {
        this.markerMode = markerMode;
        marker.setStyle(getMarkerStyle());
    }

    protected void setRippleEnable(boolean enable) {
        rippleEnable = enable;
        if (enable == true){
            lngLatVectorRipple = new LngLatVector();
            lngLatVectorRippleTwo = new LngLatVector();
            PolygonGeom polygonGeom = new PolygonGeom(lngLatVectorRipple);
            PolygonGeom polygonGeomTwo = new PolygonGeom(lngLatVectorRippleTwo);
            polygonRipple = new Polygon(polygonGeom, getPolygonRippleStyle());
            polygonRippleTwo = new Polygon(polygonGeomTwo, getPolygonRippleStyleTwo());
            circleLayer.add(polygonRipple);
            circleLayer.add(polygonRippleTwo);
        }
    }

    protected int getMarkerIcon() {
        return markerIcon;
    }

    protected float getMarkerSize() {
        return markerSize;
    }

    protected String getCircleFillColor() {
        String a = Integer.toHexString(circleFillColor.getA());
        String r = Integer.toHexString(circleFillColor.getR());
        String g = Integer.toHexString(circleFillColor.getG());
        String b = Integer.toHexString(circleFillColor.getB());
        return "#" + ((a.length() == 1) ? 0 + a : a) +
                ((r.length() == 1) ? 0 + r : r) +
                ((g.length() == 1) ? 0 + g : g) +
                ((b.length() == 1) ? 0 + b : b);
    }

    protected String getCircleStrokeColor() {
        String a = Integer.toHexString(circleStrokeColor.getA());
        String r = Integer.toHexString(circleStrokeColor.getR());
        String g = Integer.toHexString(circleStrokeColor.getG());
        String b = Integer.toHexString(circleStrokeColor.getB());
        return "#" + ((a.length() == 1) ? 0 + a : a) +
                ((r.length() == 1) ? 0 + r : r) +
                ((g.length() == 1) ? 0 + g : g) +
                ((b.length() == 1) ? 0 + b : b);
    }

    protected float getCircleOpacity() {
        return circleOpacity;
    }

    protected boolean isCircleVisible() {
        return polygon.isVisible();
    }

    protected float getCircleStrokeWidth() {
        return lineWidth;
    }

    protected MarkerOrientation getMarkerMode() {
        return markerMode;
    }

    protected boolean isRippleEnable(){
        return rippleEnable;
    }




}

