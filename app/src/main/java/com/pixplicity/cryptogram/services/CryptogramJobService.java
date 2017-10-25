package com.pixplicity.cryptogram.services;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.pixplicity.cryptogram.utils.Logger;

public class CryptogramJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        Logger.d("job", "executing job!");

        // Is there still work going on?
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        // Should this job be retried?
        return false;
    }

}