package ako555.isymphonyz.akoradio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class LandingPage extends AppCompatActivity {

    private String TAG = "LandingPage";

    private ProgressBar progressBar;

    protected boolean _active = true;
    protected int _splashTime = 5000; // time to display the splash screen in ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // thread for displaying the SplashScreen
        final Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (_active && (waited < _splashTime)) {
                        sleep(100);
                        if (_active) {
                            waited += 100;
                        }
                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    Intent intent = new Intent(getApplicationContext(), AKORadioWebView.class);
                    startActivity(intent);
                    finish();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //stuff that updates ui

                        }
                    });
                }
            }
        };
        splashTread.start();
    }

    @Override
    public void onBackPressed() {

    }
}
