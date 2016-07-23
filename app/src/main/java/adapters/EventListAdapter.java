package adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epicodus.facebook_googleeventsync.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import models.FacebookEvent;

/**
 * Created by Kyle on 7/10/2016.
 */
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FacebookEvent event);
    }

    private final OnItemClickListener listener;
    private ArrayList<FacebookEvent> mEvents = new ArrayList<>();
    private Context mContext;

    public EventListAdapter(ArrayList<FacebookEvent> events, OnItemClickListener listener) {
        mEvents = events;
        this.listener = listener;
    }

    @Override
    public EventListAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        EventViewHolder viewHolder = new EventViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventListAdapter.EventViewHolder holder, int position) {
        holder.bindEvent(mEvents.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.eventNameTextView) TextView mNameTextView;
        @Bind(R.id.descriptionTextView) TextView mDesciptionView;
        @Bind(R.id.startTimeTextView) TextView mStartTimeTextView;
        @Bind(R.id.endTimeTextView) TextView mEndTimeTextView;
        @Bind(R.id.background) LinearLayout mBackground;
        @Bind(R.id.syncButton) Button mSyncButton;

        public EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        public void bindEvent(final FacebookEvent event, final OnItemClickListener listener) {
            mNameTextView.setText(event.getName());
            mDesciptionView.setText(String.format("%1.400s", event.getDescription()));
            mStartTimeTextView.setText(event.getDisplayStart());
            mEndTimeTextView.setText(event.getDisplayEnd());
            if (event.getSyncStatus().equals("duplicate")) {
                mBackground.setBackgroundColor(Color.LTGRAY);
            }
            if (event.getSyncStatus().equals("danger zone")) {
                mBackground.setBackgroundColor(Color.YELLOW);
            }
            mSyncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(event);
                }
            });
        }
    }
}