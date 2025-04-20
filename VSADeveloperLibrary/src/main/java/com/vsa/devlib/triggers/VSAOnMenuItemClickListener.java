package com.vsa.devlib.triggers;
import com.vsa.devlib.VSAStateManager;

import android.view.MenuItem;

import org.json.JSONArray;

/**
 * VSAOnMenuItemClickListener is a wrapper around MenuItem.OnMenuItemClickListener.
 * It intercepts menu item clicks to update the verification server state before the original action.
 */
public class VSAOnMenuItemClickListener implements MenuItem.OnMenuItemClickListener {

    private final VSAStateManager vsaStateManager;
    private final JSONArray updates;
    private final MenuItem.OnMenuItemClickListener originalListener;

    /**
     * @param vsaStateManager An instance of VSAStateManager for sending updates to the server.
     * @param updates A JSONArray containing any relevant state changes.
     * @param originalListener The original menu item click listener.
     */
    public VSAOnMenuItemClickListener(
            VSAStateManager vsaStateManager,
            JSONArray updates,
            MenuItem.OnMenuItemClickListener originalListener
    ) {
        this.vsaStateManager = vsaStateManager;
        this.updates = updates;
        this.originalListener = originalListener;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // If not under agent control, simply execute the original listener.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                return originalListener.onMenuItemClick(item);
            }
            return false;
        }

        // If the action is already verified, allow the original logic to proceed.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                return originalListener.onMenuItemClick(item);
            }
            return false;
        } else {
            // Otherwise, update the state before proceeding.
            vsaStateManager.updateState(updates);
            return false;
        }
    }
}
