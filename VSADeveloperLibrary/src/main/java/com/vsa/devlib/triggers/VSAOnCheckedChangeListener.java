package com.vsa.devlib.triggers;
import com.vsa.devlib.VSAStateManager;

import android.widget.CompoundButton;

import org.json.JSONArray;

/**
 * VSAOnCheckedChangeListener is a wrapper for CompoundButton.OnCheckedChangeListener.
 * It intercepts checked change events when the agent is in control and updates the state
 * for verification before executing the original listener's logic.
 */
public class VSAOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

    private final VSAStateManager vsaStateManager;
    private final JSONArray updates;
    private final CompoundButton.OnCheckedChangeListener originalListener;

    /**
     * @param vsaStateManager An instance of VSAStateManager to communicate state updates.
     * @param updates A JSONArray containing any state updates to send before the action.
     * @param originalListener The original listener to call if the action is verified.
     */
    public VSAOnCheckedChangeListener(
            VSAStateManager vsaStateManager,
            JSONArray updates,
            CompoundButton.OnCheckedChangeListener originalListener
    ) {
        this.vsaStateManager = vsaStateManager;
        this.updates = updates;
        this.originalListener = originalListener;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // If the agent is not in control, execute the original listener directly.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                originalListener.onCheckedChanged(buttonView, isChecked);
            }
            return;
        }

        // If the action has already been verified, proceed with the original action.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                originalListener.onCheckedChanged(buttonView, isChecked);
            }
        } else {
            // Otherwise, send state updates before performing the action.
            vsaStateManager.updateState(updates);
        }
    }
}
