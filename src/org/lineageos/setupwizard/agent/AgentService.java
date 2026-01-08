package org.lineageos.setupwizard.agent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.lineageos.setupwizard.agent.llm.LLMClient;
import org.lineageos.setupwizard.agent.llm.LocalLLMClient;
import org.lineageos.setupwizard.agent.llm.RemoteLLMClient;

public class AgentService extends Service {
    private static final String TAG = "AgentService";

    private VoiceInput mVoiceInput;
    private ScreenInput mScreenInput;
    private ActionExecutor mActionExecutor;
    private LLMClient mLLMClient;

    private boolean mUseLocalLLM = true; // Toggle based on config

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AgentService created");

        mVoiceInput = new VoiceInput();
        mScreenInput = new ScreenInput();
        mActionExecutor = new ActionExecutor(this);

        if (mUseLocalLLM) {
            mLLMClient = new LocalLLMClient();
        } else {
            mLLMClient = new RemoteLLMClient("https://api.example.com", "key");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AgentService started");
        
        // Example: Start listening
        startListening();

        return START_STICKY;
    }

    private void startListening() {
        mVoiceInput.startRecording((data, length) -> {
            // Process audio data
            // In a real app, detect wake word or stream to LLM
            // mLLMClient.processAudio(data, ...);
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
    }
}
