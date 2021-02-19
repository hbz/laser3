<%@ page import="de.laser.reporting.GenericConfig" %>
<g:if test="${! data}">
    JSPC.app.reporting.current.chart.option = {}
    alert('[c3] - Keine Daten gefunden');
</g:if>
<g:elseif test="${chart == GenericConfig.CHART_PIE}">
JSPC.app.reporting.current.chart.option = {
    tooltip: {
        trigger: 'item'
    },
    legend: {
        orient: 'vertical',
        left: 'left',
    },
    series: [
        {
            type: 'pie',
            radius: '70%',
            center: ['65%', '50%'],
            data: [
                <% data.each{ it -> println "{id: ${it[0]}, name:'${it[1]}', value: ${it[2]}}," } %>
            ],
            emphasis: {
                itemStyle: {
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }
    ]
};
JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>
<g:elseif test="${chart == GenericConfig.CHART_BAR}">
JSPC.app.reporting.current.chart.option = {
    dataset: {
        source: [
                ['id', 'name', 'value'],
                <% data.reverse().each{ it -> println "[${it[0]}, '${it[1]}', ${it[2]}]," } %>
        ]
    },
    grid:  {containLabel: true},
    xAxis: {},
    yAxis: {type: 'category'},
    series: [
        {
            type: 'bar',
            encode: {
                x: 'value',
                y: 'name'
            },
            label: {
                show: true,
                position: 'right'
            }
        }
    ]
};
JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>