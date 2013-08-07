package se.miun.ant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelList  {

    public interface ChannelStateListener {
        public void onChannelSelected(ChannelWrapper channelWrapper);
        public void onLightIntensityDataUpdated(List<Integer> intensityValues);
    }

    private ChannelStateListener channelStateListener;

    private List<ListItemState> listItemStates;

    private ChannelAdapter channelAdapter;

    private Handler uiThreadHandler;

    public ChannelList(Context context) {

        listItemStates = new ArrayList<ListItemState>();
        channelAdapter = new ChannelAdapter(context, listItemStates);

        uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    public ChannelAdapter getChannelAdapter() {
        return channelAdapter;
    }

    public void setChannelStateListener(ChannelStateListener listener) {
        channelStateListener = listener;
    }

    public void addChannelWrapper(ChannelWrapper wrapper) {
        if (wrapper.isChannelAlive()) {
            listItemStates.add(new ListItemState(wrapper));
            notifyDataSetChanged();
        }
    }

    private void notifyDataSetChanged() {
        uiThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                channelAdapter.notifyDataSetChanged();
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

            ListItemState stateAtPosition = listItemStates.get(position);
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


    private class ListItemState implements ChannelWrapper.ChannelDataListener,
                                           SeekBar.OnSeekBarChangeListener,
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

                if (AntProtocolHelper.isAudioUpdatePayload(data)) {
                    try {
                        lightIntensity = AntProtocolHelper.decodeVolumeValue(data);
                        channelStateListener.onLightIntensityDataUpdated(getIntensityValues());
                    } catch (AntProtocolHelper.VolumeValueUnknownException e) {
                        Log.e(GlobalState.LOG_TAG, "Error: Volume value unknown");
                    }
                }

                lastReceivedData = data;
            }
        }

        @Override
        public void onChannelConnectionClosed() {
            listItemStates.remove(this);
            channelWrapper.releaseChannel();
            notifyDataSetChanged();
        }

        //
        // SeekBar.OnSeekBarChangeListener implementations
        //

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            sliderValue = progress;

            byte[] payload = AntProtocolHelper.makeVolumePayload(lightIntensity, sliderValue);
            channelWrapper.setBroadcastData(payload);
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
            channelStateListener.onChannelSelected(channelWrapper);
        }

        private List<Integer> getIntensityValues() {
            List<Integer> intensityValues = new ArrayList<Integer>();

            for (ListItemState itemState : listItemStates) {
                intensityValues.add(itemState.lightIntensity);
            }

            return intensityValues;
        }
    }
}
























