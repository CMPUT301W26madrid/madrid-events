package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.User;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter for displaying user profiles with avatar initials and role badges.
 *
 * <p>Role in application: supports profile browsing screens by binding {@link User}
 * information to a compact card layout and forwarding selection events to the host
 * activity or fragment.</p>
 *
 * <p>Outstanding issues: role badges are built dynamically in the adapter, so any future
 * badge styling changes must be kept in sync with this implementation.</p>
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    /**
     * Callback invoked when a profile card is selected.
     */
    public interface OnProfileClickListener {
        /**
         * Opens the selected user profile.
         *
         * @param user the selected user
         */
        void onClick(User user);
    }

    private List<User> profiles = new ArrayList<>();
    private final OnProfileClickListener listener;
    /**
     * Creates a new adapter with the supplied click listener.
     *
     * @param listener callback used when a profile row is tapped
     */
    public ProfileAdapter(OnProfileClickListener listener) { this.listener = listener; }
    /**
     * Replaces the current list of profiles.
     *
     * @param list the profiles to display
     */
    public void setProfiles(List<User> list) {
        this.profiles = new ArrayList<>(list);
        notifyDataSetChanged();
    }
    /**
     * Indicates whether any profiles are currently available.
     *
     * @return {@code true} if the profile list is empty
     */
    public boolean isEmpty() { return profiles.isEmpty(); }
    /**
     * Inflates a profile row.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a new holder for a profile row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds a user profile and its role badges to one row.
     *
     * @param h the holder receiving the bound data
     * @param position the row position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        User u = profiles.get(position);
        h.tvAvatar.setText(u.getInitials());
        h.tvName.setText(u.getName());

        // Role badges
        h.llRoles.removeAllViews();
        if (u.getRoles() != null) {
            for (String role : u.getRoles()) {
                TextView badge = new TextView(h.llRoles.getContext());
                badge.setText(capitalize(role));
                badge.setTextSize(10f);
                badge.setTextColor(0xFFFFFFFF);
                badge.setPadding(dp(h, 8), dp(h, 3), dp(h, 8), dp(h, 3));
                badge.setBackgroundResource(getBadgeRes(role));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd(dp(h, 4));
                badge.setLayoutParams(lp);
                h.llRoles.addView(badge);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    private int getBadgeRes(String role) {
        switch (role) {
            case "organizer": return R.drawable.bg_badge_accent;
            case "admin":     return R.drawable.bg_badge_red;
            default:          return R.drawable.bg_badge_green;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(ViewHolder h, int val) {
        float density = h.itemView.getContext().getResources().getDisplayMetrics().density;
        return Math.round(val * density);
    }
    /**
     * Returns the number of visible profile rows.
     *
     * @return the current profile count
     */
    @Override public int getItemCount() { return profiles.size(); }
    /**
     * Holds view references for a single profile row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName;
        LinearLayout llRoles;
        /**
         * Creates a holder for one profile item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tv_avatar);
            tvName   = v.findViewById(R.id.tv_name);
            llRoles  = v.findViewById(R.id.ll_roles);
        }
    }
}
