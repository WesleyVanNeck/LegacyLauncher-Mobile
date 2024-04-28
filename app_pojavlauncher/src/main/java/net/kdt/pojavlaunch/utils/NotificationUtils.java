package net.kdt.pojavlaunch.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import net.kdt.pojavlaunch.R;

public class NotificationUtils {

    public static final int NOTIFICATION_ID_PROGRESS_SERVICE = 1;
    public static final int NOTIFICATION_ID_GAME_SERVICE = 2;
    public static final int NOTIFICATION_ID_DOWNLOAD_LISTENER = 3;
    public static final int NOTIFICATION_ID_SHOW_ERROR = 4;
    public static final int NOTIFICATION_ID_GAME_START = 5;
    public static final int NOTIFICATION_ID_GAME_PAUSED = 6;
    public static final int NOTIFICATION_ID_GAME_RESUMED = 7;
    public static final int NOTIFICATION_ID_GAME_STOPPED = 8;

    public static final int PENDINGINTENT_CODE_KILL_PROGRESS_SERVICE = 1;
    public static final int PENDINGINTENT_CODE_KILL_GAME_SERVICE = 2;
    public static final int PENDINGINTENT_CODE_DOWNLOAD_SERVICE = 3;
    public static final int PENDINGINTENT_CODE_SHOW_ERROR = 4;
    public static final int PENDINGINTENT_CODE_GAME_START = 5;
    public static final int PENDINGINTENT_CODE_GAME_PAUSED = 6;
    public static final int PENDINGINTENT_CODE_GAME_RESUMED = 7;
    public static final int PENDINGINTENT_CODE_GAME_STOPPED = 8;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(@NonNull Context context) {
        String channelId = context.getString(R.string.notif_channel_id);
        String channelName = context.getString(R.string.notif_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static void sendBasicNotification(@NonNull Context context, int titleResId, int messageResId, Intent actionIntent,
                                             int pendingIntentCode, int notificationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);
        }

        String title = titleResId == -1 ? "" : context.getString(titleResId);
        String message = messageResId == -1 ? "" : context.getString(messageResId);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentCode, actionIntent,
                Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, context.getString(R.string.notif_channel_id));
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        if (actionIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent);
        }
        notificationBuilder.setSmallIcon(R.drawable.notif_icon);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
