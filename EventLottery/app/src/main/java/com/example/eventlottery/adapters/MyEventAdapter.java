package com.example.eventlottery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Registration;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.ViewHolder> {

    public interface OnItemClickListener { void onClick(Registration registration); }

    // We carry both Registration + event title (fetched by caller)
    public static class RegistrationWithTitle {
        public final Registration registration;
        public final String eventTitle;
        public final long eventStartDate;
        public RegistrationWithTitle(Registration r, String title, long date) {
            this.registration   = r;
            this.eventTitle     = title;
            this.eventStartDate = date;
        }
    }

    private List<RegistrationWithTitle> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setListener(OnItemClickListener l) { this.listener = l; }

    public void setItems(List<RegistrationWithTitle> list) {
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return items.isEmpty(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RegistrationWithTitle item = items.get(position);
        Registration r = item.registration;

        h.tvEventTitle.setText(item.eventTitle);
        h.tvEventDate.setText(DateUtils.formatDateShort(item.eventStartDate));
        h.tvJoinedDate.setText("Joined " + DateUtils.formatDateShort(r.getJoinedAt()));

        // Status icon emoji
        String icon;
        int badgeRes;
        switch (r.getStatus()) {
            case Registration.STATUS_WAITING:
                icon = "⏳"; badgeRes = R.drawable.bg_badge_accent; break;
            case Registration.STATUS_SELECTED:
                icon = "🎉"; badgeRes = R.drawable.bg_badge_green; break;
            case Registration.STATUS_ACCEPTED:
                icon = "✅"; badgeRes = R.drawable.bg_badge_green; break;
            case Registration.STATUS_DECLINED:
                icon = "❌"; badgeRes = R.drawable.bg_badge_red; break;
            case Registration.STATUS_CANCELLED:
                icon = "🚫"; badgeRes = R.drawable.bg_badge_grey; break;
            default:
                icon = "?"; badgeRes = R.drawable.bg_badge_grey;
        }
        h.tvStatusIcon.setText(icon);
        h.tvStatusBadge.setText(capitalize(r.getStatus()));
        h.tvStatusBadge.setBackgroundResource(badgeRes);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(r); });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatusIcon, tvEventTitle, tvEventDate, tvJoinedDate, tvStatusBadge;
        ViewHolder(View v) {
            super(v);
            tvStatusIcon  = v.findViewById(R.id.tv_status_icon);
            tvEventTitle  = v.findViewById(R.id.tv_event_title);
            tvEventDate   = v.findViewById(R.id.tv_event_date);
            tvJoinedDate  = v.findViewById(R.id.tv_joined_date);
            tvStatusBadge = v.findViewById(R.id.tv_status_badge);
        }
    }
}
