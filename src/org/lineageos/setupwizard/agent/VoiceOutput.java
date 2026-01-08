package org.lineageos.setupwizard.agent;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class VoiceOutput implements TextToSpeech.OnInitListener {
    private static final String TAG = "VoiceOutput";
    private TextToSpeech mTts;
    private boolean mIsInitialized = false;

    public VoiceOutput(Context context) {
        mTts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS Language not supported");
            } else {
                mIsInitialized = true;
            }
        } else {
            Log.e(TAG, "TTS Initialization failed");
        }
    }

    public void speak(String text) {
        if (mIsInitialized && text != null && !text.isEmpty()) {
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AgentVoice");
        }
    }

    public void shutdown() {
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }
}
