package se.miun.ant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChannelList implements ListItemState.ListItemStateListener  {

    public interface ChannelListener {
        public void onChannelSelected(ChannelWrapper channelWrapper, int lightIntensity);
        public void onLightIntensityDataUpdated(int[] intensityValues);
    }

    private ChannelListener channelListener;

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

    public void setChannelListener(ChannelListener listener) {
        channelListener = listener;
    }

    public void addChannelWrapper(ChannelWrapper wrapper) {
        if (wrapper.isChannelAlive()) {
            listItemStates.add(new ListItemState(wrapper, this));
            // Note: we do not call notifyDataSetChanged() here because the ListItemState might
            // not yet have received any intensity data. See onLightIntensityChanged().
        }
    }

    public void validateChannels() {
        Iterator<ListItemState> iterator = listItemStates.iterator();

        while (iterator.hasNext()) {
            ListItemState state = iterator.next();
            if (!state.channelWrapper.isChannelAlive()) {
                iterator.remove();
                notifyDataSetChanged();
            }
        }
    }

    public void closeChannels() {
        for (ListItemState state : listItemStates) {
            state.channelWrapper.closeChannel();
        }
    }

    public void openChannels() {
        for (ListItemState state : listItemStates) {
            state.channelWrapper.openChannel();
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

    @Override
    public void onChannelConnectionClosed(ListItemState listItemState) {
        listItemStates.remove(listItemState);
        notifyDataSetChanged();
    }

    @Override
    public void onLightIntensityChanged(int lightIntensity) {
        channelListener.onLightIntensityDataUpdated(getIntensityValues());
    }

    @Override
    public void onHasReceivedLightIntensityData() {
        notifyDataSetChanged();
    }

    @Override
    public void onChannelButtonClicked(ListItemState listItemState) {
        channelListener.onChannelSelected(listItemState.channelWrapper,
                                          listItemState.lightIntensity);
    }


    private int[] getIntensityValues() {
        int[] intensityValues = new int[listItemStates.size()];

        for (int i = 0; i < listItemStates.size(); i++) {
            intensityValues[i] = listItemStates.get(i).lightIntensity;
        }

        return intensityValues;
    }
}
