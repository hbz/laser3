<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.timelineProfiler')}</title>
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.timelineProfiler" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.timelineProfiler')}</h1>

    <table class="ui celled la-table compact table">
        <thead>
            <tr class="center aligned">
                <th>Timeline</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${globalTimelineOrder}" var="ik,iv" status="index">
                <g:set var="itemValue" value="${globalTimeline[ik]}" />

                <tr>
                    <td>
                        <strong>${ik}</strong> (${iv}) <br />

                        <div id="ct-chart-${index}" class="echarts-wrapper"></div>

                        <laser:script file="${this.getGroovyPageFileName()}">
                            JSPC.app.chartData_${index} = {
                                xAxis: {
                                    type: 'category',
                                    boundaryGap: false,
                                    data: [<% println '"' + globalTimelineDates.collect{ it.length() ? it.substring(0,5) : it }.join('","') + '"' %>]
                                },
                                yAxis: {
                                    type: 'value'
                                },
                                grid: {
                                    top:20, right:30, bottom:30, left:50
                                },
                                tooltip: {
                                    trigger: 'axis'
                                },
                                series: [{
                                    data: [<% println itemValue.join(', ') %>],
                                    type: 'line',
                                    areaStyle: { color: '#bad722' },
                                    smooth: true,
                                    lineStyle: { color: '#98b500', width: 3 },
                                }]
                            };

                            echarts.init( $('#ct-chart-${index}')[0] ).setOption( JSPC.app.chartData_${index} );
                        </laser:script>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <style>
    .echarts-wrapper {
        width: 100%;
        height: 130px;
    }
    </style>
</body>
</html>