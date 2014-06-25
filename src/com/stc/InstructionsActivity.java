package com.stc;



import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class InstructionsActivity extends Activity implements View.OnTouchListener {
	private static final String mActivityVersion = "1.0";
    private WebView mWebView;	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);		
        setContentView(R.layout.new_instructions_layout);        

		final LinearLayout parent = (LinearLayout) findViewById(R.id.instructionslayout);
		parent.setOnTouchListener (this);
		
        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        mWebView.setBackgroundColor(Color.parseColor("#C0C0C0"));      

        Button backButton = (Button) findViewById(R.id.buttonBack);
        backButton.setBackgroundColor(Color.parseColor("#C0C0C0")); 

		final LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
		layout.setBackgroundColor(Color.parseColor("#C0C0C0"));  
        
        
        mWebView.setWebChromeClient(new MyWebChromeClient());

//        mWebView.addJavascriptInterface(new DemoJavaScriptInterface(), "demo");

        mWebView.loadUrl("file:///android_asset/index.html");	

	}
	
    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
    final class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//            Log.d(LOG_TAG, message);
            result.confirm();
            return true;
        }
    }
    
    
	public void onButtonClick(View v)
	{
		 final int id = v.getId();
		    switch (id) {
		    case R.id.buttonBack:
		    	finish();
		    	
		    	break;
		    		    
		    }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
	}
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		finish();
		return false;
	}
	

}
