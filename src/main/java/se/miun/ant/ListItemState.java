package se.miun.ant;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.Arrays;

public class ListItemState implements ChannelWrapper.ChannelDataListener,
                                      SeekBar.OnSeekBarChangeListener,
                                      ImageButton.OnClickListener {

    public interface ListItemStateListener {
        public void onChannelConnectionClosed(ListItemState listItemState);
        public void onChannelButtonClicked(ListItemState listItemState);
        public void onLightIntensityChanged(int lightIntensity);
    }

    public int lightIntensity;
    public ChannelWrapper channelWrapper;

    private ListItemStateListener stateListener;

    private SeekBar intensityBar;
    private ImageButton openButton;

    private byte[] lastReceivedData;

    public ListItemState(ChannelWrapper channelWrapper, ListItemStateListener stateListener) {
        this.channelWrapper = channelWrapper;
        this.channelWrapper.setChannelDataListener(this);
        this.stateListener = stateListener;
    }

    public void setIntensityBar(SeekBar newIntensityBar) {
        if (newIntensityBar.equals(intensityBar)) return;

        if (intensityBar != null) {
            int currentProgress = intensityBar.getProgress();
            intensityBar = newIntensityBar;
            intensityBar.setProgress(currentProgress);
        } else {
            intensityBar = newIntensityBar;
            intensityBar.setProgress(lightIntensity);
        }

        intensityBar.setOnSeekBarChangeListener(this);
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
            updateLightIntensityData(data);
            lastReceivedData = data;
        }
    }

    private void updateLightIntensityData(byte[] data) {

        if (AntProtocolHelper.isAudioUpdatePayload(data)) {
            try {
                lightIntensity = AntProtocolHelper.decodeVolumeValue(data);
                stateListener.onLightIntensityChanged(lightIntensity);
            } catch (AntProtocolHelper.VolumeValueUnknownException e) {
                Log.e(GlobalState.LOG_TAG, "Error: Volume value unknown");
            }
        }
    }

    @Override
    public void onChannelConnectionClosed() {
        channelWrapper.releaseChannel();
        stateListener.onChannelConnectionClosed(this);
    }

    //
    // SeekBar.OnSeekBarChangeListener implementations
    //

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            byte[] payload = AntProtocolHelper.makeIntensityPayload(progress);
            channelWrapper.setBroadcastData(payload);
        }
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
        stateListener.onChannelButtonClicked(this);
    }
}
