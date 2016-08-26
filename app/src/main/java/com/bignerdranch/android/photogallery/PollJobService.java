package com.bignerdranch.android.photogallery;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by sam on 16/8/26.
 */
public class PollJobService extends JobService {

    private static final String TAG = "PollJobService";
    private PollTask mTask;
    private static final int JOB_ID = 1;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mTask = new PollTask(this);
        mTask.execute(jobParameters);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mTask != null) {
            mTask.cancel(true);
        }
        return false;
    }

    public static void setJobServiceSchedule(Context context, boolean isOn) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (isOn) {
            JobInfo info = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(1000 * 60)
                    .build();

            scheduler.schedule(info);
        } else {
            scheduler.cancel(JOB_ID);
        }
    }

    public static boolean isJobScheduled(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;
        for (JobInfo info : scheduler.getAllPendingJobs()) {
            if (info.getId() == JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {
        private Context mContext;

        public PollTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParam = params[0];

            if (!isNetworkAvailableAndConnected()) {
                Log.d(TAG, "Network is not connected.");
                jobFinished(jobParam, false);
                return null;
            }

            String query = QueryPreferences.getStoredQuery(mContext);
            String lastResultId = QueryPreferences.getLastResultId(mContext);
            List<GalleryItem> items;

            if (query == null) {
                items = new FlickrFetchr().fetchRecentPhotos();
            } else {
                items = new FlickrFetchr().searchPhotos(query);
            }

            if (items.size() == 0) {
                jobFinished(jobParam, false);
                return null;
            }

            String resultId = items.get(0).getId();
            if (resultId.equals(lastResultId)) {
                Log.d(TAG, "Got an old result: " + resultId);
            } else {
                Log.d(TAG, "Got a new result: " + resultId);

                Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(mContext);
                PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);

                Notification notification = new NotificationCompat.Builder(mContext)
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                notificationManager.notify(0, notification);
            }

            QueryPreferences.setLastResultId(mContext, resultId);

            jobFinished(jobParam, false);
            return null;
        }
    }
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isAvailable = cm.getActiveNetworkInfo() != null;
        boolean isConnected = isAvailable && cm.getActiveNetworkInfo().isConnected();

        return isConnected;
    }
}
