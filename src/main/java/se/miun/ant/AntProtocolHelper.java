package se.miun.ant;

public class AntProtocolHelper {

    public static class VolumeValueUnknownException extends Exception {}

    private static final int AUDIO_COMMAND_DATA_PAGE = 16;
    private static final int INTENSITY_COMMAND_NUMBER = 99;

    private static final int AUDIO_UPDATE_DATA_PAGE = 1;
    private static final int VOLUME_VALUE_UNKNOWN = 255;

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

    public static boolean isAudioUpdatePayload(byte[] payload) {
        return (payload != null && payload[0] == AUDIO_UPDATE_DATA_PAGE);
    }

    public static int decodeVolumeValue(byte[] payload) throws VolumeValueUnknownException {
        byte value = payload[1];

        if (value == VOLUME_VALUE_UNKNOWN) {
            throw new VolumeValueUnknownException();
        } else {
            return value;
        }
    }
}

