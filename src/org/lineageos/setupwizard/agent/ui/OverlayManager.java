package org.lineageos.setupwizard.agent.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class OverlayManager {
    private final Context mContext;
    private final WindowManager mWindowManager;
    private TextView mStatusView;
    private final Handler mMainHandler;

    public OverlayManager(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void showStatus(String text) {
        mMainHandler.post(() -> {
            if (mStatusView == null) {
                createView();
            }
            mStatusView.setText(text);
            mStatusView.setVisibility(android.view.View.VISIBLE);
        });
    }

    public void hide() {
        mMainHandler.post(() -> {
            if (mStatusView != null) {
                mStatusView.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void createView() {
        mStatusView = new TextView(mContext);
        mStatusView.setTextSize(14f);
        mStatusView.setTextColor(Color.WHITE);
        mStatusView.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black
        mStatusView.setPadding(20, 10, 20, 10);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Requires permission
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 100; // Offset from top

        mWindowManager.addView(mStatusView, params);
    }

    public void destroy() {
        mMainHandler.post(() -> {
            if (mStatusView != null) {
                try {
                    mWindowManager.removeView(mStatusView);
                } catch (Exception e) {
                    // Ignore
                }
                mStatusView = null;
            }
        });
    }
}
