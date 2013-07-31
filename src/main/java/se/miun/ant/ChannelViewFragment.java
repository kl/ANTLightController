package se.miun.ant;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChannelViewFragment extends Fragment {

    private ChannelWrapper channelWrapper;

    public ChannelViewFragment(ChannelWrapper channelWrapper) {
        this.channelWrapper = channelWrapper;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.channel_view_fragment, container, false);
        TextView text = (TextView)root.findViewById(R.id.view_fragment_textview);
        text.setText(channelWrapper.toString());
        return root;
    }

}
