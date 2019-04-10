package com.momo5502.stauanalyse.jam;

import com.momo5502.stauanalyse.camera.Camera;
import com.momo5502.stauanalyse.speech.Speaker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JamAnnouncer {

    private Speaker speaker;
    private Map<Camera, JamStatus> cameraStates;
    private boolean noJamHasBeenAnnouncedLast = false;

    public JamAnnouncer(Speaker speaker) {
        this.speaker = speaker;
        cameraStates = new HashMap<>();
    }

    public synchronized void updateCameras(List<Camera> cameras) {
        List<Camera> camerasToAdd = cameras.stream() //
                .filter(c -> !cameraStates.keySet().contains(c)) //
                .collect(Collectors.toList());

        List<Camera> camerasToRemove = cameraStates.keySet() //
                .stream() //
                .filter(c -> !cameras.contains(c)) //
                .collect(Collectors.toList());

        camerasToRemove.forEach(c -> cameraStates.remove(c));
        camerasToAdd.forEach(c -> cameraStates.put(c, null));
    }

    public synchronized void addStatus(JamStatus jamStatus) {
        JamStatus lastJamStatus = null;
        boolean lastGlobalJamState = isJamAnywhere();

        if (cameraStates.containsKey(jamStatus.getCamera())) {
            lastJamStatus = cameraStates.get(jamStatus.getCamera());
        }

        cameraStates.put(jamStatus.getCamera(), jamStatus);
        triggerPossibleAnnouncement(jamStatus, lastJamStatus, lastGlobalJamState);
    }

    private void triggerPossibleAnnouncement(JamStatus jamStatus, JamStatus lastJamStatus, boolean lastGlobalJamState) {
        boolean weHaveAllCamerasChecked = cameraStates.values().stream().filter(s -> s == null).count() == 0;
        boolean currentGlobalJamState = isJamAnywhere();

        if (weHaveAllCamerasChecked && !currentGlobalJamState && lastJamStatus == null) {
            if(!noJamHasBeenAnnouncedLast) {
                noJamHasBeenAnnouncedLast = true;
                announceNoJam();
            }
        } else if (jamStatus.isJammed() && (lastJamStatus == null || !lastJamStatus.isJammed())) {
            noJamHasBeenAnnouncedLast = false;
            announceJam(jamStatus);
        }
    }

    private void announceNoJam() {
        speaker.speak("Es wurde kein Stau auf der Strecke erkannt.");
    }

    private void announceJam(JamStatus jamStatus) {
        Camera camera = jamStatus.getCamera();
        int cars = jamStatus.getDetectedCars();
        int percent = jamStatus.getJamPercent();

        speaker.speak(cars + " Autos erkannt auf " + camera.getTitle() + ". Es ist zu " + percent + "% Stau.");
    }

    private boolean isJamAnywhere() {
        return cameraStates.values().stream().filter(s -> s != null && s.isJammed()).count() > 0;
    }
}
