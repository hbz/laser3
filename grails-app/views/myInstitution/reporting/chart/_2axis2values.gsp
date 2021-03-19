<%@ page import="de.laser.reporting.myInstitution.GenericConfig;de.laser.reporting.myInstitution.GenericQuery" %>
<g:if test="${! data}">
    JSPC.app.reporting.current.chart.option = {}
    $("#reporting-modal-nodata").modal('show');
</g:if>
<g:elseif test="${chart == GenericConfig.CHART_PIE}">
    JSPC.app.reporting.current.chart.option = {
        dataset: {
            source: [
                ['id', 'name', 'value1', 'value2' ],
                <% data.reverse().each{ it -> print "[${it[0]}, '${it[1]}', ${GenericQuery.getDataDetailsByIdAndKey(it[0], 'value1', dataDetails)}, ${GenericQuery.getDataDetailsByIdAndKey(it[0], 'value2', dataDetails)}]," } %>
            ]
        },
        tooltip: {
            trigger: 'item',
            formatter (params) {
                var str = params.name
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, '${chartLabels[0]}', params.value[3])
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, '${chartLabels[1]}', params.value[2])
                return str
           }
        },
        legend: {
            orient: 'vertical',
            left: 'left',
        },
        series: [
            {
                name: '${chartLabels[0]}',
                type: 'pie',
                radius: '70%',
                center: ['65%', '50%'],
                minAngle: 1,
                minShowLabelAngle: 1,
                encode: {
                    itemName: 'name',
                    value: 'value2',
                    id: 'id'
                },
                emphasis: JSPC.app.reporting.helper.series.pie.emphasis
            },
        ]
    };
    JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>
<g:elseif test="${chart == GenericConfig.CHART_BAR}">
    JSPC.app.reporting.current.chart.option = {
        dataset: {
            source: [
                ['id', 'name', 'value1', 'value2'],
                <% data.reverse().each{ it -> print "[${it[0]}, '${it[1]}', ${GenericQuery.getDataDetailsByIdAndKey(it[0], 'value1', dataDetails)}, ${GenericQuery.getDataDetailsByIdAndKey(it[0], 'value2', dataDetails) * -1}]," } %>
            ]
        },
        legend: {
            data: [ <% print chartLabels.collect{ "'${it}'" }.join(', ') %> ]
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter (params) {
                var str = params[0].name

                if (params.length == 1) {
                    if (params[0].seriesName == '${chartLabels[0]}') {
                        str += JSPC.app.reporting.helper.tooltip.getEntry(params[0].marker, params[0].seriesName, Math.abs(params[0].value[3]))
                    } else if (params[0].seriesName == '${chartLabels[1]}') {
                        str += JSPC.app.reporting.helper.tooltip.getEntry(params[0].marker, params[0].seriesName, params[0].value[2])
                    }
                }
                else if (params.length > 1) {
                    str += JSPC.app.reporting.helper.tooltip.getEntry(params[0].marker, params[0].seriesName, Math.abs(params[0].value[3]))
                    str += JSPC.app.reporting.helper.tooltip.getEntry(params[1].marker, params[1].seriesName, params[1].value[2])
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
            axisLabel: {
                formatter (value) {
                    return Math.abs(value)
                }
            }
        },
        yAxis: { type: 'category' },
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
                    x: 'value2',
                    y: 'name'
                }
            },
            {
                name: '${chartLabels[1]}',
                type: 'bar',
                stack: 'total',
                label: {
                    show: true,
                    position: 'right',
                    formatter (params) {
                        return Math.abs(params.value[2])
                    }
                },
                encode: {
                    x: 'value1',
                    y: 'name'
                }
            }
        ]
    };
    JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>