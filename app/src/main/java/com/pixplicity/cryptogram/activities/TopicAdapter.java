package com.pixplicity.cryptogram.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.providers.TopicProvider;

import java.util.Map;

class TopicAdapter extends ArrayAdapter<Topic> {

    private final LayoutInflater mInflater;

    private final int mListItemRes, mDropDownRes;

    public TopicAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mListItemRes = R.layout.item_topic;
        mDropDownRes = R.layout.item_topic_dropdown;
        add(null);
        Map<String, Topic> topics = TopicProvider.getInstance(context).getTopics();
        for (String topicId : topics.keySet()) {
            add(topics.get(topicId));
        }
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView,
                 @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mListItemRes);
    }

    public int getIndex(Topic topic) {
        if (topic != null) {
            for (int i = 0; i < getCount(); i++) {
                Topic topicMatch = getItem(i);
                if (topic.equals(topicMatch)) {
                    return i;
                }
            }
        }
        return 0;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownRes);
    }

    private View createViewFromResource(int position, @Nullable View convertView,
                                        @NonNull ViewGroup parent, int listItemRes) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(listItemRes, parent, false);
        } else {
            view = convertView;
        }
        TextView tvName = view.findViewById(R.id.tv_name);
        final Topic topic = getItem(position);
        if (topic == null) {
            tvName.setText(R.string.all_topics);
        } else {
            tvName.setText(topic.getName());
        }
        return view;
    }

}
