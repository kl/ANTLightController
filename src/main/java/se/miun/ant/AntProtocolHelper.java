package se.miun.ant;

/**
 * A class that knows how to decode and encode the proper ANT messages.
 */
public class AntProtocolHelper {

    /** Thrown if the master has set the volume to VOLUME_VALUE_UNKNOWN */
    public static class VolumeValueUnknownException extends Exception {}

    private static final int AUDIO_COMMAND_DATA_PAGE = 16;
    private static final int INTENSITY_COMMAND_NUMBER = 99;

    private static final int AUDIO_UPDATE_DATA_PAGE = 1;
    private static final int VOLUME_VALUE_UNKNOWN = 255;

    /**
     * Makes the 8 byte sized byte array that is used for the data portion when sending an ANT
     * message. Currently, the audio command data page is used, with a custom command number
     * (not specified in the ANT+ profile) used for representing the "volume" as a value between 1 and 100.
     * @param toVolumePercent the volume percent to send.
     * @return the data byte array.
     */
    public static byte[] makeIntensityPayload(int toVolumePercent) {

        return new byte[] {
            AUDIO_COMMAND_DATA_PAGE,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)toVolumePercent,
            INTENSITY_COMMAND_NUMBER
        };
    }

    /**
     * Checks if the parameter is an ANT+ audio update data payload.
     * @param payload the byte array to check.
     * @return true or false.
     */
    public static boolean isAudioUpdatePayload(byte[] payload) {
        return (payload != null && payload[0] == AUDIO_UPDATE_DATA_PAGE);
    }

    /**
     * Used to decode the volume value from an ANT+ audio update data payload.
     * @param payload the byte array data payload.
     * @return the volume value (between 0 and 100).
     * @throws VolumeValueUnknownException if the master has set the volume to VOLUME_VALUE_UNKNOWN.
     */
    public static int decodeVolumeValue(byte[] payload) throws VolumeValueUnknownException {
        byte value = payload[1];

        if (value == VOLUME_VALUE_UNKNOWN) {
            throw new VolumeValueUnknownException();
        } else {
            return value;
        }
    }
}
