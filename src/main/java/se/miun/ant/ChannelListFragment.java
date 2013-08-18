package se.miun.ant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ChannelListFragment extends ListFragment implements ChannelList.ChannelListener {

    public interface ChannelSelectedListener {
        public void onChannelSelected(ChannelWrapper channelWrapper, int lightIntensity);
    }

    private ListView listView;
    private ChannelList channelList;
    private ChannelSelectedListener channelListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        channelList = GlobalState.getInstance().getChannelList();
        channelList.setChannelListener(this);

        setEmptyText(getString(R.string.channel_list_empty_text));
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
    public void onResume() {
        super.onResume();
        channelList.validateChannels();
    }

    @Override
    public void onChannelSelected(ChannelWrapper wrapper, int lightIntensity) {
        channelListener.onChannelSelected(wrapper, lightIntensity);
    }

    @Override
    public void onLightIntensityDataUpdated(final int[] intensityValues) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int firstVisible = listView.getFirstVisiblePosition();

                for (int childIndex = 0; childIndex < listView.getChildCount(); childIndex++) {
                    int intensityIndex = firstVisible + childIndex;

                    // This can happen if a channel is added to the list view after the start of
                    // this method but before the start of the for loop.
                    if (intensityIndex >= intensityValues.length) {
                        return;
                    }

                    View row = listView.getChildAt(childIndex);
                    TextView intensityView = (TextView)row.findViewById(R.id.channel_intensity_view);
                    int lightIntensity = intensityValues[intensityIndex];
                    intensityView.setText(String.valueOf(lightIntensity));
                }
            }
        });
    }

    public void addChannelWrapper(ChannelWrapper channelWrapper) {
        channelList.addChannelWrapper(channelWrapper);
    }
}
