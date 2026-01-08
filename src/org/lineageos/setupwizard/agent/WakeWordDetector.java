package org.lineageos.setupwizard.agent;

public interface WakeWordDetector {
    boolean detect(byte[] audioData, int length);
}
