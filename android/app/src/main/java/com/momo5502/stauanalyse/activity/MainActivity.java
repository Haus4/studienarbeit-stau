package com.momo5502.stauanalyse.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.momo5502.stauanalyse.GlobalPositioningManager;
import com.momo5502.stauanalyse.R;
import com.momo5502.stauanalyse.backend.MultiImageLoader;
import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.camera.CameraImage;
import com.momo5502.stauanalyse.camera.CameraImageFetcher;
import com.momo5502.stauanalyse.camera.CameraLoader;
import com.momo5502.stauanalyse.speech.Speaker;
import com.momo5502.stauanalyse.vision.EvaluatedImage;
import com.momo5502.stauanalyse.vision.ImageEvaluator;

import org.opencv.android.OpenCVLoader;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MainActivity extends FragmentActivity {

    private Speaker speaker;
    private ImageEvaluator imageEvaluator;
    private MultiImageLoader multiImageLoader;
    private GlobalPositioningManager globalPositioningManager;
    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();

        globalPositioningManager = new GlobalPositioningManager(this);

        multiImageLoader = new MultiImageLoader();
        speaker = new Speaker(getApplicationContext());
        imageEvaluator = new ImageEvaluator();

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(9);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        globalPositioningManager.setLocationCallback((location, error) -> {
            mapController.setCenter(new GeoPoint(location));
        });

        Activity context = this;

        new CameraLoader().loadCameras((cameras, e) -> {
            List<OverlayItem> markers = cameras.stream().map(c -> new OverlayItem(c.getId(), c.getTitle(), c.getLocation())).collect(Collectors.toList());

            ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(markers,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            //do something
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, context);
            mOverlay.setFocusItemsOnTap(true);

            map.getOverlays().add(mOverlay);
        });

        run();
    }

    public void setOriginalImage(final Bitmap bitmap) {
        setImage(bitmap, R.id.og);
    }

    public void setAnalyzedImage(final Bitmap bitmap) {
        setImage(bitmap, R.id.mog);
    }

    private void setImage(final Bitmap bitmap, final int id) {
        new Handler(Looper.getMainLooper()).post(() -> {
            ImageView iv = findViewById(id);
            iv.setImageBitmap(bitmap);
        });
    }

    private void handleImages(List<CameraImage> images, Exception error) {
        if (images == null || images.isEmpty()) return;

        for (int i = 0; i < images.size(); ++i) {
            CameraImage image = images.get(i);

            if (i + 1 == images.size()) {
                EvaluatedImage evaluatedImage = imageEvaluator.evaluate(image);

                int cars = evaluatedImage.getObjects().size();
                speaker.speak(cars + " Autos erkannt.");

                if (cars > 40) {
                    speaker.speak("Es ist Stau.");
                } else {
                    speaker.speak("Es ist kein Stau.");
                }

                setOriginalImage(evaluatedImage.getImage().getBitmap());
                setAnalyzedImage(evaluatedImage.getMask().getBitmap());
            } else {
                imageEvaluator.train(image);
            }
        }
    }

    private void run() {
        new Thread(() -> {
            CameraImageFetcher cameraImageFetcher = new CameraImageFetcher("K11");
            cameraImageFetcher.setCallback(((value, error) -> handleImages(value, error)));

            while (true) {
                cameraImageFetcher.work();

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
