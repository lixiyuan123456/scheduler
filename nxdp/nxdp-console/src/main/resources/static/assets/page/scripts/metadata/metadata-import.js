var Utils = function() {
  return {
    getselect2: function(sel) {
      if (sel instanceof Array) {
        if (sel.length > 0) {
          return sel[0];
        } else {
          return {};
        }
      } else if (sel instanceof Object) {
        return sel;
      } else return {};
    },
  }
}();

toastr.options = {
    "closeButton": false,
    "debug": false,
    "newestOnTop": false,
    "progressBar": false,
    "positionClass": "toast-top-right",
    "preventDuplicates": false,
    "onclick": null,
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "5000",
    "extendedTimeOut": "1000",
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut"
  }


var MyDataTable = function(){
	return {
		init : function(){
			$('#dataTable').DataTable({
				  "bProcessing": true,
			      "language": {
			        "emptyTable": "无结果",
			        "lengthMenu": "每页&nbsp; _MENU_ &nbsp;项",
			        "search": "查找：",
			        "processing": "正在加载...",
			        "info": "显示 _START_ 到 _END_ 共 _TOTAL_ 条记录",
			      },
			      "info": true,
			      "ordering": false,
			      "paging": true,
			      "searching": false,
			      //"serverSide": true,
			      //"pageLength": 10,
			      "ajax": {
			          url: '/metadata/metadata-import/list-db-tables',
			          type: 'POST',
			          data:function(d){
			        	  d.serverId = $('#servers').val();
			      	      d.dbName = $('#dbs').val();
			      	      d.tableKw = $('#tableKw').val();
			          },
			          "dataSrc": "data"
			        },
			        "columns": [
			           { data: null,
			             render: function(data, type, full, meta) {
			               return data.tableName;
			             }
			           },
			           { data: null,
			             render: function(data, type, full, meta) {
			            	 var html  = '';
			            	 if(data.status == 'NEVER_LOADED'){
			            		 html = '<span class="btn btn-xs yellow">未导入</span>';
			            	 }else if(data.status == 'LOADED'){
			            		 html = '<span class="btn btn-xs green">已导入</span>';
			            	 }
			                 return html;
			             }
			           },
			           { data: null,
			             render: function(data, type, full, meta) {
			            	 var html  = '';
			            	 html  += "<div style='display: inline'>"
				                   +"<div style='display: inline; padding-right: 8px'><input type='checkbox' name='checkbox-unit' value='"
				                   + data.tableName + "' class='checkboxes' style='width: 15px; height: 15px;' />"
				                   + "</div>";
			            	 if(data.status == 'NEVER_LOADED'){
			            		 html += '<a href="#" class="btn btn-xs yellow" onclick="importMetadata(\''+data.tableName+'\')"><i class="fa fa-level-down">&nbsp;</i>导入</a>';
			            	 }else if(data.status == 'LOADED'){
			            		 html += '<a href="#" class="btn btn-xs green" onclick="importMetadata(\''+data.tableName+'\')"><i class="fa fa-refresh">&nbsp;</i>重新导入</a>';
			            	 }
			                 return html;
			               }
			           },
			         ],
			  })
		},
	}
}();

var importMetadata = function (tableNames) {
	var arr = [];
	if(typeof tableNames=='object' && tableNames.constructor==Array){
		arr = tableNames;
	}
	if(typeof tableNames=='string' && tableNames.constructor==String){
		arr.push(tableNames);
	}
	var data = {
	    serverId : $('#servers').val(),
	    dbName : $('#dbs').val(),
	    tableNames : arr
	}
	console.log(data);
	$.post("/metadata/metadata-import/import-metadata.do",data,function(resp){
		if(resp.status == 'ok'){
			toastr['success']('导入成功', '提示');
			$('#dataTable').DataTable().ajax.reload(null,false);
		}
	})
}

var MetadataImport = function(){

  var self = $(this);

  /*var initData = function() {
    self.serverId = 24;
    self.dbId = -1;
  }*/
  
  $('.group-checkable').on('change',function(){
	  if(this.checked){
		  $('.checkboxes').attr('checked','checked');
	  }else{
		  $('.checkboxes').removeAttr('checked');
	  }
  })
  
  $('#import-selected-button').click(function(){
	  var tableNames = [];
	  $("input[name='checkbox-unit']:checked").each(function(i,v){
		  tableNames.push($(this).val());
	  })
	  if(tableNames.length == 0){
		  return false;
	  }
	  importMetadata(tableNames);
  })
  
  var queryServers = function(){
	  self.servers = [];
	  $.post(self.contextPath + "/metadata/server/api/list-server-configs",function(data){
		  if(data.status == 'success'){
			  $.each(data.servers,function(i,server){
				  self.servers.push({id: server.id, text: server.name, selected: false});
			  })
			  $('#servers').select2({
				  placeholder: '选择服务器',
		          data: self.servers
			  }).on('change', function(){
				  self.serverId = $(this).val();
		          queryDbs({serverId: self.serverId});
			  })
		  }
	  })
  }
  
  var queryDbs = function(data){
	  self.dbs = [];
	  $.post(self.contextPath + "/metadata/metadata-import/list-dbs", data, function(data) {
		  if (data.status == 'success') {
	    	  $.each(data.dbs,function(i,db){
	    		  if (self.dbId == db.id) {
	  	            self.dbs.push({id: db.SCHEMA_NAME, text: db.SCHEMA_NAME, selected: true});
	  	          } else {
	  	            self.dbs.push({id: db.SCHEMA_NAME, text: db.SCHEMA_NAME, selected: false});
	  	          }
	    	  })
	    	  $('#dbs').select2({
				  placeholder: '选择数据库',
		          data: self.dbs
			  }).on('change', function(){
				  self.dbName = $(this).val();
		          //queryDbs({serverId: self.serverId});
			  })
	      }
	  })
  }
  
  $('#searchButton').click(function(){
	  var data = {
			  serverId : $('#servers').val(),
			  dbName : $('#dbs').val(),
			  tableKw : $('#tableKw').val()
	  }
	  console.log(data);
	  if(data.serverId == '' || data.dbName == ''){
		  toastr['error']('查询条件不完整', '提示');
		  return;
	  }
	  $('#dataTable').DataTable().ajax.reload();
	  //queryTables(data);
  })
  
  
  var queryTables = function(args){
	  $('#dataTable').DataTable({
		  "bProcessing": true,
	      "language": {
	        "emptyTable": "无结果",
	        "lengthMenu": "每页&nbsp; _MENU_ &nbsp;项",
	        "search": "查找：",
	        "processing": "正在加载...",
	        "info": "显示 _START_ 到 _END_ 共 _TOTAL_ 条记录",
	      },
	      "info": true,
	      "ordering": false,
	      "paging": true,
	      "searching": false,
	      //"serverSide": true,
	      //"pageLength": 10,
	      "ajax": {
	          url: self.contextPath + '/metadata/metadata-import/list-db-tables',
	          type: 'POST',
	          data: args,
	          "dataSrc": "data"
	        },
	        "columns": [
	           { data: null,
	             render: function(data, type, full, meta) {
	               return data.tableName;
	             }
	           },
	           { data: null,
	             render: function(data, type, full, meta) {
	            	 var html  = '';
	            	 if(data.status == 'NEVER_LOADED'){
	            		 html = '<span class="btn btn-xs yellow">未导入</span>';
	            	 }else if(data.status == 'LOADED'){
	            		 html = '<span class="btn btn-xs green">已导入</span>';
	            	 }
	                 return html;
	             }
	           },
	           { data: null,
	             render: function(data, type, full, meta) {
	            	 var html  = '';
	            	 html  += "<div style='display: inline'>"
		                   +"<div style='display: inline; padding-right: 8px'><input type='checkbox' name='checkbox-unit' value='"
		                   + data.tableName + "' class='checkboxes' />"
		                   + "</div>";
	            	 if(data.status == 'NEVER_LOADED'){
	            		 html += '<a href="#" class="btn btn-xs yellow" onclick="importMetadata(\''+data.tableName+'\')"><i class="fa fa-level-down">&nbsp;</i>导入</a>';
	            	 }else if(data.status == 'LOADED'){
	            		 html += '<a href="#" class="btn btn-xs green" onclick="importMetadata(\''+data.tableName+'\')"><i class="fa fa-refresh">&nbsp;</i>重新导入</a>';
	            	 }
	                 return html;
	               }
	           },
	         ],
	  })
  }
  

  /*
   * 初始化数据
   */
  var initTemplate = function() {
    self.servers = [];

    function formatState (state) {
      //if (!state.id) { return state.text; }
      var $state = $(
        '<span>' + state.text + '</span>'
      );
      return $state;
    };

    jQuery('#databases').select2({
      "language": {
        "noResults": function() {
          return "无搜索结果";
        }
      },
      placeholder: '选择数据库',
      templateResult: formatState,
    }).on('change', function() {
      stopFreshDataTable();
      self.dbId = $(this).val();
      self.oTable.ajax.reload();
    });

    $.post(self.contextPath + "/metadata/server/api/list-server-configs", function(data, status) {
      if (data.status == 'success') {
        _.each(data.servers, function(server) {
          if (server.id == self.serverId) {
            self.servers.push({id: server.id, text: server.name, selected: true});
            return;
          } else {
            self.servers.push({id: server.id, text: server.name, selected: false});
          }
        });
        jQuery('#servers').select2({
          "language": {
            "noResults": function() {
              return "无搜索结果";
            }
          },
          placeholder: '选择服务器',
          data: self.servers
        }).on('change', function() {
          self.serverId = $(this).val();
          fetchDatabases({serverId: self.serverId});
        });

        self.serverId = Utils.getselect2(jQuery('#servers').select2('data')).id;
        fetchDatabases({serverId: self.serverId});
      } else {
        console.log('获取服务器列表失败.');
      }
    });
  };

  var initMonitorSearch = function() {
    $('#kw').keyup(function(event) {
      if (event.keyCode == 13) {
        stopFreshDataTable();
        self.oTable.ajax.reload();
      }
    });
    $('#doSearch').on('click', function() {
      stopFreshDataTable();
      self.oTable.ajax.reload();
    });
  }

  var fetchDatabases = function(params) {
    self.databases = [];
    $.post(self.contextPath + "/metadata/db/api/list-dbs", params, function(data, status) {
      if (data.status == 'success') {
        _.each(data.databases, function(db) {
          if (self.dbId == db.id) {
            self.databases.push({id: db.id, text: db.dbName, selected: true});
          } else {
            self.databases.push({id: db.id, text: db.dbName, selected: false});
          }
        });
        function formatState (state) {
          if (!state.id) { return state.text; }
          var $state = $(
                '<span> '+ state.text +'</span>'
          );
          return $state;
        };

        //jQuery('#databases').select2('destroy');
        jQuery('#databases').select2().empty();
        jQuery('#databases').select2('');
        jQuery('#databases').select2({data: self.databases, templateResult: formatState});
      } else {
        console.log('获取服务器列表失败.');
      }
      stopFreshDataTable();
      self.dbId = Utils.getselect2($('#databases').select2('data')).id;
      if (self.dbId) {
        self.oTable.ajax.reload();
      }
    });
  };

  var initTables = function() {
    var table = $('#dataTable');
    self.oTable = table.DataTable({
      "bProcessing": true,
      "language": {
        "emptyTable": "无结果",
        "lengthMenu": "每页&nbsp; _MENU_ &nbsp;项",
        "search": "查找：",
        "processing": "正在加载...",
        "info": "显示 _START_ 到 _END_ 共 _TOTAL_ 条记录",
      },
      "info": true,
      "ordering": false,
      "paging": true,
      "searching": false,
      //"serverSide": true,
      "pageLength": 10,
      "ajax": {
        url: self.contextPath + '/metadata/metadata-import/list-db-tables',
        type: 'POST',
        data: function(d) {
          d.serverId = self.serverId;
          d.dbId = self.dbId;
          d.tableName = $('#kw').val();
          d.columns = null;
          //d.search = null;
        },
        "dataSrc": "data",
      },
      "columns": [
         { data: null,
           render: function(data, type, full, meta) {
             var innerHTML = data.table_name;
             if(/\d{6,}$/.test(data.table_name)) {
               innerHTML = innerHTML + " <a href='javascript:;' class='tooltips'" +
                   " data-original-title='The last tip!' onclick='TableLoad.loadCycleTable(\"" + data.table_name + "\")'><i class='fa fa-calendar '></i></a>";
             }
             //class="tooltips" title="12" data-original-title="The last tip!"
             return innerHTML;
           }
         },
         { data: null,
           render: function(data, type, full, meta) {
             if (data.table_id) {
               if (data.table_id > 0) {
                 tdInnerHtml = "<a href='" + self.contextPath + "/metadata/metadata-detail?tableId=" + data.table_id
                   + "' target='_blank' class='btn btn-xs green'>已导入 <i class='fa fa-search'></i></a>"
               }
               return tdInnerHtml;
             }

             tdInnerHtml = "<span class='label bg-yellow'>未导入</span>"
             return tdInnerHtml;
           }
         },
         { data: null,
           render: function(data, type, full, meta) {
             var tdInnerHtml = "<div style='display: inline'>" +
                 "<div style='display: inline; padding-right: 8px'><input type='checkbox' name='"
                 + data.table_name + "' class='checkboxes' style='width: 15px; height: 15px;' />"
                 + "</div>";
             if (data.table_id) {
               if (data.table_id > 0) {
                 tdInnerHtml = tdInnerHtml + "<button style='position: absolute' onclick='TableLoad.loadStruct(\"" + data.table_name
                   + "\")' class='btn btn-xs green'>" + "<i class='fa fa-refresh'></i>重新导入</button></div>"
               }
               return tdInnerHtml;
             }

             tdInnerHtml = tdInnerHtml + "<button style='position: absolute' onclick='TableLoad.loadStruct(\"" + data.table_name
               + "\")' class='btn btn-xs blue'>" + "<i class='fa fa-level-down'></i>导入</button></div>"
             return tdInnerHtml;
           }
         },
       ],
    });
    $('#dataTable_processing').hide();
    var tableWrapper = jQuery('#dataTable_wrapper');

    table.find('.group-checkable').change(function () {
        var set = jQuery(this).attr("data-set");
        var checked = jQuery(this).is(":checked");
        jQuery(set).each(function () {
            if (checked) {
                $(this).attr("checked", true);
                $(this).parents('tr').addClass("active");
            } else {
                $(this).attr("checked", false);
                $(this).parents('tr').removeClass("active");
            }
        });
        jQuery.uniform.update(set);
    });

    $('#optionChecked').on('click', function() {
      var cf = confirm('确定导入当前所有？');
      if (cf) {
        jQuery("#dataTable .checkboxes").each(function () {
          if (jQuery(this).is(":checked")) {
            TableLoad.loadStruct(jQuery(this)[0].name);
          }
        });
      }
    });

    table.on('change', 'tbody tr .checkboxes', function () {
        $(this).parents('tr').toggleClass("active");
    });

    tableWrapper.find('.dataTables_length select').addClass("form-control input-xsmall input-inline"); // modify table per page dropdown
  };

  var cycleLoadTable = function() {
    $('#cycleTemplateSel').on('change', function() {
      var _suffix = $(this).val();
      var tableName =  $('.cycleTemplateTable').html();
      var _ogt = tableName.replace(/\d{6,}$/, '');
      _.each(['YYYYMMDD', 'YYYYMM'], function(suffix) {
        if (suffix == _suffix) {
          var suffixAppend = "${dateSuffix}";
          if (suffix == "YYYYMM") {
            suffixAppend = "${monthSuffix}";
          }
          $('.code1').html('SET jdbc2hive.runtime.tablename.'+ suffix +'=' + suffixAppend);
          $('.code2').html('SELECT * FROM ' + _ogt + suffix);
        }
      });
    });

    $('.cycleLoadBtn').on('click', function() {
      var tableName =  $('.cycleTemplateTable').html();
      var suffix = $('#cycleTemplateSel').val();
      TableLoad.loadStruct(tableName, suffix);
    });

  };

  var refreshDataTable = function() {
    self.interVal = setInterval(function() {
      $.ajax({
        url: self.contextPath + '/metadata/table-load/api/get-load-tables',
        type: 'POST',
        data: {},
        async: true,
        success: function(rs, status) {
          if (rs.status == 'runing') {
            //提示load完成
            var page = self.oTable.page.info().page;
            self.oTable.page(page).draw(false);
            _.each(rs.loadTables, function(table) {
              toastr['success']('表[' + table.table_name + '] 加载完成', '提示');
            });
          } else if (rs.status == 'done') {
            stopFreshDataTable();
          } else {
            console.log(rs);
            console.log(status);
          }
        },
        error: function(error) {
          console.log(error);
        }
      });
    }, 3000);
  };
  var stopFreshDataTable = function() {
    if (self.interVal) {
      clearInterval(self.interVal);
    }
  };

  var handleDom = function() {
    $('#optionReload').on('click', function() {
      var serverId = $('#servers').val();
      var dbId = $('#databases').val();
      console.log("will reload all loaded tables - [" + serverId + ", " + dbId + "]");
      if (serverId > 0 && dbId > 0) {
        $('#optionReload').attr('disabled', 'disabled');
        $.ajax({
          url: self.contextPath + '/metadata/table-load/api/reload-tables',
          type: 'POST',
          data: {dbId: dbId, serverId: serverId},
          async: true,
          success: function(rs, status) {
            $('#optionReload').removeAttr('disabled', 'disabled');
            if (rs.status == 'success') {
              toastr['info'](rs.msg, '提示');
            } else {
              toastr['error'](rs.msg, '错误');
            }
          },
          error: function() {
            toastr['error']('操作失败', '错误');
            $('#optionReload').removeAttr('disabled', 'disabled');
          }
        });
      } else {
        toastr['error']('请选择数据库.', '错误');
      }
    });
  }

  return {

    init: function(opts) {
      self.contextPath = opts.contextPath;
      queryServers();
      //initData();
      //initTemplate();
      //initMonitorSearch();
      //initTables();
      //cycleLoadTable();
      //handleDom();
    },

    loadStruct: function(tableName, suffix) {
      if (suffix) {
        //..
      } else {
        suffix = null;
      }
      $.ajax({
        url: self.contextPath + '/metadata/table-load/api/load-structure',
        type: 'POST',
        data: {tableName: tableName, dbId: self.dbId, suffix: suffix},
        async: true,
        success: function(rs, status) {
          if (rs.status == 'success') {
            toastr['info']('已提交至后台进程， 稍后会自动刷新', '提示');
            stopFreshDataTable();
            refreshDataTable();
          } else if (rs.status == 'reject') {
            toastr['warning'](rs.msg, '警告');
          } else {
            toastr['error']('导入表结构失败.', '错误');
          }
        },
        error: function() {
          toastr['error']('导入表结构失败.', '错误');
        }
      });
    },
    loadCycleTable: function(tableName) {
      $('.cycleTemplateTable').html(tableName);
      var sel = document.getElementById("cycleTemplateSel");
      $(sel).find('option').remove();
      var suffixPtn = /\d{6,}$/i;
      var cursuffix = "";
      var _suffix = tableName.match(suffixPtn);
      if (_suffix && _suffix.length > 0) {
        cursuffix = _suffix[0];
      }

      $('.code1').html('SET jdbc2hive.runtime.tablename.YYYYMM=${dateSuffix}');
      $('.code2').html('SELECT * FROM ' + _ogt + "YYYYMMDD");

      var _ogt = tableName.replace(/\d{6,}$/, '');
      _.each(['YYYYMMDD', 'YYYYMM'], function(suffix) {
        var option = document.createElement("option");
        option.text = _ogt + suffix;
        option.value = suffix;
        if(suffix.length == cursuffix.length) {
          option.selected = true;
          var suffixAppend = "${dateSuffix}";
          if (suffix == "YYYYMM") {
            suffixAppend = "${monthSuffix}";
          }
          $('.code1').html('SET jdbc2hive.runtime.tablename.'+ suffix +'=' + suffixAppend);
          $('.code2').html('SELECT * FROM ' + _ogt + suffix);
        }
        sel.add(option);
      });
      $('#static').modal('show');
    },
    clearCache: function() {
      $.ajax({
        url: self.contextPath + '/metadata/table-load/api/empty-cache',
        type: 'POST',
        data: {},
        async: true,
        success: function() {
          toastr['success']('缓存清除成功.', '提示');
        },
        error: function() {
          toastr['error']('缓存清除失败.', '错误');
        }
      });
    },
  }
}();
