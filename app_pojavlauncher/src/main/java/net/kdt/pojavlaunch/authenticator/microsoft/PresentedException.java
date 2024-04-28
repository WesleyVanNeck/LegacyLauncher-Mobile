package net.kdt.pojavlaunch.authenticator.microsoft;

import android.content.Context;

public final class PresentedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int localizationStringId;
    private final Object[] extraArgs;

    public PresentedException(int localizationStringId, Object... extraArgs) {
        super();
        this.localizationStringId = localizationStringId;
        this.extraArgs = extraArgs;
    }

    public PresentedException(Throwable cause, int localizationStringId, Object... extraArgs) {
        super(cause);
        this.localizationStringId = localizationStringId;
        this.extraArgs = extraArgs;
    }

    public String getLocalizedMessage(Context context) {
        return context.getString(localizationStringId, extraArgs);
    }

    @Override
    public String getMessage() {
        return getLocalizedMessage(getContext());
    }

    private Context getContext() {
        Throwable cause = getCause();
        while (cause != null) {
            if (cause instanceof Context) {
                return (Context) cause;
            }
            cause = cause.getCause();
        }
        throw new IllegalStateException("No Context available to get localized message.");
    }
}
