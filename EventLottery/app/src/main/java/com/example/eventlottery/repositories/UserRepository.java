/**
 * Role: Repository / Data Access Object (DAO)
 * Purpose: Centralizes all Firestore operations for the 'users' collection.
 * It manages the creation, retrieval, deletion, and searching of users,
 * as well as targeted updates like FCM token refresh and notification preferences.
 *
 * Design Pattern: Repository Pattern. It abstracts the data source (Firebase)
 * from the rest of the application, allowing Activities and ViewModels
 * to interact with User data through Task-based asynchronous methods.
 */


package com.example.eventlottery.repositories;

import com.example.eventlottery.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final String COLLECTION = "users";
    private final CollectionReference usersRef;

    public UserRepository() {
        usersRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

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
        if (email == null || email.trim().isEmpty()) return Tasks.forResult(null);
        return usersRef.whereEqualTo("email", email.toLowerCase().trim()).get();
    }

    public Task<QuerySnapshot> getUserByEmailOriginal(String email) {
        if (email == null || email.trim().isEmpty()) return Tasks.forResult(null);
        return usersRef.whereEqualTo("email", email.trim()).get();
    }

    public Task<QuerySnapshot> getUserByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return Tasks.forResult(null);
        return usersRef.whereEqualTo("phone", phone.trim()).get();
    }

    public Task<QuerySnapshot> searchByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return Tasks.forResult(null);
        return usersRef.orderBy("phone")
                .startAt(phone.trim())
                .endAt(phone.trim() + "\uf8ff")
                .get();
    }

    public Task<QuerySnapshot> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) return Tasks.forResult(null);
        String query = name.toLowerCase().trim();
        return usersRef.orderBy("nameLowercase")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get();
    }

    public Task<QuerySnapshot> searchByNameOriginal(String name) {
        if (name == null || name.trim().isEmpty()) return Tasks.forResult(null);
        String query = name.trim();
        return usersRef.orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get();
    }

    public Task<Boolean> checkUserExists(String email, String phone) {
        List<Filter> filters = new ArrayList<>();
        if (email != null && !email.trim().isEmpty()) {
            filters.add(Filter.equalTo("email", email.toLowerCase().trim()));
        }
        if (phone != null && !phone.trim().isEmpty()) {
            filters.add(Filter.equalTo("phone", phone.trim()));
        }

        if (filters.isEmpty()) return Tasks.forResult(false);

        Filter combined = filters.size() == 2 ? Filter.or(filters.get(0), filters.get(1)) : filters.get(0);

        return usersRef.where(combined).get().continueWith(task -> {
            QuerySnapshot qs = task.getResult();
            return qs != null && !qs.isEmpty();
        });
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
