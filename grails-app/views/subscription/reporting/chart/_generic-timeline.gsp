JSPC.app.reporting.current.chart.option = {
    dataset: {
        dimensions: ['id', 'name', 'isCurrent', 'startDate', 'endDate', 'idList', 'value', 'plus', 'minus'],
        source: [
            <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}, '${it[3]}', '${it[4]}', ${it[5]}, ${it[6]}, ${it[7]}, ${it[8]}]," } %>
        ]
    },
    grid:  {
        top: 60,
        bottom: 10,
        containLabel: true
    },
    legend: {
        top: 'top',
    },
    xAxis: {
        type: 'category',
        axisLabel: {
            formatter: function(id, index) {
                var elem = JSPC.app.reporting.current.chart.option.dataset.source[index]
                return elem[3] + ' - ' + elem[4]
            }
        }
    },
    yAxis: { },
    series: [
        {
            name: '${chartLabels[0]}',
            color: 'rgb(238,102,102)',
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
            color: 'rgb(144,202,117)',
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
            color: 'rgb(58,111,196)',
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