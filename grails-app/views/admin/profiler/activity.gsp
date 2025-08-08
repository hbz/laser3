<%@ page import="de.laser.system.SystemActivityProfiler" %>
<laser:htmlStart message="menu.admin.profilerActivity">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.profiler" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.profiler" type="admin" total="${SystemActivityProfiler.executeQuery('select count(*) from SystemActivityProfiler')[0]}" />

    <g:render template="profiler/menu" />

    <div class="ui fluid card">
        <table class="ui la-table compact table">
            <thead>
                <tr class="center aligned">
                    <th>Benutzer online im Durchschnitt</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <div id="ct-chart-0" class="echarts-activity"></div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.profiler = {
            chart_config: {},
            charts: {}
        }

        JSPC.app.profiler.chart_config.base = {
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

        JSPC.app.profiler.chart_config.chart_0 = Object.assign({}, JSPC.app.profiler.chart_config.base);

        JSPC.app.profiler.chart_config.chart_0.series = [{
            data: [<% print activity.entrySet()[0].value[1].join(',') %>],
            name: 'Durchschnittl. Nutzer',
            type: 'bar',
            animation: false,
            areaStyle: { color: 'rgba(58,111,196, 0.3)' },
            lineStyle: { color: 'rgb(58,111,196)', width: 3 },
            itemStyle: { color: 'rgb(58,111,196)' },
        }]

        var chart = echarts.init( $('#ct-chart-0')[0] );
        chart.setOption( JSPC.app.profiler.chart_config.chart_0 );
        JSPC.app.profiler.charts.chart_0 = chart;

        $(window).resize(function () {
            JSPC.app.profiler.charts.chart_0.resize();
        });
    </laser:script>

    <g:each in="${activity}" var="itemKey,itemValue" status="index">
        <g:if test="${index > 0}">
            <div class="ui fluid card">
                <div class="content">
                    <div class="header"> ${itemKey} </div>
                    <div class="content">
                        <div id="ct-chart-${index}" class="echarts-wrapper"></div>

                        <laser:script file="${this.getGroovyPageFileName()}">
                            JSPC.app.profiler.chart_config.chart_${index} = Object.assign({}, JSPC.app.profiler.chart_config.base);

                            JSPC.app.profiler.chart_config.chart_${index}.series[0].data = [<% print itemValue[1].join(',') %>];
                            JSPC.app.profiler.chart_config.chart_${index}.series[1].data = [<% print itemValue[0].join(', ') %>];

                            var chart = echarts.init( $('#ct-chart-${index}')[0] );
                            chart.setOption( JSPC.app.profiler.chart_config.chart_${index} );
                            JSPC.app.profiler.charts.chart_${index} = chart;

                            $(window).resize(function () {
                                JSPC.app.profiler.charts.chart_${index}.resize();
                            });
                        </laser:script>
                    </div>
                </div>
            </div>
        </g:if>
    </g:each>

    <style>
        .echarts-activity {
            width: 100%;
            height: 160px;
        }
        .echarts-wrapper {
            width: 100%;
            height: 120px;
        }
    </style>

<laser:htmlEnd />