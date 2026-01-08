package org.lineageos.setupwizard.agent;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputEvent;
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
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent down = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        injectInput(down);

        MotionEvent up = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);
        injectInput(up);
    }

    public void typeText(String text) {
        // Simple implementation for typing text via key events
        // In a real agent, we might use a virtual keyboard or InputConnection
        // For now, let's just log it or simulate basic keys if needed
        Log.d(TAG, "Typing: " + text);
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
