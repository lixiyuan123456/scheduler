var Process = function() {
  var self = $(this);

  var getHadoopUserGroupNameByDeptId = function(deptId) {
    var hadoopUserGroupName = null;
    $.each(self.hadoopUserGroups, function(i, obj) {
      if ($.inArray(deptId, obj.deptIds) != -1) {
        hadoopUserGroupName = obj.name;
        return false;
      }
    });
    return hadoopUserGroupName;
  }

  var initDatatable = function() {
    self.datatable = $('#dataTable').DataTable({
      "serverSide" : true,
      "ordering" : false,
      "searching" : false,
      "ajax" : {
        url : self.contextPath + '/register/list-registers',
        data : function(d) {
          d.columns = null;
          d.order = null;
          //d.search = null;
        }
      },
      "columns" : [/*{
        data : null,
        render : function(data, type, full, meta) {
          var innerHtml = "<input type='checkbox' data-set='" + data.id + "' class='checkboxes' style='width: 15px; height: 15px;' />";
          return innerHtml;
        }
      },*/ {
        data : "userPyname"
      }, {
        data : "userName"
      }, {
        data : "deptName"
      }, {
        data : "description"
      }, {
        data : null,
        render : function(data, type, full, meta) {
          var html = '';
          if (data.status == 0) {
            var hadoopUserGroupName = getHadoopUserGroupNameByDeptId(data.deptId);
            if (hadoopUserGroupName == null) {
              html += '<select id="id-iQueryUserGroupSelect_' + data.id + '" data-deptId="' + data.deptId + '" class="form-control dropdown class-iQueryUserGroup" style="width:100px;" onchange="selectHadoopUserGroup(' + data.id + ',' + data.deptId + ')">';
              $.each(self.hadoopUserGroups, function(i, obj) {
                html += '<option value="' + obj.name + '">' + obj.name + '</option>';
              });
              html += '<option value="0">新建用户组</option>';
              html += '</select>';
            } else {
              html += '<select id="id-iQueryUserGroupSelect_' + data.id + '" data-deptId="' + data.deptId + '" class="form-control dropdown class-iQueryUserGroup" style="width:100px;" disabled>';
              html += '<option value="' + hadoopUserGroupName + '" selected>' + hadoopUserGroupName + '</option>';
              html += '</select>';
            }
            // 绑定事件
            /*
             * $('#id-iQueryUserGroupSelect_'+data.id).unbind('change');
             * $('#id-iQueryUserGroupSelect_'+data.id).bind('change',function(){
             * var id = $(this).val(); if(id == '0'){//新增 //清空
             * $('#iQueryUserGroupForm')[0].reset();
             * $('#id-hiddenRegisterId').val(data.id); $('#iQueryUserGroupForm
             * input[name="registerDeptId"]').val(data.deptId);
             * $('#iQueryUserGroupModal').modal('show'); } })
             */
          }
          // $('.class-iQueryUserGroup').select2();
          return html;
        }
      }, {
        data : null,
        render : function(data, type, full, meta) {
          var innerHtml = '';
          if (data.status == 0) {
            innerHtml = '<a href="#" class="btn btn-link" onclick="Process.ratify(\'' + data.id + '\')">同意</a>';
            innerHtml += '<a href="#" class="btn btn-link" onclick="Process.reject(\'' + data.id + '\')">拒绝</a>';
          }
          return innerHtml;
        }
      }, {
        data : null,
        render : function(data, type, full, meta) {
          var innerHtml = '';
          if (data.status == 1) {
            innerHtml = data.operatorName + '-批准';
          }
          if (data.status == 2) {
            innerHtml = data.operatorName + '-拒绝';
          }
          return innerHtml;
        }
      }]
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

  $('#iQueryUserGroupCancelBtn').click(function() {
    var first = $('#id-iQueryUserGroupSelect_' + $('#id-hiddenRegisterId').val()).find('option').eq(0).val();
    $('#id-iQueryUserGroupSelect_' + $('#id-hiddenRegisterId').val()).val(first);
    $('#iQueryUserGroupModal').modal('hide');
  })

  $('#iQueryUserGroupSureBtn').click(function() {
    var hadoopUserName = $('#iQueryUserGroupForm input[name="hadoopUserName"]').val();
    var registerDeptId = $('#iQueryUserGroupForm input[name="registerDeptId"]').val();
    $.post('/register/new-hadoop-user-group.do', $('#iQueryUserGroupForm').serialize(), function(data) {
      if (data.status == 'ok') {
        $.get('/register/all-hadoop-user-groups.do', function(resp) {
          if (resp.status == 'error') {
            toastr.error(resp.data);
            return false;
          }
          self.hadoopUserGroups = resp.hadoopUserGroups;
          var optionHtml = '';
          $.each(self.hadoopUserGroups, function(i, hadoopUserGroup) {
            optionHtml += '<option value="' + hadoopUserGroup.name + '">' + hadoopUserGroup.name + '</option>'
          })
          $('.class-iQueryUserGroup').each(function(i, select) {
            var old = $(this).val();
            $(this).empty();
            $(this).append(optionHtml);
            $(this).val(old);
            if ($(this).attr('data-deptId') == registerDeptId) {
              $(this).val(hadoopUserName);
              $(this).attr('disabled', 'disabled');
            }
          })
          // 选中新增的组
          // $('#id-iQueryUserGroupSelect_'+$('#id-hiddenRegisterId').val()).val(hadoopUserName);
          // $('#id-iQueryUserGroupSelect_'+$('#id-hiddenRegisterId').val()).attr('disabled','disabled');
          $('#iQueryUserGroupModal').modal('hide');
        })
      }
      if (data.status == 'error') {
        toastr.error(data.data);
        return false;
      }
    })

  })

  return {
    init : function(opts) {
      self.contextPath = opts.contextPath;
      self.userId = opts.userId;
      // self.iQueryUserGroups = opts.iQueryUserGroups;
      self.hadoopUserGroups = opts.hadoopUserGroups;
      initDatatable();
    },

    // 批准
    ratify : function(registerId) {
      $.post(self.contextPath + '/register/operate', {
        "id" : registerId,
        "operate" : 1,
        "hadoopUserGroupName" : $('#id-iQueryUserGroupSelect_' + registerId).val()
      }, function(data) {
        if (data.status == 'ok') {
          // self.datatable.draw(false);
          var settings = $("#dataTable").dataTable().fnSettings();
          $("#dataTable").dataTable().fnPageChange(settings._iDisplayStart / settings._iDisplayLength);
        }
      });
    },

    // 拒绝
    reject : function(registerId) {
      $.post(self.contextPath + '/register/operate', {
        "id" : registerId,
        "operate" : 0
      }, function(data) {
        if (data.status == 'ok') {
          self.datatable.draw(false);
          var settings = $("#dataTable").dataTable().fnSettings();
          $("#dataTable").dataTable().fnPageChange(settings._iDisplayStart / settings._iDisplayLength);
        }
      });
    }
  }
}();

var selectHadoopUserGroup = function(registerId, deptId) {
  var id = $('#id-iQueryUserGroupSelect_' + registerId).val();
  if (id == '0') {// 新增
    // 清空
    $('#iQueryUserGroupForm')[0].reset();
    $('#id-hiddenRegisterId').val(registerId);
    $('#iQueryUserGroupForm input[name="registerDeptId"]').val(deptId);
    $('#iQueryUserGroupModal').modal('show');
  }
}