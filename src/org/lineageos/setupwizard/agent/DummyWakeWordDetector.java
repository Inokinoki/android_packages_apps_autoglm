package org.lineageos.setupwizard.agent;

public class DummyWakeWordDetector implements WakeWordDetector {
    @Override
    public boolean detect(byte[] audioData, int length) {
        // Real implementation would run a lightweight model (e.g. Porcupine, localized Keyword Spotting)
        // Here we just return false
        return false;
    }
}
