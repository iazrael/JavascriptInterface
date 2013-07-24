package com.imatlas.jsb;

import android.webkit.WebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: azraellong
 * Date: 13-7-23
 * Time: 上午11:58
 */
public class AuthorizeJavascriptBridge extends JavascriptBridge {

	private static final String KEY_CMD_RIGHTS = "rights";
	public static final String KEY_DOMAIN_AUTHS = "auths";
	public static final String KEY_LEVEL = "level";

	private JSONObject config;

	private OnExecuteCommandListener listener = new OnExecuteCommandListener() {
		@Override
		public boolean shouldExecuteCommand(String domain, Command command) {
			return hasCommandRight(domain, command.getName());
		}

		@Override
		public void onCommandNotFound(Command command) {
			//nothing
		}
	};

	public AuthorizeJavascriptBridge(WebView mWebView, String configString) throws JSONException {
		super(mWebView);
		this.setOnExecuteCommandListener(listener);
		this.setConfig(configString);
	}

	/**
	 * 必须确保configString有auths和rights字段
	 * @param configString
	 * @throws JSONException
	 */
	public void setConfig(String configString) throws JSONException {
		JSONObject json = new JSONObject(configString);
		JSONObject cmdRights = json.getJSONObject(KEY_CMD_RIGHTS);
		JSONObject domainAuths = config.getJSONObject(KEY_DOMAIN_AUTHS);
		JSONArray cmdNames = cmdRights.names();
		for (int i = 0, len = cmdNames.length(); i < len; i++) {
			String name = cmdNames.getString(i);
			Object value = cmdRights.get(name);
			if (!(value instanceof JSONObject)) {
				JSONObject obj = new JSONObject();
				obj.put(KEY_LEVEL, value);
				cmdRights.put(name, obj);
			}
		}
		config = json;
	}

	private boolean hasCommandRight(String domain, String cmdName) {
		JSONObject cmdRights = config.optJSONObject(KEY_CMD_RIGHTS);
		JSONObject domainAuths = config.optJSONObject(KEY_DOMAIN_AUTHS);
		JSONObject cmdConfig = cmdRights.optJSONObject(cmdName);
		if (cmdConfig == null) {//没有配置的命令, 全都不给调用
			return false;
		}
		int cmdLevel = cmdConfig.optInt(KEY_LEVEL);
		JSONArray domainNames = domainAuths.names();
		for (int i = 0, len = domainNames.length(); i < len; i++) {
			String name = domainNames.optString(i);
			int level = domainAuths.optInt(name);
			if (isDomainMatch(name, domain) && level <= cmdLevel) {
				return true;
			}
		}
		return false;
	}

	private boolean isDomainMatch(String pattern, String domain) {
		pattern =pattern.replaceAll("\\.", "\\\\.")
			.replaceAll("\\*", ".+");
		pattern = "^" + pattern + "$";
		return domain.matches(pattern);
	}

}
