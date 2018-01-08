package ch.mse.biketracks.utils;

/**
 * Utility class
 * @param <X>
 * @param <Y>
 */
public class Tuple<X, Y> {
    public final X first;
    public final Y second;
    public Tuple(X first, Y second) {
        this.first = first;
        this.second = second;
    }
}
