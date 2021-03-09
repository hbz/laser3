JSPC.app.reporting.current.chart.option = {
    dataset: {
        dimensions: ['id', 'name', 'startDate', 'endDate', 'value', 'current'],
        source: [
            <% data.each{ it -> print "[${it[0]}, '${it[1]}', '${it[2]}', '${it[3]}', ${it[4]}, ${it[5]}]," } %>
        ]
    },
    grid:  {
        top: 40,
        bottom: 10,
        containLabel: true
    },
    xAxis: {
        type: 'category',
        axisLabel: {
            formatter: function(id, index) {
                var elem = JSPC.app.reporting.current.chart.option.dataset.source[index]
                return elem[2] + ' - ' + elem[3]
            }
        }
    },
    yAxis: { },
    series: [
        {
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
                color: 'rgba(0, 0, 180, 0.3)'
            }
        }
    ]
};
JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>