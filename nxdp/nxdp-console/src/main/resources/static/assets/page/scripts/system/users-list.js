var Process = function() {
  var self = $(this);
  var initDatatable = function() {
    self.datatable = $('#dataTable').DataTable({
      "ordering" : false,
      "searching" : false,
      "serverSide" : true,
      "ajax" : {
        url : self.contextPath + '/user/api/search-users',
        type : 'POST',
        data : function(d) {
          d.columns = null;
          d.order = null;
          // d.search = null;
        }
      },
      "columns" : [/*
       * { data : null, render : function(data, type, full, meta) {
       * var innerHtml = "<input type='checkbox' data-set='" +
       * data.id + "' class='checkboxes' style='width: 15px;
       * height: 15px;' />"; return innerHtml; } },
       */{
        data : "userName"
      }, {
        data : "trueName"
      }, {
        data : "deptName"
      }, {
        data : null,
        render : function(data, type, full, meta) {
          var innerHtml = '';
          if (data.permissionLevel != 1) {
            innerHtml += '<a href="javascript:void(0);" class="btn btn-link" onclick="Process.deleteUserById(\'' + data.id + '\')">删除</a>';
          }
          innerHtml += '<a id="enableAgentBtn_'+data.pyName+'" class="btn btn-link" onClick="Process.enableAgent(\'' + data.pyName + '\')">启用代理</a>';
          //innerHtml += '<a id="disableAgentBtn_'+data.pyName+'" class="btn btn-link" onClick="Process.disableAgent(\'' + data.pyName + '\')">停用代理</a>';
          return innerHtml;
        }
      }],
    });

    $('.checkbox-group').change(function() {
      var checked = $(this).is(":checked");
      $('.checkboxes:checkbox').each(function() {
        if (checked) {
          $(this).attr("checked", true);
          $(this).parents('tr').addClass("active");
        } else {
          $(this).attr("checked", false);
          $(this).parents('tr').removeClass("active");
        }
      });
      jQuery.uniform.update($(this));
    });

    $('#dataTable').on('change', 'tbody tr .checkboxes', function() {
      $(this).parents('tr').toggleClass("active");
    });
  }

  return {
    init: function(opts) {
      self.contextPath = opts.contextPath;
      self.userId = opts.userId;
      initDatatable();
    },
    deleteUserById: function(userId) {
      $.post(self.contextPath + '/user/api/delete-user', {
        "userId" : userId
      }, function(data) {
        if (data.status == 'ok') {
          // $("#dataTable").dataTable().fnDraw(false);
          // $("#dataTable").dataTable().fnDestroy();
          var settings = $("#dataTable").dataTable().fnSettings();
          $("#dataTable").dataTable().fnPageChange(settings._iDisplayStart / settings._iDisplayLength);
          // $("#dataTable").dataTable().fnDraw(false);
        }
      });
    },
    enableAgent: function(agentUser){
      $.post('/agentUser/enableAgent.do',{'agentUser':agentUser},function(r){
        if(r.status != 'ok'){
          bootbox.alert(r.msg);
          return false;
        }
        //$('#enableAgentBtn_'+agentUser).hide();
        //$('#disableAgentBtn_'+agentUser).show();
        window.location.href = "/";
      });
    },
    disableAgent: function(agentUser){
      $.post('/agentUser/disableAgent.do',{'agentUser':agentUser},function(r){
        if(r.status != 'ok'){
          bootbox.alert(r.msg);
          return false;
        }
        //$('#enableAgentBtn_'+agentUser).show();
        //$('#disableAgentBtn_'+agentUser).hide();
        window.location.href = "/";
      });
    },
  }
}();