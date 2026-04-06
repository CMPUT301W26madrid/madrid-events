## EventLottery

Welcome to **EventLottery**, an Android application for fair event registration at community centres.

Our app helps entrants join popular community events without needing to constantly refresh a webpage or race for spots. Instead, organizers collect interested entrants in a waiting list and use a lottery system to fairly select participants. The app also supports replacement draws, QR-based event access, notifications, and role-based event management.



## Table of Contents

- [Team Members](#team-members)
- [Compatibility](#compatibility)
- [Overview](#overview)
- [Features](#features)
- [Roles](#roles)
- [Tech Stack](#tech-stack)
- [Wiki Page](#wiki-page)

## Team Members: 
|CCID| Github Username|
|----------|----------|
|ashossai| Abrar797|
|mansibye| mansib-yeasfi|
|premvee1| PremGill24|
|tasfi| tasfikamali-rgb|
|toluolay| iamtiolu|
|yutao12| yutao17|

## Compatibility
This application has been tested and is confirmed to run on **Android 16.0 ("Baklava"), API Level 36.0**.

While the app is expected to run on other Android versions as well, very old Android versions are not recommended, as they may introduce compatibility issues or limited support for certain features.

For the best experience, run the app on the emulator or device version we recommend using the tested version listed above.

## Overview
EventLottery is a mobile application designed for community centre events that are popular and fill up quickly. Instead of rewarding only the fastest users, the app gives entrants a fair way to express interest by joining a waiting list during the registration period.

Once registration closes, organizers can draw a specified number of entrants from the waiting list. Selected entrants are notified and can accept or decline their invitation. If someone declines or is cancelled later, the system can draw a replacement applicant. This makes registration more accessible, more flexible, and fairer for people with work, disability, or other scheduling limitations.

The app also supports QR-code access to event details, event poster images, optional geolocation requirements, profile management, comment features, and separate workflows for entrants, organizers, and administrators.


## Features

### Entrant Features
- Join the waiting list for a specific event
- Leave the waiting list for an event
- Browse events open for registration
- Search and filter events
- View event details from a promotional QR code
- Sign up for an event directly from the event details page
- Receive notifications when selected, not selected, or invited
- Accept or decline an invitation after being chosen
- Get another chance when replacement draws happen
- View waiting list counts and registration-related information
- Manage personal profile information
- View event history
- Post and view event comments

### Organizer Features
- Create public events with promotional QR codes
- Create private events without public listing or promotional QR codes
- Set registration periods
- View waiting list entrants
- Draw a specified number of participants from the waiting list
- Draw replacement applicants when invited entrants decline or cancel
- View invited, cancelled, and enrolled entrant lists
- Upload and update event poster images
- Enable or disable geolocation requirements
- View where entrants joined from on a map
- Send notifications to waiting, invited, or cancelled entrants
- Export final enrolled entrant lists in CSV format
- Manage comments on events
- Assign co-organizers to events

### Administrator Features
- Browse and remove events
- Browse and remove profiles
- Browse and remove uploaded images
- Remove organizers who violate app policy
- Review logs of notifications sent by organizers
- Remove inappropriate event comments
- Act as an administrator while also supporting organizer and entrant capabilities



## Roles

### Entrant
An entrant is a user who joins the waiting list for events and may later be invited to register.

### Organizer
An organizer creates and manages events, monitors waiting lists, runs lottery draws, and handles event communication.

### Administrator
An administrator manages the app infrastructure and moderates events, profiles, images, organizers, and comments.



## Tech Stack

- **Android**
- **Firebase**
  - Event data storage
  - Attendee and waiting list management
  - Real-time status updates
- **QR Code Scanning**
- **Image Upload Support**
- **Geolocation Verification**




## Wiki Page
This wiki page contains everything you need to know about our app. [Wiki Page](https://github.com/CMPUT301W26madrid/madrid-events/wiki)
## Project Backlog
This is our Project Backlog, to see all of the issues in progress. [Project Backlog](https://github.com/orgs/CMPUT301W26madrid/projects/1/views/1)


