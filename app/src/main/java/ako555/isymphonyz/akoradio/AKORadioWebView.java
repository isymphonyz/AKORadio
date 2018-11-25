package ako555.isymphonyz.akoradio;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ako555.isymphonyz.akoradio.connection.AllowAPI;
import ako555.isymphonyz.akoradio.utils.AppJavaScriptProxy;
import ako555.isymphonyz.akoradio.utils.MyConfiguration;
import ako555.isymphonyz.akoradio.utils.UrlCache;

public class AKORadioWebView extends AppCompatActivity {

    private String TAG = "AKORadioWebView";

    private ProgressBar progressBar;
    private WebView webView;

    private String url = MyConfiguration.URL_RADIO_MAP;

    private AllowAPI allowAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        webView = (WebView) findViewById(R.id.webView);

        //progressBar.setVisibility(View.VISIBLE);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setSaveFormData(true);
        webSettings.setSavePassword(true);

        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);

        webView.addJavascriptInterface(new AppJavaScriptProxy(this, webView), "androidAppProxy");
        //webView.loadUrl(url);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("fromAndroid()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //store / process result received from executing Javascript.
                }
            });
        }

        Log.d(TAG, "loadUrl URL: " + url);
        if (Build.VERSION.SDK_INT >= 21) {
            webView.getSettings().setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.loadUrl(url);

        allowAPI = new AllowAPI();
        allowAPI.setListener(new AllowAPI.AllowAPIListener() {
            @Override
            public void onAllowAPIPreExecuteConcluded() {

            }

            @Override
            public void onAllowAPIPostExecuteConcluded(String result) {
                try {
                    Log.d(TAG, "result: " + result);
                    JSONObject jObj = new JSONObject(result);
                    String status = jObj.optString("allow");

                    Log.d(TAG, "status: " + status);
                    if(status.equals("1")) {

                    } else {
                        finish();
                    }
                } catch (JSONException e) {

                }
            }
        });
        allowAPI.execute("");
    }

    private class WebViewClientImpl extends WebViewClient {

        private Activity activity = null;
        private UrlCache urlCache = null;

        public WebViewClientImpl(Activity activity) {
            this.activity = activity;
            this.urlCache = new UrlCache(activity);

            this.urlCache.register("http://tutorials.jenkov.com/", "tutorials-jenkov-com.html",
                    "text/html", "UTF-8", 5 * UrlCache.ONE_MINUTE);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading URL: " + url);
            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.d(TAG, "WebResourceResponse URL: " + url);
            if(url.startsWith("http://tutorials.jenkov.com/images/logo.png")){
                String mimeType = "image/png";
                String encoding = "";
                URL urlObj = null;
                InputStream input = null;
                try {
                    urlObj = new URL(url);
                    URLConnection urlConnection = urlObj.openConnection();
                    urlConnection.getInputStream();
                    input = urlConnection.getInputStream();
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                WebResourceResponse response = new WebResourceResponse(mimeType, encoding, input);

                return response;
            }

            //progressBar.setVisibility(View.GONE);

            return this.urlCache.load(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished URL: " + url);

            if("http://tutorials.jenkov.com/".equals(url)){
                this.urlCache.load("http://tutorials.jenkov.com/java/index.html");
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
