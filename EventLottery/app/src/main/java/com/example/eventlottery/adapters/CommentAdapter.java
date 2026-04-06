package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Comment;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter for event comment threads.
 *
 * <p>Role in application: displays {@link Comment} entries on event detail screens and
 * exposes edit or delete controls according to the current user and organizer privileges.</p>
 *
 * <p>Outstanding issues: permission checks are handled locally using the current user ID and
 * organizer flag, so any future moderation rules must be updated in this adapter as well.</p>
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    /**
     * Callback invoked when a comment should be deleted.
     */
    public interface OnDeleteListener {
        /**
         * Deletes the selected comment.
         *
         * @param comment the comment selected for deletion
         */
        void onDelete(Comment comment);
    }
    /**
     * Callback invoked when a comment should be edited.
     */
    public interface OnEditListener {
        /**
         * Starts editing for the selected comment.
         *
         * @param comment the comment selected for editing
         */
        void onEdit(Comment comment);
    }

    private List<Comment> comments = new ArrayList<>();
    private String currentUserId;
    private boolean isOrganizer;
    private OnDeleteListener deleteListener;
    private OnEditListener editListener;
    /**
     * Creates a comment adapter for the current viewer context.
     *
     * @param currentUserId the ID of the signed-in user
     * @param isOrganizer whether the viewer has organizer moderation privileges
     */
    public CommentAdapter(String currentUserId, boolean isOrganizer) {
        this.currentUserId = currentUserId;
        this.isOrganizer   = isOrganizer;
    }
    /**
     * Registers the delete callback.
     *
     * @param l listener used for comment deletion
     */
    public void setDeleteListener(OnDeleteListener l) { this.deleteListener = l; }
    /**
     * Registers the edit callback.
     *
     * @param l listener used for comment editing
     */
    public void setEditListener(OnEditListener l) { this.editListener = l; }
    /**
     * Replaces the visible comment list.
     *
     * @param list the comments to display
     */
    public void setComments(List<Comment> list) {
        this.comments = new ArrayList<>(list);
        notifyDataSetChanged();
    }
    /**
     * Indicates whether the comment list is empty.
     *
     * @return {@code true} if there are no comments to display
     */
    public boolean isEmpty() { return comments.isEmpty(); }
    /**
     * Inflates a single comment row.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a holder for one comment row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds comment content and permission-based controls to one row.
     *
     * @param h the holder receiving comment data
     * @param position the adapter position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Comment c = comments.get(position);
        h.tvAvatar.setText(c.getInitials());
        h.tvName.setText(c.getUserName());
        
        String text = c.getText();
        if (c.isEdited()) {
            text += " (edited)";
        }
        h.tvText.setText(text);
        
        h.tvTime.setText(DateUtils.formatRelative(c.getCreatedAt()));

        boolean canDelete = isOrganizer || c.getUserId().equals(currentUserId);
        h.ivDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        if (canDelete) {
            h.ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(c);
            });
        }

        boolean canEdit = c.getUserId().equals(currentUserId);
        h.ivEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        if (canEdit) {
            h.ivEdit.setOnClickListener(v -> {
                if (editListener != null) editListener.onEdit(c);
            });
        }
    }
    /**
     * Returns the number of visible comments.
     *
     * @return the current comment count
     */
    @Override public int getItemCount() { return comments.size(); }
    /**
     * Holds view references for one comment row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvText, tvTime;
        ImageView ivDelete, ivEdit;
        /**
         * Creates a holder for one comment item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tv_avatar);
            tvName   = v.findViewById(R.id.tv_name);
            tvText   = v.findViewById(R.id.tv_text);
            tvTime   = v.findViewById(R.id.tv_time);
            ivDelete = v.findViewById(R.id.iv_delete);
            ivEdit   = v.findViewById(R.id.iv_edit);
        }
    }
}
