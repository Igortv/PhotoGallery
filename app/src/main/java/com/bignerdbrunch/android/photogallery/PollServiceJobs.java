package com.bignerdbrunch.android.photogallery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

public class PollServiceJobs extends JobService{
    private static final String TAG = "PollServiceJobs";
    private static final int JOB_ID = 1;
    private PollTask mCurrentTask;

    public PollServiceJobs(){

    }

    @Override
    public boolean onStartJob(JobParameters params) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return true;
    }

    public static void setServiceSchedule(Context context, boolean isOn) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (isOn) {
            JobInfo jobInfo = new JobInfo.Builder(
                    JOB_ID, new ComponentName(context, PollServiceJobs.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(1000 * 60 * 1)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        } else {
            scheduler.cancel(JOB_ID);
        }

        QueryPreferences.setAlarmOn(context, isOn);
    }
    public static boolean isServiceStarted(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        for (JobInfo jobInfo: scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                return true;
            }
        }
        return false;
    }

    private class PollTask extends AsyncTask<JobParameters,Void,Void> {
        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParams = params[0];

            PollServiceUtils.pollFlickr(PollServiceJobs.this);

            jobFinished(jobParams, false);
            return null;
        }
    }
}
