!function(){
	jsb.buildCommands([
		'mq.getVersion',
		'mq.view.messagebox',
		'mq.net.getLocation',
		'mq.view.showLoading'
	]);
	jsb.buildCommands({
		'mq.device.isMobileQQ': function(){
			var cmdResult = jsb.execute('mq.device.isMobileQQ');
			return !!cmdResult.result;
		},
		'mq.device.getBuildVersion': function(){
			var cmdResult = jsb.execute('mq.device.getBuildVersion');
            return cmdResult.buildVersion;
		}
	});
	jsb.registerCommand('alert', function(cmd){
		alert( '这是java对js的调用------\n' + JSON.stringify(cmd) + '\n========\n');
		cmd.setResult({"alert callback": "alert success"});
	});



	function addButton(text, func){
		var btn = document.createElement('button');
        	btn.innerHTML = text;
        	btn.onclick=func;
        	document.body.appendChild(btn);
	}
	addButton( 'mq.view.messagebox', function(){
		mq.view.messagebox({'text': '你好, messagebox!'}, function(cmd, response){
                alert('调用messagebox回来啦\n' + JSON.stringify(response));
            });
	});
	addButton( 'mq.getVersion', function(){
    		alert('版本号是: ' + mq.getVersion().version);
    	});
    addButton( 'mq.net.getLocation', function(){
        		 mq.net.getLocation(function(cmd, data){
        		    alert(data);
        		});
        	});
    addButton( 'mq.view.showLoading', function(){
        		 mq.view.showLoading({'xxx':'yyy'});
        	});
    addButton( 'mq.device.isMobileQQ', function(){
                    alert('是不是手q: ' + mq.device.isMobileQQ());
                });
    addButton( 'mq.device.getBuildVersion', function(){
                     alert('BuildVersion: ' + mq.device.getBuildVersion());
                });
}();