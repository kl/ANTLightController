package se.miun.ant;

public class AntProtocolHelper {

    public static class VolumeValueUnknownException extends Exception {}

    private static final int AUDIO_COMMAND_DATA_PAGE = 16;
    private static final int INCREASE_VOLUME_COMMAND = 4;
    private static final int DECREASE_VOLUME_COMMAND = 5;

    private static final int AUDIO_UPDATE_DATA_PAGE = 1;
    private static final int VOLUME_VALUE_UNKNOWN = 255;

    public static byte[] makeVolumePayload(int fromVolumePercent, int toVolumePercent) {

        byte roundedDifferencePercent = getRoundedDifferencePercent(fromVolumePercent,
                                                                    toVolumePercent);

        boolean isIncrement = toVolumePercent >= fromVolumePercent;

        byte commandNumber = (byte)((isIncrement) ? INCREASE_VOLUME_COMMAND : DECREASE_VOLUME_COMMAND);


        return new byte[] {
            AUDIO_COMMAND_DATA_PAGE,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            (byte)0xFF,
            roundedDifferencePercent,
            commandNumber
        };
    }

    public static boolean isAudioUpdatePayload(byte[] payload) {
        return payload[0] == AUDIO_UPDATE_DATA_PAGE;
    }

    public static int decodeVolumeValue(byte[] payload) throws VolumeValueUnknownException {
        byte value = payload[1];

        if (value == VOLUME_VALUE_UNKNOWN) {
            throw new VolumeValueUnknownException();
        } else {
            return value;
        }
    }

    // Helpers

    private static byte getRoundedDifferencePercent(int from, int to) {
        int larger = Math.max(from, to);
        int smaller = Math.min(from, to);
        int difference = larger - smaller;
        float differencePercent = ((float)difference / (float)larger) * 100;
        return (byte)Math.round(differencePercent);
    }
}

