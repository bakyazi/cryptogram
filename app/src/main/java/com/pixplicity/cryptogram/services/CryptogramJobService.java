package com.pixplicity.cryptogram.services;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.pixplicity.cryptogram.utils.Logger;

public class CryptogramJobService extends JobService {

    public static final String TAG_PERIODIC_DOWNLOAD = "periodic-download";

    @Override
    public boolean onStartJob(JobParameters job) {
        switch (job.getTag()) {
            case TAG_PERIODIC_DOWNLOAD: {
                onPeriodicDownload(job);
            }
            break;
            default: {
                Logger.w("job", "unknown job " + job.getTag());
                jobFinished(job, false);
            }
            break;
        }

        // Is there still work going on?
        return false;
    }

    protected void onPeriodicDownload(JobParameters job) {
        // TODO
        jobFinished(job, false);
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        // Should this job be retried?
        return false;
    }

}
