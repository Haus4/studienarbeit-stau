package com.momo5502.stauanalyse.speech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

import com.momo5502.stauanalyse.activity.MainActivity;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import static android.content.Context.AUDIO_SERVICE;

public class Speaker implements OnInitListener {

    private TextToSpeech textToSpeech;
    private ContextWrapper context;
    private boolean initialized = false;
    private volatile int id = 1;
    private Queue<String> messageQueue = new LinkedList<>();

    public Speaker(ContextWrapper context) {
        this.context = context;
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                if (Integer.parseInt(s) == id) {
                    setBluetoothSco(false);
                }
            }

            @Override
            public void onError(String s) {
                if (Integer.parseInt(s) == id) {
                    setBluetoothSco(false);
                }
            }
        });
    }

    public void speak(String text) {
        if(context instanceof MainActivity) {
            ((MainActivity)context).getDebugView().markVoiceTriggered();
        }

        synchronized (messageQueue) {
            if (!initialized) {
                messageQueue.add(text);
            } else {
                speakInternal(text);
            }
        }
    }

    private void speakInternal(String text) {
        int messageId = ++id;
        setBluetoothSco(true);
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, messageId + "");
    }

    private boolean hasBluetoothConnection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && BluetoothProfile.STATE_CONNECTED == bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET));
    }

    private void setBluetoothSco(boolean value) {
        if (!hasBluetoothConnection()) return;

        AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(AUDIO_SERVICE);

        if (value && !audioManager.isBluetoothScoOn()) {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.startBluetoothSco();
            audioManager.setBluetoothScoOn(true);

            awaitAdaptation();
        } else if (!value && audioManager.isBluetoothScoOn()) {
            awaitAdaptation();

            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.GERMAN);

            synchronized (messageQueue) {
                initialized = true;
                while (!messageQueue.isEmpty()) {
                    speakInternal(messageQueue.remove());
                }
            }
        }
    }

    private static void awaitAdaptation() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
