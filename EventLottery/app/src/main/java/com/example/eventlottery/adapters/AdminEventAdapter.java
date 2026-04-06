package com.example.eventlottery.adapters;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.models.Event;
import com.example.eventlottery.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;
/**
 * RecyclerView adapter used by administrator screens to display events for browsing,
 * moderation, and image review.
 *
 * <p>Role in application: binds {@link Event} data to admin event cards and supports
 * click actions for opening an event, deleting an event, or reviewing uploaded poster
 * images. When image moderation mode is enabled, the adapter focuses on poster preview
 * and deletion instead of normal card navigation.</p>
 *
 * <p>Outstanding issues: filtering currently checks title and organizer name only, and
 * image decoding failures are handled by hiding the preview rather than reporting a
 * detailed error.</p>
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {
    /**
     * Callback interface for admin actions on event rows.
     */
    public interface OnEventClickListener {
        /**
         * Opens the selected event for closer inspection.
         *
         * @param event the event that was tapped
         */
        void onClick(Event event);
        /**
         * Requests deletion of the selected event or its associated content.
         *
         * @param event the event selected for deletion
         */
        void onDelete(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private List<Event> allEvents = new ArrayList<>();
    private OnEventClickListener listener;
    private boolean showImages = false;
    /**
     * Registers the callback listener used for item interactions.
     *
     * @param l listener that handles open and delete actions
     */
    public void setListener(OnEventClickListener l) { this.listener = l; }
    /**
     * Replaces the current dataset and configures whether poster previews should be shown.
     *
     * @param list the events to display
     * @param showImages {@code true} to enable image review mode, {@code false} for normal mode
     */
    public void setEvents(List<Event> list, boolean showImages) {
        this.showImages = showImages;
        this.allEvents = new ArrayList<>(list);
        this.events    = new ArrayList<>(list);
        notifyDataSetChanged();
    }
    /**
     * Filters the displayed events by event title or organizer name.
     *
     * @param query the search text entered by the administrator
     */
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
    /**
     * Returns the total number of loaded events before filtering.
     *
     * @return the size of the full event list
     */
    public int getTotalCount() { return allEvents.size(); }
    /**
     * Indicates whether the current filtered list is empty.
     *
     * @return {@code true} if no events are currently visible
     */
    public boolean isEmpty() { return events.isEmpty(); }
    /**
     * Inflates a single admin event card view.
     *
     * @param parent the parent RecyclerView
     * @param viewType the requested view type
     * @return a new {@link ViewHolder} for an admin event row
     */
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new ViewHolder(v);
    }
    /**
     * Binds one event to the supplied row and configures the available admin actions.
     *
     * @param h the holder receiving bound data
     * @param position the adapter position being displayed
     */
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

        if (showImages) {
            h.tvDeleteLabel.setVisibility(View.VISIBLE);
            if (e.getPosterUrl() != null && !e.getPosterUrl().isEmpty()) {
                h.ivPosterPreview.setVisibility(View.VISIBLE);
                loadPoster(h.ivPosterPreview, e.getPosterUrl());
                
                // Enlarge image on click
                h.ivPosterPreview.setOnClickListener(v -> showEnlargedImage(v.getContext(), e.getPosterUrl()));
            } else {
                h.ivPosterPreview.setVisibility(View.GONE);
            }
            h.itemView.setOnClickListener(null);
            
            // Allow deleting by clicking the text label as well
            h.tvDeleteLabel.setOnClickListener(v -> { if (listener != null) listener.onDelete(e); });
        } else {
            h.tvDeleteLabel.setVisibility(View.GONE);
            h.ivPosterPreview.setVisibility(View.GONE);
            h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(e); });
            h.tvDeleteLabel.setOnClickListener(null);
        }

        h.ivDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(e); });
    }
    /**
     * Loads a poster image into the given image view from either a URL or a Base64 string.
     *
     * @param imageView the target view for the poster
     * @param posterStr the stored poster value
     */
    private void loadPoster(ImageView imageView, String posterStr) {
        if (posterStr.startsWith("http")) {
            Glide.with(imageView.getContext()).load(posterStr).into(imageView);
        } else {
            try {
                byte[] decodedString = Base64.decode(posterStr, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            } catch (Exception ex) {
                imageView.setVisibility(View.GONE);
            }
        }
    }
    /**
     * Displays the selected poster in a full-screen dialog for easier moderation.
     *
     * @param context the context used to create the dialog
     * @param posterStr the stored poster value to display
     */
    private void showEnlargedImage(android.content.Context context, String posterStr) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_image);
        
        ImageView ivLarge = dialog.findViewById(R.id.iv_full_image);
        loadPoster(ivLarge, posterStr);
        
        dialog.findViewById(R.id.btn_close_image).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    /**
     * Returns the number of currently visible events.
     *
     * @return the filtered event count
     */
    @Override public int getItemCount() { return events.size(); }
    /**
     * Holds view references for a single admin event row.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvOrganizer, tvStatus, tvDate, tvDeleteLabel;
        ImageView ivDelete, ivPosterPreview;
        /**
         * Creates a holder for one admin event item view.
         *
         * @param v the inflated item view
         */
        ViewHolder(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tv_title);
            tvOrganizer = v.findViewById(R.id.tv_organizer);
            tvStatus   = v.findViewById(R.id.tv_status);
            tvDate     = v.findViewById(R.id.tv_date);
            ivDelete   = v.findViewById(R.id.iv_delete);
            ivPosterPreview = v.findViewById(R.id.iv_poster_preview);
            tvDeleteLabel = v.findViewById(R.id.tv_delete_label);
        }
    }
}
