package net.kdt.pojavlaunch.modloaders.modpacks.imagecache;

import android.graphics.Bitmap;

/**
 * A class for caching images used by mod icons.
 */
public class ModIconCache {

    /**
     * An interface for receiving images as they become available.
     *
     * @param <T> the type of the image receiver
     */
    public interface ImageReceiver<T> {
        /**
         * Called when the image becomes available.
         *
         * @param image the image that has become available
         */
        void onImageAvailable(Bitmap image);
    }
}


ModIconCache.ImageReceiver<MyImageReceiver> receiver = new ModIconCache.ImageReceiver<MyImageReceiver>() {
    @Override
    public void onImageAvailable(Bitmap image) {
        if (image != null) {
            // Do something with the image
        }
    }
};
