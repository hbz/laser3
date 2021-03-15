JSPC.app.reporting.current.chart.option = {
    dataset: {
        dimensions: ['id', 'name', 'isCurrent', 'startDate', 'endDate', 'value', 'plus', 'minus'],
        source: [
            <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}, '${it[3]}', '${it[4]}', ${it[5]}, ${it[6]}, ${it[7]}]," } %>
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
                return elem[3] + ' - ' + elem[4]
            }
        }
    },
    yAxis: { },
    tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter (params) {
            var index = JSPC.app.reporting.current.chart.option.dataset.dimensions.length - 1
            var str = params[0].data[3] + ' - ' +  params[0].data[4]
            str += JSPC.app.reporting.helper.tooltip.getEntry(params[0].marker, params[0].seriesName, params[0].data[ index - params[0].seriesIndex ])
            str += JSPC.app.reporting.helper.tooltip.getEntry(params[1].marker, params[1].seriesName, params[1].data[ index - params[1].seriesIndex ])
            str += JSPC.app.reporting.helper.tooltip.getEntry(params[2].marker, params[2].seriesName, params[2].data[ index - params[2].seriesIndex ])
            return str
        }
    },
    series: [
        {
            name: '${chartLabels[0]}',
            color: JSPC.app.reporting.helper.series.color.red,
            type: 'bar',
            encode: {
                x: 'id',
                y: 'minus'
            },
            label: {
                show: true,
                position: 'top'
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('red', (params.data[2] == true))
                }
            }
        },
        {
            name: '${chartLabels[1]}',
            color: JSPC.app.reporting.helper.series.color.green,
            type: 'bar',
            encode: {
                x: 'id',
                y: 'plus'
            },
            label: {
                show: true,
                position: 'top'
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('green', (params.data[2] == true))
                }
            }
        },
        {
            name: '${chartLabels[2]}',
            color: JSPC.app.reporting.helper.series.color.blue,
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
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('blue', (params.data[2] == true))
                }
            }
        }
    ]
};
JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>