/**
 * This is common javascript file.
 */

var commonOpenMarket = {
		callAjaxCrossPlatform :	function (url,httpMethod, data, type, successCallBack,errorCallBack,async,cache) 
				{
					if (typeof url == "undefined") {
						return;
				    }
					if (typeof httpMethod == "undefined") {
						httpMethod = 'GET';
				    }
					if (typeof data == "undefined") {
						data='';
				    }
					if (typeof type == "undefined") {
						type = 'json';
				    }
					if (typeof async == "undefined") {
				        async = true;
				    }
				    if (typeof cache == "undefined") {
				        cache = false;
				    }
				
				    var ajaxObj = $.ajax({
				        type: httpMethod.toUpperCase(),
				        headers: {
				            'Accept': 'application/json',
				            'Content-Type': 'application/json; charset=utf-8',
				            'Access-Control-Allow-Origin' : '*'
				        },
				        xhrFields: {
				            withCredentials: true
				        },
				        url: url,
				        data: JSON.stringify(data),
				        dataType: type,
				        async: async,
				        cache: cache,
				        success: successCallBack,
				        error: errorCallBack
				    });
				    return ajaxObj;
				},
				
				callAjax :	function (url,httpMethod, data, type, successCallBack,errorCallBack,async,cache) 
				{
					if (typeof url == "undefined") {
						return;
				    }
					if (typeof httpMethod == "undefined") {
						httpMethod = 'GET';
				    }
					if (typeof data == "undefined") {
						data='';
				    }
					if (typeof type == "undefined") {
						type = 'json';
				    }
					if (typeof async == "undefined") {
				        async = true;
				    }
				    if (typeof cache == "undefined") {
				        cache = false;
				    }
				
				    var ajaxObj = $.ajax({
				        type: httpMethod.toUpperCase(),
				        url: url,
				        data: data,
				        beforeSend : function (xhr, settings) {
				        	xhr.setRequestHeader(header,token);
				        },
				        dataType: type,
				        async: async,
				        cache: cache,
				        success: successCallBack,
				        error: errorCallBack
				    });
				    return ajaxObj;
				    
				}
}