Abrar – Contributions and Implemented User Stories

In addition to implementing several features of the application, I was also responsible for writing the test cases for the app. I created unit tests and UI tests to verify that key features such as event creation, database interactions with Firebase, and list displays work correctly. These tests helped ensure that the application behaves as expected and that new changes do not break existing functionality.

US 02.01.01 – Organizer creates a new event with QR code

For this user story, I implemented the feature that allows organizers to create a new event through the app. The organizer enters event details through the event creation interface, and the information is stored in Firebase.

Each event is assigned a unique identifier, which is used to generate a QR code associated with that event. This QR code allows entrants to quickly access or join the event. I also wrote test cases to verify that events are created correctly, stored in the database, and that the QR code is generated properly.

US 02.01.04 – Organizer sets a registration period

For this feature, I implemented the ability for organizers to define the registration start and end times for an event. These values are entered through the event creation interface and stored as part of the event data in Firebase.

I also added validation to ensure the registration end time occurs after the start time. Test cases were written to confirm that the registration period is stored correctly and that invalid inputs are handled properly.

US 02.02.01 – Organizer views waiting list (Partially Completed)

I partially implemented the feature that allows organizers to view the list of entrants on the waiting list for a specific event. Waiting list data is retrieved from Firebase and displayed in the organizer interface using a RecyclerView.

This allows organizers to see the entrants who have registered for the event. While the basic retrieval and display functionality works, some additional improvements are still in progress. Test cases were also written to verify that waiting list data is correctly retrieved and displayed.

US 02.06.01 – Organizer views invited entrants

For this user story, I implemented the functionality that allows organizers to view entrants who have been invited after the lottery selection process. The system retrieves invited entrants from Firebase and displays them in the organizer interface using a RecyclerView.

This allows organizers to track which participants have been selected and invited to the event. I also wrote test cases to verify that the correct invited entrants are retrieved and displayed.
