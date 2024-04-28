package net.kdt.pojavlaunch;

import android.annotation.TargetApi;
import android.content.*;
import android.os.*;
import androidx.appcompat.app.*;
import android.util.*;
import androidx.appcompat.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.res.Resources;
import java.io.Serializable;

public class FatalErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        boolean storageAllow = extras != null && extras.getBoolean("storageAllow", false);
        String stackTrace = null;
        String strSavePath = extras != null ? extras.getString("savePath") : "";

        if (extras != null && extras.getSerializable("throwable") != null) {
            Throwable th = (Throwable) extras.getSerializable("throwable");
            stackTrace = Log.getStackTraceString(th);
        }

        String errHeader = storageAllow ?
                String.format("Crash stack trace saved to %s.", strSavePath) :
                "Storage permission is required to save crash stack trace!";

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.error_fatal))
                .setMessage(errHeader + "\n\n" + (stackTrace != null ? stackTrace : ""))
                .setPositiveButton(android.R.string.ok, (p1, p2) -> finish())
                .setNegativeButton(R.string.global_restart, (p1, p2) -> startActivity(new Intent(FatalErrorActivity.this, LauncherActivity.class)))
                .setNeutralButton(android.R.string.copy, (p1, p2) -> {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        return;
                    }

                    ClipboardManager mgr = (ClipboardManager) FatalErrorActivity.this.getSystemService(CLIPBOARD_SERVICE);
                    if (mgr == null) {
                        return;
                    }

                    try (ClipData clip = ClipData.newPlainText("error", stackTrace)) {
                        mgr.setPrimaryClip(clip);
                    }

                    finish();
                })
                .setCancelable(false)
                .show();
    }

    public static void showError(Context ctx, String savePath, boolean storageAllow, Throwable th) {
        Intent fatalErrorIntent = new Intent(ctx, FatalErrorActivity.class);
        fatalErrorIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        fatalErrorIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (th != null) {
            fatalErrorIntent.putExtra("throwable", th);
        }
        if (savePath != null) {
            fatalErrorIntent.putExtra("savePath", savePath);
        }
        fatalErrorIntent.putExtra("storageAllow", storageAllow);
        ctx.startActivity(fatalErrorIntent);
    }
}
