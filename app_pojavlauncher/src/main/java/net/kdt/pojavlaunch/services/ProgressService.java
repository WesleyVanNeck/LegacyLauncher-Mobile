package net.kdt.pojavlaunch.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.utils.NotificationUtils;

public class ProgressService extends Service implements TaskCountListener {

    private static final String TAG = "ProgressService";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_id";

    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder mNotificationBuilder;

    /** Simple wrapper to start the service */
    public static void startService(Context context){
        Intent intent = new Intent(context, ProgressService.class);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        Intent killIntent = new Intent(getApplicationContext(), ProgressService.class);
        killIntent.putExtra("kill", true);
        PendingIntent pendingKillIntent = PendingIntent.getService(this, NotificationUtils.PENDINGINTENT_CODE_KILL_PROGRESS_SERVICE
                , killIntent, Build.VERSION.SDK_INT >=23 ? PendingIntent.FLAG_IMMUTABLE : 0);

        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.lazy_service_default_title))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,  getString(R.string.notification_terminate), pendingKillIntent)
                .setSmallIcon(R.drawable.notif_icon)
                .setNotificationSilent();

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getBooleanExtra("kill", false)) {
            stopSelf(); // otherwise Android tries to restart the service since it "crashed"
            Process.killProcess(Process.myPid());
            return START_NOT_STICKY;
        }

        Log.d(TAG, "Started!");
        updateNotification(ProgressKeeper.getTaskCount());

        if(ProgressKeeper.getTaskCount() < 1) {
            stopSelf();
        } else {
            ProgressKeeper.addTaskCountListener(this, false);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        ProgressKeeper.removeTaskCountListener(this);
        super.onDestroy();
    }

    @Override
    public void onUpdateTaskCount(int taskCount) {
        updateNotification(taskCount);
    }

    private void updateNotification(int taskCount) {
        Tools.runOnMainThread(() -> {
            if(taskCount > 0) {
                mNotificationBuilder.setContentText(getString(R.string.progresslayout_tasks_in_progress, taskCount));
                notificationManagerCompat.notify(NOTIFICATION_ID, mNotificationBuilder.build());
            } else {
                stopSelf();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Progress Service",
                NotificationManager.IMPORTANCE_LOW);

        channel.setDescription("Shows progress of tasks");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
