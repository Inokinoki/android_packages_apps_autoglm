package org.lineageos.setupwizard.agent;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import org.lineageos.setupwizard.agent.llm.LLMClient;
import org.lineageos.setupwizard.agent.llm.LocalLLMClient;
import org.lineageos.setupwizard.agent.llm.RemoteLLMClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class AgentService extends Service {
    private static final String TAG = "AgentService";

    private VoiceInput mVoiceInput;
    private ScreenInput mScreenInput;
    private ActionExecutor mActionExecutor;
    private LLMClient mLLMClient;

    private Handler mHandler;
    private HandlerThread mWorkerThread;

    private boolean mUseLocalLLM = true; // Toggle based on config
    private WakeWordDetector mWakeWordDetector;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AgentService created");

        mWorkerThread = new HandlerThread("AgentWorker");
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());

        mVoiceInput = new VoiceInput();
        mScreenInput = new ScreenInput();
        mActionExecutor = new ActionExecutor(this);
        mWakeWordDetector = new DummyWakeWordDetector();

        if (mUseLocalLLM) {
            mLLMClient = new LocalLLMClient();
        } else {
            mLLMClient = new RemoteLLMClient("https://api.example.com", "key");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AgentService started");
        
        if (intent != null && "org.lineageos.setupwizard.agent.ACTION_TRIGGER".equals(intent.getAction())) {
            Log.d(TAG, "Trigger action received via intent");
            mHandler.post(this::simulateUserRequest);
        } else {
             // Default behavior
             startListening();
        }

        return START_STICKY;
    }

    private void startListening() {
        Log.d(TAG, "Starting voice listening...");
        mVoiceInput.startRecording((data, length) -> {
            if (mWakeWordDetector.detect(data, length)) {
                 Log.d(TAG, "Wake word detected!");
                 // Stop listening for wake word, start listening for command, or just trigger action
                 mHandler.post(this::simulateUserRequest);
            }
        });
        
        // Simulation Trigger for demonstration purposes
        mHandler.postDelayed(this::simulateUserRequest, 5000);
    }
    
    private void simulateUserRequest() {
        Log.d(TAG, "Simulating user request...");
        
        // 1. Capture Screen
        Bitmap screen = mScreenInput.captureScreen();
        byte[] imageBytes = null;
        if (screen != null) {
            Log.d(TAG, "Screen captured: " + screen.getWidth() + "x" + screen.getHeight());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            screen.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imageBytes = stream.toByteArray();
        } else {
            Log.e(TAG, "Screen capture failed (null)");
        }

        // 2. Capture UI Hierarchy
        String uiJson = "[]";
        if (AgentAccessibilityService.getInstance() != null) {
            List<AgentAccessibilityService.UiNode> nodes = AgentAccessibilityService.getInstance().getUiHierarchy();
            try {
                JSONArray jsonArray = new JSONArray();
                for (AgentAccessibilityService.UiNode node : nodes) {
                    JSONObject obj = new JSONObject();
                    obj.put("text", node.text);
                    obj.put("class", node.className);
                    obj.put("desc", node.contentDescription);
                    obj.put("bounds", node.bounds.toShortString());
                    obj.put("clickable", node.isClickable);
                    jsonArray.put(obj);
                }
                uiJson = jsonArray.toString();
                Log.d(TAG, "UI Hierarchy captured: " + nodes.size() + " nodes");
            } catch (JSONException e) {
                Log.e(TAG, "Failed to serialize UI hierarchy", e);
            }
        } else {
            Log.w(TAG, "Accessibility Service not connected");
        }

        // 3. Send to LLM (Simulated Prompt: "Click the settings button")
        String prompt = "I see the screen. Please click the settings button.";
        
        if (imageBytes != null) {
             mLLMClient.processHybrid(imageBytes, uiJson, prompt, new LLMClient.Callback() {
                 @Override
                 public void onResponse(String text) {
                     handleLLMResponse(text);
                 }
                 @Override
                 public void onError(Exception e) {
                     Log.e(TAG, "LLM Error", e);
                 }
             });
        } else {
             mLLMClient.processInput(prompt, new LLMClient.Callback() {
                 @Override
                 public void onResponse(String text) {
                     handleLLMResponse(text);
                 }
                 @Override
                 public void onError(Exception e) {
                     Log.e(TAG, "LLM Error", e);
                 }
             });
        }
    }
    
    private void handleLLMResponse(String response) {
        Log.d(TAG, "LLM Response: " + response);
        // 3. Parse response and execute action
        // For demonstration, let's assume the LLM returned a coordinate or action command.
        // "ACTION:TAP:500:1000"
        
        // Hardcoded action for demo:
        mHandler.post(() -> {
            mActionExecutor.tap(500, 1000);
            mActionExecutor.typeText("Hello World");
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVoiceInput.stopRecording();
        mWorkerThread.quitSafely();
    }
}
