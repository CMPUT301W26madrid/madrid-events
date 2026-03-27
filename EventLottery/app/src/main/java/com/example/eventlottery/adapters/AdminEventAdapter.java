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
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    public interface OnDeleteListener { void onDelete(Event event); }

    private List<Event> events = new ArrayList<>();
    private List<Event> allEvents = new ArrayList<>();
    private OnDeleteListener deleteListener;

    public void setDeleteListener(OnDeleteListener l) { this.deleteListener = l; }

    public void setEvents(List<Event> list) {
        this.allEvents = new ArrayList<>(list);
        this.events    = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            events = new ArrayList<>(allEvents);
        } else {
            events = new ArrayList<>();
            String q = query.toLowerCase();
            for (Event e : allEvents) {
                if ((e.getTitle() != null && e.getTitle().toLowerCase().contains(q)) ||
                    (e.getOrganizerName() != null && e.getOrganizerName().toLowerCase().contains(q))) {
                    events.add(e);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getTotalCount() { return allEvents.size(); }
    public boolean isEmpty() { return events.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Event e = events.get(position);
        h.tvTitle.setText(e.getTitle());
        h.tvOrganizer.setText("by " + e.getOrganizerName());
        h.tvDate.setText(DateUtils.formatDateShort(e.getEventStartDate()));

        String status = e.getStatus();
        h.tvStatus.setText(status.toUpperCase());
        switch (status) {
            case Event.STATUS_OPEN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_green); break;
            case Event.STATUS_DRAWN:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_accent); break;
            default:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_grey);
        }

        h.ivDelete.setOnClickListener(v -> { if (deleteListener != null) deleteListener.onDelete(e); });
    }

    @Override public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvOrganizer, tvStatus, tvDate;
        ImageView ivDelete;
        ViewHolder(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tv_title);
            tvOrganizer = v.findViewById(R.id.tv_organizer);
            tvStatus   = v.findViewById(R.id.tv_status);
            tvDate     = v.findViewById(R.id.tv_date);
            ivDelete   = v.findViewById(R.id.iv_delete);
        }
    }
}
