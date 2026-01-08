package org.lineageos.setupwizard.agent;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class AgentAccessibilityService extends AccessibilityService {
    private static final String TAG = "AgentAccService";
    private static AgentAccessibilityService sInstance;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility Service Connected");
        sInstance = this;
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS | 
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We can monitor events here if needed
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted");
        sInstance = null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    public static AgentAccessibilityService getInstance() {
        return sInstance;
    }

    public AccessibilityNodeInfo getRootNode() {
        return getRootInActiveWindow();
    }
    
    public List<UiNode> getUiHierarchy() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return new ArrayList<>();
        
        List<UiNode> nodes = new ArrayList<>();
        traverseNode(root, nodes, 0);
        return nodes;
    }

    private void traverseNode(AccessibilityNodeInfo node, List<UiNode> list, int depth) {
        if (node == null) return;
        
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        
        // Only add interesting nodes (visible, has text, or clickable)
        if (node.isVisibleToUser() && (node.getText() != null || node.isClickable() || node.isEditable())) {
            UiNode uiNode = new UiNode();
            uiNode.text = node.getText() != null ? node.getText().toString() : "";
            uiNode.className = node.getClassName() != null ? node.getClassName().toString() : "";
            uiNode.bounds = bounds;
            uiNode.isClickable = node.isClickable();
            uiNode.contentDescription = node.getContentDescription() != null ? node.getContentDescription().toString() : "";
            list.add(uiNode);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverseNode(node.getChild(i), list, depth + 1);
        }
    }
    
    public static class UiNode {
        public String text;
        public String className;
        public String contentDescription;
        public Rect bounds;
        public boolean isClickable;
        
        @Override
        public String toString() {
            return "UiNode{" +
                    "text='" + text + '\'' +
                    ", bounds=" + bounds +
                    ", cls=" + className +
                    '}';
        }
    }
}
