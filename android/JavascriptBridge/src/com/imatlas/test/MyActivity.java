package com.imatlas.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import com.imatlas.jsb.JavascriptBridge;
import org.json.JSONException;
import org.json.JSONObject;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Button btn = (Button) findViewById(R.id.button1);
		Button btn2 = (Button) findViewById(R.id.button2);

		final String initUrl = "http://appx.imatlas.com/test/test.html";
		final WebView webView = (WebView)findViewById(R.id.webView1);
		final JavascriptBridge jsb = new JavascriptBridge(webView);
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				jsb.setCurrentUrl(url);
				super.onPageStarted(view, url, favicon);
			}
		});
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);




		jsb.registerCommand("mq.getVersion", new JavascriptBridge.Function() {

			@Override
			public void onExecute(JavascriptBridge.Command command) {

				try {
					JSONObject result = new JSONObject();
					result.put("retcode", 0);
					result.put("version", "1.0.1");
					command.setResult(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		jsb.registerCommand("mq.device.getBuildVersion", new JavascriptBridge.Function() {

			@Override
			public void onExecute(JavascriptBridge.Command command) {
				Bundle result = new Bundle();
				result.putString("buildVersion", "1.3.1.0");
				command.setResult(result);
			}
		});
		jsb.registerCommand("mq.device.isMobileQQ", new JavascriptBridge.Function() {

			@Override
			public void onExecute(JavascriptBridge.Command command) {
				command.setResult("{'result':true}");
			}
		});
		//添加个 messagebox 方法给js
		jsb.registerCommand("mq.view.messagebox", new JavascriptBridge.Function() {

			@Override
			public void onExecute(JavascriptBridge.Command command) {
				JSONObject params = command.getParams();
				Toast.makeText(getApplicationContext(), "这是js调用, 参数是: " + params.toString(), Toast.LENGTH_LONG)
						.show();

				try {
					JSONObject result = new JSONObject();
					result.put("retcode", 1);
					result.put("message", "messagebox callback");
					command.setResult(result);
				} catch (Exception e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		});
		jsb.setOnExecuteCommandListener(new JavascriptBridge.OnExecuteCommandListener() {
			@Override
			public boolean shouldExecuteCommand(String domain, JavascriptBridge.Command command) {
				if(domain.equals("appx.imatlas.com")){
					return true;
				}
				return false;
			}

			@Override
			public void onCommandNotFound(JavascriptBridge.Command command) {
			}
		});
		webView.loadUrl(initUrl);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl(initUrl);
			}
		});

		btn2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				JSONObject params = new JSONObject();
				try {
					params.put("asdfasdf", "123123");
					//调用js提供的alert方法
					jsb.execute("alert", params, new JavascriptBridge.Callback() {
						@Override
						public void onComplete(JavascriptBridge.Command command, JSONObject response) {
							Toast.makeText(getApplicationContext(), "调用js的回调: " + response.toString(), Toast.LENGTH_LONG)
									.show();

						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}


			}
		});
	}

}
