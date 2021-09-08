function packRelationData(){
  var mode = $('#taskConfig input[name="outputMode"]:checked').val();
  var value = '';
  if(mode == 1){
    var arr = new Array();
    $('#taskConfig select[name="outputModeValue"] option:selected').each(function(){
      var element = new Object();
      element.id = $(this).val();
      element.name = $(this).attr('data-fullname');
      arr.push(element);
    });
    value = JSON.stringify(arr);
  }else if(mode == 2){
    value = $('#taskConfig input[name="outputModeValue"]').val();
  }
  var json = new Object();
  json.mode = mode;
  json.value = value;
  return JSON.stringify(json);
}
function writeRelationDataBack(task) {
  if (!task || !task.details || task.details == '') {
    return false;
  }
  var json = JSON.parse(task.details);
  if(!json.out || json.out == ''){
    return false;
  }
  var out = JSON.parse(json.out);
  $('#taskConfig input[name="outputMode"][value="' + out.mode + '"]').attr('checked', true).trigger('click');
  if (out.mode == 1 && out.value && out.value != '') {
    var arr = JSON.parse(out.value);
    var tableIds = new Array();
    $.each(arr,function(i,e){
      tableIds.push(e.id);
    });
    if(tableIds.length == 0){
      return false;
    }
    $.get('/metadata/common/table/matchByTableIds.do',{tableIds:tableIds},function(re){
      if (re.status != 'ok') {
        bootbox.alert(re.message);
        return false;
      }
      var html = '';
      $.each(re.data, function(i, e) {
        html += '<option value="' + e.id + '" data-id="' + e.id + '" data-name="' + e.name + '" data-fullName="' + e.fullName + '" selected="selected">' + e.fullName + '</option>';
      });
      $('#taskConfig select[name="outputModeValue"]').empty().append(html);
      $('#taskConfig select[name="outputModeValue"]').selectpicker('refresh');
    });
  }
  if (out.mode == 2) {
    $('#taskConfig input[name="outputModeValue"]').val(out.value);
  }
}

function initializeDrawDependencyRelationshipMap(data){
  var graph = new G6.Graph({
    container: 'jobDependencyRelationshipMapViewer',
    width: window.innerWidth,
    height: window.innerHeight,
    modes: {
      default: [
        /*{
          type: 'collapse-expand',
          onChange: function onChange(item, collapsed) {
            var data = item.get('model').data;
            data.collapsed = collapsed;
            return true;
          }
        },*/
        {
          type: 'tooltip',
          formatText(item) {
            return item.label;
          }
        },
        'drag-canvas',
        'zoom-canvas',
      ]
    },
    /*defaultEdge: {
      shape: 'cubic-horizontal'
    },*/
    /*layout: {
      type: 'mindmap',
      direction: 'H',
      getHeight: function getHeight() {
        return 16;
      },
      getWidth: function getWidth() {
        return 16;
      },
      getVGap: function getVGap() {
        return 10;
      },
      getHGap: function getHGap() {
        return 100;
      }
    }*/
  });
  graph.data(data);
  graph.render();
  graph.fitView();
  graph.on('node:click', (ev) => {
    var model = ev.item.getModel();
    if(model.nodeType != 1){
      toastr.error('该节点不是任务节点，仅支持点击任务节点');
      return false;
    }
    redrawDependencyRelationshipMap(graph,model.id);
  });
}

function redrawDependencyRelationshipMap(graph,jobId){
  var data = reloadDependencyRelationshipData(jobId);
  graph.changeData(data);
}

function reloadDependencyRelationshipData(jobId){
  var result = new Object();
  $.ajax({
    url : '/scheduler/task/api/getDependencyRelationship.do',
    data : {jobId:jobId},
    async : false,
    dataType : 'json',
    success : function(res){
      result = res;
    }
  });
  return result;
}

function drawDependencyRelationshipMap(jobId){
  var data = reloadDependencyRelationshipData(jobId);
  initializeDrawDependencyRelationshipMap(data);
}

$(function() {
  // 血缘关系
  var listenToIoModeChangeEvent = function(tag, ioMode) {
    if (ioMode == 0) {// 无
      $('#' + tag + 'ModeValue').val('').attr('readonly', true).show();
      $('#' + tag + 'ModeValueSelect').empty().selectpicker('refresh').selectpicker('hide');
    }
    if (ioMode == 1) {// 数据表
      $('#' + tag + 'ModeValue').val('').attr('readonly', true).hide();
      $('#' + tag + 'ModeValueSelect').empty().selectpicker('refresh').selectpicker('show');
    }
    if (ioMode == 2) {// HDFS
      $('#' + tag + 'ModeValue').val('').attr('readonly', false).show();
      $('#' + tag + 'ModeValueSelect').empty().selectpicker('refresh').selectpicker('hide');
    }
  };
  $('input[name$="Mode"]').on('click', function() {
    listenToIoModeChangeEvent($(this).attr('name').replace('Mode', ''), $(this).val());
  });
  $('select[id$="ModeValueSelect"]').selectpicker('hide').on('shown.bs.select', function() {
    var selector = $(this);
    selector.parent().find(".bs-searchbox").find("input").unbind('keydown').attr("placeholder", "请输入关键字查询").bind('keydown', function(e) {
      if (e.which == 13) {
        $.get('/metadata/common/table/matchByTableName.do', {
          tableName : $(this).val()
        }, function(re) {
          if (re.status != 'ok') {
            bootbox.alert(re.message);
            return false;
          }
          var html = '';
          $.each(re.data, function(i, e) {
            html += '<option value="' + e.id + '" data-id="' + e.id + '" data-name="' + e.name + '" data-fullName="' + e.fullName + '">' + e.fullName + '</option>';
          });
          selector.find('option:selected').each(function() {
            html += '<option value="' + $(this).attr('value') + '" data-id="' + $(this).attr('data-id') + '" data-name="' + $(this).attr('data-name') + '" data-fullName="' + $(this).attr('data-fullName') + '" selected="selected">' + $(this).text() + '</option>';
          });
          selector.empty().append(html);
          selector.selectpicker('refresh');
        });
      }
    });
  }).on('changed.bs.select', function(e, clickedIndex, isSelected, previousValue) {
    // $(this).val($(this).selectpicker('val'));
  });
});
