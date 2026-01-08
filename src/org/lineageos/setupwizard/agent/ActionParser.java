package org.lineageos.setupwizard.agent;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionParser {
    private static final String TAG = "ActionParser";
    private final ActionExecutor mExecutor;

    // Regex patterns for actions
    private static final Pattern TAP_PATTERN = Pattern.compile("TAP\\((\\d+),\\s*(\\d+)\\)");
    private static final Pattern SCROLL_PATTERN = Pattern.compile("SCROLL\\((\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
    private static final Pattern TYPE_PATTERN = Pattern.compile("TYPE\\((.*?)\\)");

    public ActionParser(ActionExecutor executor) {
        mExecutor = executor;
    }

    public void parseAndExecute(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            String actionString = json.optString("action", "");
            String thought = json.optString("thought", "");
            
            Log.d(TAG, "Thought: " + thought);
            Log.d(TAG, "Action: " + actionString);

            if (actionString.isEmpty()) return;

            // Handle multiple actions if separated by semicolon? 
            // For now assume single action per turn as per prompt instruction
            executeActionCommand(actionString);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse LLM response: " + jsonResponse, e);
        }
    }

    private void executeActionCommand(String command) {
        command = command.trim();

        if (command.equals("BACK")) {
            mExecutor.pressBack();
            return;
        }
        if (command.equals("HOME")) {
            mExecutor.pressHome();
            return;
        }

        Matcher tapMatcher = TAP_PATTERN.matcher(command);
        if (tapMatcher.find()) {
            float x = Float.parseFloat(tapMatcher.group(1));
            float y = Float.parseFloat(tapMatcher.group(2));
            mExecutor.tap(x, y);
            return;
        }

        Matcher scrollMatcher = SCROLL_PATTERN.matcher(command);
        if (scrollMatcher.find()) {
            float x1 = Float.parseFloat(scrollMatcher.group(1));
            float y1 = Float.parseFloat(scrollMatcher.group(2));
            float x2 = Float.parseFloat(scrollMatcher.group(3));
            float y2 = Float.parseFloat(scrollMatcher.group(4));
            // Default duration 300ms
            mExecutor.swipe(x1, y1, x2, y2, 300);
            return;
        }

        Matcher typeMatcher = TYPE_PATTERN.matcher(command);
        if (typeMatcher.find()) {
            String text = typeMatcher.group(1);
            mExecutor.typeText(text);
            return;
        }
        
        Log.w(TAG, "Unknown action command: " + command);
    }
}
