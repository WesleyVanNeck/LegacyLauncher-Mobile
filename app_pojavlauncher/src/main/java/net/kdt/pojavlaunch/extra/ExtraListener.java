package net.kdt.pojavlaunch.extra;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.BiConsumer;

/**
 * Listener interface for the ExtraCore.
 * Allows listening to a virtually unlimited amount of values.
 */
public interface ExtraListener<T> {

    /**
     * Called when a new value is set for the given key.
     *
     * @param key   The name of the value.
     * @param value The new value as an object.
     * @return Whether the listener consumed the event (stopped listening).
     */
    boolean onValueSet(String key, @NonNull T value);

    /**
     * Convenience method to create a new ExtraListener instance that simply calls the given
     * BiConsumer when a new value is set.
     *
     * @param consumer The BiConsumer to call when a new value is set.
     * @param <V>      The type of the value.
     * @return A new ExtraListener instance.
     */
    static <V> ExtraListener<V> fromBiConsumer(BiConsumer<String, V> consumer) {
        return new ExtraListener<V>() {
            @Override
            public boolean onValueSet(String key, @NonNull V value) {
                consumer.accept(key, value);
                return false;
            }
        };
    }

    /**
     * Convenience method to create a new ExtraListener instance that simply calls the given
     * method when a new value is set.
     *
     * @param methodName The name of the method to call when a new value is set.
     * @param obj        The object to call the method on.
     * @param <V>        The type of the value.
     * @return A new ExtraListener instance.
     */
    static <V> ExtraListener<V> fromMethod(String methodName, Object obj) {
        return new ExtraListener<V>() {
            @Override
            public boolean onValueSet(String key, @NonNull V value) {
                try {
                    obj.getClass().getMethod(methodName, String.class, V.class).invoke(obj, key, value);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke method " + methodName, e);
                }
                return false;
            }
        };
    }

    /**
     * Convenience method to create a new ExtraListener instance that simply calls the given
     * method when a new value is set, and returns the result of the method as the consumed value.
     *
     * @param methodName The name of the method to call when a new value is set.
     * @param obj        The object to call the method on.
     * @param <V>        The type of the value.
     * @param <R>        The type of the result.
     * @return A new ExtraListener instance.
     */
    static <V, R> ExtraListener<V> fromConsumingMethod(String methodName, Object obj) {
        return new ExtraListener<V>() {
            @Override
            public boolean onValueSet(String key, @NonNull V value) {
                try {
                    R result = (R) obj.getClass().getMethod(methodName, String.class, V.class).invoke(obj, key, value);
                    return true; // consumed
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke method " + methodName, e);
                }
            }
        };
    }
}
