package se.miun.ant;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChannelViewFragment extends Fragment implements ChannelWrapper.ChannelDataListener,
                                                             SeekBar.OnSeekBarChangeListener,
                                                             Button.OnClickListener {

    private ChannelWrapper channelWrapper;
    private int lightIntensity;

    private TextView titleTextView;
    private TextView intensityTextView;
    private Button channelCloseButton;
    private SeekBar intensitySlider;

    public ChannelViewFragment(ChannelWrapper channelWrapper, int lightIntensity) {
        this.channelWrapper = channelWrapper;
        this.lightIntensity = lightIntensity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelWrapper.addChannelDataListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        channelWrapper.removeChannelDataListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.channel_view_fragment, container, false);
        getViewReferences(root);

        titleTextView.setText("Name placeholder");
        intensitySlider.setProgress(lightIntensity);
        intensitySlider.setOnSeekBarChangeListener(this);
        channelCloseButton.setOnClickListener(this);

        updateLightIntensityView();

        return root;
    }

    private void getViewReferences(View root) {
        titleTextView      = (TextView)root.findViewById(R.id.view_fragment_title);
        intensityTextView  = (TextView)root.findViewById(R.id.view_fragment_intensity);
        channelCloseButton = (Button)root.findViewById(R.id.view_fragment_close_button);
        intensitySlider    = (SeekBar)root.findViewById(R.id.view_fragment_intensity_slider);
    }

    public void onClick(View view) {
        closeChannelAndPopBackStack();
    }

    @Override
    public void onChannelDataReceived(byte[] data, ChannelWrapper channelWrapper) {

        if (AntProtocolHelper.isAudioUpdatePayload(data)) {
            try {
                lightIntensity = AntProtocolHelper.decodeVolumeValue(data);
                updateLightIntensityView();
            } catch (AntProtocolHelper.VolumeValueUnknownException e) {
                Log.e(GlobalState.LOG_TAG, "Error: Volume value unknown");
            }
        }

    }

    private void updateLightIntensityView() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                intensityTextView.setText(String.valueOf(lightIntensity));
            }
        });
    }

    @Override
    public void onChannelConnectionClosed() {
        showConnectionClosedToast();
        closeChannelAndPopBackStack();
    }

    private void closeChannelAndPopBackStack() {
        channelWrapper.releaseChannel();
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private void showConnectionClosedToast() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getActivity(),
                               R.string.connection_closed_toast,
                               Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            byte[] payload = AntProtocolHelper.makeIntensityPayload(progress);
            sendAcknowledgedData(payload);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private void sendAcknowledgedData(byte[] payload) {
        try {
            channelWrapper.sendAcknowledgedData(payload);
        } catch (ChannelWrapper.ChannelDataSendException e) {
            Log.e(GlobalState.LOG_TAG, e.getMessage());
        }
    }
}
