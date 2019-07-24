package com.fundamentals.notificationjobservice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

// SOS: JobService only works as of API 21. If I need to use this functionality in previous versions,
// I can use WorkManager which works in a similar way.
public class NotificationJobService extends JobService {

    private static final String PRIMARY_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".primary_notification_channel";

    NotificationManager mNotificationManager;

    // SOS: I can either do the job in onStartJob and return false OR do the job in the background,
    // in which case I return true here and the work is continued in that thread (how?). In the latter
    // case, I have to call jobFinished() when the thread is done
    @Override
    public boolean onStartJob(JobParameters params) {
        // SOS: Ignore the notification stuff. The same as in NotifyMe project
        createNotificationChannel();

        PendingIntent intent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder
                (this, PRIMARY_CHANNEL_ID)
                .setContentTitle("Job Service")
                .setContentText("Your Job ran to completion!")
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_job_running)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        mNotificationManager.notify(0, builder.build());

        return false;
    }

    // SOS: this is called when the job is done OR when the conditions are no longer met in which
    // case the job is stopped mid-work. In the latter case, I must return true if I want the job to
    // be re-scheduled instead of just dropped
    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private void createNotificationChannel() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Job Service notification", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifications from Job Service");

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
