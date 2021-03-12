JSPC.app.reporting.current.chart.option = {
    dataset: {
        dimensions: ['id', 'name', 'isCurrent', 'startDate', 'endDate', 'placeholder', 'valueCons', 'valueConsTax' /*, 'valueSubscr', 'valueSubscrTax', 'valueOwn', 'valueOwnTax' */ ],
        source: [
            <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}, '${it[3]}', '${it[4]}', ${it[5]}, ${it[6]}, ${it[7]}, ${it[8]}, ${it[9]}, ${it[10]}, ${it[11]}]," } %>
        ]
    },
    grid:  {
        top: 60,
        bottom: 10,
        containLabel: true
    },
    legend: { top: 'top' },
    xAxis: {
        type: 'category',
        axisLabel: {
            formatter: function(id, index) {
                var elem = JSPC.app.reporting.current.chart.option.dataset.source[index]
                return elem[3] + ' - ' + elem[4]
            }
        }
    },
    yAxis: {},
    series: [
        {
            name: '${chartLabels[0]}',
            type: 'bar',
            encode: {
                x: 'id',
                y: 'valueCons'
            },
            label: {
                show: true,
                position: 'top',
                formatter (params) {
                    return new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params.data[6])
                }
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('green', (params.data[2] == true))
                }
            }
        },
        {
            name: '${chartLabels[1]}',
            type: 'bar',
            encode: {
                x: 'id',
                y: 'valueConsTax'
            },
            label: {
                show: true,
                position: 'top',
                formatter (params) {
                    return new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params.data[7])
                }
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