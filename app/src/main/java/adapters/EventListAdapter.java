package adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private ArrayList<FacebookEvent> mEvents = new ArrayList<>();
    private Context mContext;

    public EventListAdapter(Context context, ArrayList<FacebookEvent> events) {
        mContext = context;
        mEvents = events;
    }

    @Override
    public EventListAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        EventViewHolder viewHolder = new EventViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventListAdapter.EventViewHolder holder, int position) {
        holder.bindEvent(mEvents.get(position));
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
        private Context mContext;

        public EventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        public void bindEvent(FacebookEvent event) {
            mNameTextView.setText(event.getName());
            mDesciptionView.setText(event.getDescription());
            mStartTimeTextView.setText(event.getDisplayStart());
            mEndTimeTextView.setText(event.getDisplayEnd());
            if (event.getSyncStatus() == "duplicate") {
                mBackground.setBackgroundColor(Color.LTGRAY);
            }
            if (event.getSyncStatus() == "danger zone") {
                mBackground.setBackgroundColor(Color.YELLOW);
        }
    }
}