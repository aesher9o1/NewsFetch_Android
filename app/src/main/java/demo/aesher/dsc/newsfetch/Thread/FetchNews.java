package demo.aesher.dsc.newsfetch.Thread;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import demo.aesher.dsc.newsfetch.Const.Constants;
import demo.aesher.dsc.newsfetch.Interfaces.AsyncNews;
import demo.aesher.dsc.newsfetch.MainActivity;


public  class FetchNews extends AsyncTask<String, Void, String> {

    private AsyncNews asyncNews;
    private List<NewsModel> locationModels= new ArrayList<>();
    private String body="";

    private String TAG = "FetchNewsWarns";

    public FetchNews(AsyncNews asyncNews){
        this.asyncNews = asyncNews; }


    @Override
    protected String doInBackground(String... strings) {
        try{
                if(URLUtil.isValidUrl(strings[0]))
                     body = Jsoup.connect(strings[0]).ignoreContentType(true).execute().body();
                else if(Constants.isJSONValid(strings[0]))
                    body = strings[0];


                JSONObject jsonObject = new JSONObject(body);
                JSONArray jsonArray = jsonObject.getJSONArray("articles");

                 Log.w(TAG, jsonArray.length()+"");
                 for(int i =0; i<jsonArray.length();i++){

                     String source;
                     String title;
                     String desc;
                     String url;
                     String urlToImage;
                     String content;
                     Date publishedAt= null;

                     JSONObject jsonObject2 = new JSONObject(String.valueOf(jsonArray.get(i)));
                     JSONObject sourceObj = new JSONObject(jsonObject2.get("source").toString());



                     source = sourceObj.getString("name");
                     title = jsonObject2.getString("title");
                     desc = jsonObject2.getString("description");
                     url = jsonObject2.get("url").toString();
                     urlToImage = jsonObject2.get("urlToImage").toString();
                     content = jsonObject2.getString("content");

                     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss",Locale.US);
                     try {
                         publishedAt = formatter.parse(jsonObject2.getString("publishedAt"));
                     } catch (ParseException e) {
                         Log.w(TAG, e.toString());
                     }

                     NewsModel locationModel = new NewsModel(source,title,desc,url,urlToImage,content,publishedAt);
                     locationModels.add(locationModel);


          }

        }
        catch (Exception e){
            Log.w(TAG,e.toString());
        }
        return body;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        asyncNews.processFinished(locationModels,body);
    }

}
