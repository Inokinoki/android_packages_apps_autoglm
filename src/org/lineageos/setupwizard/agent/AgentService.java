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

import org.lineageos.setupwizard.agent.ui.OverlayManager;
import org.lineageos.setupwizard.agent.llm.PromptBuilder;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class AgentService extends Service {
    private static final String TAG = "AgentService";

    private VoiceInput mVoiceInput;
    private VoiceOutput mVoiceOutput;
    private ScreenInput mScreenInput;
    private ActionExecutor mActionExecutor;
    private ActionParser mActionParser;
    private LLMClient mLLMClient;
    private PromptBuilder mPromptBuilder;
    private OverlayManager mOverlayManager;

    private Handler mHandler;
    private HandlerThread mWorkerThread;

    private boolean mUseLocalLLM = true;
    private WakeWordDetector mWakeWordDetector;
    private boolean mIsListeningForCommand = false;
    private ByteArrayOutputStream mAudioBuffer; // Buffer for command audio

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AgentService created");

        mWorkerThread = new HandlerThread("AgentWorker");
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());

        mVoiceInput = new VoiceInput();
        mVoiceOutput = new VoiceOutput(this);
        mScreenInput = new ScreenInput();
        mActionExecutor = new ActionExecutor(this);
        mActionParser = new ActionParser(mActionExecutor);
        mWakeWordDetector = new DummyWakeWordDetector();
        mPromptBuilder = new PromptBuilder();
        mOverlayManager = new OverlayManager(this);

        if (mUseLocalLLM) {
            mLLMClient = new LocalLLMClient();
        } else {
            mLLMClient = new RemoteLLMClient("https://api.example.com", "key");
        }
    }

    private void startListening() {
        Log.d(TAG, "Starting voice listening...");
        mAudioBuffer = new ByteArrayOutputStream();
        
        mVoiceInput.startRecording(
            (data, length) -> {
                // Always buffer audio if we are listening for a command
                if (mIsListeningForCommand) {
                    mAudioBuffer.write(data, 0, length);
                } else {
                    // Otherwise, feed to Wake Word detector
                    if (mWakeWordDetector.detect(data, length)) {
                         Log.d(TAG, "Wake word detected!");
                         onWakeWordDetected();
                    }
                }
            },
            new VoiceInput.VadCallback() {
                @Override
                public void onSpeechStart() {
                    Log.d(TAG, "Speech started");
                }

                @Override
                public void onSpeechEnd() {
                    Log.d(TAG, "Speech ended");
                    if (mIsListeningForCommand) {
                        // User finished speaking the command
                        onCommandFinished();
                    }
                }
            }
        );
    }
    
    private void onWakeWordDetected() {
        mIsListeningForCommand = true;
        mAudioBuffer.reset();
        mOverlayManager.showStatus("Listening...");
        // Optional: Play chime
    }
    
    private void onCommandFinished() {
        mIsListeningForCommand = false;
        mOverlayManager.showStatus("Thinking...");
        
        // 1. Convert Audio to Text (Stub)
        byte[] commandAudio = mAudioBuffer.toByteArray();
        // mLLMClient.processAudio(commandAudio, ...); 
        // For now, assume a text prompt is generated or passed directly
        // In this demo, we skip STT and just use the simulated prompt logic for now
        // or trigger the hybrid flow.
        
        mHandler.post(this::simulateUserRequest);
    }

    private void handleLLMResponse(String response) {
        Log.d(TAG, "LLM Response: " + response);
        mOverlayManager.hide();
        
        // Parse and Execute
        mActionParser.parseAndExecute(response);
        
        // Speak response if any (simple heuristic)
        // If the LLM response contains a "thought" we might speak it? 
        // Or if it generates a specific "speak" action (not yet implemented in parser)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVoiceInput.stopRecording();
        mVoiceOutput.shutdown();
        mOverlayManager.destroy();
        mWorkerThread.quitSafely();
    }
