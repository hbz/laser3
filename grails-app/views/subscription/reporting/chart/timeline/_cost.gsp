<%@ page import="de.laser.reporting.report.myInstitution.base.BaseQuery" %>
<g:if test="${data}">
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
        showTitle: false,
        orient: 'vertical',
        itemGap: 15,
        right: 0,
        tooltip: JSPC.app.reporting.helper._toolbox.tooltip,
        feature: {
            saveAsImage: {
                title: '${message(code:'reporting.chart.toolbox.saveAsImage')}',
                icon: 'image://${resource(dir:'images', file:'reporting/download.svg', absolute:true)}'
            },
            myLegendToggle: {
                title: '${message(code:'reporting.chart.toolbox.toggleLegend')}',
                icon: 'image://${resource(dir:'images', file:'reporting/menu.svg', absolute:true)}',
                onclick: function (){
                    var show = ! JSPC.app.reporting.current.chart.echart.getOption().legend[0].show
                    JSPC.app.reporting.current.chart.echart.setOption({ legend: {show: show} })
                }
            },
            myReorderLabels: {
                title: '${message(code:'reporting.chart.toolbox.reorderLabels')}',
                icon: 'image://${resource(dir:'images', file:'reporting/hash.svg', absolute:true)}',
                onclick: function (){
                    JSPC.app.reporting.current.chart.echart.getModel().getSeries().forEach( function(s, idx) {
                        if (! s.option.label.rotate) {
                            s.option.label.rotate = 90
                            s.option.label.align = 'left'
                            s.option.label.verticalAlign = 'middle'
                            s.option.label.position = 'insideRight'
                            s.option.label.distance = 15

                            s.option.label.color = '#383838'

                            s.option.label.backgroundColor = '#ffffff'
                            s.option.label.borderColor = 'inherit'
                            s.option.label.borderWidth = 1
                            s.option.label.padding = [5, 8]
                        }
                        else if (s.option.label.position == 'insideRight') {
                            s.option.label.position = 'top'
                            s.option.label.distance = 10

                            s.option.label.color = null
                            s.option.label.textBorderWidth = 2
                            s.option.label.textBorderColor = '#ffffff'

                            s.option.label.backgroundColor = 'transparent'
                            s.option.label.borderWidth = 0
                            s.option.label.padding = 0
                        }
                        else {
                            if (s.option.label.rotate == 90) {
                                s.option.label.rotate = 60
                            }
                            else if (s.option.label.rotate == 60) {
                                s.option.label.rotate = 0
                                s.option.label.align = null
                                s.option.label.verticalAlign = null
                                s.option.label.position = 'top'
                                s.option.label.distance = 5
                            }
                        }

                        JSPC.app.reporting.current.chart.echart.resize()
                    })
                }
            }
        }
    },
    tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter (params) {
            var str = params[0].data[6]
            for (var i=0; i<params.length; i++) {
                var ci = new Intl.NumberFormat(JSPC.vars.language, { style: 'currency', currency: 'EUR' }).format(params[i].data[ params[i].seriesIndex + 2 ])
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
                    return new Intl.NumberFormat(JSPC.vars.language, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
                }
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
                    return new Intl.NumberFormat(JSPC.vars.language, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
                }
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
                    return new Intl.NumberFormat(JSPC.vars.language, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
                }
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
                    return new Intl.NumberFormat(JSPC.vars.language, { style: 'currency', currency: 'EUR' }).format(params.data[ index ])
                }
            },
            itemStyle: {
                color: function(params) {
                    return JSPC.app.reporting.helper.series.bar.itemStyle.color('blue', (params.data[7] == true))
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