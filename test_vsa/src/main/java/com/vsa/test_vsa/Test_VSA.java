package com.vsa.test_vsa;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.vsa.devlib.VSAStateManager;
import com.vsa.devlib.triggers.VSAOnClickListener;

import org.json.JSONArray;

public class Test_VSA extends AppCompatActivity {

    private static final String TAG = "Test_VSA";

    private EditText etHost, etPort;
    private Button btnConnect, btnDefinePredicate, btnTestTrigger;
    private TextView tvLog;

    /**
     * VSAStateManager:
     *  - Core class that manages the connection to the VSA verification server
     *  - Allows defining new states (predicates) and updating their variables
     *  - Interacts with the server to check if an action is "verified" before it proceeds
     */
    private VSAStateManager vsaStateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_vsa);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // View initialization (non-VSA part)
        etHost = findViewById(R.id.etHost);
        etPort = findViewById(R.id.etPort);
        btnConnect = findViewById(R.id.btnConnect);
        btnDefinePredicate = findViewById(R.id.btnDefinePredicate);
        btnTestTrigger = findViewById(R.id.btnTestTrigger);
        tvLog = findViewById(R.id.tvLog);

        /**
         * Instantiate the VSAStateManager:
         *  - Manages all communication and state updates for verification
         */
        vsaStateManager = new VSAStateManager(this);

        // Connect to the server (non-VSA logic mostly)
        btnConnect.setOnClickListener(v -> connectToServer());

        // Define the predicate on the server
        btnDefinePredicate.setOnClickListener(v -> definePredicate());

        /**
         * Test trigger button:
         *  - Wraps the click listener using VSAOnClickListener
         *  - Before executing the original onClick, it sends an "updateState" to the VSA server
         *    if the action hasn't been verified yet.
         *  - If the action is "verified," the original onClick is executed immediately.
         */
        btnTestTrigger.setOnClickListener(
                new VSAOnClickListener(
                        vsaStateManager,       // Manager that handles VSA updates & verification
                        buildUpdateStates(),   // JSON specifying how to update the "RestaurantInfo" state
                        v -> appendLog("Original onClick executed (action verified)!")
                )
        );
    }

    private void connectToServer() {
        String host = etHost.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();

        if (host.isEmpty() || portStr.isEmpty()) {
            appendLog("Host/Port is empty!");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            appendLog("Port must be a number!");
            return;
        }

        new Thread(() -> {
            try {
                /**
                 * Connect to the VSA verification server:
                 *  - Opens a socket to the server
                 *  - Starts listening for verification commands (e.g. "Action_Verified")
                 */
                vsaStateManager.connect(host, port);
                runOnUiThread(() -> appendLog("Connected to server: " + host + ":" + port));
            } catch (Exception e) {
                Log.e(TAG, "Failed to connect server", e);
                runOnUiThread(() -> appendLog("Connection failed: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * definePredicate():
     *  - Demonstrates how a developer would define a new VSA state/predicate
     *  - The JSON array here includes the state name ("RestaurantInfo"), a description,
     *    and the variables relevant to that state
     *  - Once defined, the server recognizes it as a potential predicate for verification
     */
    private void definePredicate() {
        try {
            // Example array with a single JSON object representing the new state
            JSONArray defineArray = new JSONArray(
                    "[{\"name\":\"RestaurantInfo\"," +
                            "\"description\":\"Information about the restaurant\"," +
                            "\"variables\":[{\"name\":\"String\"},{\"location\":\"String\"}]}]"
            );
            // Call VSAStateManager's defineState to communicate with the verification server
            vsaStateManager.defineState(defineArray);
            appendLog("defineState sent: " + defineArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Error defining predicate: " + e.getMessage());
        }
    }

    /**
     * buildUpdateStates():
     *  - Prepares a JSON array specifying how to update the "RestaurantInfo" state
     *  - This JSON is sent to the server BEFORE an action executes (via triggers like VSAOnClickListener)
     *  - The server can then decide if the action is permissible based on user instructions
     */
    private JSONArray buildUpdateStates() {
        JSONArray updates = new JSONArray();
        try {
            // This JSON says: "Update stateName=RestaurantInfo with name=Sushi Place, location=Tokyo."
            updates = new JSONArray(
                    "[{\"stateName\":\"RestaurantInfo\",\"name\":\"Sushi Place\",\"location\":\"Tokyo\"}]"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updates;
    }

    private void appendLog(String msg) {
        String current = tvLog.getText().toString();
        tvLog.setText(String.format("%s\n%s", current, msg));
    }

    /**
     * onDestroy():
     *  - Closes the socket and any resources in the VSAStateManager
     *  - Ensures no further communication attempts occur after the Activity is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
}
