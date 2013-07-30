/**
 * @author azrael
 * @date 2013-7-19
 */
package com.imatlas.jsb;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebView;


import com.imatlas.util.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * @author azrael
 *         JavascriptBridge
 *         连接 Java 和 Javascript 的桥梁
 */
public class JavascriptBridge {

	/**
	 * js方法的命名空间, js里的使用 window.__JavascriptBridge__.xxx 调用java提供给js的接口
	 */
	private static final String API_NAMESPACE = "__JavascriptBridge__";

	private static final int EXECUTE_JAVA_FUNCTION = 1;
	private static final int EXECUTE_JAVA_CALLBACK = 2;
	private static final int EXECUTE_JAVASCRIPT_FUNCTION = 3;
	private static final int EXECUTE_JAVASCRIPT_CALLBACK = 4;
	private static final int EXECUTE_JAVA_FUNCTION_SYNC = 5;

	public static final String PARAM_SERIAL = "serial";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_PARAMS = "params";
	public static final String PARAM_RESULT = "result";
	public static final String PARAM_TYPE = "type";

	public static final String VALUE_SERIAL_PREFIX = "java_";
	public static final String VALUE_JAVASCRIPT_FUNCTION_NAME = "jsb.__java_callback";

	/**
	 * 保存java提供给js的接口列表
	 */
	private HashMap<String, Function> mJavaMethodMap;

	private HashMap<String, Callback> mJavascriptCallbackMap;

	private OnExecuteCommandListener mOnExecuteCommandListener;

	private WebView mWebView;

	private String mCurrentUrl;

	//使用handler来确保webview相关的接口是在ui线程调用的
	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			Command command;
			switch (message.what) {
				case EXECUTE_JAVA_FUNCTION:
					command = (Command) message.obj;
					executeJavaFunction(command);
					return true;
				case EXECUTE_JAVA_CALLBACK:
					command = (Command) message.obj;
					executeJavaCallback(command);
					return true;
				case EXECUTE_JAVASCRIPT_CALLBACK:
				case EXECUTE_JAVASCRIPT_FUNCTION:
					command = (Command) message.obj;
					executeJavascriptFunction(command);
					return true;
			}
			return false;
		}
	});

	/**
	 * 必须在UI线程中实例化这个类
	 * @param webView
	 */
	public JavascriptBridge(WebView webView) {
		mWebView = webView;

		mJavaMethodMap = new HashMap<String, Function>();
		mJavascriptCallbackMap = new HashMap<String, Callback>();

		mWebView.addJavascriptInterface(new JavascriptInterface(), API_NAMESPACE);

	}

	/**
	 * 添加一个java方法给js调用
	 *
	 * @param cmdName
	 * @param function
	 */
	public void registerCommand(String cmdName, Function function) {
		mJavaMethodMap.put(cmdName, function);
	}

	/**
	 * 设置当命令将要被执行时的监听者
	 *
	 * @param listener
	 */
	public void setOnExecuteCommandListener(OnExecuteCommandListener listener) {
		this.mOnExecuteCommandListener = listener;
	}

	/**
	 * 提供给 java 调用 webview 中的 javascript 方法, javascript 的主要接口函数
	 *
	 * @param cmdName
	 * @param params
	 * @param callback
	 */
	public void execute(String cmdName, JSONObject params, Callback callback) {
		Command command = new Command(cmdName, params, EXECUTE_JAVASCRIPT_FUNCTION);
		if (callback != null) {
			mJavascriptCallbackMap.put(command.serial, callback);
		}
		mHandler.obtainMessage(command.type, command).sendToTarget();
	}

	/**
	 * 设置当前webview加载的url, 如果要支持同步API, 必须在Webview的onPageStart事件中设置该值
	 * @param url
	 */
	public void setCurrentUrl(String url) {
		this.mCurrentUrl = url;
	}

	/**
	 * 获取当前webview的页面的域名, 如果有调用 setCurrentUrl, 则优先使用设置的url来处理
	 *
	 * @return 当webview未加载页面的时候返回null
	 */
	private String getCurrentDomain() {
		String url = mCurrentUrl != null ? mCurrentUrl : mWebView.getUrl();
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		Uri uri = Uri.parse(url);
		if (uri == null) {
			return null;
		}
		String host = uri.getHost();
		return host;
	}

	/**
	 * 执行command对应的java方法(被js调用)
	 * @param command
	 */
	private void executeJavaFunction(Command command) {
		String domain = getCurrentDomain();
		if (domain == null) {//什么? 当前页面竟然没url!!
			return;
		}
		if (TextUtils.isEmpty(command.name)) {//什么? 命令为空也敢来?
			return;
		}
		if (mOnExecuteCommandListener != null) {
			boolean shouldExec = mOnExecuteCommandListener.shouldExecuteCommand(domain, command);
			if (!shouldExec) {
				return;
			}
		}
		Function function = mJavaMethodMap.get(command.name);
		if (function != null) {
			function.onExecute(command);
		}else if (mOnExecuteCommandListener != null) {
			mOnExecuteCommandListener.onCommandNotFound(command);
		}
		return;
	}

	/**
	 * 执行java对js的调用的回调
	 * @param command
	 */
	private void executeJavaCallback(Command command) {
		Callback callback = mJavascriptCallbackMap.get(command.serial);
		callback.onComplete(command, command.result);
	}

	/**
	 * 执行command对应的javascript方法(被java调用)
	 * @param command
	 * @throws org.json.JSONException
	 */
	private void executeJavascriptFunction(Command command) {
		String cmdString = command.toString();
		mWebView.loadUrl("javascript:" + VALUE_JAVASCRIPT_FUNCTION_NAME + "(" + cmdString + ")");
	}

	private static long seed = 0;

	private static String createSerial() {
		return VALUE_SERIAL_PREFIX + ++seed;
	}

	/**
	 * 调用js方法后的回调
	 *
	 * @author azrael
	 */
	public interface Callback {
		/**
		 * js方法执行后会调用该方法回调
		 *
		 * @param result
		 * @param command
		 */
		public void onComplete(Command command, JSONObject result);
	}

	/**
	 * 提供给js的java方法
	 *
	 * @author azrael
	 */
	public interface Function {
		/**
		 * 被js调用是执行的java
		 * 如果该接口是同步接口, 则可以直接返回调用结果
		 * @param command
		 */
		public void onExecute(Command command);
	}

	/**
	 * 当一个命令将要被js调用时, JavascriptBridge会执行该listener
	 */
	public interface OnExecuteCommandListener {
		/**
		 * 当js调用命令前, 会执行该方法
		 *
		 * @param domain  要执行命令的页面的域名
		 * @param command 要被执行的命令对象
		 * @return 返回 true 将拒绝执行该命令, 返回 false 则允许其执行
		 */
		public boolean shouldExecuteCommand(String domain, Command command);

		/**
		 * 当一个命令被调用时, 找不到命令对应的java方法时, 执行该方法
		 * @param command
		 */
		public void onCommandNotFound(Command command);
	}

	/**
	 * js对java的调用命令封装
	 *
	 * @author azrael
	 */
	public class Command {
		String serial;
		String name;
		JSONObject params;
		int type;
		JSONObject result;

		public Command(JSONObject cmdObj) {
			this.serial = cmdObj.optString(PARAM_SERIAL);
			this.name = cmdObj.optString(PARAM_NAME);
			this.params = cmdObj.optJSONObject(PARAM_PARAMS);
			this.result = cmdObj.optJSONObject(PARAM_RESULT);
			this.type = cmdObj.optInt(PARAM_TYPE);
		}

		public Command(String name, JSONObject params, int type) {
			this.serial = createSerial();
			this.name = name;
			this.params = params;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public JSONObject getParams() {
			return params;
		}

		public JSONObject getResult(){
			return result;
		}

		/**
		 * 把命令的内容序列化成json字符串
		 */
		@Override
		public String toString() {
			JSONObject cmdObj = new JSONObject();
			try {
				cmdObj.put(PARAM_SERIAL, this.serial);
				cmdObj.put(PARAM_TYPE, this.type);
				cmdObj.put(PARAM_NAME, this.name);
				cmdObj.put(PARAM_PARAMS, this.params);
				cmdObj.put(PARAM_RESULT, this.result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return cmdObj.toString();
		}

		/**
		 * 释放该命令保存的内容, 防止被再次触发
		 */
		public void release() {
			this.serial = null;
			this.name = null;
			this.params = null;
			this.result = null;
			this.type = 0;
		}


		/**
		 * 设置本次java调用的结果
		 *
		 * @param result
		 */
		public void setResult(JSONObject result) {
			this.result = result;
			int type = this.type;
			this.type = EXECUTE_JAVASCRIPT_CALLBACK;
			if(type != EXECUTE_JAVA_FUNCTION_SYNC){
				mHandler.obtainMessage(this.type, this).sendToTarget();
			}
		}


		/**
		 * 设置本次java调用的结果
		 *传入json字符串
		 * @param jsonString
		 */
		public void setResult(String jsonString) {
			JSONObject json = null;
			try {
				json = new JSONObject(jsonString);
			} catch (JSONException e) {
			}
			this.setResult(json);
		}

		/**
		 * 设置本次java调用的结果
		 *传入bundle实例
		 * @param bundle
		 */
		public void setResult(Bundle bundle) {
			JSONObject json = null;
			try {
				json = JSONUtil.bundleToJSON(bundle);
			} catch (JSONException e) {
			}
			this.setResult(json);
		}
	}

	/**
	 * 提供给js调用的接口
	 *
	 * @author azrael
	 */
	class JavascriptInterface {

		/**
		 * 提供给页面js用来调用java的方法, java的主要接口函数
		 *
		 * @param cmdString
		 */
		public String execute(String cmdString) throws JSONException {
			JSONObject cmdObj = new JSONObject(cmdString);
			Command command = new Command(cmdObj);
			if (command.type == EXECUTE_JAVA_FUNCTION_SYNC) {
				executeJavaFunction(command);
				return command.toString();
			}else{
				mHandler.obtainMessage(command.type, command).sendToTarget();
				return null;
			}
		}

		/**
		 * 提供给页面js用来设置java对js的调用结果
		 *
		 * @param cmdString
		 * @throws org.json.JSONException
		 */
		public void setResult(String cmdString) throws JSONException {
			JSONObject cmdObj = new JSONObject(cmdString);
			Command command = new Command(cmdObj);
			command.type = EXECUTE_JAVA_CALLBACK;
			 mHandler.obtainMessage(command.type, command).sendToTarget();
		}


	}


}
