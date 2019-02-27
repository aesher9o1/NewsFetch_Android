package demo.aesher.dsc.newsfetch.Const;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalData {
    private static final String APP_SHARED_PREFS = Constants.SharedPreferenceName;

    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    private static final String reminderStatus="reminderStatus";
    private static final String hour="hour";
    private static final String min="min";

    public LocalData(Context context)
    {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }


    public boolean getReminderStatus()
    {
        return appSharedPrefs.getBoolean(reminderStatus, false);
    }

    public void setReminderStatus(boolean status)
    {
        prefsEditor.putBoolean(reminderStatus, status);
        prefsEditor.commit();
    }


    public int get_hour()
    {
        return appSharedPrefs.getInt(hour, 20);
    }

    public void set_hour(int h)
    {
        prefsEditor.putInt(hour, h);
        prefsEditor.commit();
    }


    public int get_min()
    {
        return appSharedPrefs.getInt(min, 0);
    }

    public void set_min(int m)
    {
        prefsEditor.putInt(min, m);
        prefsEditor.commit();
    }

    public void set_darkUI(){
        prefsEditor.putBoolean("darkUI", !appSharedPrefs.getBoolean("darkUI", false));
        prefsEditor.commit();
    }

    public boolean get_darkUI(){
        return appSharedPrefs.getBoolean("darkUI",false);
    }

    public void set_body(String body){
        prefsEditor.putString("body",body);
        prefsEditor.apply();
    }

    public String get_body(){
        return appSharedPrefs.getString("body",null);
    }

    public void set_queryURL(String normalQueryUrl){
        prefsEditor.putString("queryURL",normalQueryUrl);
        prefsEditor.apply();
    }

    public String get_queryURL(){
      return   appSharedPrefs.getString("queryURL", null);
    }


    public void reset()
    {
        prefsEditor.clear();
        prefsEditor.commit();

    }

}
