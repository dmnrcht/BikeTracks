package ch.mse.biketracks.utils;

import android.graphics.Color;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * List of high contrasting colors for google map to distinguish tracks from the map
 * The list is not exhaustive and may need some changes
 */
public enum ContrastColor {
    ORANGE(Color.rgb(255, 127, 0)),
    RED_DARK(Color.rgb(145, 15, 15)),
    GREEN(Color.rgb(32, 109, 0)),
    GREEN_LIGHT(Color.rgb(96, 196, 54)),
    CYAN(Color.rgb(22, 142, 116)),
    BLUE_LIGHT(Color.rgb(1, 166, 188)),
    BLUE(Color.rgb(1, 31, 196)),
    PURPLE(Color.rgb(134, 43, 209)),
    PINK(Color.rgb(206, 20, 187)),
    BLACK(Color.rgb(0, 0, 0));

    private static final List<ContrastColor> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private final int color;
    private static final Random RANDOM = new Random();

    ContrastColor(int color) {
        this.color = color;
    }

    public static int randomColor()  {
        return VALUES.get(RANDOM.nextInt(SIZE)).color;
    }
}