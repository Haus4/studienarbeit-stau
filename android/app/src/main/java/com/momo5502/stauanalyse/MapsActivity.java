package com.momo5502.stauanalyse;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.bgsegm.BackgroundSubtractorMOG;
import org.opencv.bgsegm.Bgsegm;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;

import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private List<Camera> cameras;
    private LocationManager locationManager;
    private Marker self;

    private Polyline movement;

    private final int LOCATION_LIMIT = 5;
    private List<Location> firstLocations;
    private List<Location> lastLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        /*setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }*/

        if (!OpenCVLoader.initDebug()) {
            Log.d("hi", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d("HI", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("hi", "OpenCV loaded successfully");
                    doShit();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    void displayImage(final Bitmap og, final Bitmap mog) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ImageView iv = (ImageView) findViewById(R.id.og);
                iv.setImageBitmap(og);

                iv = (ImageView) findViewById(R.id.mog);
                iv.setImageBitmap(mog);
            }
        });
    }

    Bitmap getBitmapForMaterial(Mat mat) {
        Bitmap bm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bm);

        return bm;
    }

    Bitmap getBitmapForBGS(BackgroundSubtractor bgs, String name, Mat mat) {
        Mat mask = new Mat();
        bgs.apply(mat, mask);

        Imgproc.putText(mask, name, new Point(30, 80), Core.FONT_HERSHEY_SIMPLEX, 2.2, new Scalar(200, 200, 0), 2);

        return getBitmapForMaterial(mask);
    }

    void doShit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final BackgroundSubtractorMOG mog = Bgsegm.createBackgroundSubtractorMOG();

                CameraImageLoader imageLoader = new CameraImageLoader();

                while (true) {
                    imageLoader.get("KA061", new Downloader.DownloadCallback() {
                        @Override
                        public void onFinished(byte[] result, Exception error) {
                            Mat mat = Imgcodecs.imdecode(new MatOfByte(result), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

                            Mat ogImg = new Mat();
                            Imgproc.cvtColor(mat, ogImg, Imgproc.COLOR_BGR2RGB, 3);
                            Bitmap og = getBitmapForMaterial(ogImg);

                            Bitmap _mog = getBitmapForBGS(mog, "mog", mat);

                            displayImage(og, _mog);
                        }
                    });

                    try {
                        Thread.sleep(1000 * 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 1, this);
        }

        new CameraLoader().loadCameras(new CameraLoaderListener() {
            @Override
            public void onLoad(final List<Camera> cameras, Exception e) {
                if (cameras != null) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mapCameras(cameras);
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        });
    }

    private void mapCameras(List<Camera> cameras) {
        this.cameras = cameras;
        for (Camera camera : this.cameras) {
            mMap.addMarker(new MarkerOptions().position(camera.getLocation()).title(camera.getId()));
        }
    }

    private void getRelevantCameras() {
        //lastLocations.
    }

    private void storeLocation(Location location) {
        if (firstLocations == null) firstLocations = new LinkedList<>();
        if (lastLocations == null) lastLocations = new LinkedList<>();

        if (firstLocations.size() < LOCATION_LIMIT) firstLocations.add(location);

        lastLocations.add(location);
        while (lastLocations.size() > LOCATION_LIMIT) {
            lastLocations.remove(0);
        }

        mapMovementVector();
    }

    private void mapMovementVector() {
        double latOld = 0, lonOld = 0, latNew = 0, lonNew = 0;

        for (int i = 0; i < firstLocations.size(); ++i) {
            latOld += firstLocations.get(i).getLatitude();
            lonOld += firstLocations.get(i).getLongitude();
        }

        for (int i = 0; i < lastLocations.size(); ++i) {
            latNew += lastLocations.get(i).getLatitude();
            lonNew += lastLocations.get(i).getLongitude();
        }

        latOld /= firstLocations.size();
        lonOld /= firstLocations.size();
        latNew /= lastLocations.size();
        lonNew /= lastLocations.size();

        LatLng locationOld = new LatLng(latOld, lonOld);
        LatLng locationNew = new LatLng(latNew, lonNew);

        if (movement == null)
            movement = mMap.addPolyline(new PolylineOptions().add(locationOld, locationNew));

        List<LatLng> locations = new LinkedList<>();
        locations.add(locationOld);
        locations.add(locationNew);

        movement.setPoints(locations);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        if (self == null) {
            self = mMap.addMarker(new MarkerOptions().position(loc).title("You"));
        }

        self.setPosition(loc);

        storeLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        System.out.println(s);
    }

    @Override
    public void onProviderEnabled(String s) {
        System.out.println(s);
    }

    @Override
    public void onProviderDisabled(String s) {
        System.out.println(s);
    }
}
