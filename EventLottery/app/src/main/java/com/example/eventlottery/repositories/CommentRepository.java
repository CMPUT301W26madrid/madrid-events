/**
 * Role: Repository / Data Access Object (DAO)
 * Purpose: Provides an abstraction layer for all Firestore operations related
 * to event comments. It handles the creation, retrieval, and real-time
 * synchronization of comment streams for specific events, as well as
 * administrative moderation (deletion) tasks.
 *
 * Design Pattern: Repository Pattern. This decouples the ViewModel/Activity
 * from the specific database implementation (Firebase Firestore).
 */

package com.example.eventlottery.repositories;

import com.example.eventlottery.models.Comment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class CommentRepository {
    private static final String COLLECTION = "comments";
    private final CollectionReference commentRef;

    public CommentRepository() {
        commentRef = FirebaseFirestore.getInstance().collection(COLLECTION);
    }

    public Task<Void> addComment(Comment comment) {
        return commentRef.document().set(comment);
    }

    public Task<QuerySnapshot> getCommentsForEvent(String eventId) {
        return commentRef.whereEqualTo("eventId", eventId).get();
    }

    public Task<Void> deleteComment(String commentId) {
        return commentRef.document(commentId).delete();
    }

    public Task<Void> updateComment(Comment comment) {
        return commentRef.document(comment.getId()).set(comment);
    }
}
