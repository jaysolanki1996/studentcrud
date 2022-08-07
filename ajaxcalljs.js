function setAvailableUnits() {
		var userId = $("#user").val();
		var digitalCurrencyId = $("#digitalCurrency").val();

		if ((userId != null && userId != "undefined" && userId != "")
				&& (digitalCurrencyId != null
						&& digitalCurrencyId != "undefined" && digitalCurrencyId != "")) {
			var sendInfo = {
				userId : userId,
				digitalCurrencyId : digitalCurrencyId
			};
			var url = '${appContextName}/admin/userWallet/getAvailableUnits';
			var data = sendInfo;
			var type = 'json';
			commonOpenMarket.callAjax(url, 'GET', data, type,
					function(response) {
						if (response != null) {
							$('#availableUnits').text(response);
						}
					});
		}

	}

	function login() {
		var email = 'john.doe123@gmail.com';
		var passwordStr = 'password';
		var sendInfo = {
			emailAddress : email,
			password : passwordStr
		};
		var url = 'http://dev.openmarkets.exchange:8080/a/r/api/v1/login';
		var data = sendInfo;
		var type = 'json';
		commonOpenMarket.callAjaxCrossPlatform(url, 'POST', data, type,
				function(response) {
					if (response != null) {
						console.log(response);
					}
				}, function(jqXHR, textStatus, errorThrown) {
					console.log('Please try again.');
					console.log("jqXHR" + jqXHR);
					console.log("textStatus" + textStatus);
					console.log("errorThrown" + errorThrown);
				});

	}