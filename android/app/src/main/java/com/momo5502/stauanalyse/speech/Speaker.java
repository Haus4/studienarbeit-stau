package com.momo5502.stauanalyse.speech;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

import static android.content.Context.AUDIO_SERVICE;

public class Speaker implements OnInitListener {

    private TextToSpeech textToSpeech;
    private Context context;
    private boolean initialized = false;
    private volatile int id = 1;
    private Queue<String> messageQueue = new LinkedList<>();

    public Speaker(Context context) {
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

    private void setBluetoothSco(boolean value) {
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);

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
