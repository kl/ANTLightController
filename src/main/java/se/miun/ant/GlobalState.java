package se.miun.ant;


import android.content.Context;

public class GlobalState {

    public static final String LOG_TAG = "ANTLightController";

    public static void setApplicationContext(Context context) {
        if (applicationContext == null) applicationContext = context;
    }

    // TODO: should be private. Only public for debugging.
    public static Context applicationContext;

    private static GlobalState instance;

    private ChannelRetriever channelRetriever;
    private ChannelList channelList;

    public ChannelRetriever getChannelRetriever() {
        return channelRetriever;
    }

    public ChannelList getChannelList() {
        return channelList;
    }

    public static GlobalState getInstance() {
        if (instance == null) instance = new GlobalState();
        return instance;
    }

    private GlobalState() {
        channelRetriever = new ChannelRetriever(applicationContext);
        channelList = new ChannelList(applicationContext);
    }

    public void closeChannels() {
        channelList.closeChannels();
    }

    public void openChannels() {
        channelList.openChannels();
    }
}
