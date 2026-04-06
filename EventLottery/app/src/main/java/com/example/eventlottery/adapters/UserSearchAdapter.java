package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.User;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter used to show user search results when organizers invite entrants
 * or otherwise look up profiles by name, email, or phone.
 *
 * <p>Role in application: binds lightweight {@link User} profile data to a reusable list
 * item and forwards selection events to the surrounding dialog or screen.</p>
 *
 * <p>Outstanding issues: this adapter assumes the supplied list is already de-duplicated
 * and sorted by the calling component.</p>
 */
public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {
    /**
     * Callback invoked when a user search result is selected.
     */
    public interface OnUserClickListener {
        /**
         * Handles selection of a user result.
         *
         * @param user the chosen user
         */
        void onUserClick(User user);
    }

    private List<User> users = new ArrayList<>();
    private final OnUserClickListener listener;
    /**
     * Creates a new adapter for user search results.
     *
     * @param listener callback used when a result row is tapped
     */
    public UserSearchAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }
    /**
     * Replaces the displayed user search results.
     *
     * @param users the users to show in the list
     */
    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
    /**
     * Inflates a single user result row.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a holder for one search result row
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds a user search result to the supplied holder.
     *
     * @param holder the row holder receiving data
     * @param position the adapter position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvInitials.setText(user.getInitials());
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }
    /**
     * Returns the number of visible search results.
     *
     * @return the current result count
     */
    @Override
    public int getItemCount() {
        return users.size();
    }
    /**
     * Holds view references for one user search result row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvEmail;
        /**
         * Creates a holder for one user result item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvInitials = v.findViewById(R.id.tv_avatar);
            tvName = v.findViewById(R.id.tv_name);
            tvEmail = v.findViewById(R.id.tv_email);
        }
    }
}
