package se.miun.ant;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelListFragment extends ListFragment {

    public interface OnChannelSelectedListener {
        public void onChannelSelected(ChannelWrapper channelWrapper);
    }

    public static final String TAG = "ANTLightController";

    private ListView listView;
    private ChannelAdapter channelsAdapter;

    private OnChannelSelectedListener channelSelectedListener;
    private List<ListItemState> itemStates = new ArrayList<ListItemState>();

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        channelsAdapter = new ChannelAdapter(getActivity(), itemStates);

        listView = getListView();
        listView.setItemsCanFocus(false);
        setListAdapter(channelsAdapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            channelSelectedListener = (OnChannelSelectedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnChannelSelectedListener");
        }
    }

    public void addChannelWrapper(ChannelWrapper channelWrapper) {

        if (channelWrapper.isChannelAlive()) {
            itemStates.add(new ListItemState(channelWrapper));
            notifyDataSetChanged();
        }
    }

    private void notifyDataSetChanged() {
        ChannelListFragment.this.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                channelsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateListData() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int firstVisible = listView.getFirstVisiblePosition();

                for (int i = 0; i < listView.getChildCount(); i++) {

                    ListItemState state = itemStates.get(firstVisible + i);
                    View row = listView.getChildAt(i);
                    TextView intensityView = (TextView)row.findViewById(R.id.channel_intensity_view);
                    intensityView.setText(String.valueOf(state.lightIntensity));
                }
            }
        });
    }

    private class ChannelAdapter extends ArrayAdapter<ListItemState> {

        private LayoutInflater inflater;

        public ChannelAdapter(Context context, List<ListItemState> itemStates) {
            super(context, R.layout.channel_list_view, itemStates);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.channel_list_view, null);
            }

            TextView intensityView = null;
            SeekBar intensityBar = null;
            ImageButton openButton = null;

            ViewHolder holder = (ViewHolder)convertView.getTag();

            if (holder != null) {
                intensityView = holder.intensityView;
                intensityBar = holder.intensityBar;
                openButton = holder.openButton;
            } else {
                intensityView = (TextView)convertView.findViewById(R.id.channel_intensity_view);
                intensityBar = (SeekBar)convertView.findViewById(R.id.channel_intensity_seekbar);
                openButton = (ImageButton)convertView.findViewById(R.id.channel_open_imagebutton);
                convertView.setTag(new ViewHolder(intensityView, intensityBar, openButton));
            }

            ListItemState stateAtPosition = itemStates.get(position);
            stateAtPosition.setIntensityBar(intensityBar);
            stateAtPosition.setOpenButton(openButton);

            intensityView.setText(String.valueOf(stateAtPosition.lightIntensity));
            intensityBar.setProgress(stateAtPosition.sliderValue);

            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
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

    private class ListItemState
            implements ChannelWrapper.ChannelDataListener, SeekBar.OnSeekBarChangeListener,
                       ImageButton.OnClickListener {

        public int lightIntensity;
        public int sliderValue;
        public ChannelWrapper channelWrapper;

        private SeekBar intensityBar;
        private ImageButton openButton;

        private byte[] lastReceivedData;

        public ListItemState(ChannelWrapper channelWrapper) {
            this.channelWrapper = channelWrapper;
            this.channelWrapper.setChannelDataListener(this);
            lightIntensity = 0;
            sliderValue = 0;
        }

        public void setIntensityBar(SeekBar intensityBar) {
            this.intensityBar = intensityBar;
            this.intensityBar.setOnSeekBarChangeListener(this);
        }

        public void setOpenButton(ImageButton openButton) {
            this.openButton = openButton;
            this.openButton.setOnClickListener(this);
        }

        //
        // ChannelWrapper.ChannelDataListener implementations
        //

        @Override
        public void onChannelDataReceived(byte[] data, ChannelWrapper channelWrapper) {

            if (!Arrays.equals(data, lastReceivedData)) {
                lastReceivedData = data;
                lightIntensity = data[0];
                updateListData();
            }
        }

        @Override
        public void onChannelConnectionClosed() {
            itemStates.remove(this);
            channelWrapper.releaseChannel();
            notifyDataSetChanged();
        }

        //
        // SeekBar.OnSeekBarChangeListener implementations
        //

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            sliderValue = progress;
            channelWrapper.setBroadcastData((byte)sliderValue);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        //
        // ImageButton.OnClickListener implementation
        //

        @Override
        public void onClick(View view) {
            channelSelectedListener.onChannelSelected(channelWrapper);
        }
    }
}
