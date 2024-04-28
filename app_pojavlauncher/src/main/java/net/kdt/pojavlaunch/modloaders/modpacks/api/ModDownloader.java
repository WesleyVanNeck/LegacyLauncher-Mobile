package net.kdt.pojavlaunch.modloaders.modpacks.api;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class for downloading mods asynchronously.
 */
public class ModDownloader {
    private static final int DOWNLOAD_RETRY_COUNT = 5;
    private static final int DOWNLOAD_RETRY_DELAY_MILLIS = 1000;

    private final ExecutorService mDownloadPool;
    private final AtomicBoolean mTerminator;
    private final AtomicLong mDownloadSize;
    private final File mDestinationDirectory;
    private final boolean mUseFileCount;
    private volatile IOException mFirstIOException;
    private volatile long mTotalSize;

    /**
     * Constructs a new ModDownloader instance with the given destination directory.
     *
     * @param destinationDirectory the directory where the downloaded files will be saved
     */
    public ModDownloader(File destinationDirectory) {
        this(destinationDirectory, false);
    }

    /**
     * Constructs a new ModDownloader instance with the given destination directory and file count mode.
     *
     * @param destinationDirectory the directory where the downloaded files will be saved
     * @param useFileCount         whether to use file count mode or not
     */
    public ModDownloader(File destinationDirectory, boolean useFileCount) {
        this.mDownloadPool = Executors.newFixedThreadPool(4);
        this.mTerminator = new AtomicBoolean(false);
        this.mDownloadSize = new AtomicLong(0);
        this.mDestinationDirectory = destinationDirectory;
        this.mUseFileCount = useFileCount;
        if (mUseFileCount) {
            mTotalSize = 0;
        } else {
            mTotalSize = -1;
        }
    }

    /**
     * Submits a download task for the given file.
     *
     * @param fileSize      the size of the file to be downloaded
     * @param relativePath  the relative path of the file in the destination directory
     * @param downloadHash  the SHA-1 hash of the file to be downloaded
     * @param downloadUrls  the URLs of the file to be downloaded
     */
    public void submitDownload(int fileSize, String relativePath, @Nullable String downloadHash, String... downloadUrls) {
        if (mUseFileCount) {
            mTotalSize += 1;
        } else {
            mTotalSize += fileSize;
        }
        for (String downloadUrl : downloadUrls) {
            mDownloadPool.submit(() -> downloadFile(fileSize, relativePath, downloadHash, downloadUrl));
        }
    }

    /**
     * Submits a download task for the given file using a FileInfoProvider.
     *
     * @param infoProvider the FileInfoProvider for the file to be downloaded
     */
    public void submitDownload(FileInfoProvider infoProvider) {
        if (!mUseFileCount) {
            throw new RuntimeException("This method can only be used in a file-counting ModDownloader");
        }
        mTotalSize += 1;
        mDownloadPool.submit(() -> {
            try {
                FileInfo fileInfo = infoProvider.getFileInfo();
                if (fileInfo == null) {
                    return;
                }
                downloadFile(fileInfo.fileSize, fileInfo.relativePath, fileInfo.sha1, fileInfo.url);
            } catch (IOException e) {
                downloadFailed(e);
            }
        });
    }

    /**
     * Waits for all download tasks to complete and updates the progress.
     *
     * @param feedback the feedback object to update the progress
     * @throws IOException if any download task fails
     */
    public void awaitFinish(Tools.DownloaderFeedback feedback) throws IOException {
        try {
            for (Future<?> future : mDownloadPool.shutdownNow()) {
                future.get();
            }
            mDownloadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            if (mTerminator.get()) {
                throw mFirstIOException;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException(e.getCause());
        }
    }

    /**
     * Downloads the file from the given URL and saves it to the destination directory.
     *
     * @param fileSize      the size of the file to be downloaded
     * @param relativePath  the relative path of the file in the destination directory
     * @param downloadHash  the SHA-1 hash of the file to be downloaded
     * @param downloadUrl   the URL of the file to be downloaded
     */
    private void downloadFile(int fileSize, String relativePath, @Nullable String downloadHash, String downloadUrl) {
        for (int i = 0; i < DOWNLOAD_RETRY_COUNT; i++) {
            try {
                File destinationFile = new File(mDestinationDirectory, relativePath);
                if (mUseFileCount) {
                    DownloadUtils.downloadFile(downloadUrl, destinationFile);
                } else {
                    DownloadUtils.downloadFile(downloadUrl, destinationFile, fileSize);
                }
                if (downloadHash != null) {
                    DownloadUtils.ensureSha1(destinationFile, downloadHash);
                }
                mDownloadSize.addAndGet(fileSize);
                return;
            } catch (IOException e) {
                if (i == DOWNLOAD_RETRY_COUNT - 1) {
                    downloadFailed(e);
                } else {
                    try {
                        TimeUnit.MILLISECONDS.sleep(DOWNLOAD_RETRY_DELAY_MILLIS);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    /**
     * Sets the first IO exception that occurred during download.
     *
     * @param exception the first IO exception that occurred during download
     */
    private void downloadFailed(IOException exception) {
        synchronized (this) {
            if (mFirstIOException == null) {
                mFirstIOException = exception;
            }
        }
        mTerminator.set(true);
    }

    /**
     * A class representing the file information needed for downloading.
     */
    public static class FileInfo {
        public final int fileSize;
        public final String relativePath;
        public final String sha1;
        public final String url;

        /**
         * Constructs a new FileInfo instance with the given parameters.
         *
         * @param fileSize  the size of the file
         * @param relativePath  the relative path of the file in the destination directory
         * @param sha1  the SHA-1 hash of the file
         * @param url  the URL of the file
         */
        public FileInfo(int fileSize, String relativePath, String sha1, String url) {
            this.fileSize = fileSize;
            this.relativePath = relativePath;
            this.sha1 = sha1;
            this.url = url;
        }
    }

    /**
     * A functional interface for providing file information.
     */
    @FunctionalInterface
    public interface FileInfoProvider {
        FileInfo getFileInfo() throws IOException;
    }
}
