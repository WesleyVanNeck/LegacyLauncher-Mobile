package net.kdt.pojavlaunch.modloaders.modpacks.imagecache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class IconCacheJanitor implements Runnable {

    // Constants
    private static final long CACHE_SIZE_LIMIT = 100 * 1024 * 1024; // 100 MB
    private static final long CACHE_BRINGDOWN = 50 * 1024 * 1024; // 50 MB
    private static final int MIN_FILES_TO_CLEAN = 10; // Minimum number of files to clean up

    // Singleton instance
    private static IconCacheJanitor instance;

    // Private constructor
    private IconCacheJanitor() {
    }

    // Get the singleton instance
    public static IconCacheJanitor getInstance() {
        if (instance == null) {
            instance = new IconCacheJanitor();
        }
        return instance;
    }

    @Override
    public void run() {
        Path modIconCachePath = ModIconCache.getImageCachePath();
        if (!Files.isDirectory(modIconCachePath) || !Files.isReadable(modIconCachePath)) {
            return;
        }

        List<Path> modIconFiles = new ArrayList<>();
        AtomicLong directoryFileSize = new AtomicLong(0);

        try {
            Files.list(modIconCachePath).forEach(path -> {
                if (!Files.isRegularFile(path) || !Files.isReadable(path) || !Files.isWritable(path)) {
                    return;
                }
                directoryFileSize.addAndGet(Files.size(path));
                modIconFiles.add(path);
            });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (directoryFileSize.get() < CACHE_SIZE_LIMIT) {
            System.out.println("Skipping cleanup because there's not enough to clean up");
            return;
        }

        modIconFiles.sort((x, y) -> {
            try {
                return Files.getLastModifiedTime(y).compareTo(Files.getLastModifiedTime(x));
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        });

        int filesCleanedUp = 0;
        for (Path modFile : modIconFiles) {
            if (directoryFileSize.get() < CACHE_BRINGDOWN) {
                break;
            }
            try {
                if (Files.deleteIfExists(modFile)) {
                    directoryFileSize.addAndGet(-Files.size(modFile));
                    filesCleanedUp++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Cleaned up " + filesCleanedUp + " files");
    }

    /**
     * Runs the janitor task, unless there was one running already or one has ran already
     */
    public void runJanitor() {
        if (sJanitorFuture != null || sJanitorRan) {
            return;
        }

        sJanitorFuture = PojavApplication.sExecutorService.submit(() -> {
            getInstance().run();
            sJanitorFuture = null;
            sJanitorRan = true;
        });
    }

    /**
     * Waits for the janitor task to finish, if there is one running already
     * Note that the thread waiting must not be interrupted.
     */
    public void waitForJanitorToFinish() {
        if (sJanitorFuture == null) {
            return;
        }

        try {
            sJanitorFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Should not happen!", e);
        }
    }
}
