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
    <semui:crumb message="menu.yoda.profiler" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top">${message(code:'menu.yoda.profiler')}</h1>

    <nav class="ui secondary menu">
        <g:link controller="yoda" action="systemProfiler" class="item">Ladezeiten</g:link>
        <g:link controller="yoda" action="activityProfiler" class="item">Nutzerzahlen</g:link>
        <g:link controller="yoda" action="timelineProfiler" class="item active">Seitenaufrufe</g:link>
    </nav>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr class="center aligned">
                <th>Registrierte Seitenaufrufe am Tag</th>
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
                            <g:if test="${index == 0}">
                                JSPC.app.chartData_base = {
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
                                        trigger: 'axis'
                                    },
                                    series: [{
                                        data: [],
                                        name: 'Seitenaufrufe',
                                        type: 'line',
                                        smooth: true,
                                        animation: false,
                                        areaStyle: { color: 'rgba(58,111,196, 0.3)' },
                                        lineStyle: { color: 'rgb(58,111,196)', width: 2 },
                                    }]
                                }
                            </g:if>

                            JSPC.app.chartData_${index} = Object.assign({}, JSPC.app.chartData_base);
                            JSPC.app.chartData_${index}.xAxis.data = [<% print '"' + globalTimelineDates.collect{ it.length() ? it.substring(0,5) : it }.join('","') + '"' %>];
                            JSPC.app.chartData_${index}.series[0].data = [<% print itemValue.join(', ') %>];

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