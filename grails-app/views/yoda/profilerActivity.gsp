<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.profilerActivity')}</title>
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.profiler" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top">${message(code:'menu.yoda.profiler')}</h1>

    <nav class="ui secondary menu">
        <g:link controller="yoda" action="profilerLoadtime" class="item">Ladezeiten</g:link>
        <g:link controller="yoda" action="profilerActivity" class="item active">Nutzerzahlen</g:link>
        <g:link controller="yoda" action="profilerTimeline" class="item">Seitenaufrufe</g:link>
    </nav>

    <table class="ui celled la-js-responsive-table la-table compact wide table">
        <thead>
            <tr class="center aligned">
                <th>Zeitraum</th>
                <th colspan="4">Benutzer online</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${activity}" var="itemKey,itemValue" status="index">
                <tr>
                    <td class="center aligned">
                        ${itemKey}
                    </td>
                    <td style="width:90%">
                        <div id="ct-chart-${index}" class="echarts-wrapper"></div>

                        <laser:script file="${this.getGroovyPageFileName()}">
                            <g:if test="${index == 0}">
                                JSPC.app.chartData_base = {
                                    xAxis: {
                                        type: 'category',
                                        boundaryGap: false,
                                        data: [<% print '"' + labels.collect{ it.length() ? it.substring(0,3) + '00' : it }.join('","') + '"' %>]
                                    },
                                    yAxis: {
                                        type: 'value',
                                        minInterval: 1
                                    },
                                    grid: {
                                        top:20, right:20, bottom:30, left:40
                                    },
                                    tooltip: {
                                        trigger: 'axis',
                                        formatter: function(params, ticket, callback) {
                                            var label = 'Keine Nutzer online';
                                            var value = '';
                                            if (params.length > 1) {
                                                var max = params[0].value + params[1].value;
                                                if (max > 0 ) {
                                                    label = 'Nutzer online';
                                                    value = params[0].value + '-' + max;
                                                }
                                            } else {
                                                if (params[0].value > 0 ) {
                                                    label = 'Nutzer online (Mittelwert)';
                                                    value = params[0].value.toFixed(2)
                                                }
                                            }
                                            return params[0].name + '<br/>' + params[0].marker + ' ' + label + '&nbsp;&nbsp;&nbsp;<strong style="float:right">' + value + '</strong>'
                                        }
                                    },
                                    series: [
                                        {
                                            data: [],
                                            name: 'Nutzer (min.)',
                                            stack: 'minMax',
                                            type: 'line',
                                            smooth: true,
                                            animation: false,
                                            lineStyle: { color: 'rgb(58,111,196)', width: 2 },
                                            itemStyle: { color: 'rgb(58,111,196)' },
                                            areaStyle: { color: 'rgba(58,111,196, 0.6)' }
                                        },
                                        {
                                            data: [],
                                            name: 'Nutzer (max.)',
                                            stack: 'minMax',
                                            type: 'line',
                                            smooth: true,
                                            animation: false,
                                            lineStyle: { color: 'rgb(58,111,196)', width: 2  },
                                            itemStyle: { color: 'rgb(58,111,196)' },
                                            areaStyle: { color: 'rgba(58,111,196, 0.3)' }
                                        }
                                    ]
                                }
                            </g:if>
                            JSPC.app.chartData_${index} = Object.assign({}, JSPC.app.chartData_base);

                            <g:if test="${index == 0}">
                                JSPC.app.chartData_${index}.series = [{
                                    data: [<% print itemValue[1].join(',') %>],
                                    name: 'Durchschnittl. Nutzer',
                                    type: 'bar',
                                    animation: false,
                                    areaStyle: { color: 'rgba(58,111,196, 0.3)' },
                                    lineStyle: { color: 'rgb(58,111,196)', width: 3 },
                                    itemStyle: { color: 'rgb(58,111,196)' },
                                }]
                            </g:if>
                            <g:else>
                                JSPC.app.chartData_${index}.series[0].data = [<% print itemValue[1].join(',') %>];
                                JSPC.app.chartData_${index}.series[1].data = [<% print itemValue[0].join(', ') %>];
                            </g:else>

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