package com.example.eventlottery.repositories;

import com.example.eventlottery.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class UserRepository {
    private static final String COLLECTION = "users";
    private final CollectionReference usersRef;

    public UserRepository() {
        usersRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

    /**
     * Saves a new user and returns a Task<DocumentReference> so the caller
     * can get the auto-generated Firestore document ID.
     */
    public Task<DocumentReference> addUser(User user) {
        return usersRef.add(user);
    }

    public Task<Void> updateUser(String userId, User user) {
        return usersRef.document(userId).set(user);
    }

    public Task<DocumentSnapshot> getUserById(String userId) {
        return usersRef.document(userId).get();
    }

    public Task<QuerySnapshot> getUserByDeviceId(String deviceId) {
        return usersRef.whereEqualTo("deviceId", deviceId).get();
    }

    public Task<QuerySnapshot> getUserByEmail(String email) {
        return usersRef.whereEqualTo("email", email).limit(1).get();
    }

    public Task<QuerySnapshot> getAllUsers() {
        return usersRef.orderBy("name", Query.Direction.ASCENDING).get();
    }

    public Task<Void> deleteUser(String userId) {
        return usersRef.document(userId).delete();
    }

    public Task<Void> updateFcmToken(String userId, String token) {
        return usersRef.document(userId).update("fcmToken", token);
    }

    public Task<Void> updateNotificationPreference(String userId, boolean enabled) {
        return usersRef.document(userId).update("pushNotificationsEnabled", enabled);
    }
}
