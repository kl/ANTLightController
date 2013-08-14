package se.miun.ant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class ChannelAdapter extends ArrayAdapter<ListItemState> {

    private List<ListItemState> itemStates;
    private LayoutInflater inflater;

    public ChannelAdapter(Context context, List<ListItemState> itemStates) {
        super(context, R.layout.channel_list_view, itemStates);
        this.itemStates = itemStates;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = inflater.inflate(R.layout.channel_list_view, null);

        try {
            ListItemState stateAtPosition = itemStates.get(position);
            return setViewState(convertView, stateAtPosition);
        } catch (IndexOutOfBoundsException e) {
            // This can happen if a ListItemState gets removed from itemStates after
            // the start of this method but before the start of the try block.
            return convertView;
        }
    }

    private View setViewState(View channelView, ListItemState stateAtPosition) {

        TextView intensityView;
        SeekBar intensityBar;
        ImageButton openButton;

        ViewHolder holder = (ViewHolder)channelView.getTag();

        if (holder != null) {
            intensityView = holder.intensityView;
            intensityBar = holder.intensityBar;
            openButton = holder.openButton;
        } else {
            intensityView = (TextView)channelView.findViewById(R.id.channel_intensity_view);
            openButton = (ImageButton)channelView.findViewById(R.id.channel_open_imagebutton);
            intensityBar = (SeekBar)channelView.findViewById(R.id.channel_intensity_seekbar);
            channelView.setTag(new ViewHolder(intensityView, intensityBar, openButton));
        }

        stateAtPosition.setIntensityBar(intensityBar);
        stateAtPosition.setOpenButton(openButton);

        intensityBar.setProgress(stateAtPosition.lightIntensity);
        intensityView.setText(String.valueOf(stateAtPosition.lightIntensity));

        return channelView;
    }

    private class ViewHolder {
        protected TextView intensityView;
        protected SeekBar intensityBar;
        protected ImageButton openButton;

        public ViewHolder(TextView intensityView, SeekBar intensityBar, ImageButton openButton) {
            this.intensityView = intensityView;
            this.intensityBar = intensityBar;
            this.openButton = openButton;
        }
    }
}
