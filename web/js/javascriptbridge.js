/**
 * JavascriptBridge
 * 连接 Java 和 Javascript 的桥梁
 * @author azrael
 */
(function(global){
	var NAMESPACE = 'jsb'
	var API_NAMESPACE = '__JavascriptBridge__';
	var API_CALLBACK_NAME =  '__java_callback';
	var VALUE_SERIAL_PREFIX = 'js_'

	var EXECUTE_JAVA_FUNCTION = 1;
	var EXECUTE_JAVA_CALLBACK = 2;
	var EXECUTE_JAVASCRIPT_FUNCTION = 3;
	var EXECUTE_JAVASCRIPT_CALLBACK = 4;
	var EXECUTE_JAVA_FUNCTION_SYNC = 5;

	var context = global[NAMESPACE] = {};
	var api = global[API_NAMESPACE] || null;
	if(!api){
//		return alert('发生错误, 未找到本地 api 对象!');
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

	var setResult = function(result){
		var cmd = this;
		cmd.result = result;
		api.setResult(JSON.stringify(cmd));
	}

	var createNamespace = function(name){
		var arr = name.split('.');
		var space = window, a;
		for(var i in arr){
			a = arr[i];
			!space[a] && (space[a] = {});
			space = space[a];
		}
		return space;
	}

	var createFunction = function(cmdName){
		return function(){
			var argus = Array.prototype.slice.apply(arguments);
			argus.unshift(cmdName);
			return context.execute.apply(context, argus);
		}
	}

	/**
	 * 添加提供给java调用的js方法
	 * @param {[type]} method [description]
	 * @param {[type]} func   [description]
	 */
	context.registerCommand = function(cmdName, func){
		mJsMethodMap[cmdName] = func;
	}

	context[API_CALLBACK_NAME] = function(cmd){
		if(cmd.type === EXECUTE_JAVASCRIPT_CALLBACK){
		//执行java之后的回调
			var callback = null;
			if(callback = mJavaCallbackMap[cmd.serial]){
				mJavaCallbackMap[cmd.serial] = null;
				callback(cmd, cmd.result);
			}
		}else if(cmd.type === EXECUTE_JAVASCRIPT_FUNCTION){
		//java调用js
			var method = null;
			if(method = mJsMethodMap[cmd.name]){
				//绑定设置回调结果的方法到cmd对象
				cmd.setResult = setResult;
				method(cmd);
			}
		}
	}

	/**
	 * 请求调用java方法
	 * @param  {[type]}   cmd      [description]
	 * @param  {[type]}   params   [description]
	 * @param  {Function} callback [description]
	 * @return {[type]}
	 */
	context.execute = function(cmdName, params, callback){
		var cmd, cmdString;
		if(arguments.length === 2){
			if(typeof params === 'function'){
				callback = params;
				params = null;
			}
		}
		cmd = {
			serial: createSerial(),
			name: cmdName,
			params: params || {}
		};
		if(typeof callback === 'function'){//有指定回调, 异步调用
			mJavaCallbackMap[cmd.serial] = callback;
			cmd.type = EXECUTE_JAVA_FUNCTION;
			cmdString = JSON.stringify(cmd);
            api.execute(cmdString);
		}else{
			//同步调用
			cmd.type = EXECUTE_JAVA_FUNCTION_SYNC;
			cmdString = JSON.stringify(cmd);
            cmdString = api.execute(cmdString);
            cmd = JSON.parse(cmdString);
            return cmd.result;
		}

	}


	/**
	 * 用于批量创建js接口提供给外部的方法
	 */
	context.buildCommands = function(cmdNameArr){
		var name, index, funcName, np;
		for(var i in cmdNameArr){
			name = cmdNameArr[i];
			index = name.lastIndexOf('.');
			if(index === -1){
				np = global;
				funcName = name;
			}else{
				np = createNamespace(name.substring(0, index));
				funcName = name.substring(index + 1);
			}
			np[funcName] = createFunction(name);
		}
	}

})(window);