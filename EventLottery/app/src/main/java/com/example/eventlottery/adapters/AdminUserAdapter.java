package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.User;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter for administrator profile management screens.
 *
 * <p>Role in application: shows user profiles, their role badges, and delete controls so
 * administrators can browse, inspect, and moderate app accounts.</p>
 *
 * <p>Outstanding issues: filtering currently supports only name and email, and badge text
 * is generated directly in the adapter instead of through a shared styling helper.</p>
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    /**
     * Callback invoked when an admin requests deletion of a user.
     */
    public interface OnDeleteListener {
        /**
         * Deletes or removes the selected user profile.
         *
         * @param user the user selected for deletion
         */
        void onDelete(User user);
    }
    /**
     * Callback invoked when an admin opens a user profile.
     */
    public interface OnUserClickListener {
        /**
         * Opens the selected user profile.
         *
         * @param user the selected user
         */
        void onClick(User user);
    }

    private List<User> users = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private OnDeleteListener deleteListener;
    private OnUserClickListener clickListener;
    /**
     * Registers the delete callback.
     *
     * @param l listener used for delete actions
     */
    public void setDeleteListener(OnDeleteListener l) { this.deleteListener = l; }
    /**
     * Registers the click callback used for row selection.
     *
     * @param l listener used when a user row is tapped
     */
    public void setClickListener(OnUserClickListener l) { this.clickListener = l; }
    /**
     * Replaces the full admin user dataset.
     *
     * @param list the users to display
     */
    public void setUsers(List<User> list) {
        this.allUsers = new ArrayList<>(list);
        this.users    = new ArrayList<>(list);
        notifyDataSetChanged();
    }
    /**
     * Filters the visible users by name or email.
     *
     * @param query the search text entered by the administrator
     */
    public void filter(String query) {
        if (query.isEmpty()) {
            users = new ArrayList<>(allUsers);
        } else {
            users = new ArrayList<>();
            String q = query.toLowerCase();
            for (User u : allUsers) {
                if ((u.getName() != null && u.getName().toLowerCase().contains(q)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))) {
                    users.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }
    /**
     * Returns the total number of users before filtering.
     *
     * @return the full user count
     */
    public int getTotalCount() { return allUsers.size(); }
    /**
     * Indicates whether the current filtered list is empty.
     *
     * @return {@code true} if no users are currently shown
     */
    public boolean isEmpty() { return users.isEmpty(); }
    /**
     * Inflates one admin user row.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a holder for a single user row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds user information and moderation actions to one row.
     *
     * @param h the holder receiving the bound user data
     * @param position the adapter position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        User u = users.get(position);
        h.tvAvatar.setText(u.getInitials());
        h.tvName.setText(u.getName());
        h.tvEmail.setText(u.getEmail());

        // Role badges
        h.llRoleBadges.removeAllViews();
        if (u.getRoles() != null) {
            for (String role : u.getRoles()) {
                TextView badge = new TextView(h.llRoleBadges.getContext());
                badge.setText(capitalize(role));
                badge.setTextSize(10f);
                badge.setTextColor(0xFF374151);
                badge.setPadding(dp(h, 8), dp(h, 3), dp(h, 8), dp(h, 3));
                badge.setBackgroundResource(R.drawable.bg_info_cell);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(h, 4));
                badge.setLayoutParams(lp);
                h.llRoleBadges.addView(badge);
            }
        }

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(u);
        });

        h.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(u);
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(ViewHolder h, int val) {
        float d = h.itemView.getContext().getResources().getDisplayMetrics().density;
        return Math.round(val * d);
    }
    /**
     * Returns the number of currently visible users.
     *
     * @return the filtered user count
     */
    @Override public int getItemCount() { return users.size(); }
    /**
     * Holds view references for one admin user row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail;
        LinearLayout llRoleBadges;
        ImageView ivDelete;
        /**
         * Creates a holder for one admin user item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvAvatar    = v.findViewById(R.id.tv_avatar);
            tvName      = v.findViewById(R.id.tv_name);
            tvEmail     = v.findViewById(R.id.tv_email);
            llRoleBadges = v.findViewById(R.id.ll_role_badges);
            ivDelete    = v.findViewById(R.id.iv_delete);
        }
    }
}
