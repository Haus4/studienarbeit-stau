package com.momo5502.stauanalyse.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.momo5502.stauanalyse.R;
import com.momo5502.stauanalyse.backend.AvailableCamerasLoader;
import com.momo5502.stauanalyse.backend.BackendConnector;
import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.camera.CameraImage;
import com.momo5502.stauanalyse.camera.CameraImages;
import com.momo5502.stauanalyse.execution.CameraImageExecuter;
import com.momo5502.stauanalyse.execution.EvaluationExecutor;
import com.momo5502.stauanalyse.execution.PositionExecuter;
import com.momo5502.stauanalyse.position.Direction;
import com.momo5502.stauanalyse.position.Position;
import com.momo5502.stauanalyse.speech.Speaker;
import com.momo5502.stauanalyse.util.Downloader;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainActivity extends FragmentActivity implements PositionExecuter.EventListener, CameraImageExecuter.EventListener, EvaluationExecutor.EventListener {

    private MapView map;
    private Speaker speaker;

    private BackendConnector backendConnector;

    private PositionExecuter positionExecuter;
    private CameraImageExecuter cameraImageExecuter;
    private EvaluationExecutor evaluationExecutor;
    private AvailableCamerasLoader availableCamerasLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();

        backendConnector = new BackendConnector("http://172.16.72.180");
        availableCamerasLoader = new AvailableCamerasLoader(backendConnector);

        positionExecuter = new PositionExecuter(this, this);
        cameraImageExecuter = new CameraImageExecuter(backendConnector, this);
        evaluationExecutor = new EvaluationExecutor(backendConnector, this);

        speaker = new Speaker(getApplicationContext());

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(9);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        availableCamerasLoader.load((cameras, error) -> {
            if(cameras != null) {
                positionExecuter.setCameraFilter(cameras);
            }
            run();
        });
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

    private void run() {
        new Thread(() -> {
            while (true) {
                cameraImageExecuter.runFrame();
                positionExecuter.runFrame();
                evaluationExecutor.runFrame();
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

    @Override
    public void onCamerasLoaded(List<Camera> cameras) {
        List<OverlayItem> markers = cameras.stream().map(c -> new OverlayItem(c.getId(), c.getTitle(), c.getLocation().getGeoPoint())).collect(Collectors.toList());

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
                }, this);
        mOverlay.setFocusItemsOnTap(true);

        map.getOverlays().add(mOverlay);
    }

    @Override
    public void onPositionChanged(Position position) {
        IMapController mapController = map.getController();
        mapController.setCenter(position.getGeoPoint());
    }

    @Override
    public void onRelevantCamerasChanged(List<Camera> cameras, Optional<Direction> direction) {
        evaluationExecutor.updateDirection(direction);
        evaluationExecutor.updateCameras(cameras);

        cameraImageExecuter.updateCameras(cameras);
    }

    @Override
    public void onImagesReceived(CameraImages images) {
        evaluationExecutor.updateImages(images);
    }

    @Override
    public void onImageEvaluation(Camera camera, EvaluatedImage evaluatedImage) {
        int cars = evaluatedImage.getObjects().size();
        speaker.speak(cars + " Autos erkannt.");

        if (cars > 40) {
            speaker.speak("Es ist Stau.");
        } else {
            speaker.speak("Es ist kein Stau.");
        }

        setOriginalImage(evaluatedImage.getImage().getBitmap());
        setAnalyzedImage(evaluatedImage.getMask().getBitmap());
    }

    @Override
    public void onDirectionChanged(Direction direction) {
        if (direction == Direction.Frankfurt) {
            speaker.speak("Du gehst richtung Frankfurt.");
        } else if (direction == Direction.Basel) {
            speaker.speak("Du gehst richtung Basel.");
        } else {
            speaker.speak("Richtung nicht feststellbar.");
        }
    }
}
