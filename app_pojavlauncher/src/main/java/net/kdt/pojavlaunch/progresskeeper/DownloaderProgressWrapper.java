package net.kdt.pojavlaunch.progresskeeper;

import static net.kdt.pojavlaunch.Tools.BYTE_TO_MB;

import net.kdt.pojavlaunch.Tools;

public class DownloaderProgressWrapper implements Tools.DownloaderFeedback {

    private final int mProgressString;
    private final String mProgressRecord;
    public String extraString = null;

    public static final long BYTE_TO_MB = 1024 * 1024;

    /**
     * A simple wrapper to send the downloader progress to ProgressKeeper
     * @param progressString the string that will be used in the progress reporter
     * @param progressRecord the record for ProgressKeeper
     */
    public DownloaderProgressWrapper(int progressString, String progressRecord) {
        this(progressString, progressRecord, null);
    }

    public DownloaderProgressWrapper(int progressString, String progressRecord, String extraString) {
        this.mProgressString = progressString;
        this.mProgressRecord = progressRecord;
        this.extraString = extraString;
    }

    @Override
    public void updateProgress(long curr, long max) {
        Object[] va;
        if (extraString != null) {
            va = new Object[3];
            va[0] = extraString;
            va[1] = curr / BYTE_TO_MB;
            va[2] = max / BYTE_TO_MB;
        } else {
            va = new Object[2];
            va[0] = curr / BYTE_TO_MB;
            va[1] = max / BYTE_TO_MB;
        }

        String message = java.text.MessageFormat.format(
                "{0} - {1}/{2} MB",
                mProgressRecord,
                va[0],
                va[1]);

        ProgressKeeper.submitProgress(mProgressString, (int) Math.max((float) curr / max * 100, 0), message);
    }
}
