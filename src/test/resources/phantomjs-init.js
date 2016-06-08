var fs = require('fs');

var webPage = require('webpage');
var page = webPage.create();

page.onInitialized = function() {
	page.viewportSize = {
	  width: 1600,
	  height: 900
	};	
}

fs.touch('target/phantomjs-init.marker');

return "OK";
