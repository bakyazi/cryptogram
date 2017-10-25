package com.pixplicity.cryptogram.services;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.Logger;

public class CryptogramJobService extends JobService {

    public static final String TAG_PERIODIC_DOWNLOAD = "periodic-download";

    @Override
    public boolean onStartJob(JobParameters job) {
        Logger.d("job", "executing job!");
        switch (job.getTag()) {
            case TAG_PERIODIC_DOWNLOAD: {
                onPeriodicDownload();
            }
            break;
        }

        // Is there still work going on?
        return false;
    }

    protected void onPeriodicDownload() {
        Notification notification = new NotificationCompat.Builder(this, "testing")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Job executed")
                .build();
        NotificationManagerCompat.from(this).notify(1001, notification);
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        // Should this job be retried?
        return false;
    }

}
