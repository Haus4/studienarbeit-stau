package com.momo5502.stauanalyse.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class Speaker implements OnInitListener {

    private TextToSpeech textToSpeech;
    private boolean initialized = false;
    private Queue<String> messageQueue = new LinkedList<>();


    public Speaker(Context context) {
        textToSpeech = new TextToSpeech(context, this);
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
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
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
}
