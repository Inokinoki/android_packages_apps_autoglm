package org.lineageos.setupwizard.agent.llm;

public interface LLMClient {
    interface Callback {
        void onResponse(String text);
        void onError(Exception e);
    }

    void processInput(String prompt, Callback callback);
    void processAudio(byte[] audioData, Callback callback);
    void processImage(byte[] imageData, String prompt, Callback callback);
}
