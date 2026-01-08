package org.lineageos.setupwizard.agent.llm;

import android.util.Log;

public class LocalLLMClient implements LLMClient {
    private static final String TAG = "LocalLLMClient";

    // Load native library
    static {
        try {
            System.loadLibrary("llm_agent_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load llm_agent_jni", e);
        }
    }

    // Native methods
    private native String runInference(String prompt);
    private native String runAudioInference(byte[] audioData);
    private native String runImageInference(byte[] imageData, String prompt);

    @Override
    public void processInput(String prompt, Callback callback) {
         new Thread(() -> {
            try {
                String result = runInference(prompt);
                callback.onResponse(result);
            } catch (UnsatisfiedLinkError e) {
                 callback.onResponse("Local LLM not available (lib missing)");
            }
        }).start();
    }

    @Override
    public void processAudio(byte[] audioData, Callback callback) {
        // ...
    }

    @Override
    public void processImage(byte[] imageData, String prompt, Callback callback) {
        // ...
    }
}
