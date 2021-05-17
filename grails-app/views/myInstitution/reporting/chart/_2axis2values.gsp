<%@ page import="de.laser.reporting.myInstitution.base.BaseConfig;de.laser.reporting.myInstitution.base.BaseQuery" %>
<g:if test="${! data}">
    JSPC.app.reporting.current.chart.option = {}
    $("#reporting-modal-nodata").modal('show');
</g:if>
<g:elseif test="${chart == BaseConfig.CHART_PIE}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${labels.tooltip}',
            show: false
        },
        toolbox: JSPC.app.reporting.helper.toolbox,
        dataset: {
            source: [
                ['id', 'name', 'value1', 'value2' ],
                <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${BaseQuery.getDataDetailsByIdAndKey(it[0], 'value1', dataDetails)}, ${BaseQuery.getDataDetailsByIdAndKey(it[0], 'value2', dataDetails)}]," } %>
            ]
        },
        tooltip: {
            trigger: 'item',
            formatter (params) {
                var str = params.name
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, '${labels.chart[0]}', params.value[3])
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, '${labels.chart[1]}', params.value[2])
                return str
           }
        },
        legend: {
            orient: 'vertical',
            left: 'left',
        },
        series: [
            {
                name: '${labels.chart[0]}',
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
</g:elseif>
<g:elseif test="${chart == BaseConfig.CHART_BAR}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${labels.tooltip}',
            show: false
        },
        toolbox: JSPC.app.reporting.helper.toolbox,
        dataset: {
            source: [
                ['id', 'name', 'value1', 'value2'],
                <% data.reverse().each{ it -> print "[${it[0]}, '${it[1]}', ${BaseQuery.getDataDetailsByIdAndKey(it[0], 'value1', dataDetails)}, ${BaseQuery.getDataDetailsByIdAndKey(it[0], 'value2', dataDetails) * -1}]," } %>
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
                if (params.length == 1) {
                    if (params[0].seriesName == '${labels.chart[0]}') {
                        str += JSPC.app.reporting.helper.tooltip.getEntry(params[0].marker, params[0].seriesName, Math.abs(params[0].value[3]))
                    } else if (params[0].seriesName == '${labels.chart[1]}') {
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
                    x: 'value2',
                    y: 'name'
                },
                label: {
                    show: true,
                    position: 'left',
                    formatter (params) { return Math.abs(params.value[3]) }
                }
            },
            {
                name: '${labels.chart[1]}',
                type: 'bar',
                stack: 'total',
                 encode: {
                    x: 'value1',
                    y: 'name'
                },
                label: {
                    show: true,
                    position: 'right',
                    formatter (params) { return Math.abs(params.value[2]) }
                }
            }
        ]
    };
</g:elseif>