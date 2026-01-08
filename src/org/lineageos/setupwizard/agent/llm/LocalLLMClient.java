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
    private native boolean loadModel(String path, int type); // 0=NCNN, 1=ONNX
    private native String runInference(String prompt);
    private native String runAudioInference(byte[] audioData);
    private native String runImageInference(byte[] imageData, String prompt);
    
    public LocalLLMClient() {
        // Initialize model in background or standard location
        // Example: /data/local/tmp/model
        new Thread(() -> {
            try {
                // Try loading NCNN by default
                loadModel("/data/local/tmp/model", 0); 
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Native method not found", e);
            }
        }).start();
    }

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

    @Override
    public void processHybrid(byte[] imageData, String uiHierarchyJson, String prompt, Callback callback) {
        // ...
    }
}
