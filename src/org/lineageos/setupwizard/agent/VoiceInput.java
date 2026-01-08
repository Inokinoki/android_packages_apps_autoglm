package org.lineageos.setupwizard.agent;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class VoiceInput {
    private static final String TAG = "VoiceInput";
    private static final int SAMPLE_RATE = 16000; // 16kHz for LLM/STT usually
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord mAudioRecord;
    private boolean mIsRecording = false;
    private Thread mRecordingThread;

    public void startRecording(final AudioDataCallback callback) {
        if (mIsRecording) return;

        try {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed");
                return;
            }

            mIsRecording = true;
            mAudioRecord.startRecording();

            mRecordingThread = new Thread(() -> {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (mIsRecording) {
                    int read = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    if (read > 0) {
                        callback.onAudioData(buffer, read);
                    }
                }
            });
            mRecordingThread.start();
            Log.d(TAG, "Recording started");
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for recording", e);
        }
    }

    public void stopRecording() {
        if (!mIsRecording) return;
        mIsRecording = false;
        try {
            if (mRecordingThread != null) {
                mRecordingThread.join();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for recording thread", e);
        }
        
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        Log.d(TAG, "Recording stopped");
    }

    public interface AudioDataCallback {
        void onAudioData(byte[] data, int length);
    }
}
