package demo.aesher.dsc.newsfetch;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mapzen.speakerbox.Speakerbox;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import demo.aesher.dsc.newsfetch.Const.Constants;
import demo.aesher.dsc.newsfetch.Const.LocalData;
import demo.aesher.dsc.newsfetch.Const.NotificationScheduler;
import demo.aesher.dsc.newsfetch.Interfaces.RecyclerNewsClick;
import demo.aesher.dsc.newsfetch.Thread.FetchNews;
import demo.aesher.dsc.newsfetch.Interfaces.AsyncNews;
import demo.aesher.dsc.newsfetch.Thread.NewsModel;
import demo.aesher.dsc.newsfetch.Thread.NewsRecycler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity" ;
    LocalData localData;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.nightView)
    ImageView NightViewToggle;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.searchBox)
    EditText searchBox;


    @OnClick(R.id.notification)
    public void showNotificationHandlerDialogue(){
        showTimePickerDialog(localData.get_hour(), localData.get_min());
    }

    //Slide Panel Components
    @BindView(R.id.heading)
    TextView heading;
    @BindView(R.id.newsImage)
    ImageView newsImage;
    @BindView(R.id.newsContent)
    TextView newsContent;
    @BindView(R.id.speak)
    RelativeLayout speak;
    @BindView(R.id.share)
    RelativeLayout share;
    @BindView(R.id.openBrowser)
    RelativeLayout openBrowser;


    NewsRecycler newsRecycler;
    Speakerbox speakerbox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        localData= new LocalData(this);

        if (localData.get_darkUI())
            this.setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        speakerbox = new Speakerbox(getApplication());

        fetchNews(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNews(true);
            }
        });

        NightViewToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localData.set_darkUI();
                startActivity(new Intent(MainActivity.this,MainActivity.class));

            }
        });


    }


    public String createRequestUrl(Boolean calledByRefresh){
        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String param = tm.getNetworkCountryIso();
        final String CONSUMER_KEY = "b7e520bda6fa4f989ce5906cb156a0ec";
        String normalQueryUrl= "https://newsapi.org/v2/top-headlines?country="+param+"&apiKey="+ CONSUMER_KEY;

       localData.set_queryURL(normalQueryUrl);

        if(calledByRefresh)
            return normalQueryUrl;

        String restoredText = localData.get_body();

        return (restoredText==null)? normalQueryUrl : restoredText;

    }

    private void  fetchNews(boolean calledByRefresh){
        new FetchNews(new AsyncNews() {
            @Override
            public void processFinished(List<NewsModel> s, String body) {
                swipeRefreshLayout.setRefreshing(false);

                if(s.size()!=0)
                    localData.set_body(body);


                recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setNestedScrollingEnabled(true);
                newsRecycler = new NewsRecycler(s);
                recyclerView.setAdapter(newsRecycler);

                newsRecycler.setNewsClickListener(new RecyclerNewsClick() {
                    @Override
                    public void newsClicked(NewsModel newsModel) {
                        setNews(newsModel);
                    }
                });

                searchBox.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                            newsRecycler.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });



                Toast.makeText(getApplicationContext(),"Finished Fetching", Toast.LENGTH_SHORT).show();
            }
        }).execute(createRequestUrl(calledByRefresh));
    }

    public void setNews(final NewsModel news){
        heading.setText(news.getTitle());
        Picasso.get().load(news.getUrlToImage()).placeholder(R.mipmap.ic_launcher).into(newsImage);
        newsContent.setText(news.getContent());

        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakerbox.play(news.getContent());
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
                share.putExtra(Intent.EXTRA_TEXT, news.getUrl());

                startActivity(Intent.createChooser(share, "Share the amazing news with your friends"));
            }
        });

        openBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(news.getUrl()));
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else
            super.onBackPressed();
    }


    private void showTimePickerDialog(int h, int m) {

        final View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_timepicker, (ViewGroup) findViewById(android.R.id.content), false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Set Alarm", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
                localData.set_hour(timePicker.getHour());
                localData.set_min(timePicker.getMinute());
                NotificationScheduler.setReminder(MainActivity.this, NewsService.class, localData.get_hour(), localData.get_min());
                Toast.makeText(getApplicationContext(), "Your notification has been scheduled for the given time", Toast.LENGTH_LONG).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();





    }
}
