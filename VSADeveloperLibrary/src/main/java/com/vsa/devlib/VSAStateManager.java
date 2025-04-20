package com.vsa.devlib;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * VSAStateManager is responsible for:
 * 1. Establishing a socket connection to the verification server.
 * 2. Listening to server messages to toggle agent control and action verification flags.
 * 3. Sending state definitions and state updates to the server for verification.
 */
public class VSAStateManager {
    private static final String TAG = "VSAStateManager";

    private final Context context;

    // Socket-related members for communication with the verification server
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private BufferedReader reader;

    // Flags indicating whether the agent is in control (agent_control)
    // and whether the current action has been verified (verified_action)
    private final AtomicBoolean agent_control = new AtomicBoolean(false);
    private final AtomicBoolean verified_action = new AtomicBoolean(false);

    /**
     * Constructor that stores the application context.
     *
     * @param context The Android context of the host application.
     */
    public VSAStateManager(Context context) {
        this.context = context;
    }

    /**
     * Establishes a socket connection with the verification server.
     *
     * @param host The server's IP or hostname.
     * @param port The server's port.
     * @throws IOException if the connection fails.
     */
    public void connect(String host, int port) throws IOException {
        try {
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(in));

            // Launches a background thread to listen for messages from the server
            listenServerMessages();

            Log.i(TAG, "Connected to server: " + host + ":" + port);
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to verification server", e);
            throw e;
        }
    }

    /**
     * Starts a thread to continuously listen to server messages.
     * Messages typically include "Agent_Control" and "Action_Verified" updates.
     */
    private void listenServerMessages() {
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d(TAG, "Server -> " + line);
                    try {
                        JSONObject json = new JSONObject(line.trim());
                        if (json.has("Agent_Control")) {
                            boolean control = json.getBoolean("Agent_Control");
                            agent_control.set(control);
                            Log.d(TAG, "Agent_Control updated: " + control);
                        } else if (json.has("Action_Verified")) {
                            boolean verified = json.getBoolean("Action_Verified");
                            verified_action.set(verified);
                            Log.d(TAG, "Action_Verified updated: " + verified);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing server message", e);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Server connection closed or encountered error", e);
            }
        }).start();
    }

    /**
     * Closes the socket and any associated streams to release resources.
     */
    public void close() {
        try {
            if (reader != null) reader.close();
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            Log.i(TAG, "Socket connection closed.");
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket/streams", e);
        }
    }

    /**
     * Checks if the agent is currently controlling the UI.
     *
     * @return true if the agent is in control, false otherwise.
     */
    public boolean isAgentControlActive() {
        return agent_control.get();
    }

    /**
     * Checks if the current action has been verified by the server.
     *
     * @return true if the action is verified, false otherwise.
     */
    public boolean isVerifiedActionActive() {
        return verified_action.get();
    }

    /**
     * Sends a JSON object to the server over the established socket connection.
     *
     * @param json The JSON object to send.
     */
    private void sendJson(JSONObject json) {
        if (socket == null || socket.isClosed()) {
            Log.w(TAG, "Socket is not connected. Cannot send data.");
            return;
        }
        try {
            String msg = json.toString() + "\n";
            out.write(msg.getBytes());
            out.flush();
            Log.d(TAG, "Sent JSON -> " + msg);
        } catch (IOException e) {
            Log.e(TAG, "Failed to send JSON", e);
        }
    }

    /**
     * Defines a new state (predicate) locally and sends the definition to the verification server.
     *
     * Example JSON payload:
     * [{
     *   "name": "RestaurantInfo",
     *   "description": "Information about the restaurant",
     *   "variables": [{"name": "String"}...]
     * }...]
     *
     * @param newStateDef A JSONArray describing the new state definition.
     */
    public void defineState(JSONArray newStateDef) {
        if (socket == null || socket.isClosed()) {
            Log.w(TAG, "No connection to server. Cannot send update.");
            return;
        }
        if (newStateDef == null) {
            Log.w(TAG, "newStateDef called with null object.");
            return;
        }
        try {
            JSONObject toServer = new JSONObject();
            toServer.put("type", "defineState");
            toServer.put("appName", context.getPackageName()); // The package name as the app identifier
            toServer.put("state", newStateDef);
            sendJson(toServer);
        } catch (Exception e) {
            Log.e(TAG, "Error sending defineState to server", e);
        }
    }

    /**
     * Updates the current state on the client side and sends the new state data to the server.
     * This method allows the developer to package any relevant state information in a single JSON object.
     *
     * Example JSON payload:
     * [{
     *   "stateName": "RestaurantInfo",
     *   "name": "Sushi Place",
     *   "location": "Tokyo"
     * }...]
     *
     * @param updatesArray A JSONArray containing the states name and the key-value pairs to update.
     */
    public void updateState(JSONArray updatesArray) {
        if (socket == null || socket.isClosed()) {
            Log.w(TAG, "No connection to server. Cannot send update.");
            return;
        }
        if (updatesArray == null) {
            Log.w(TAG, "updateState called with null object.");
            return;
        }

        try {
            JSONObject toServer = new JSONObject();
            toServer.put("type", "updateState");
            toServer.put("appName", context.getPackageName());
            toServer.put("updates", updatesArray);

            sendJson(toServer);
            Log.d(TAG, "updateState sent -> " + toServer.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error building updateState JSON", e);
        }
    }
}
