package anomaly.experiment.controller.utils;

import java.util.concurrent.TimeUnit;

public class Utils {

    /**
     * Convert a duration value to milliseconds.
     *
     * @param duration  Numerical value representing the duration.
     * @param dimension Character represents the dimension of the duration. Can be [s]econds, [m]inutes,
     *                  [h]ours or [d]ays.
     * @return Duration value in milliseconds.
     */
    public static long getDurationInMS(long duration, char dimension) {
        TimeUnit t;
        switch (dimension) {
            case 's':
                t = TimeUnit.SECONDS;
                break;
            case 'm':
                t = TimeUnit.MINUTES;
                break;
            case 'h':
                t = TimeUnit.HOURS;
                break;
            case 'd':
                t = TimeUnit.DAYS;
                break;
            default:
                throw new IllegalArgumentException(String.format("Character %c is not a valid dimension.", dimension));
        }
        if (duration <= 0) {
            throw new IllegalArgumentException(String.format("Duration must be positive > 0, " +
                    "but was set to %d", duration));
        }

        return t.toMillis(duration);
    }
}
