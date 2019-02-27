package demo.aesher.dsc.newsfetch;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import demo.aesher.dsc.newsfetch.Const.Constants;
import demo.aesher.dsc.newsfetch.Const.LocalData;
import demo.aesher.dsc.newsfetch.Const.NotificationScheduler;
import demo.aesher.dsc.newsfetch.Interfaces.AsyncNews;
import demo.aesher.dsc.newsfetch.Thread.FetchNews;
import demo.aesher.dsc.newsfetch.Thread.NewsModel;


public class NewsService extends BroadcastReceiver {
    LocalData localData;


    @Override
    public void onReceive(final Context context, Intent intent) {
        localData = new LocalData(context);
        if (intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                NotificationScheduler.setReminder(context, NewsService.class,
                        localData.get_hour(), localData.get_min());
                return;
            }
        }

        new FetchNews(new AsyncNews() {
            @Override
            public void processFinished(List<NewsModel> s, String body) {
                if(s.size()!=0)
                    localData.set_body(body);
                NotificationScheduler.showNotification(context, s.get(0).getUrlToImage(),
                        s.get(0).getTitle(), s.get(0).getDesc());


            }
        }).execute(localData.get_queryURL());


    }
}
