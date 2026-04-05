package com.example.eventlottery.repositories;

import com.example.eventlottery.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
        if (email == null) return Tasks.forResult(null);
        return usersRef.whereEqualTo("email", email.toLowerCase()).get();
    }

    public Task<QuerySnapshot> getUserByEmailOriginal(String email) {
        if (email == null) return Tasks.forResult(null);
        return usersRef.whereEqualTo("email", email).get();
    }

    public Task<QuerySnapshot> getUserByPhone(String phone) {
        if (phone == null) return Tasks.forResult(null);
        return usersRef.whereEqualTo("phone", phone).get();
    }

    public Task<QuerySnapshot> searchByPhone(String phone) {
        return usersRef.orderBy("phone").startAt(phone).endAt(phone + "\uf8ff").get();
    }

    public Task<QuerySnapshot> searchByName(String name) {
        return usersRef.orderBy("name").startAt(name.toLowerCase()).endAt(name.toLowerCase() + "\uf8ff").get();
    }

    public Task<QuerySnapshot> searchByNameOriginal(String name) {
        return usersRef.orderBy("name").startAt(name).endAt(name + "\uf8ff").get();
    }

    public Task<Boolean> checkUserExists(String email, String phone) {
        Task<QuerySnapshot> emailTask = getUserByEmail(email);
        Task<QuerySnapshot> phoneTask = getUserByPhone(phone);

        return Tasks.whenAllSuccess(emailTask, phoneTask).continueWith(task -> {
            boolean emailExists = emailTask.getResult() != null && !emailTask.getResult().isEmpty();
            boolean phoneExists = phoneTask.getResult() != null && !phoneTask.getResult().isEmpty();
            return emailExists || phoneExists;
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
