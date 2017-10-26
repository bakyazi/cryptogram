package com.pixplicity.cryptogram.services;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.utils.Logger;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        CryptogramApp.getInstance().getApiService().getFree().enqueue(new Callback<Map<String, Topic>>() {
            @Override
            public void onResponse(Call<Map<String, Topic>> call, Response<Map<String, Topic>> response) {
                Logger.d("api", "response: HTTP " + response.code());
                ResponseBody body = response.raw().body();
                Logger.d("api", "response: " + body.contentLength() + " bytes");
                jobFinished(job, false);
            }

            @Override
            public void onFailure(Call<Map<String, Topic>> call, Throwable t) {
                Logger.d("api", "failed", t);
                jobFinished(job, true);
            }
        });
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        // Should this job be retried?
        return false;
    }

}
