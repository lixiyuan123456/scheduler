var ECharts={  
//加载Echarts配置文件 
ChartConfig: function (container, option) { //container:为页面要渲染图表的容器，option为已经初始化好的图表类型的option配置

	//var base_path = "/bakery";
	var base_path = "";
	var chart_path = base_path+"/assets/global/plugins/echarts/echarts.min";//配置图表请求路径
	
    require.config({//引入常用的图表类型的配置

        paths: {

            echarts: chart_path
        }

    });

    this.option = { chart: {}, option: option, container: container };
    return this.option;

},

	 
 
  
//数据格式化  
ChartDataFormate: {

    FormateNOGroupData: function (data) {//data的格式如上的Result1，这种格式的数据，多用于饼图、单一的柱形图的数据源

        var categories = [];

        var datas = [];

        for (var i = 0; i < data.length; i++) {

            categories.push(data[i].name || "");

            datas.push({ name: data[i].name, value: data[i].value || 0 });

        }

        return { category: categories, data: datas };

    },

    FormateGroupData: function (data, type, is_stack) {//data的格式如上的Result2，type为要渲染的图表类型：可以为line，bar，is_stack表示为是否是堆积图，这种格式的数据多用于展示多条折线图、分组的柱图

        var chart_type = 'line';

        if (type)

            chart_type = type || 'line';

        var xAxis = [];

        var group = [];

        var series = [];

        for (var i = 0; i < data.length; i++) {

            for (var j = 0; j < xAxis.length && xAxis[j] != data[i].name; j++);

            if (j == xAxis.length)

                xAxis.push(data[i].name);

            for (var k = 0; k < group.length && group[k] != data[i].group; k++);

            if (k == group.length)

                group.push(data[i].group);

        }



        for (var i = 0; i < group.length; i++) {

            var temp = [];

            for (var j = 0; j < data.length; j++) {

                if (group[i] == data[j].group) {

                    if (type == "map")

                        temp.push({ name: data[j].name, value: data[i].value });

                    else

                        temp.push(data[j].value);

                }

            }

            switch (type) {

                case 'bar':

                	var itemStyle = {
                        normal: {
                            label: {
                                show: true,
                                position: 'top',
                                formatter: '{c}'
                            }
                        }
                    };
                	
                    var series_temp = { name: group[i], data: temp, type: chart_type, itemStyle:itemStyle};

                    if (is_stack)

                        series_temp = $.extend({}, { stack: 'stack' }, series_temp);

                    break;

                case 'map':

                    var series_temp = {

                        name: group[i], type: chart_type, mapType: 'china', selectedMode: 'single',

                        itemStyle: {

                            normal: { label: { show: true} },

                            emphasis: { label: { show: true} }

                        },

                        data: temp

                    };

                    break;

                case 'line':

                    var series_temp = { name: group[i], data: temp, type: chart_type };

                    if (is_stack)

                        series_temp = $.extend({}, { stack: 'stack' }, series_temp);

                    break;

                default:

                    var series_temp = { name: group[i], data: temp, type: chart_type };

            }

            series.push(series_temp);

        }

        return { category: group, xAxis: xAxis, series: series };

    },
},
  
//初始化常用的图表类型  
ChartOptionTemplates: {

    CommonOption: {//通用的图表基本配置

        tooltip: {

            trigger: 'axis'//tooltip触发方式:axis以X轴线触发,item以每一个数据项触发

        },

        toolbox: {

            show: true, //是否显示工具栏

            feature: {

                //mark: true,

                dataView: { readOnly: false }, //数据预览

                restore: true, //复原

                saveAsImage: true //是否保存图片

            }

        }

    },

    CommonLineOption: {//通用的折线图表的基本配置

       tooltip: {
            trigger: 'axis'

        },
        
        toolbox: {
	        show : true,
	        feature : {
	            //mark : {show: true},
	            dataView : {show: true, readOnly: false},
	            magicType : {show: true, type: ['line', 'bar']},
	            restore : {show: true},
	            saveAsImage : {show: true}
	        }
	    },
    },

    Pie: function (data, name) {//data:数据格式：{name：xxx,value:xxx}...

        var pie_datas = ECharts.ChartDataFormate.FormateNOGroupData(data);

        var option = {
    		title : {
    	        text: name,
    	        x: 'center'
    	    },
            tooltip: {

                trigger: 'item',

                formatter: '{b} : {c} ({d}/%)',

                show: true

            },
            
            toolbox: {
                show : true,
                feature : {
                    //mark : {show: true},
                    dataView : {show: true, readOnly: false},
                    magicType : {
                        show: true, 
                        type: ['pie', 'funnel'],
                        option: {
                            funnel: {
                                x: '25%',
                                width: '50%',
                                funnelAlign: 'left',
                                max: 1548
                            }
                        }
                    },
                    restore : {show: true},
                    saveAsImage : {show: true}
                }
            },

            legend: {

                orient: 'horizontal',

                x: 'left',

                data: pie_datas.category

            },

            series: [

                {
                	itemStyle:{
                        normal: {
                        	color: function(params) {
                                // build a color map as your need.
                                var colorList = [
                                  '#E87C25','#9BCA63','#26a69a','#C1232B'
                                ];
                                return colorList[params.dataIndex]
                            },
                            label: {
                                show: true,
                                position: 'top',
                                formatter: '{b}:{c}'
                            }
                        }
                    },
                    name: name || "",

                    type: 'pie',

                    radius: ['50%', '70%'],
                    avoidLabelOverlap: false,
                    label: {
                        normal: {
                            show: false,
                            position: 'center'
                        },
                        emphasis: {
                            show: true,
                            textStyle: {
                                fontSize: '30',
                                fontWeight: 'bold'
                            }
                        }
                    },
                    labelLine: {
                        normal: {
                            show: false
                        }
                    },
                    data: pie_datas.data

                }

            ]

        };

        return $.extend({}, ECharts.ChartOptionTemplates.CommonOption, option);

    },

Lines: function (data, name, is_stack) { //data:数据格式：{name：xxx,group:xxx,value:xxx}...

        var stackline_datas = ECharts.ChartDataFormate.FormateGroupData(data, 'line', is_stack);

        var option = {

            legend: {

                data: stackline_datas.category

            },
            
            dataZoom: {
                show: true,
                realtime : true,
                start : 80
            },

            xAxis: [{

                type: 'category', //X轴均为category，Y轴均为value

                data: stackline_datas.xAxis,

                boundaryGap: false//数值轴两端的空白策略

            }],

            yAxis: [{

                name: name || '',

                type: 'value',

                splitArea: { show: true }

            }],

            series: stackline_datas.series

        };

        return $.extend({}, ECharts.ChartOptionTemplates.CommonLineOption, option);

    },
Bar:  function (data, name, is_stack) {//data:数据格式：{name：xxx,group:xxx,value:xxx}...
	var bars_data = ECharts.ChartDataFormate.FormateGroupData(data, 'bar', is_stack);
	option = {
		    tooltip : {
		        trigger: 'axis',
	        	axisPointer : {           // 坐标轴指示器，坐标轴触发有效
	                type : 'shadow'       // 默认为直线，可选为：'line' | 'shadow'
	            }
		    },
		    grid: {
		        left: '3%',
		        right: '4%',
		        bottom: '3%',
		        containLabel: true
		    },
		    xAxis : [
		        {
		            type : 'category',
		            data : bars_data.xAxis,
		            axisTick: {
		                alignWithLabel: true
		            }
		        }
		    ],
		    yAxis : [
		        {
		            type : 'value'
		        }
		    ],
		    series : bars_data.series
		};
	return $.extend({}, ECharts.ChartOptionTemplates.CommonLineOption, option);
},

BarReverse:  function (data, name, is_stack) {//data:数据格式：{name：xxx,group:xxx,value:xxx}...
	var bars_data = ECharts.ChartDataFormate.FormateGroupData(data, 'bar', is_stack);
	option = {
		    tooltip : {
		        trigger: 'axis'
		    },
		    //calculable : true,
		    xAxis : [
		        {
		        	type : 'value'
		        }
		    ],
		    yAxis : [
		        {
		        	data : bars_data.xAxis,
		            type : 'category'	
		        }
		    ],
		    series : bars_data.series
		};
	return $.extend({}, ECharts.ChartOptionTemplates.CommonLineOption, option);
},

Bars: function (data, name, is_stack) {//data:数据格式：{name：xxx,group:xxx,value:xxx}...

        var bars_dates = ECharts.ChartDataFormate.FormateGroupData(data, 'bar', is_stack);

        var option = {

            legend: bars_dates.category,

            xAxis: [{

                type: 'category',

                data: bars_dates.xAxis,

                axisLabel: {

                    show: true,

                    interval: 'auto',

                    rotate: 0,

                    margion: 8

                }

            }],

            yAxis: [{

                type: 'value',

                name: name || '',

                splitArea: { show: true }

            }],

            series: bars_dates.series

        };

        return $.extend({}, ECharts.ChartOptionTemplates.CommonLineOption, option);

    }

//其他的图表配置，如柱图+折线、地图
},

  
//渲染图表
Charts: {
    RenderChart: function (option) {
        require([
                 'echarts'
      ],
      function (ec) {
          echarts = ec;
          if (option.chart && option.chart.dispose)
              option.chart.dispose();
          
          option.chart = echarts.init(option.container);
          window.onresize = option.chart.resize;
          option.chart.setOption(option.option, true);

      });

    }
},

//渲染其他的图表类型，如：地图  
RenderMap:function(option){}//渲染地图 
  
};