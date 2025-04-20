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