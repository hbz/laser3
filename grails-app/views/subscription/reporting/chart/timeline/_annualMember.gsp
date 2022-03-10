<g:if test="${data}">
JSPC.app.reporting.current.chart.option = {
    dataset: {
        dimensions: ['id', 'name', 'value', 'isCurrent'],
        source: [
            <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}, ${it[3]}]," } %>
        ]
    },
    grid:  {
        top: 60,
        right: '5%',
        bottom: 10,
        left: '5%',
        containLabel: true
    },
    legend: {
        top: 'top',
    },
    xAxis: {
        type: 'category',
        axisLabel: {
            formatter: function(id, index) {
                var elem = JSPC.app.reporting.current.chart.option.dataset.source[ index ]
                return elem[1]
            }
        }
    },
    yAxis: { },
    toolbox: JSPC.app.reporting.helper.toolbox,
    tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter (params) {
            var index = JSPC.app.reporting.current.chart.option.dataset.dimensions.length - 1
            var str = params[0].data[1]
            for (var i=0; i<params.length; i++) {
                str += JSPC.app.reporting.helper.tooltip.getEntry(params[i].marker, params[i].seriesName, params[i].data[ 2 ])
            }
            return str
        }
    },
    series: [
        {
            name: '${labels.chart[0]}',
            color: JSPC.app.reporting.helper.series._color.blue,
            type: 'bar',
            encode: {
                x: 'id',
                y: 'value'
            },
            label: {
                show: true,
                position: 'top'
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('blue', (params.data[3] == true))
                }
            }
        }
    ]
};
</g:if>
<g:elseif test="${data != null && data.isEmpty()}">
    JSPC.app.reporting.current.chart.statusCode = 204
</g:elseif>
<g:else>
    JSPC.app.reporting.current.chart.statusCode = 500
</g:else>