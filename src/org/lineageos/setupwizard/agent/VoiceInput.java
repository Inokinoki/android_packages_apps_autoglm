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
        startRecording(callback, null);
    }
    
    public void startRecording(final AudioDataCallback callback, final VadCallback vadCallback) {
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

    public interface VadCallback {
        void onSpeechStart();
        void onSpeechEnd();
    }

    // Simple energy-based VAD parameters
    private static final int VAD_THRESHOLD = 500; // Arbitrary threshold for 16-bit PCM
    private static final int SILENCE_DURATION_MS = 1500; // 1.5 seconds of silence to trigger end
    
    public void startRecording(final AudioDataCallback dataCallback, final VadCallback vadCallback) {
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
                boolean isSpeech = false;
                long silenceStartTime = 0;

                while (mIsRecording) {
                    int read = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    if (read > 0) {
                        dataCallback.onAudioData(buffer, read);
                        
                        // Simple VAD logic
                        boolean currentFrameHasSpeech = false;
                        for (int i = 0; i < read; i += 2) {
                            short sample = (short) ((buffer[i] & 0xFF) | (buffer[i+1] << 8));
                            if (Math.abs(sample) > VAD_THRESHOLD) {
                                currentFrameHasSpeech = true;
                                break;
                            }
                        }
                        
                        if (currentFrameHasSpeech) {
                            if (!isSpeech) {
                                isSpeech = true;
                                if (vadCallback != null) vadCallback.onSpeechStart();
                            }
                            silenceStartTime = 0;
                        } else {
                            if (isSpeech) {
                                if (silenceStartTime == 0) silenceStartTime = System.currentTimeMillis();
                                if (System.currentTimeMillis() - silenceStartTime > SILENCE_DURATION_MS) {
                                    isSpeech = false;
                                    if (vadCallback != null) vadCallback.onSpeechEnd();
                                    silenceStartTime = 0;
                                }
                            }
                        }
                    }
                }
            });
            mRecordingThread.start();
            Log.d(TAG, "Recording started with VAD");
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for recording", e);
        }
    }
}
