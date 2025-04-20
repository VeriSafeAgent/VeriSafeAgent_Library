package com.vsa.devlib.triggers;
import com.vsa.devlib.VSAStateManager;

import android.view.View;
import org.json.JSONArray;

/**
 * VSAOnClickListener intercepts view click events.
 * It sends state updates to the server for verification before invoking the original listener's onClick method.
 */
public class VSAOnClickListener implements View.OnClickListener {

    private final VSAStateManager vsaStateManager;
    private final JSONArray updates;
    private final View.OnClickListener originalListener;

    /**
     * @param vsaStateManager An instance of VSAStateManager for state updates.
     * @param updates A JSONArray containing the state variables to update before verification.
     * @param originalListener The original click listener.
     */
    public VSAOnClickListener(
            VSAStateManager vsaStateManager,
            JSONArray updates,
            View.OnClickListener originalListener
    ) {
        this.vsaStateManager = vsaStateManager;
        this.updates = updates;
        this.originalListener = originalListener;
    }

    @Override
    public void onClick(View v) {
        // If the agent is not controlling the UI, just call the original listener.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                originalListener.onClick(v);
            }
            return;
        }

        // If the action is already verified, proceed with the original logic.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                originalListener.onClick(v);
            }
        } else {
            // Otherwise, send state updates to the server before performing the action.
            vsaStateManager.updateState(updates);
        }
    }
}
