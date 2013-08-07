package se.miun.ant;


import android.content.Context;

public class GlobalState {

    public static final String LOG_TAG = "ANTLightController";

    public static void setApplicationContext(Context context) {
        applicationContext = context;
    }

    private static Context applicationContext;

    private static GlobalState instance;

    private ChannelList channelList;

    public ChannelList getChannelList() {
        return channelList;
    }

    public static GlobalState getInstance() {
        if (instance == null) instance = new GlobalState();
        return instance;
    }

    public GlobalState() {
        channelList = new ChannelList(applicationContext);
    }
}
