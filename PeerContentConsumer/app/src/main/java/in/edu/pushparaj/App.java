package in.edu.pushparaj;

import android.app.Application;
import android.content.SharedPreferences;

import org.ligi.tracedroid.TraceDroid;

public class App extends Application {

    public static SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        //Instantiating shared preference
        App.preferences = getSharedPreferences( getPackageName() + "_preferences", MODE_PRIVATE);

        TraceDroid.init(this);
    }

}
