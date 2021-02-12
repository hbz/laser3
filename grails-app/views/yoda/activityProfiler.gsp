<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.activityProfiler')}</title>
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.activityProfiler" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.activityProfiler')}</h1>

    <table class="ui celled la-table compact wide table">
        <thead>
            <tr class="center aligned">
                <th>Zeitraum</th>
                <th colspan="4">Benutzer online (min/max)</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${activity}" var="itemKey,itemValue" status="index">
                <tr>
                    <td class="center aligned">
                        ${itemKey}
                    </td>
                    <td colspan="4">
                        <div id="ct-chart-${index}" class="echarts-wrapper"></div>

                        <laser:script file="${this.getGroovyPageFileName()}">

                            JSPC.app.chartData_${index} = {
                                xAxis: {
                                    type: 'category',
                                    boundaryGap: false,
                                    data: [<% println '"' + labels.collect{ it.length() ? it.substring(0,3) + '00' : it }.join('","') + '"' %>]
                                },
                                yAxis: {
                                    type: 'value'
                                },
                                grid: {
                                    top:20, right:20, bottom:30, left:40
                                },
                                tooltip: {
                                    trigger: 'axis'
                                },
                                series: [
                                {
                                    data: [<% println itemValue[1].join(',') %>],
                                    name: 'max',
                                    stack: 'max',
                                    type: 'line',
                                    areaStyle: { color: '<%= index == 0 ? '#bad722' : '#bad722' %>' },
                                    smooth: true,
                                    lineStyle: { color: '<%= index == 0 ? '#98b500' : '#98b500' %>', width: 3 },
                                },
                                {
                                    data: [<% println itemValue[0].join(', ') %>],
                                    name: 'min',
                                    stack: 'min',
                                    type: 'line',
                                    areaStyle: { color: '<%= index == 0 ? '#bad722' : '#bad722' %>' },
                                    smooth: true,
                                    lineStyle: { color: '<%= index == 0 ? '#98b500' : '#98b500' %>', width: 3 },
                                }
                                ]
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
            height: 150px;
        }
    </style>
</body>
</html>