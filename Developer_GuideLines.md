# Guidelines for Defining Predicates
This document provides guidelines on how to define predicates

## Syntax of the Domain-Specific Language

```
Specification ::= Rule*
       Rule ::= (Predicate ∧ ⋯ ∧ Predicate) → Objective
              | (Predicate ∧ ⋯ ∧ Predicate) → Done
       Predicate ::= Objective | StatePred(Constraint)
 Constraint ::= Variable Operator Constant
   Constant ::= String | Number | Boolean
              | Date | Time | Enumeration
   Operator ::= =|≠|>|≥|<|≤|∈|≠
```

## Overview
Predicates can be mapped to objects in the app and are used to represent what values each app object should have.
While the app is running, the values of each object are tracked through the developer library, continuously checking whether the values meet user requirements.

However, defining all objects as Predicates is unnecessary work. The developer's role is to understand the app's context well and selectively define and track only the important objects of the app.

For example, in a flight booking application, objects representing ticket information will be important. In a social media app, post, comment, and user objects will be important. Developers are encouraged to prioritize defining important objects in their applications to support verification of critical requirements.

## Guidelines
1. Define predicates for important objects first.
2. Do not divide a single object into multiple predicates.
3. Describe the context of the object as detailed as possible.
4. Do not define duplicated predicates for the same object.
5. Set the unique value of each object as the key (e.g. id, user name, option name, etc.)


## Examples
#### 1. NotificationSettings
```json
"NotificationSettings": {
        "description": "Status for the current notification settings, taking each setting name and setting status as arguments. Examples of setting names include \"Private messages\", \"Chat messages\", \"Mention of u/username\", and the setting status is one of All on, Inbox, or All off.",
        "variables": [
            {
                "name": "setting_name",
                "is_key": true,
                "type": "Text"
            },
            {
                "name": "setting_status",
                "type": "Enum",
                "enum_values": [
                    "All on",
                    "Inbox",
                    "All off"
                ]
            }
        ]
    }
```

#### 2. SortPostsBy
```json
"MyContentSortMetric": {
        "description": "The metric by which the contents I've uploaded list is currently sorted. This includes options like Views, Upvotes, Comments, Shares (external), and Date posted. The selected metric is indicated by a checkmark. Bookmark is not relevant to this predicate.",
        "variables": [
            {
                "name": "selected_metric",
                "type": "Enum",
                "enum_values": [
                "Views",
                "Upvotes",
                "Comments",
                "Shares (external)",
                "Date posted"
                ]
            }
        ]
    },
```


# Guidelines for Predicate Update
Below are guidelines for how to update predicates.

## Overview
Predicates are updated when the app's state changes. Here, app state means the app's window, dialog, or other UI components that are currently displayed.
These states can be mapped to the objects in the app. App developers instrument the app source code to track the state of the app and update the predicates accordingly.
Specifically, developers insert state update code into the app's event handlers. The Verifier will track these state changes and update their predicates.

However, it is not necessary to update predicates for all events. Developers should write code that performs updates at decisive moments - points where a state becomes finalized and will no longer change.
For example, when searching for airline tickets, the selected date should only be considered final when the user clicks the search button. Before clicking the search button, the date can be changed at any time, so it's not yet in a finalized state.

## Guidelines
1. Update the state at the event where the app state is finalized.
2. If there is no decisive moment, update the state at the event where the state is changed.

## Code Level Implementation

This section provides practical implementation details based on the test app, showing how to integrate VeriSafeAgent into your Android application.

### 1. Setting Up VSAStateManager

The `VSAStateManager` is the core class that manages the connection to the VSA verification server. Here's how to set it up:

```java
// Initialize VSAStateManager
VSAStateManager vsaStateManager = new VSAStateManager();

// Connect to the server
// ⚠️ IMPORTANT: The server address MUST match exactly with the verification server address
// Using an incorrect address will cause verification failures
vsaStateManager.connect("localhost", 8080);


### 2. Defining Predicates

Predicates are defined using JSON arrays. Each predicate has a name, description, and variables:

```java
private void definePredicate() {
    try {
        // Define a predicate with name, description, and variables
        JSONArray defineArray = new JSONArray(
            "[{\"name\":\"RestaurantInfo\"," +
            "\"description\":\"Information about the restaurant\"," +
            "\"variables\":[{\"name\":\"String\"},{\"location\":\"String\"}]}]"
        );
        
        // Send the definition to the server
        vsaStateManager.defineState(defineArray);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### 3. Updating State

Before performing an action, you need to update the state with current values:

```java
private JSONArray buildUpdateStates() {
    JSONArray updates = new JSONArray();
    try {
        // Update state variables with current values
        updates = new JSONArray(
            "[{\"stateName\":\"RestaurantInfo\"," +
            "\"name\":\"Sushi Place\"," +
            "\"location\":\"Tokyo\"}]"
        );
    } catch (Exception e) {
        e.printStackTrace();
    }
    return updates;
}
```

### 4. Using VSA Triggers

VSA provides various triggers that intercept UI events and verify actions before they proceed. Here's how to use them:

```java
// Example: Using VSAOnClickListener
Button myButton = findViewById(R.id.myButton);
myButton.setOnClickListener(new VSAOnClickListener(
    vsaStateManager,
    buildUpdateStates(),
    new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Your original click handling code
        }
    }
));
```

### 4. Using VSA Triggers (Simplified Approach)

VSA provides various triggers that intercept UI events and verify actions before they proceed. The simplest approach is to directly wrap your existing click listeners with VSA triggers:

```java
// Example: Using VSAOnClickListener with direct JSON array
Button myButton = findViewById(R.id.myButton);
myButton.setOnClickListener(new VSAOnClickListener(
    vsaStateManager,
    new JSONArray("[{\"stateName\":\"RestaurantInfo\"," +
                  "\"name\":\"Sushi Place\"," +
                  "\"location\":\"Tokyo\"}]"),
    new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Your original click handling code
        }
    }
));
```

This approach is much simpler as it eliminates the need for separate methods to build state updates. You can directly include the JSON array in the VSAOnClickListener constructor.

**Important Note:** The VSAOnClickListener will only execute your original click handler if the verification passes. If verification fails, the agent will prevent the click action and send the predicate values to the server for analysis.

### 5. Complete Implementation Example

Here's a complete example showing how to integrate VeriSafeAgent into an Android activity:

```java
public class MyActivity extends AppCompatActivity {
    private VSAStateManager vsaStateManager;
    private Button actionButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        
        // Initialize VSAStateManager
        vsaStateManager = new VSAStateManager();
        vsaStateManager.connect("localhost", 8080);
        
        // Define predicates
        definePredicates();
        
        // Set up UI with VSA triggers
        actionButton = findViewById(R.id.actionButton);
        actionButton.setOnClickListener(new VSAOnClickListener(
            vsaStateManager,
            buildUpdateStates(),
            v -> performAction()
        ));
    }
    
    private void definePredicates() {
        try {
            JSONArray defineArray = new JSONArray(
                "[{\"name\":\"MyPredicate\"," +
                "\"description\":\"My predicate description\"," +
                "\"variables\":[{\"var1\":\"String\"},{\"var2\":\"Integer\"}]}]"
            );
            vsaStateManager.defineState(defineArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private JSONArray buildUpdateStates() {
        try {
            return new JSONArray(
                "[{\"stateName\":\"MyPredicate\"," +
                "\"var1\":\"current value\"," +
                "\"var2\":42}]"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    private void performAction() {
        // Your action code here
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
}
```

This implementation guide provides a practical foundation for integrating VeriSafeAgent into your Android applications. Follow these patterns and best practices to ensure successful implementation.

## Detailed Implementation Examples

This section provides more detailed examples that incorporate the concepts from the earlier sections, making it easier for developers to understand how to implement VeriSafeAgent in real-world scenarios.

### Example 1: Social Media App - Post Management

In a social media app, posts are important objects that need to be tracked. Let's implement a feature that allows users to create, edit, and delete posts with VeriSafeAgent verification.

#### 1. Define Predicates

First, define predicates for the important objects in your app:

```java
private void definePredicates() {
    try {
        // Define predicates for posts and user authentication
        JSONArray defineArray = new JSONArray(
            "[{\"name\":\"Post\"," +
            "\"description\":\"Information about a social media post\"," +
            "\"variables\":[" +
            "{\"id\":\"String\",\"is_key\":true}," +
            "{\"content\":\"String\"}," +
            "{\"author\":\"String\"}," +
            "{\"timestamp\":\"String\"}," +
            "{\"is_edited\":\"Boolean\"}" +
            "]}]"
        );
        
        // Send the definition to the server
        vsaStateManager.defineState(defineArray);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

#### 2. Create Post Activity

Now, implement the activity for creating a new post:

```java
public class CreatePostActivity extends AppCompatActivity {
    private VSAStateManager vsaStateManager;
    private EditText contentEditText;
    private Button postButton;
    private String currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        
        // Initialize VSAStateManager
        vsaStateManager = new VSAStateManager();
        vsaStateManager.connect("localhost", 8080);
        
        // Get current user ID (in a real app, this would come from authentication)
        currentUserId = getCurrentUserId();
        
        // Find UI elements
        contentEditText = findViewById(R.id.content_edit_text);
        postButton = findViewById(R.id.post_button);
        
        // Set up post button with VSA verification
        postButton.setOnClickListener(new VSAOnClickListener(
            vsaStateManager,
            buildCreatePostUpdates(),
            v -> createPost()
        ));
    }
    
    private JSONArray buildCreatePostUpdates() {
        try {
            // Create a unique ID for the new post
            String postId = UUID.randomUUID().toString();
            
            // Get current timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
            
            // Build state updates for the new post
            return new JSONArray(
                "[{\"stateName\":\"Post\"," +
                "\"id\":\"" + postId + "\"," +
                "\"content\":\"" + contentEditText.getText().toString() + "\"," +
                "\"author\":\"" + currentUserId + "\"," +
                "\"timestamp\":\"" + timestamp + "\"," +
                "\"is_edited\":false}]"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    private void createPost() {
        // Get post content
        String content = contentEditText.getText().toString();
        
        // Validate content
        if (content.isEmpty()) {
            Toast.makeText(this, "Post content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create post in database
        // ...
        
        // Show success message
        Toast.makeText(this, "Post created successfully", Toast.LENGTH_SHORT).show();
        
        // Finish activity
        finish();
    }
    
    private String getCurrentUserId() {
        // In a real app, this would come from authentication
        return "user123";
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
}
```

#### 3. Edit Post Activity

Now, implement the activity for editing an existing post:

```java
public class EditPostActivity extends AppCompatActivity {
    private VSAStateManager vsaStateManager;
    private EditText contentEditText;
    private Button saveButton;
    private String postId;
    private String currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        
        // Initialize VSAStateManager
        vsaStateManager = new VSAStateManager();
        vsaStateManager.connect("localhost", 8080);
        
        // Get post ID from intent
        postId = getIntent().getStringExtra("post_id");
        
        // Get current user ID (in a real app, this would come from authentication)
        currentUserId = getCurrentUserId();
        
        // Find UI elements
        contentEditText = findViewById(R.id.content_edit_text);
        saveButton = findViewById(R.id.save_button);
        
        // Load post content
        loadPostContent();
        
        // Set up save button with VSA verification
        saveButton.setOnClickListener(new VSAOnClickListener(
            vsaStateManager,
            buildEditPostUpdates(),
            v -> savePost()
        ));
    }
    
    private void loadPostContent() {
        // In a real app, this would load the post from a database
        // For this example, we'll just set some dummy content
        contentEditText.setText("This is the original post content");
    }
    
    private JSONArray buildEditPostUpdates() {
        try {
            // Get current timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
            
            // Build state updates for the edited post
            return new JSONArray(
                "[{\"stateName\":\"Post\"," +
                "\"id\":\"" + postId + "\"," +
                "\"content\":\"" + contentEditText.getText().toString() + "\"," +
                "\"author\":\"" + currentUserId + "\"," +
                "\"timestamp\":\"" + timestamp + "\"," +
                "\"is_edited\":true}]"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    private void savePost() {
        // Get post content
        String content = contentEditText.getText().toString();
        
        // Validate content
        if (content.isEmpty()) {
            Toast.makeText(this, "Post content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update post in database
        // ...
        
        // Show success message
        Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show();
        
        // Finish activity
        finish();
    }
    
    private String getCurrentUserId() {
        // In a real app, this would come from authentication
        return "user123";
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
}
```

### Example 2: E-commerce App - Shopping Cart

In an e-commerce app, the shopping cart is an important object that needs to be tracked. Let's implement a feature that allows users to add items to their cart with VeriSafeAgent verification.

#### 1. Define Predicates

First, define predicates for the important objects in your app:

```java
private void definePredicates() {
    try {
        // Define predicates for products and cart items
        JSONArray defineArray = new JSONArray(
            "[{\"name\":\"Product\"," +
            "\"description\":\"Information about a product\"," +
            "\"variables\":[" +
            "{\"id\":\"String\",\"is_key\":true}," +
            "{\"name\":\"String\"}," +
            "{\"price\":\"Number\"}," +
            "{\"stock\":\"Number\"}" +
            "]}, " +
            "{\"name\":\"CartItem\"," +
            "\"description\":\"Information about an item in the shopping cart\"," +
            "\"variables\":[" +
            "{\"product_id\":\"String\",\"is_key\":true}," +
            "{\"quantity\":\"Number\"}" +
            "]}]"
        );
        
        // Send the definition to the server
        vsaStateManager.defineState(defineArray);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

#### 2. Product Detail Activity

Now, implement the activity for viewing product details and adding items to the cart:

```java
public class ProductDetailActivity extends AppCompatActivity {
    private VSAStateManager vsaStateManager;
    private TextView nameTextView;
    private TextView priceTextView;
    private TextView stockTextView;
    private EditText quantityEditText;
    private Button addToCartButton;
    private String productId;
    private String productName;
    private double productPrice;
    private int productStock;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        
        // Initialize VSAStateManager
        vsaStateManager = new VSAStateManager();
        vsaStateManager.connect("localhost", 8080);
        
        // Get product ID from intent
        productId = getIntent().getStringExtra("product_id");
        
        // Find UI elements
        nameTextView = findViewById(R.id.name_text_view);
        priceTextView = findViewById(R.id.price_text_view);
        stockTextView = findViewById(R.id.stock_text_view);
        quantityEditText = findViewById(R.id.quantity_edit_text);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        
        // Load product details
        loadProductDetails();
        
        // Set up add to cart button with VSA verification
        addToCartButton.setOnClickListener(new VSAOnClickListener(
            vsaStateManager,
            buildAddToCartUpdates(),
            v -> addToCart()
        ));
    }
    
    private void loadProductDetails() {
        // In a real app, this would load the product from a database
        // For this example, we'll just set some dummy values
        productName = "Sample Product";
        productPrice = 29.99;
        productStock = 10;
        
        // Update UI
        nameTextView.setText(productName);
        priceTextView.setText(String.format("$%.2f", productPrice));
        stockTextView.setText(String.format("Stock: %d", productStock));
    }
    
    private JSONArray buildAddToCartUpdates() {
        try {
            // Get quantity from input
            int quantity = Integer.parseInt(quantityEditText.getText().toString());
            
            // Build state updates for the product and cart item
            return new JSONArray(
                "[{\"stateName\":\"Product\"," +
                "\"id\":\"" + productId + "\"," +
                "\"name\":\"" + productName + "\"," +
                "\"price\":" + productPrice + "," +
                "\"stock\":" + (productStock - quantity) + "}, " +
                "{\"stateName\":\"CartItem\"," +
                "\"product_id\":\"" + productId + "\"," +
                "\"quantity\":" + quantity + "}]"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    private void addToCart() {
        try {
            // Get quantity from input
            int quantity = Integer.parseInt(quantityEditText.getText().toString());
            
            // Validate quantity
            if (quantity <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (quantity > productStock) {
                Toast.makeText(this, "Not enough stock available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Add item to cart in database
            // ...
            
            // Show success message
            Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show();
            
            // Finish activity
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
}
```

### Example 3: Flight Booking App - Ticket Selection

In a flight booking app, ticket information is an important object that needs to be tracked. Let's implement a feature that allows users to select flight tickets with VeriSafeAgent verification.

#### 1. Define Predicates

First, define predicates for the important objects in your app:

```java
private void definePredicates() {
    try {
        // Define predicates for flights and tickets
        JSONArray defineArray = new JSONArray(
            "[{\"name\":\"Flight\"," +
            "\"description\":\"Information about a flight\"," +
            "\"variables\":[" +
            "{\"id\":\"String\",\"is_key\":true}," +
            "{\"origin\":\"String\"}," +
            "{\"destination\":\"String\"}," +
            "{\"departure_time\":\"String\"}," +
            "{\"arrival_time\":\"String\"}," +
            "{\"price\":\"Number\"}" +
            "]}, " +
            "{\"name\":\"SelectedTicket\"," +
            "\"description\":\"Information about the selected ticket\"," +
            "\"variables\":[" +
            "{\"flight_id\":\"String\",\"is_key\":true}," +
            "{\"passenger_name\":\"String\"}," +
            "{\"passenger_email\":\"String\"}," +
            "{\"seat_number\":\"String\"}" +
            "]}]"
        );
        
        // Send the definition to the server
        vsaStateManager.defineState(defineArray);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

#### 2. Flight Selection Activity

Now, implement the activity for selecting a flight:

```java
public class FlightSelectionActivity extends AppCompatActivity {
    private VSAStateManager vsaStateManager;
    private RecyclerView flightsRecyclerView;
    private Button searchButton;
    private EditText originEditText;
    private EditText destinationEditText;
    private EditText dateEditText;
    private List<Flight> flights;
    private FlightAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_selection);
        
        // Initialize VSAStateManager
        vsaStateManager = new VSAStateManager();
        vsaStateManager.connect("localhost", 8080);
        
        // Find UI elements
        flightsRecyclerView = findViewById(R.id.flights_recycler_view);
        searchButton = findViewById(R.id.search_button);
        originEditText = findViewById(R.id.origin_edit_text);
        destinationEditText = findViewById(R.id.destination_edit_text);
        dateEditText = findViewById(R.id.date_edit_text);
        
        // Initialize flights list
        flights = new ArrayList<>();
        
        // Set up RecyclerView
        adapter = new FlightAdapter(flights, this::onFlightSelected);
        flightsRecyclerView.setAdapter(adapter);
        flightsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Set up search button
        searchButton.setOnClickListener(v -> searchFlights());
    }
    
    private void searchFlights() {
        // Get search criteria
        String origin = originEditText.getText().toString();
        String destination = destinationEditText.getText().toString();
        String date = dateEditText.getText().toString();
        
        // Validate search criteria
        if (origin.isEmpty() || destination.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please enter all search criteria", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Search flights (in a real app, this would query a database or API)
        // For this example, we'll just add some dummy flights
        flights.clear();
        flights.add(new Flight("FL001", "New York", "Los Angeles", "10:00 AM", "1:00 PM", 299.99));
        flights.add(new Flight("FL002", "New York", "Los Angeles", "2:00 PM", "5:00 PM", 349.99));
        flights.add(new Flight("FL003", "New York", "Los Angeles", "6:00 PM", "9:00 PM", 249.99));
        
        // Update RecyclerView
        adapter.notifyDataSetChanged();
    }
    
    private void onFlightSelected(Flight flight) {
        // Start ticket selection activity
        Intent intent = new Intent(this, TicketSelectionActivity.class);
        intent.putExtra("flight_id", flight.getId());
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
    
    // Flight data class
    private static class Flight {
        private String id;
        private String origin;
        private String destination;
        private String departureTime;
        private String arrivalTime;
        private double price;
        
        public Flight(String id, String origin, String destination, String departureTime, String arrivalTime, double price) {
            this.id = id;
            this.origin = origin;
            this.destination = destination;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.price = price;
        }
        
        public String getId() { return id; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getDepartureTime() { return departureTime; }
        public String getArrivalTime() { return arrivalTime; }
        public double getPrice() { return price; }
    }
    
    // Flight adapter for RecyclerView
    private static class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {
        private List<Flight> flights;
        private OnFlightSelectedListener listener;
        
        public FlightAdapter(List<Flight> flights, OnFlightSelectedListener listener) {
            this.flights = flights;
            this.listener = listener;
        }
        
        @Override
        public FlightViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flight, parent, false);
            return new FlightViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(FlightViewHolder holder, int position) {
            Flight flight = flights.get(position);
            holder.bind(flight);
        }
        
        @Override
        public int getItemCount() {
            return flights.size();
        }
        
        public class FlightViewHolder extends RecyclerView.ViewHolder {
            private TextView originTextView;
            private TextView destinationTextView;
            private TextView timeTextView;
            private TextView priceTextView;
            
            public FlightViewHolder(View itemView) {
                super(itemView);
                originTextView = itemView.findViewById(R.id.origin_text_view);
                destinationTextView = itemView.findViewById(R.id.destination_text_view);
                timeTextView = itemView.findViewById(R.id.time_text_view);
                priceTextView = itemView.findViewById(R.id.price_text_view);
                
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFlightSelected(flights.get(position));
                    }
                });
            }
            
            public void bind(Flight flight) {
                originTextView.setText(flight.getOrigin());
                destinationTextView.setText(flight.getDestination());
                timeTextView.setText(flight.getDepartureTime() + " - " + flight.getArrivalTime());
                priceTextView.setText(String.format("$%.2f", flight.getPrice()));
            }
        }
        
        public interface OnFlightSelectedListener {
            void onFlightSelected(Flight flight);
        }
    }
}
```

#### 3. Ticket Selection Activity

Now, implement the activity for selecting a ticket for a flight:

```java
public class TicketSelectionActivity extends AppCompatActivity {
    private VSAStateManager vsaStateManager;
    private TextView flightInfoTextView;
    private EditText passengerNameEditText;
    private EditText passengerEmailEditText;
    private Spinner seatSpinner;
    private Button bookButton;
    private String flightId;
    private Flight flight;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_selection);
        
        // Initialize VSAStateManager
        vsaStateManager = new VSAStateManager();
        vsaStateManager.connect("localhost", 8080);
        
        // Get flight ID from intent
        flightId = getIntent().getStringExtra("flight_id");
        
        // Find UI elements
        flightInfoTextView = findViewById(R.id.flight_info_text_view);
        passengerNameEditText = findViewById(R.id.passenger_name_edit_text);
        passengerEmailEditText = findViewById(R.id.passenger_email_edit_text);
        seatSpinner = findViewById(R.id.seat_spinner);
        bookButton = findViewById(R.id.book_button);
        
        // Load flight details
        loadFlightDetails();
        
        // Set up seat spinner
        setupSeatSpinner();
        
        // Set up book button with VSA verification
        bookButton.setOnClickListener(new VSAOnClickListener(
            vsaStateManager,
            buildTicketSelectionUpdates(),
            v -> bookTicket()
        ));
    }
    
    private void loadFlightDetails() {
        // In a real app, this would load the flight from a database
        // For this example, we'll just set some dummy values
        flight = new Flight(flightId, "New York", "Los Angeles", "10:00 AM", "1:00 PM", 299.99);
        
        // Update UI
        flightInfoTextView.setText(String.format(
            "%s to %s\n%s - %s\n$%.2f",
            flight.getOrigin(),
            flight.getDestination(),
            flight.getDepartureTime(),
            flight.getArrivalTime(),
            flight.getPrice()
        ));
    }
    
    private void setupSeatSpinner() {
        // In a real app, this would load available seats from a database
        // For this example, we'll just add some dummy seats
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this,
            R.array.seats,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seatSpinner.setAdapter(adapter);
    }
    
    private JSONArray buildTicketSelectionUpdates() {
        try {
            // Get passenger details
            String passengerName = passengerNameEditText.getText().toString();
            String passengerEmail = passengerEmailEditText.getText().toString();
            String seatNumber = seatSpinner.getSelectedItem().toString();
            
            // Build state updates for the selected ticket
            return new JSONArray(
                "[{\"stateName\":\"SelectedTicket\"," +
                "\"flight_id\":\"" + flightId + "\"," +
                "\"passenger_name\":\"" + passengerName + "\"," +
                "\"passenger_email\":\"" + passengerEmail + "\"," +
                "\"seat_number\":\"" + seatNumber + "\"}]"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    private void bookTicket() {
        // Get passenger details
        String passengerName = passengerNameEditText.getText().toString();
        String passengerEmail = passengerEmailEditText.getText().toString();
        String seatNumber = seatSpinner.getSelectedItem().toString();
        
        // Validate passenger details
        if (passengerName.isEmpty() || passengerEmail.isEmpty()) {
            Toast.makeText(this, "Please enter all passenger details", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Book ticket in database
        // ...
        
        // Show success message
        Toast.makeText(this, "Ticket booked successfully", Toast.LENGTH_SHORT).show();
        
        // Finish activity
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
    
    // Flight data class (same as in FlightSelectionActivity)
    private static class Flight {
        private String id;
        private String origin;
        private String destination;
        private String departureTime;
        private String arrivalTime;
        private double price;
        
        public Flight(String id, String origin, String destination, String departureTime, String arrivalTime, double price) {
            this.id = id;
            this.origin = origin;
            this.destination = destination;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.price = price;
        }
        
        public String getId() { return id; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getDepartureTime() { return departureTime; }
        public String getArrivalTime() { return arrivalTime; }
        public double getPrice() { return price; }
    }
}
```

### Example 4: Notification Settings - Using the NotificationSettings Predicate

Let's implement a feature that allows users to manage their notification settings using the NotificationSettings predicate from the earlier examples.

#### 1. Define Predicates

First, define the NotificationSettings predicate:

```java
private void definePredicates() {
    try {
        // Define the NotificationSettings predicate
        JSONArray defineArray = new JSONArray(
            "[{\"name\":\"NotificationSettings\"," +
            "\"description\":\"Status for the current notification settings, taking each setting name and setting status as arguments. Examples of setting names include \\\"Private messages\\\", \\\"Chat messages\\\", \\\"Mention of u/username\\\", and the setting status is one of All on, Inbox, or All off.\"," +
            "\"variables\":[" +
            "{\"name\":\"setting_name\",\"is_key\":true,\"type\":\"Text\"}," +
            "{\"name\":\"setting_status\",\"type\":\"Enum\",\"enum_values\":[\"All on\",\"Inbox\",\"All off\"]}" +
            "]}]"
        );
        
        // Send the definition to the server
        vsaStateManager.defineState(defineArray);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

#### 2. Notification Settings Activity

Now, implement the activity for managing notification settings:

```java
public class NotificationSettingsActivity extends AppCompatActivity {
    private VSAStateManager vsaStateManager;
    private RecyclerView settingsRecyclerView;
    private Button saveButton;
    private List<NotificationSetting> settings;
    private NotificationSettingsAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        
        // Initialize VSAStateManager
        vsaStateManager = new VSAStateManager();
        vsaStateManager.connect("localhost", 8080);
        
        // Find UI elements
        settingsRecyclerView = findViewById(R.id.settings_recycler_view);
        saveButton = findViewById(R.id.save_button);
        
        // Initialize settings list
        settings = new ArrayList<>();
        
        // Set up RecyclerView
        adapter = new NotificationSettingsAdapter(settings);
        settingsRecyclerView.setAdapter(adapter);
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Load settings
        loadSettings();
        
        // Set up save button with VSA verification
        saveButton.setOnClickListener(new VSAOnClickListener(
            vsaStateManager,
            buildNotificationSettingsUpdates(),
            v -> saveSettings()
        ));
    }
    
    private void loadSettings() {
        // In a real app, this would load settings from a database
        // For this example, we'll just add some dummy settings
        settings.clear();
        settings.add(new NotificationSetting("Private messages", "All on"));
        settings.add(new NotificationSetting("Chat messages", "Inbox"));
        settings.add(new NotificationSetting("Mention of u/username", "All off"));
        
        // Update RecyclerView
        adapter.notifyDataSetChanged();
    }
    
    private JSONArray buildNotificationSettingsUpdates() {
        try {
            // Build state updates for all notification settings
            JSONArray updates = new JSONArray();
            
            for (NotificationSetting setting : settings) {
                JSONObject settingUpdate = new JSONObject();
                settingUpdate.put("stateName", "NotificationSettings");
                settingUpdate.put("setting_name", setting.getName());
                settingUpdate.put("setting_status", setting.getStatus());
                updates.put(settingUpdate);
            }
            
            return updates;
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    private void saveSettings() {
        // Save settings in database
        // ...
        
        // Show success message
        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        
        // Finish activity
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        vsaStateManager.close();
    }
    
    // NotificationSetting data class
    private static class NotificationSetting {
        private String name;
        private String status;
        
        public NotificationSetting(String name, String status) {
            this.name = name;
            this.status = status;
        }
        
        public String getName() { return name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // NotificationSettings adapter for RecyclerView
    private class NotificationSettingsAdapter extends RecyclerView.Adapter<NotificationSettingsAdapter.SettingViewHolder> {
        private List<NotificationSetting> settings;
        
        public NotificationSettingsAdapter(List<NotificationSetting> settings) {
            this.settings = settings;
        }
        
        @Override
        public SettingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_setting, parent, false);
            return new SettingViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(SettingViewHolder holder, int position) {
            NotificationSetting setting = settings.get(position);
            holder.bind(setting);
        }
        
        @Override
        public int getItemCount() {
            return settings.size();
        }
        
        public class SettingViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTextView;
            private Spinner statusSpinner;
            
            public SettingViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.name_text_view);
                statusSpinner = itemView.findViewById(R.id.status_spinner);
                
                // Set up status spinner
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    itemView.getContext(),
                    R.array.notification_statuses,
                    android.R.layout.simple_spinner_item
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                statusSpinner.setAdapter(adapter);
                
                // Set up status spinner listener
                statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String status = parent.getItemAtPosition(position).toString();
                        int adapterPosition = getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            settings.get(adapterPosition).setStatus(status);
                        }
                    }
                    
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do nothing
                    }
                });
            }
            
            public void bind(NotificationSetting setting) {
                nameTextView.setText(setting.getName());
                
                // Set spinner selection based on setting status
                String[] statuses = {"All on", "Inbox", "All off"};
                for (int i = 0; i < statuses.length; i++) {
                    if (statuses[i].equals(setting.getStatus())) {
                        statusSpinner.setSelection(i);
                        break;
                    }
                }
            }
        }
    }
}
```

These detailed examples demonstrate how to implement VeriSafeAgent in real-world scenarios, incorporating the concepts from the earlier sections of the document. Each example shows how to define predicates, update state, and use VSA triggers to verify actions before they proceed.
