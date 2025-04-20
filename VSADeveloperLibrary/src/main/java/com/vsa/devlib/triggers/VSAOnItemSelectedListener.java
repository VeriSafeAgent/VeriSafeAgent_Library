package com.vsa.devlib.triggers;
import com.vsa.devlib.VSAStateManager;

import android.view.View;
import android.widget.AdapterView;

import org.json.JSONArray;

/**
 * VSAOnItemSelectedListener is a wrapper for AdapterView.OnItemSelectedListener.
 * It intercepts item selection events and updates the state for verification before continuing.
 */
public class VSAOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    private final VSAStateManager vsaStateManager;
    private final JSONArray updates;
    private final AdapterView.OnItemSelectedListener originalListener;

    /**
     * @param vsaStateManager An instance of VSAStateManager for managing state updates.
     * @param updates A JSONArray containing the state updates.
     * @param originalListener The original item selection listener to be invoked after verification.
     */
    public VSAOnItemSelectedListener(
            VSAStateManager vsaStateManager,
            JSONArray updates,
            AdapterView.OnItemSelectedListener originalListener
    ) {
        this.vsaStateManager = vsaStateManager;
        this.updates = updates;
        this.originalListener = originalListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // If the agent is not in control, call the original listener directly.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                originalListener.onItemSelected(parent, view, position, id);
            }
            return;
        }

        // If the action is already verified, proceed with the original logic.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                originalListener.onItemSelected(parent, view, position, id);
            }
        } else {
            // Send state updates for verification before continuing.
            vsaStateManager.updateState(updates);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Always call the original onNothingSelected if it exists;
        // no special verification logic is applied here.
        if (originalListener != null) {
            originalListener.onNothingSelected(parent);
        }
    }
}
