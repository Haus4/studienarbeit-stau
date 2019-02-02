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
import android.speech.tts.TextToSpeech;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private List<Camera> cameras;
    private LocationManager locationManager;
    private Marker self;

    private Polyline movement;

    private final int LOCATION_LIMIT = 5;
    private List<Location> firstLocations;
    private List<Location> lastLocations;

    private TextToSpeech tts;

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

        final MapsActivity copy = this;

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.GERMAN);

                if (!OpenCVLoader.initDebug()) {
                    Log.d("hi", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, copy, mLoaderCallback);
                } else {
                    Log.d("HI", "OpenCV library found inside package. Using it!");
                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }
            }
        });
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

    Mat getMatForBGS(BackgroundSubtractor bgs, String name, Mat mat) {
        Mat mask = new Mat();
        bgs.apply(mat, mask);

        //Imgproc.putText(mask, name, new Point(30, 80), Core.FONT_HERSHEY_SIMPLEX, 2.2, new Scalar(200, 200, 0), 2);

        return mask;
    }

    void doShit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (tts == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                final BackgroundSubtractorMOG mog = Bgsegm.createBackgroundSubtractorMOG();

                MirkoDownloader mirkoDownloader = new MirkoDownloader();
                mirkoDownloader.getLatest(new MirkoDownloader.Callback() {
                    @Override
                    public void onFinished(List<byte[]> images, Exception error) {
                        //speak(images.size() + " Bilder empfangen");
                        for (int i = 0; i < images.size(); ++i) {
                            parseImage(mog, images.get(i), i + 1 == images.size());
                        }
                    }
                });

                /*CameraImageLoader imageLoader = new CameraImageLoader();

                while (true) {
                    imageLoader.get("KA061", new Downloader.Callback() {
                        @Override
                        public void onFinished(byte[] result, Exception error) {
                            parseImage(mog, result, true);
                        }
                    });

                    try {
                        Thread.sleep(1000 * 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/
            }
        }).start();
    }

    private Mat cleanMask(Mat mask) {
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));

        Mat closed = new Mat();
        Imgproc.morphologyEx(mask, closed, MORPH_CLOSE, kernel);

        Mat opened = new Mat();
        Imgproc.morphologyEx(closed, opened, MORPH_OPEN, kernel);

        Mat dilated = new Mat();
        Imgproc.dilate(opened, dilated, kernel, new Point(-1, -1), 2);

        return dilated;
    }

    private void parseImage(BackgroundSubtractor mog, byte[] result, boolean display) {
        Mat mat = Imgcodecs.imdecode(new MatOfByte(result), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        Mat mogMask = getMatForBGS(mog, "mog", mat);
        mogMask = cleanMask(mogMask);

        Bitmap _mog = getBitmapForMaterial(mogMask);

        if (display) {
            analyzeCars(mogMask, mat);

            Mat ogImg = new Mat();
            Imgproc.cvtColor(mat, ogImg, Imgproc.COLOR_BGR2RGB, 3);
            Bitmap og = getBitmapForMaterial(ogImg);
            displayImage(og, _mog);
        }
    }

    List<Rect> convertContours(List<MatOfPoint> contours) {
        List<Rect> cars = new ArrayList<>();

        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            cars.add(rect);
        }

        return cars;
    }

    List<Rect> filterContours(List<Rect> contours) {
        List<Rect> cars = new ArrayList<>();

        for (Rect contour : contours) {
            if (contour.width > 3 && contour.height > 3) {
                cars.add(contour);
            }
        }

        return cars;
    }

    private List<Rect> mergeContours(List<Rect> contours) {
        List<Rect> result = new LinkedList<>();
        result.addAll(contours);

        boolean hadChange = false;
        do {
            hadChange = false;
            for (int i = 0; i < result.size() && !hadChange; ++i) {
                for (int j = 0; j < result.size() && !hadChange; ++j) {

                    if (i == j) continue;

                    Rect first = result.get(i);
                    Rect second = result.get(j);

                    if (RectangleIntersector.hasIntersection(first, second)) {
                        int x = Math.min(first.x, second.x);
                        int y = Math.min(first.y, second.y);

                        int x_max = Math.max(first.x + first.width, second.x + second.width);
                        int y_max = Math.max(first.y + first.height, second.y + second.height);

                        Rect newRect = new Rect(x, y, x_max - x, y_max - y);

                        result.remove(first);
                        result.remove(second);
                        result.add(newRect);

                        hadChange = true;
                    }
                }
            }
        } while (hadChange);

        return result;
    }

    private void analyzeCars(Mat mask, Mat image) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

        //speak(contours.size() + " Konturen erkannt.");

        List<Rect> cars = convertContours(contours);
        cars = filterContours(cars);
        cars = mergeContours(cars);

        speak(cars.size() + " Autos erkannt.");

        for (Rect car : cars) {
            Imgproc.rectangle(image, new Point(car.x, car.y), new Point(car.x + car.width, car.y + car.height), new Scalar(0, 200, 0));
        }

        if (cars.size() > 10) {
            speak("Es ist Stau.");
        } else {
            speak("Es ist kein Stau.");
        }
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

    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
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
