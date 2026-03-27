package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private List<Event> events   = new ArrayList<>();
    private List<Event> allEvents = new ArrayList<>();
    private final OnEventClickListener listener;

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.allEvents = new ArrayList<>(events);
        this.events    = new ArrayList<>(events);
        notifyDataSetChanged();
    }

    public void filter(String query, String statusFilter) {
        String lowerQuery = query == null ? "" : query.trim().toLowerCase();
        events = new ArrayList<>();
        for (Event e : allEvents) {
            // Null-safe string comparisons (Bug fix: NPE if title/location/status is null)
            String title    = e.getTitle()    != null ? e.getTitle().toLowerCase()    : "";
            String location = e.getLocation() != null ? e.getLocation().toLowerCase() : "";
            String status   = e.getStatus()   != null ? e.getStatus().toLowerCase()   : "";

            boolean matchesQuery = lowerQuery.isEmpty()
                    || title.contains(lowerQuery)
                    || location.contains(lowerQuery);

            boolean matchesStatus = "all".equals(statusFilter)
                    || status.equalsIgnoreCase(statusFilter);

            if (matchesQuery && matchesStatus) events.add(e);
        }
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return events.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Event e = events.get(position);

        // Null-safe setText calls (Bug fix: any field could be null from Firestore)
        h.tvTitle.setText(e.getTitle() != null ? e.getTitle() : "");
        h.tvDescription.setText(e.getDescription() != null ? e.getDescription() : "");
        h.tvLocation.setText(e.getLocation() != null ? e.getLocation() : "");
        h.tvDate.setText(DateUtils.formatDateShort(e.getEventStartDate()));
        h.tvCapacity.setText(e.getWaitingListCount() + "/" + e.getCapacity() + " spots");
        h.tvPrice.setText(e.getFormattedPrice());

        // Status badge — default to "open" if null
        String status = e.getStatus() != null ? e.getStatus() : Event.STATUS_OPEN;
        h.tvStatus.setText(status.toUpperCase());
        switch (status) {
            case Event.STATUS_OPEN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_green);  break;
            case Event.STATUS_CLOSED:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_grey);   break;
            case Event.STATUS_DRAWN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_accent); break;
            default:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_grey);
        }

        // Days-left countdown
        if (e.isRegistrationOpen()) {
            long days = e.getDaysLeftToRegister();
            h.llDaysLeft.setVisibility(View.VISIBLE);
            h.tvDaysLeft.setText(days + " days left to register");
        } else {
            h.llDaysLeft.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onEventClick(e));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvLocation, tvDate,
                 tvCapacity, tvPrice, tvStatus, tvDaysLeft;
        View llDaysLeft;

        ViewHolder(View v) {
            super(v);
            tvTitle       = v.findViewById(R.id.tv_title);
            tvDescription = v.findViewById(R.id.tv_description);
            tvLocation    = v.findViewById(R.id.tv_location);
            tvDate        = v.findViewById(R.id.tv_date);
            tvCapacity    = v.findViewById(R.id.tv_capacity);
            tvPrice       = v.findViewById(R.id.tv_price);
            tvStatus      = v.findViewById(R.id.tv_status);
            tvDaysLeft    = v.findViewById(R.id.tv_days_left);
            llDaysLeft    = v.findViewById(R.id.ll_days_left);
        }
    }
}
