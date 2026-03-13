Prem – Contributions and Implemented User Stories

In addition to implementing several entrant-side features of the application, I was responsible for developing the core waiting list interaction functionality. My work focused on allowing entrants to discover events, join or leave waiting lists, and interact with the system in a way that supports the event lottery workflow. These features are essential because they allow users to participate in events and create the pool of entrants that organizers later select from.

US 01.01.01 – Entrant joins the waiting list for a specific event

For this user story, I implemented the functionality that allows an entrant to join the waiting list for an event. When an entrant selects an event, the system adds their profile information to the waiting list stored in Firebase.

The implementation ensures that each entrant is associated with the correct event and that duplicate entries are prevented. This allows organizers to maintain an accurate list of participants who are interested in attending the event.

US 01.01.02 – Entrant leaves the waiting list for a specific event

For this feature, I implemented the ability for entrants to remove themselves from the waiting list if they decide they are no longer interested in attending the event.

When the entrant chooses to leave the waiting list, their entry is removed from the event’s waiting list data in Firebase. This ensures that the waiting list always reflects the current participants and prevents organizers from selecting users who are no longer interested in attending.

US 01.01.03 – Entrant views events open for joining the waiting list

For this user story, I implemented the feature that allows entrants to browse a list of events that are currently open for registration. The system retrieves available events from Firebase and displays them in the application interface.

This allows entrants to easily discover events and choose which waiting lists they want to join. The event list is dynamically loaded and presented using a RecyclerView to efficiently display multiple events.

US 01.05.04 – Entrant views total number of entrants on the waiting list (Partially Completed)

For this feature, I began implementing the functionality that allows entrants to view the total number of participants currently on the waiting list for an event.

The system retrieves the number of entrants associated with the event from Firebase and displays the count within the event interface. This helps entrants understand how many people are competing for the available spots in the event.

The core logic for retrieving the count has been implemented, but additional interface improvements and final integration are still in progress.
