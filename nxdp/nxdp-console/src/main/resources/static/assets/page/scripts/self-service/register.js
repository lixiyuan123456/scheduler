var Process = function () {
	var self = $(this);
	
	$('#registerButton').click(function(){
		$.post(self.contextPath + '/register/register.do', $('#registerForm').serialize() , function(data){
			if(data.status != 'ok'){
				toastr['error'](data.data, '提示');
				return false;
			}else{
				location.reload();
			}
		})
	})
	
	return {
		init : function (opts) {
			self.contextPath = opts.contextPath;
			self.userId = opts.userId;
		}
	}
}();