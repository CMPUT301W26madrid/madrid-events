package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter for events managed by an organizer.
 *
 * <p>Role in application: binds organizer-owned {@link Event} objects to event cards that
 * summarize waiting list counts, accepted counts, and lottery status while exposing open
 * and edit actions.</p>
 *
 * <p>Outstanding issues: status labels are mapped locally in the adapter, so additional
 * event states require updates here as well as in the model and UI filters.</p>
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {
    /**
     * Callback interface for organizer event interactions.
     */
    public interface OnEventClickListener {
        /**
         * Opens the selected event management screen.
         *
         * @param event the selected event
         */
        void onClick(Event event);
        /**
         * Starts editing for the selected event.
         *
         * @param event the event selected for editing
         */
        void onEditClick(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;
    /**
     * Registers the listener used for event row interactions.
     *
     * @param l listener used for open and edit actions
     */
    public void setListener(OnEventClickListener l) { this.listener = l; }
    /**
     * Replaces the organizer event dataset.
     *
     * @param list the events to display
     */
    public void setEvents(List<Event> list) {
        this.events = new ArrayList<>(list);
        notifyDataSetChanged();
    }
    /**
     * Indicates whether any organizer events are currently available.
     *
     * @return {@code true} if the adapter has no events
     */
    public boolean isEmpty() { return events.isEmpty(); }
    /**
     * Inflates an organizer event card.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a holder for one organizer event row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_event, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds organizer-facing event summary data to one row.
     *
     * @param h the holder receiving row data
     * @param position the adapter position being displayed
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Event e = events.get(position);
        h.tvTitle.setText(e.getTitle());
        h.tvEntrantCount.setText(e.getWaitingListCount() + " entrants");
        h.tvAcceptedCount.setText(e.getAcceptedCount() + "/" + e.getCapacity() + " accepted");

        String status = e.getStatus();
        String label;
        int badgeRes;
        switch (status) {
            case Event.STATUS_OPEN:
                label = "Registration Open"; badgeRes = R.drawable.bg_badge_accent; break;
            case Event.STATUS_CLOSED:
                label = "Registration Closed"; badgeRes = R.drawable.bg_badge_grey; break;
            case Event.STATUS_DRAWN:
                label = "Lottery Drawn"; badgeRes = R.drawable.bg_badge_green; break;
            case Event.STATUS_COMPLETED:
                label = "Completed"; badgeRes = R.drawable.bg_badge_grey; break;
            default:
                label = capitalize(status); badgeRes = R.drawable.bg_badge_grey;
        }
        h.tvStatus.setText(label);
        h.tvStatus.setBackgroundResource(badgeRes);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(e); });
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEditClick(e); });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    /**
     * Returns the number of organizer events currently displayed.
     *
     * @return the current event count
     */
    @Override public int getItemCount() { return events.size(); }
    /**
     * Holds view references for one organizer event row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvEntrantCount, tvAcceptedCount, tvStatus;
        ImageView btnEdit;
        /**
         * Creates a holder for one organizer event item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvTitle        = v.findViewById(R.id.tv_title);
            tvEntrantCount = v.findViewById(R.id.tv_entrant_count);
            tvAcceptedCount = v.findViewById(R.id.tv_accepted_count);
            tvStatus       = v.findViewById(R.id.tv_status);
            btnEdit        = v.findViewById(R.id.btn_edit_event);
        }
    }
}
