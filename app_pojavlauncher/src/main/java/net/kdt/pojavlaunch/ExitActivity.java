package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Tools.shareLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

@Keep
public class ExitActivity extends AppCompatActivity {

    @SuppressLint("StringFormatInvalid") //invalid on some translations but valid on most, cant fix that atm
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int code = getIntent().getIntExtra("code", -1);

        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.mcn_exit_title, code))
                .setPositiveButton(R.string.main_share_logs, (dialog, which) -> shareLog(this))
                .setOnDismissListener(dialog -> finish())
                .show();
    }

    public static void showExitMessage(Context ctx, int code) {
        Intent i = new Intent(ctx, ExitActivity.class);
        i.putExtra("code", code);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }
}
