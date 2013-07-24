package com.imatlas.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
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

		final WebView webView = (WebView)findViewById(R.id.webView1);
		webView.setWebChromeClient(new WebChromeClient());
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		final JavascriptBridge jsb = new JavascriptBridge(webView);

		//添加个 messagebox 方法给js
		jsb.registerCommand("messagebox", new JavascriptBridge.Function() {

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
				if(domain.equals("www.imatlas.com")){
					return true;
				}
				return false;
			}

			@Override
			public void onCommandNotFound(JavascriptBridge.Command command) {
			}
		});
		webView.loadUrl("http://www.imatlas.com/test.html");
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				webView.loadUrl("http://www.imatlas.com/test.html");
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
