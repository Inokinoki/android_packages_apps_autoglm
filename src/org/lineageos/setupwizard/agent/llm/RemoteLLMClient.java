package org.lineageos.setupwizard.agent.llm;

import android.util.Log;
import java.io.IOException;

public class RemoteLLMClient implements LLMClient {
    private static final String TAG = "RemoteLLMClient";
    private final String mEndpoint;
    private final String mApiKey;

    public RemoteLLMClient(String endpoint, String apiKey) {
        this.mEndpoint = endpoint;
        this.mApiKey = apiKey;
    }

    @Override
    public void processInput(String prompt, Callback callback) {
        // Implement HTTP request here
        // Using standard HttpURLConnection to avoid extra dependencies if possible
        // or just log for this stub
        Log.d(TAG, "Sending prompt to remote LLM: " + prompt);
        // Simulate response
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                callback.onResponse("I received your request: " + prompt);
            } catch (InterruptedException e) {
                callback.onError(e);
            }
        }).start();
    }

    @Override
    public void processAudio(byte[] audioData, Callback callback) {
         Log.d(TAG, "Sending audio to remote LLM");
         // STT + LLM logic
    }

    @Override
    public void processImage(byte[] imageData, String prompt, Callback callback) {
        Log.d(TAG, "Sending image to remote LLM");
        // VLM logic
    }

    @Override
    public void processHybrid(byte[] imageData, String uiHierarchyJson, String prompt, Callback callback) {
        Log.d(TAG, "Sending Hybrid (Image + UI Tree) to remote LLM");
        // VLM + Text logic
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                callback.onResponse("I see the screen with " + uiHierarchyJson.length() + " chars of UI tree. Executing action.");
            } catch (InterruptedException e) {
                callback.onError(e);
            }
        }).start();
    }
}
