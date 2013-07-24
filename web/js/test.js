!function(){
	jsb.buildCommands([
		'mq.getVersion',
		'getXXX',
		'mq.net.getLocation',
		'mq.view.showLoading'
	]);
	jsb.registerCommand('alert', function(cmd){
		alert( '这是java对js的调用------\n' + JSON.stringify(cmd) + '\n========\n');
		cmd.setResult({"alert callback": "alert success"});
	});
	var btn = document.createElement('button');
	btn.innerHTML = '点击我呀';
	btn.onclick=function(){
		jsb.execute('messagebox', {'text': '你好, messagebox!'}, function(cmd, response){
			alert('调用messagebox回来啦\n' + JSON.stringify(response));
		});
	}
	document.body.appendChild(btn);
}();