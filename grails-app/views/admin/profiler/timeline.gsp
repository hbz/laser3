<%@ page import="de.laser.system.SystemProfiler" %>
<laser:htmlStart message="menu.admin.profilerTimeline">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.profiler" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.profiler" type="admin" total="${SystemProfiler.executeQuery('select count(*) from SystemProfiler')[0]}" />

    <g:render template="profiler/menu" />

    <div class="ui fluid card">
        <table class="ui la-table compact table">
            <thead>
                <tr class="center aligned">
                    <th>Seitenaufrufe am Tag - Top 10</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <div id="echart-timeline"></div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.profiler = {}

        JSPC.app.profiler.chart_config= {
            xAxis: {
               type: 'category',
               data: []
            },
            yAxis: {
               type: 'value',
               minInterval: 1
            },
            grid: {
               top:20, right:30, bottom:30, left:50
            },
            tooltip: {
                trigger: 'axis',
                formatter: function (params, event) {
                    let content = '<br/>'
                    let top10tmp = []
                    params.forEach(function (e) { top10tmp.push(e.value) })
                    let top10 = top10tmp.sort((a, b) => b - a).slice(0, 10)

                    let ov = 0
                    params.sort(function(a,b){ return b.value - a.value }).forEach(function (e) {
                        if (e.value > 0) {
                            if (e.value >= top10[9]) {
                                content = content + '<br/>' + e.marker + ' <span>' + e.seriesName + '</span>&nbsp;&nbsp;&nbsp;<strong style="float:right">' + e.value + '</strong>'
                            }
                            else {
                                ov = ov + e.value
                            }
                        }
                    })
                    if (ov) {
                        content = content + '<br/><span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:#dedede;"></span> <span> weitere Aufrufe</span>&nbsp;&nbsp;&nbsp;<span style="float:right">' + ov + '</span>'
                    }

                    return '<div><strong>' + params[0].name + '</strong>' + content + '</div>'
                }
            },
            series: []
        }

        JSPC.app.profiler.chart_config.xAxis.data = [<% print '"' + globalTimelineDates.collect{ it.length() ? it.substring(0,5) : it }.join('","') + '"' %>];

        <g:each in="${globalTimelineOrder}" var="ik,iv" status="index">
            <g:set var="itemValue" value="${globalTimeline[ik]}" />

            JSPC.app.profiler.chart_config.series.push( {
                name: '${ik}',
                        type: 'bar',
                        stack: 'total',
                        animation: true,
                        animationDuration: 150,
                        emphasis: { focus: 'series' },
                        data: [<% print itemValue.join(', ') %>]
                    });

        </g:each>

        JSPC.app.profiler.chart = echarts.init( $('#echart-timeline')[0] );
        JSPC.app.profiler.chart.setOption( JSPC.app.profiler.chart_config);
        $(window).resize(function () {
            JSPC.app.profiler.chart.resize();
        });
    </laser:script>

    <style>
    #echart-timeline {
        width: 100%;
        height: 600px;
    }
    </style>

<laser:htmlEnd />