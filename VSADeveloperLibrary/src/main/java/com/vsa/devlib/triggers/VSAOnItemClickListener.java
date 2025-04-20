package com.vsa.devlib.triggers;
import com.vsa.devlib.VSAStateManager;

import android.view.View;
import android.widget.AdapterView;

import org.json.JSONArray;


/**
 * VSAOnItemClickListener wraps AdapterView.OnItemClickListener to intercept item click events in list-based views.
 * It updates the state for verification before calling the original listener logic.
 */
public class VSAOnItemClickListener implements AdapterView.OnItemClickListener {

    private final VSAStateManager vsaStateManager;
    private final JSONArray updates;
    private final AdapterView.OnItemClickListener originalListener;

    /**
     * @param vsaStateManager An instance of VSAStateManager for communication with the verification server.
     * @param updates A JSONArray containing the state to update.
     * @param originalListener The original item click listener.
     */
    public VSAOnItemClickListener(
            VSAStateManager vsaStateManager,
            JSONArray updates,
            AdapterView.OnItemClickListener originalListener
    ) {
        this.vsaStateManager = vsaStateManager;
        this.updates = updates;
        this.originalListener = originalListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // If the agent is not controlling the UI, execute the original logic.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                originalListener.onItemClick(parent, view, position, id);
            }
            return;
        }

        // If the action is already verified, call the original listener.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                originalListener.onItemClick(parent, view, position, id);
            }
        } else {
            // Otherwise, update state before performing the click action.
            vsaStateManager.updateState(updates);
        }
    }
}
