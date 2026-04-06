package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Registration;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter for event registration lists such as waiting, selected, accepted,
 * declined, or cancelled entrants.
 *
 * <p>Role in application: shows participant identity and registration status for organizer
 * management screens and can optionally expose a cancel button for administrative actions.</p>
 *
 * <p>Outstanding issues: avatar generation currently uses only the first character of the
 * user name, and status styling depends on a fixed set of known registration states.</p>
 */
public class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder> {
    /**
     * Callback invoked when a registration should be cancelled.
     */
    public interface OnCancelListener {
        /**
         * Cancels the selected registration.
         *
         * @param registration the registration chosen for cancellation
         */
        void onCancel(Registration registration);
    }

    private List<Registration> registrations = new ArrayList<>();
    private final boolean showCancelButton;
    private OnCancelListener cancelListener;
    /**
     * Creates a registration adapter.
     *
     * @param showCancelButton {@code true} to display cancel controls when allowed
     */
    public RegistrationAdapter(boolean showCancelButton) {
        this.showCancelButton = showCancelButton;
    }
    /**
     * Registers the listener used for cancellation actions.
     *
     * @param l listener used when the cancel icon is tapped
     */
    public void setCancelListener(OnCancelListener l) { this.cancelListener = l; }
    /**
     * Replaces the registration dataset.
     *
     * @param list the registrations to display
     */
    public void setRegistrations(List<Registration> list) {
        this.registrations = new ArrayList<>(list);
        notifyDataSetChanged();
    }
    /**
     * Indicates whether the registration list is empty.
     *
     * @return {@code true} if no registrations are currently shown
     */
    public boolean isEmpty() { return registrations.isEmpty(); }
    /**
     * Inflates a registration row.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a holder for one registration row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registration, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds a registration record and its status badge to one row.
     *
     * @param h the holder receiving registration data
     * @param position the adapter position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Registration r = registrations.get(position);
        String initials = r.getUserName() != null && !r.getUserName().isEmpty()
                ? String.valueOf(r.getUserName().charAt(0)).toUpperCase() : "?";
        h.tvAvatar.setText(initials);
        h.tvName.setText(r.getUserName());
        h.tvEmail.setText(r.getUserEmail());

        // Status badge
        h.tvStatusBadge.setText(r.getStatus().toUpperCase());
        switch (r.getStatus()) {
            case Registration.STATUS_WAITING:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_accent); break;
            case Registration.STATUS_SELECTED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green); break;
            case Registration.STATUS_ACCEPTED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green); break;
            case Registration.STATUS_DECLINED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_red); break;
            case Registration.STATUS_CANCELLED:
                h.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_grey); break;
        }

        if (showCancelButton && cancelListener != null &&
                !Registration.STATUS_CANCELLED.equals(r.getStatus())) {
            h.ivCancel.setVisibility(View.VISIBLE);
            h.ivCancel.setOnClickListener(v -> cancelListener.onCancel(r));
        } else {
            h.ivCancel.setVisibility(View.GONE);
        }
    }
    /**
     * Returns the number of visible registration rows.
     *
     * @return the current registration count
     */
    @Override public int getItemCount() { return registrations.size(); }
    /**
     * Holds view references for one registration row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail, tvStatusBadge;
        ImageView ivCancel;
        /**
         * Creates a holder for one registration item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvAvatar     = v.findViewById(R.id.tv_avatar);
            tvName       = v.findViewById(R.id.tv_name);
            tvEmail      = v.findViewById(R.id.tv_email);
            tvStatusBadge = v.findViewById(R.id.tv_status_badge);
            ivCancel     = v.findViewById(R.id.iv_cancel);
        }
    }
}
