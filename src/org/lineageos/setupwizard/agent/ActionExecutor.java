package org.lineageos.setupwizard.agent;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.util.Log;

public class ActionExecutor {
    private static final String TAG = "ActionExecutor";
    private final InputManager mInputManager;

    public ActionExecutor(Context context) {
        mInputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
    }

    public void tap(float x, float y) {
        Log.d(TAG, "Executing Tap: " + x + ", " + y);
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent down = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        injectInput(down);

        MotionEvent up = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);
        up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        injectInput(up);
    }

    public void swipe(float x1, float y1, float x2, float y2, int durationMs) {
        Log.d(TAG, "Executing Swipe: " + x1 + "," + y1 + " -> " + x2 + "," + y2);
        long downTime = SystemClock.uptimeMillis();
        long eventTime = downTime;

        // Down
        MotionEvent down = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x1, y1, 0);
        down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        injectInput(down);

        // Move
        int steps = durationMs / 10; // 10ms per step
        if (steps < 1) steps = 1;
        float dx = (x2 - x1) / steps;
        float dy = (y2 - y1) / steps;

        for (int i = 0; i < steps; i++) {
            eventTime += 10;
            float x = x1 + dx * i;
            float y = y1 + dy * i;
            MotionEvent move = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
            move.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            injectInput(move);
        }

        // Up
        eventTime += 10;
        MotionEvent up = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x2, y2, 0);
        up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        injectInput(up);
    }

    public void typeText(String text) {
        Log.d(TAG, "Typing: " + text);
        KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        KeyEvent[] events = kcm.getEvents(text.toCharArray());

        if (events != null) {
            for (KeyEvent event : events) {
                injectInput(event);
            }
        }
    }

    public void pressHome() {
        injectKey(KeyEvent.KEYCODE_HOME);
    }

    public void pressBack() {
        injectKey(KeyEvent.KEYCODE_BACK);
    }
    
    public void pressEnter() {
        injectKey(KeyEvent.KEYCODE_ENTER);
    }

    private void injectKey(int keyCode) {
        long now = SystemClock.uptimeMillis();
        injectInput(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
        injectInput(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
    }

    private boolean injectInput(InputEvent event) {
        try {
            // mode 0: INJECT_INPUT_EVENT_MODE_ASYNC
            return mInputManager.injectInputEvent(event, 0);
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject event", e);
            return false;
        }
    }
}
