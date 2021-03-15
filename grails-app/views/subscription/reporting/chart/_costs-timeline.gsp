JSPC.app.reporting.current.chart.option = {
    dataset: {
        dimensions: ['id', 'name', 'isCurrent', 'startDate', 'endDate', 'valueCons', 'valueConsTax' ],
        source: [
            <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}, '${it[3]}', '${it[4]}', ${it[5]}, ${it[6]}]," } %>
        ]
    },
    grid:  {
        top: 60,
        right: '5%',
        bottom: 10,
        left: '5%',
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
    tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter (params) {
            var c1 = new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params[0].data[ params[0].seriesIndex + 5 ])
            var c2 = new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params[1].data[ params[1].seriesIndex + 5 ])

            var str = params[0].data[3] + ' - ' +  params[0].data[4]
            str += JSPC.app.reporting.helper.tooltip.getEntry(params[0].marker, params[0].seriesName, c1)
            str += JSPC.app.reporting.helper.tooltip.getEntry(params[1].marker, params[1].seriesName, c2)
            return str
        }
    },
    series: [
        {
            name: '${chartLabels[0]}',
            color: JSPC.app.reporting.helper.series.color.green,
            type: 'bar',
            encode: {
                x: 'id',
                y: 'valueCons'
            },
            label: {
                show: true,
                position: 'top',
                formatter (params) {
                    var index = JSPC.app.reporting.current.chart.option.dataset.dimensions.indexOf('valueCons')
                    return new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
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
            color: JSPC.app.reporting.helper.series.color.blue,
            type: 'bar',
            encode: {
                x: 'id',
                y: 'valueConsTax'
            },
            label: {
                show: true,
                position: 'top',
                formatter (params) {
                    var index = JSPC.app.reporting.current.chart.option.dataset.dimensions.indexOf('valueConsTax')
                    return new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
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