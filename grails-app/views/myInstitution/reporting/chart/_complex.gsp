<%@ page import="de.laser.reporting.GenericConfig; de.laser.reporting.GenericQuery" %>
<g:if test="${! data}">
    JSPC.app.reporting.current.chart.option = {}
    alert('[msg:3] - Keine Daten gefunden');
</g:if>
<g:elseif test="${chart == GenericConfig.CHART_PIE}">
    JSPC.app.reporting.current.chart.option = {
        dataset: {
            source: [
                ['id', 'name', 'valueMatches', 'valueObjects' ],
                <% data.reverse().each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}, ${GenericQuery.findDataDetailsIdListById( it[0], dataDetails).unique().size()}]," } %>
            ]
        },
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
                center: ['65%', '50%'],
                encode: {
                    itemName: 'name',
                    value: 'valueMatches',
                    id: 'id'
                },
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0,0,0,0.3)'
                    }
                }
            }
        ]
    };
    JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>
<g:elseif test="${chart == GenericConfig.CHART_BAR}">
    JSPC.app.reporting.current.chart.option = {
        dataset: {
            source: [
                ['id', 'name', 'valueMatches', 'valueObjects'],
                <% data.reverse().each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}, ${GenericQuery.findDataDetailsIdListById( it[0], dataDetails).unique().size() * -1}]," } %>
            ]
        },
        legend: {
            data: [ <% print chartLabels.collect{ "'${it}'" }.join(', ') %> ]
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            },
            formatter (params) {
                var str = params[0].name

                if (params.length > 1) {
                    str += '<br/>' + params[0].marker + params[0].seriesName + '&nbsp;&nbsp;&nbsp;<strong>' + Math.abs(params[0].value[3]) + '</strong>'
                    str += '<br/>' + params[1].marker + params[1].seriesName + '&nbsp;&nbsp;&nbsp;<strong>' + params[1].value[2] + '</strong>'
                    return str
                }
                return null
           }
        },
        grid:  {
            top: 40,
            bottom: 10,
            containLabel: true
        },
        xAxis: {
            type: 'value',
            axisLabel: {
                formatter (value) {
                    return Math.abs(value)
                }
            }
        },
        yAxis: {
            type: 'category'
        },
        series: [
            {
                name: '${chartLabels[0]}',
                type: 'bar',
                stack: 'total',
                label: {
                    show: true,
                    position: 'left',
                    formatter (params) {
                        return Math.abs(params.value[3])
                    }
                },
                encode: {
                    x: 'valueObjects',
                    y: 'name'
                }
            },
            {
                name: '${chartLabels[1]}',
                type: 'bar',
                stack: 'total',
                label: {
                    show: true,
                    position: 'right'
                },
                encode: {
                    x: 'valueMatches',
                    y: 'name'
                }
            }
        ]
    };
    JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>

"<span style=\"display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:#5470c6;\"></span>"