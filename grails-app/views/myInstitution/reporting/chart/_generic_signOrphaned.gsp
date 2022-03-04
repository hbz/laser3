<%@ page import="de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.reporting.report.myInstitution.base.BaseQuery" %>
<g:if test="${data && chart == BaseConfig.CHART_PIE}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${labels.tooltip}',
            show: false
        },
        dataset: {
            source: [
                ['id', 'name', 'value', 'isOrphaned', 'tmpCmb'],
                <% data.each{ it -> print "[${it[0]}, '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}, ${it[3]}, ${it[2] + it[3]}]," } %>
            ]
        },
        legend: {
            bottom: 0,
            left: 'center',
            z: 1
        },
        toolbox: JSPC.app.reporting.helper._pie.toolbox,
        tooltip: {
            trigger: 'item',
            formatter (params) {
                var str = params.name
                if (! params.data[3]) {
                    str += '<br/>' + params.marker + ' ${labels.chart[0]}'
                } else {
                    str += '<br/>' + params.marker + ' ${labels.chart[1]}'
                }
                return str
           }
        },
        series: [
            {
                type: 'pie',
                radius: ['15%', '70%'],
                center: ['50%', '40%'],
                minAngle: 1,
                minShowLabelAngle: 1,
                encode: {
                    itemName: 'name',
                    value: 'tmpCmb',
                    id: 'id'
                },
                emphasis: JSPC.app.reporting.helper.series._pie.emphasis,
                itemStyle: {
                    borderColor: JSPC.app.reporting.helper.series._color.background,
                    borderWidth: 1,
                    color: function(params) {
                        if (params.data[3]) {
                            return JSPC.app.reporting.helper.series._color.ice
                        } else {
                            return JSPC.app.reporting.helper.series._color.blue;
                        }
                    }
                }
            }
        ]
    };
</g:if>
<g:elseif test="${data && chart == BaseConfig.CHART_BAR}">
    JSPC.app.reporting.current.chart.option = {
        color: [
            JSPC.app.reporting.helper.series._color.blue, JSPC.app.reporting.helper.series._color.ice
        ],
        title: {
            text: '${labels.tooltip}',
            show: false
        },
        toolbox: JSPC.app.reporting.helper.toolbox,
        dataset: {
            source: [
                ['id', 'name', 'value1', 'value2'],
                <% data.reverse().each{ it -> print "[${it[0]}, '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}, ${it[3] ? it[3] * -1 : 0}]," } %>
            ]
        },
        legend: {
            data: [ <% print labels.chart.collect{ "'${it}'" }.join(', ') %> ]
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter (params) {
                var str = params[0].name
                if (params.length > 1) {
                    if (params[0].value[2]) {
                        str += '<br/>' + params[0].marker + ' ' + params[0].seriesName
                    }
                    if (params[0].value[3]) {
                        str += '<br/>' + params[1].marker + ' ' + params[1].seriesName
                    }
                }
                return str
           }
        },
        grid:  {
            top: 40,
            right: '5%',
            bottom: 10,
            left: '5%',
            containLabel: true
        },
        xAxis: {
            type: 'value',
            offset: 5,
            minInterval: 1,
            axisLabel: {
                formatter (value) { return Math.abs(value) }
            }
        },
        yAxis: {
            type: 'category',
            offset: 5,
            minInterval: 1,
            axisTick: { show: true },
            axisLine: { onZero: true }
        },
        series: [
            {
                name: '${labels.chart[0]}',
                type: 'bar',
                stack: 'total',
                encode: {
                    x: 'value1',
                    y: 'name'
                },
                label: {
                    show: true,
                    position: 'right',
                    formatter (params) {
                        var v = Math.abs(params.value[2])
                        if (v > 0) { return v } else { return '' }
                    }
                }
            },
            {
                name: '${labels.chart[1]}',
                type: 'bar',
                stack: 'total',
                encode: {
                    x: 'value2',
                    y: 'name'
                },
                label: {
                    show: true,
                    position: 'left',
                    formatter (params) {
                        var v = Math.abs(params.value[3])
                        if (v > 0) { return v } else { return '' }
                    }
                }
            }
        ]
    };
</g:elseif>
<g:elseif test="${data != null && data.isEmpty()}">
    JSPC.app.reporting.current.chart.statusCode = 204
</g:elseif>
<g:else>
    JSPC.app.reporting.current.chart.statusCode = 500
</g:else>