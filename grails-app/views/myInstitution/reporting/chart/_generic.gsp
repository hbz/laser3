<g:if test="${! data}">
    JSPC.app.reporting.chartOption = {}
    alert('Keine Daten gefunden');
</g:if>
<g:elseif test="${chart == 'pie'}">
JSPC.app.reporting.chartOption = {
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
            data: [
                <%
                    data.each{ it -> println "{value: ${it[1]}, name:'${it[0]}'}," }
                %>
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
</g:elseif>
<g:elseif test="${chart == 'bar'}">
JSPC.app.reporting.chartOption = {
    dataset: {
        source: [
                ['count', 'type'],
                <%
                    data.each{ it -> println "[${it[1]}, '${it[0]}']," }
                %>
        ]
    },
    grid:  {containLabel: true},
    xAxis: {name: 'Anzahl'},
    yAxis: {type: 'category'},
    series: [
        {
            type: 'bar',
            encode: {
                x: 'count',
                y: 'type'
            },
            label: {
                show: true,
                position: 'right'
            }
        }
    ]
};
</g:elseif>