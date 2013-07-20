/**
 * JavascriptBridge
 * 连接 Java 和 Javascript 的桥梁
 * @author azrael
 */
(function(){
	var NAMESPACE = 'jsb'
	var API_NAMESPACE = '__JavascriptBridge__';
	var API_CALLBACK_NAME = NAMESPACE + '.' + '__java_callback';
	var VALUE_SERIAL_PREFIX = 'js_'

	var EXECUTE_JAVA_FUNCTION = 1;
	var EXECUTE_JAVA_CALLBACK = 2;
	var EXECUTE_JAVASCRIPT_FUNCTION = 3;
	var EXECUTE_JAVASCRIPT_CALLBACK = 4;

	var context = window[NAMESPACE] = {};
	var api = window[API_NAMESPACE] || null;
	if(!api){
		return alert('发生错误, 未找到本地 api 对象!');
	}
	/**
	 * 保存提供给java调用的js方法列表
	 * @type {Object}
	 */
	var mJsMethodMap = {};

	/**
	 *  保存调用java之后的回调函数列表
	 * @type {Object}
	 */
	var mJavaCallbackMap = {};

	var serial = 0;

	var createSerial = function(){
		return VALUE_SERIAL_PREFIX + ++serial;
	}

	/**
	 * 添加提供给java调用的js方法
	 * @param {[type]} method [description]
	 * @param {[type]} func   [description]
	 */
	context.addJavascriptMethod = function(method, func){
		mJsMethodMap[method] = func;
	}

	context[API_CALLBACK_NAME] = function(cmdString){
		var cmd = JSON.parse(cmdString);
		if(cmd.type === EXECUTE_JAVA_CALLBACK){
		//执行java之后的回调
			var callback = null;
			if(callback = mJavaCallbackMap[cmd.serial]){
				mJavaCallbackMap[cmd.serial] = null;
				callback(cmd.result, cmd.name);
			}
		}else if(cmd.type === EXECUTE_JAVASCRIPT_FUNCTION){
		//java调用js
		}
	}

	/**
	 * 请求调用java方法
	 * @param  {[type]}   cmd      [description]
	 * @param  {[type]}   params   [description]
	 * @param  {Function} callback [description]
	 * @return {[type]}
	 */
	context.executeJava = function(cmdName, params, callback){
		params = params || {};
		var cmd = {
			serial: createSerial(),
			cmdName: cmdName,
			params: params
		}
		if(typeof callback === 'function'){
			mJavaCallbackMap[serial] = callback;
		}
		var cmdString = JSON.stringify(cmd);
		api.execute(cmdString);
	}


})();