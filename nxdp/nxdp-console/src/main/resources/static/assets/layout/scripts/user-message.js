/**
UserMessage script to show user notice message
**/
var UserMessage = function () {
	var self = $(this);
    // get user message
    var getUserMessage = function () {
    	var data = {};
        $.getJSON(self.contextPath + '/announcement/api/get-notice-message', data, function(result) {
            if (result.status != 'success') {
            	toastr["error"](result.msg, "提示");
                return;
            }
            var userMsg = result.userMsg;
            var msgList = "";
            var newMsgNum = 0;
            self.msgIds = new Array();
            //下面使用each进行Json数组遍历
            $.each(userMsg, function (n, msg) {
                if(msg.have_readed == 0){
                	self.msgIds.push(msg.id);
                	newMsgNum++;
                }
                var lis = "";
                lis += "<li>";
                lis += "<a href='javascript:UserMessage.viewMessage("+msg.id+");'>";
                lis += "<span class='time'>"+msg.interval+"</span>";
                lis += "<span class='details'>";
                lis += "<span class='label label-sm label-icon'>";
                lis += "<i class='fa fa-bullhorn'></i><i class='fa "+(msg.have_readed == 0?"fa-envelope":"fa-envelope-o")+"'></i>";
                lis += "</span>"+msg.title+" </span>";
                lis += "</a>";
                lis += "</li>";
                msgList += lis;
            });
            if(newMsgNum == 0){
            	$("#messageNum").addClass("hidden");;
            }else{
            	$("#messageNum").removeClass("hidden");
            	$('#messageNum').html(newMsgNum);
            }
            
            $('#newMsgNum').html(" You have <strong>"+newMsgNum+" New</strong> Messages");
            $('#userMsgList').html(msgList);
        }).always(function() {
        	
        });
    };
    
    $("#signReadAll").click(function(){
    	  if(self.msgIds.length ==0) return;
    	  var data = {'msgIds': self.msgIds.join(',')};
    	  $.getJSON(self.contextPath + '/announcement/api/sign-all-message', data, function(result) {
              if (result.status != 'success') {
              	toastr["error"](result.msg, "提示");
                  return;
              }
              self.msgIds = new Array();
          }).always(function() {
         	 getUserMessage();
          });
    });

    //查看消息
    var viewMessage = function(msgId) {
    	var data = {'msgId': msgId};
        $.getJSON(self.contextPath + '/announcement/api/view-notice-message', data, function(result) {
            if (result.status != 'success') {
            	toastr["error"](result.msg, "提示");
                return;
            }
            var message = result.message;
            bootbox.dialog({
        	  title: "<h1>"+message.title+"</h1>",
        	  className: "notice-message",
        	  message: message.message,
        	  buttons: {  
                  OK: {  
                      label: "OK",  
                      className: "btn-primary",  
                      callback: function () {
                      }  
                  }  
              }  
        	});
        }).always(function() {
        	 getUserMessage();
        });
    };
    
    //只显示运维或者其他紧急通知
    var showSystemMessage = function() {
    	var data = {'msgId': msgId};
        $.getJSON(self.contextPath + '/announcement/api/show-system-message', data, function(result) {
            if (result.status != 'success') {
            	toastr["error"](result.msg, "提示");
                return;
            }
            var message = result.message;
            
            $('#messageModal .modal-title').html(message.title);
            $('#messageModal .modal-body').html(message.message);
            $('#messageModal').modal('show');
        }).always(function() {
        	 getUserMessage();
        });
    };

    return {

        //main function to initiate the theme
        init: function(opts) {
        	self.contextPath = opts.contextPath;
        	//getUserMessage(); 
        },
        viewMessage: function(msgId) {
        	viewMessage(msgId); 
        }
    };

}();