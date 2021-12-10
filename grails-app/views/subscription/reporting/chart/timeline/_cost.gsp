<%@ page import="de.laser.reporting.report.myInstitution.base.BaseQuery" %>
JSPC.app.reporting.current.chart.option = {
    dataset: {
        dimensions: ['id', 'name', 'valueNeutralCons', 'valueNeutralConsTax', 'valueCons', 'valueConsTax', 'annual', 'isCurrent'],
        source: [
            <% data.each{ it -> print "[${it[0]}, '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]},  ${it[3]}, ${it[4]},  ${it[5]}, '${it[6]}', ${it[7]}]," } %>
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
                return JSPC.app.reporting.current.chart.option.dataset.source[ index ][ 6 ]
            }
        }
    },
    yAxis: {},
    toolbox: {
        showTitle: true,
        orient: 'vertical',
        itemGap: 15,
        right: 0,
        feature: {
            saveAsImage: {
                title: '${message(code:'reporting.chart.toolbox.saveAsImage')}',
                icon: 'image://${resource(dir:'images', file:'reporting/download.svg', absolute:true)}'
            },
%{--            myReorderLabels: {--}%
%{--                title: '${message(code:'reporting.chart.toolbox.reorderLabels')}',--}%
%{--                icon: 'image://${resource(dir:'images', file:'reporting/slash-square.svg', absolute:true)}',--}%
%{--                onclick: function (){--}%
%{--                }--}%
%{--            }--}%
        }
    },
    tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter (params) {
            var str = params[0].data[6]
            for (var i=0; i<params.length; i++) {
                var ci = new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params[i].data[ params[i].seriesIndex + 2 ])
                str += JSPC.app.reporting.helper.tooltip.getEntry(params[i].marker, params[i].seriesName, ci)
            }
            return str
        }
    },
    series: [
        {
            name: '${labels.chart[0]}',
            color: JSPC.app.reporting.helper.series._color.yellow,
            type: 'bar',
            encode: {
                x: 'id',
                y: 'valueNeutralCons'
            },
            label: {
                show: true,
                position: 'top',
                formatter (params) {
                    var index = JSPC.app.reporting.current.chart.option.dataset.dimensions.indexOf('valueNeutralCons')
                    return new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
                },
                padding: [3, 5, 0, 5]
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('yellow', (params.data[7] == true))
                }
            }
        },
        {
            name: '${labels.chart[1]}',
            color: JSPC.app.reporting.helper.series._color.orange,
            type: 'bar',
            encode: {
                x: 'id',
                y: 'valueNeutralConsTax'
            },
            label: {
                show: true,
                position: 'top',
                formatter (params) {
                    var index = JSPC.app.reporting.current.chart.option.dataset.dimensions.indexOf('valueNeutralConsTax')
                    return new Intl.NumberFormat(JSPC.vars.locale, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
                },
                padding: [3, 5, 0, 5]
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('orange', (params.data[7] == true))
                }
            }
        },
        {
            name: '${labels.chart[2]}',
            color: JSPC.app.reporting.helper.series._color.green,
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
                },
                padding: [3, 5, 0, 5]
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('green', (params.data[7] == true))
                }
            }
        },
        {
            name: '${labels.chart[3]}',
            color: JSPC.app.reporting.helper.series._color.blue,
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
                },
                padding: [3, 5, 0, 5]
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('blue', (params.data[7] == true))
                }
            }
        }
    ]
};