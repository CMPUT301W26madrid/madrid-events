**Tiolu - Contributions and Implemented User Stories**

In this project, I was responsible for implementing several entrant-related features of the application. My work focused on managing entrant profiles, handling device identification, and giving users control over notification settings. These features ensure that entrants can register and manage their personal information while allowing the system to uniquely identify users and respect their notification preferences.

**US 01.02.01 – Entrant provides personal information**

For this user story, I implemented the functionality that allows entrants to provide their personal information when using the application. Entrants can enter details such as their name and contact information through the profile interface.
The information entered by the entrant is stored in Firebase so that it can be accessed later when the entrant participates in events. This allows the system to associate entrant information with event registrations and other application features.
Validation checks were also added to ensure that required information is entered correctly before the profile is saved.

**US 01.02.02 – Entrant updates profile information**

For this feature, I implemented the ability for entrants to update their existing profile information. Users can modify their previously entered personal details through the profile management interface.
When changes are made, the updated information is saved to Firebase, replacing the previous data associated with that entrant. This ensures that entrant records remain accurate and up to date.
The system retrieves the existing profile information and populates the input fields so that users can easily review and edit their details.

**US 01.07.01 – Entrant is identified by device**

For this user story, I implemented the functionality that allows entrants to be uniquely identified using their device. The system generates and stores a device identifier that is used to recognize the entrant when they access the application.
This device ID is stored locally and associated with the entrant's profile in Firebase. Using device identification allows the application to recognize returning users without requiring account login, ensuring that entrant data and event participation are correctly linked to the same user.

**US 01.04.03 – Entrant opts out of notifications**

For this feature, I implemented the option that allows entrants to opt out of receiving notifications from the application. This setting can be controlled through the profile or settings interface.
When a user disables notifications, the preference is stored in Firebase and used by the system when sending event-related notifications. If notifications are turned off, the system will not send alerts such as lottery results or event invitations to that user.
This functionality ensures that entrants have control over their notification preferences while still maintaining their participation in events.

