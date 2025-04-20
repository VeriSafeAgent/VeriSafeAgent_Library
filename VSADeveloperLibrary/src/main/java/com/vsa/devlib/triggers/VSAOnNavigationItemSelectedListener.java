package com.vsa.devlib.triggers;
import com.vsa.devlib.VSAStateManager;

import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;

import org.json.JSONArray;

/**
 * VSAOnNavigationItemSelectedListener intercepts navigation item selections in a NavigationView.
 * It updates the state for verification before executing the original listener's logic.
 */
public class VSAOnNavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {

    private final VSAStateManager vsaStateManager;
    private final JSONArray updates;
    private final NavigationView.OnNavigationItemSelectedListener originalListener;

    /**
     * @param vsaStateManager An instance of VSAStateManager for sending state updates to the server.
     * @param updates A JSONArray containing any relevant state changes.
     * @param originalListener The original navigation item selection listener.
     */
    public VSAOnNavigationItemSelectedListener(
            VSAStateManager vsaStateManager,
            JSONArray updates,
            NavigationView.OnNavigationItemSelectedListener originalListener
    ) {
        this.vsaStateManager = vsaStateManager;
        this.updates = updates;
        this.originalListener = originalListener;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // If not under agent control, call the original listener immediately.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                return originalListener.onNavigationItemSelected(item);
            }
            return false;
        }

        // If the action is verified, run the original logic.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                return originalListener.onNavigationItemSelected(item);
            }
            return false;
        } else {
            // Otherwise, send the state updates for verification first.
            vsaStateManager.updateState(updates);
            return false;
        }
    }
}
