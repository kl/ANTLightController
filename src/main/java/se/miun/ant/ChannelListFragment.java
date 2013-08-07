package se.miun.ant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class ChannelListFragment extends ListFragment implements ChannelList.ChannelStateListener {

    public interface ChannelSelectedListener {
        public void onChannelSelected(ChannelWrapper channelWrapper);
    }

    private ListView listView;
    private ChannelList channelList;
    private ChannelSelectedListener channelListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        channelList = GlobalState.getInstance().getChannelList();
        channelList.setChannelStateListener(this);

        listView = getListView();
        listView.setItemsCanFocus(false);
        setListAdapter(channelList.getChannelAdapter());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            channelListener = (ChannelSelectedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnChannelSelectedListener");
        }
    }

    @Override
    public void onChannelSelected(ChannelWrapper wrapper) {
        channelListener.onChannelSelected(wrapper);
    }

    @Override // TODO: test this with a list that scrolls
    public void onLightIntensityDataUpdated(final List<Integer> intensityValues) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int firstVisible = listView.getFirstVisiblePosition();

                for (int i = 0; i < listView.getChildCount(); i++) {

                    View row = listView.getChildAt(i);
                    TextView intensityView = (TextView)row.findViewById(R.id.channel_intensity_view);
                    int lightIntensity = intensityValues.get(firstVisible + i);
                    intensityView.setText(String.valueOf(lightIntensity));
                }
            }
        });
    }

    public void addChannelWrapper(ChannelWrapper channelWrapper) {
        channelList.addChannelWrapper(channelWrapper);
    }
}
