<%@ page import="de.laser.system.SystemProfiler" %>
<laser:htmlStart message="menu.yoda.profilerTimeline">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.profiler" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.profiler" type="yoda" total="${SystemProfiler.executeQuery('select count(*) from SystemProfiler')[0]}" />

    <g:render template="profiler/menu" />

    <div class="ui segment">

        <table class="ui celled la-js-responsive-table la-table compact table">
            <thead>
                <tr class="center aligned">
                    <th>Registrierte Seitenaufrufe am Tag</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <div id="echart-combined"></div>
                    </td>
                </tr>
            </tbody>
        </table>

    </div>

    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.chartData_combined = {
            xAxis: {
               type: 'category',
               boundaryGap: false,
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
                formatter: function (params,event) {
                    let content = ''
                    let top10tmp = []
                    params.forEach(function (e) { top10tmp.push(e.value) })
                    let top10 = top10tmp.sort((a, b) => b - a).slice(0, 10)

                    params.forEach(function (e) {
                        if (e.value > 0) {
                            if (e.value >= top10[4]) {
                                content = content + '<br/>' + e.marker + ' <span>' + e.seriesName + '</span>&nbsp;&nbsp;&nbsp;<span style="float:right">' + e.value + '</span>'
                                }
                                else {
                                    content = content + '<br/>' + e.marker + ' <span style="color:#c7c9cb">' + e.seriesName + '</span>&nbsp;&nbsp;&nbsp;<span style="color:#c7c9cb;float:right">' + e.value + '</span>'
                                }
                            }
                        })
                        return '<div><strong>' + params[0].name + '</strong>' + content + '</div>'
                    }
                },
                series: []
            }

            JSPC.app.chartData_combined.xAxis.data = [<% print '"' + globalTimelineDates.collect{ it.length() ? it.substring(0,5) : it }.join('","') + '"' %>];

        <g:each in="${globalTimelineOrder}" var="ik,iv" status="index">
            <g:set var="itemValue" value="${globalTimeline[ik]}" />

            JSPC.app.chartData_combined.series.push( {
                name: '${ik}',
                        type: 'bar',
                        stack: 'total',
                        animation: false,
                        data: [<% print itemValue.join(', ') %>]
                    });

        </g:each>

        JSPC.app.chart_combined = echarts.init( $('#echart-combined')[0] );
        JSPC.app.chart_combined.setOption( JSPC.app.chartData_combined );
        $(window).resize(function () {
            JSPC.app.chart_combined.resize();
        });
    </laser:script>

    <style>
    #echart-combined {
        width: 100%;
        height: 600px;
    }
    </style>

<laser:htmlEnd />