package se.miun.ant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ChannelListFragment extends ListFragment
        implements ChannelWrapper.ChannelDataListener {

    public interface OnChannelSelectedListener {
        public void onChannelSelected(ChannelWrapper channelWrapper);
    }

    public static final String TAG = "ANTLightController";

    private List<ChannelWrapper> channelWrappers;
    private List<String> channelDisplayStrings;

    private ArrayAdapter<String> channelsAdapter;
    private OnChannelSelectedListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        channelWrappers = new ArrayList<ChannelWrapper>();
        channelDisplayStrings = new ArrayList<String>();

        channelsAdapter = new ArrayAdapter<String>(getActivity(),
                                                   android.R.layout.simple_list_item_1,
                                                   channelDisplayStrings);
        setListAdapter(channelsAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (OnChannelSelectedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnChannelSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView listView, View clickedView, int position, long id) {

    }

    @Override
    public void onChannelWrapperDataReceived(final byte[] data, final ChannelWrapper wrapper) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int listIndex = getListIndexOf(wrapper);
                channelDisplayStrings.set(listIndex, String.valueOf(data[0]));
                channelsAdapter.notifyDataSetChanged();
            }
        });
    }

    public void addChannelWrapper(ChannelWrapper channelWrapper) {
        if (channelWrapper.isChannelAlive()) {

            channelWrappers.add(channelWrapper);
            channelDisplayStrings.add("");

            channelWrapper.setChannelDataListener(this);
        }
    }

    private int getListIndexOf(ChannelWrapper wrapper) {

        for (int index = 0; index < channelWrappers.size(); index++) {
            if (wrapper.equals(channelWrappers.get(index))) {
                return index;
            }
        }

        throw new IllegalArgumentException("The channel wrapper (" + wrapper +
                ") does not exist in the channel wrapper list.");
    }
}





