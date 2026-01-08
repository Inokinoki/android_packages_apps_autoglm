package org.lineageos.setupwizard.agent.llm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PromptBuilder {
    
    private static final String SYSTEM_PROMPT = 
        "You are an Android UI Agent. You can control the device based on the screen and user request.\n" +
        "Output format: JSON with 'thought' and 'action'.\n" +
        "Action types: TAP(x,y), TYPE(text), BACK, HOME, SCROLL(x1,y1,x2,y2).\n" +
        "Example: { \"thought\": \"User wants to open settings\", \"action\": \"TAP(500,1000)\" }";

    private final List<JSONObject> history = new ArrayList<>();

    public String buildPrompt(String userRequest, String uiHierarchyJson) {
        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_PROMPT).append("\n\n");
        
        // Add minimal history (last 3 turns)
        if (!history.isEmpty()) {
            sb.append("History:\n");
            int start = Math.max(0, history.size() - 3);
            for (int i = start; i < history.size(); i++) {
                sb.append(history.get(i).toString()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("Current Screen UI Tree:\n").append(uiHierarchyJson).append("\n\n");
        sb.append("User Request: ").append(userRequest).append("\n");
        
        return sb.toString();
    }
    
    public void addToHistory(String userRequest, String agentResponse) {
        try {
            JSONObject entry = new JSONObject();
            entry.put("user", userRequest);
            entry.put("agent", agentResponse);
            history.add(entry);
        } catch (JSONException e) {
            // Ignore
        }
    }
}
