package com.momo5502.stauanalyse.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.momo5502.stauanalyse.R;
import com.momo5502.stauanalyse.backend.AvailableCamerasLoader;
import com.momo5502.stauanalyse.backend.BackendConnector;
import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.camera.CameraImages;
import com.momo5502.stauanalyse.execution.CameraImageExecuter;
import com.momo5502.stauanalyse.execution.EvaluationExecutor;
import com.momo5502.stauanalyse.execution.PositionExecuter;
import com.momo5502.stauanalyse.position.Direction;
import com.momo5502.stauanalyse.position.Position;
import com.momo5502.stauanalyse.speech.Speaker;
import com.momo5502.stauanalyse.vision.EvaluatedImage;

import org.opencv.android.OpenCVLoader;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements PositionExecuter.EventListener, CameraImageExecuter.EventListener, EvaluationExecutor.EventListener {

    private MapView map;
    private Speaker speaker;

    private BackendConnector backendConnector;

    private PositionExecuter positionExecuter;
    private CameraImageExecuter cameraImageExecuter;
    private EvaluationExecutor evaluationExecutor;
    private AvailableCamerasLoader availableCamerasLoader;

    private Map<Camera, CameraStatus> cameraStatusMap = new HashMap<>();
    private LinearLayout statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();

        setupTabs();

        speaker = new Speaker(getApplicationContext());

        delayUntilLoaded();
    }

    private void delayUntilLoaded() {
        new Thread(() -> {
            while (map == null || statusView == null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (map != null && statusView != null) return;

                    map = findViewById(R.id.map);
                    statusView = findViewById(R.id.statusView);

                    if (map != null && statusView != null) {
                        setupApplication();
                    }
                });


                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setupApplication() {
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setBuiltInZoomControls(true);
            map.setMultiTouchControls(true);

            IMapController mapController = map.getController();
            mapController.setZoom(9);
            GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
            mapController.setCenter(startPoint);
        }

        backendConnector = new BackendConnector("https://stau.bomhardt.de");
        availableCamerasLoader = new AvailableCamerasLoader(backendConnector);

        positionExecuter = new PositionExecuter(this, this);
        cameraImageExecuter = new CameraImageExecuter(backendConnector, this);
        evaluationExecutor = new EvaluationExecutor(backendConnector, this);

        availableCamerasLoader.load((cameras, error) -> {
            if (cameras != null) {
                positionExecuter.setCameraFilter(cameras);
            }
            run();
        });
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        final FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void updateStatus(final Camera camera, final EvaluatedImage evaluatedImage) {
        new Handler(Looper.getMainLooper()).post(() -> {
            synchronized (cameraStatusMap) {

                CameraStatus cameraStatus;
                if (cameraStatusMap.containsKey(camera)) {
                    cameraStatus = cameraStatusMap.get(camera);
                } else {
                    cameraStatus = new CameraStatus(this, camera);
                    cameraStatusMap.put(camera, cameraStatus);
                    if (statusView != null) {
                        statusView.addView(cameraStatus);
                    }
                }

                cameraStatus.setScene(evaluatedImage.getImage().getBitmap());
                cameraStatus.setMask(evaluatedImage.getMask().getBitmap());

                int cars = evaluatedImage.getObjects().size();
                speaker.speak(cars + " Autos erkannt auf " + camera.getId() + ".");

                if (cars > 40) {
                    speaker.speak("Es ist Stau.");
                    cameraStatus.setDescription(cars + " Autos. Stau!.");
                } else {
                    speaker.speak("Es ist kein Stau.");
                    cameraStatus.setDescription(cars + " Autos. Kein Stau.");
                }
            }
        });
    }

    public void removeOldCameras(final List<Camera> cameras) {
        new Handler(Looper.getMainLooper()).post(() -> {
            synchronized (cameraStatusMap) {
                List<Camera> camerasToRemove = cameraStatusMap.keySet() //
                        .stream() //
                        .filter(c -> !cameras.contains(c)) //
                        .collect(Collectors.toList());

                camerasToRemove.forEach(c -> {
                    CameraStatus status = cameraStatusMap.remove(c);
                    if (statusView != null) {
                        statusView.removeView(status);
                    }
                });
            }
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
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
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

        if (map != null) {
            map.getOverlays().add(mOverlay);
        }
    }

    @Override
    public void onPositionChanged(Position position) {
        if (map != null) {
            IMapController mapController = map.getController();
            mapController.setCenter(position.getGeoPoint());
        }
    }

    @Override
    public void onRelevantCamerasChanged(List<Camera> cameras, Optional<Direction> direction) {
        removeOldCameras(cameras);

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
        updateStatus(camera, evaluatedImage);
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
