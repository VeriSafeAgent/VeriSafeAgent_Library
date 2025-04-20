package com.vsa.devlib.triggers;
import com.vsa.devlib.VSAStateManager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;

/**
 * VSAOnTabSelectedListener intercepts tab selection events in a TabLayout.
 * It sends state updates for verification before invoking the original tab listener methods.
 */
public class VSAOnTabSelectedListener implements TabLayout.OnTabSelectedListener {

    private final VSAStateManager vsaStateManager;
    private final JSONArray updates;
    private final TabLayout.OnTabSelectedListener originalListener;

    /**
     * @param vsaStateManager An instance of VSAStateManager to handle verification logic.
     * @param updates A JSONArray containing any relevant state updates.
     * @param originalListener The original tab selection listener.
     */
    public VSAOnTabSelectedListener(
            VSAStateManager vsaStateManager,
            JSONArray updates,
            TabLayout.OnTabSelectedListener originalListener
    ) {
        this.vsaStateManager = vsaStateManager;
        this.updates = updates;
        this.originalListener = originalListener;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // If the agent is not in control, delegate directly to the original listener.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                originalListener.onTabSelected(tab);
            }
            return;
        }

        // If the action is already verified, proceed with the original logic.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                originalListener.onTabSelected(tab);
            }
        } else {
            // Otherwise, update state before performing the action.
            vsaStateManager.updateState(updates);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // Agent control is not relevant for tab unselection; call the original listener if present.
        if (originalListener != null) {
            originalListener.onTabUnselected(tab);
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // If the agent is not in control, delegate directly to the original listener.
        if (!vsaStateManager.isAgentControlActive()) {
            if (originalListener != null) {
                originalListener.onTabReselected(tab);
            }
            return;
        }

        // If the action is verified, call the original logic.
        if (vsaStateManager.isVerifiedActionActive()) {
            if (originalListener != null) {
                originalListener.onTabReselected(tab);
            }
        } else {
            // Otherwise, update state for verification.
            vsaStateManager.updateState(updates);
        }
    }
}
