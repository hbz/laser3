<%@ page import="de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.reporting.report.myInstitution.base.BaseQuery" %>
<g:if test="${data && chart == BaseConfig.CHART_PIE}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${labels.tooltip}',
            show: false
        },
        dataset: {
            source: [
                ['id', 'name', 'value'],
                <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}]," } %>
            ]
        },
        legend: JSPC.app.reporting.helper._pie.legend,
        toolbox: JSPC.app.reporting.helper._pie.toolbox,
        tooltip: {
            trigger: 'item',
            formatter (params) {
                var str = JSPC.app.reporting.current.chart.option.title.text
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, params.name, params.data[2])
                return str
           }
        },
%{--        legend: {--}%
%{--            bottom: 0,--}%
%{--            left: 'center',--}%
%{--            z: 1,--}%
%{--            formatter: function (value) {--}%
%{--                return value.replace(/\s\(ID:[0-9]*\)/,'')--}%
%{--            }--}%
%{--        },--}%
        series: [
            {
                type: 'pie',
                radius: [0, '70%'],
                center: ['50%', '40%'],
                minAngle: 1,
                minShowLabelAngle: 1,
                encode: {
                    itemName: 'name',
                    value: 'value',
                    id: 'id'
                },
                emphasis: JSPC.app.reporting.helper.series._pie.emphasis,
                label: {
                    formatter: function (obj) {
                        return obj.name.replace(/\s\(ID:[0-9]*\)/,'')
                    }
                },
                itemStyle: {
                    color: function(params) {
                        if (JSPC.helper.contains(['${BaseQuery.getChartLabel(BaseQuery.NO_DATA_LABEL)}', '${BaseQuery.getChartLabel(BaseQuery.NO_MATCH_LABEL)}', '${BaseQuery.getChartLabel(BaseQuery.NO_PROVIDER_LABEL)}', '${BaseQuery.getChartLabel(BaseQuery.NO_STARTDATE_LABEL)}'], params.name)) {
                            return JSPC.app.reporting.helper.series._color.redInactiveSolid
                        }
                        else if (JSPC.helper.contains(['${BaseQuery.getChartLabel(BaseQuery.NO_ENDDATE_LABEL)}'], params.name)) {
                            return JSPC.app.reporting.helper.series._color.ice
                        }
                        else {
                            return JSPC.app.reporting.helper.series._color.palette[params.dataIndex % JSPC.app.reporting.helper.series._color.palette.length];
                        }
                    }
                }
            }
        ]
    };
</g:if>
<g:elseif test="${data && chart == BaseConfig.CHART_BAR}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${labels.tooltip}',
            show: false
        },
        toolbox: JSPC.app.reporting.helper.toolbox,
        dataset: {
            source: [
                ['id', 'name', 'value'],
                <% data.reverse().each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}]," } %>
            ]
        },
        tooltip: {
            trigger: 'item',
            formatter (params) {
                var str = JSPC.app.reporting.current.chart.option.title.text
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, params.name, params.data[2])
                return str
           }
        },
        grid:  {
            top: 10,
            right: '5%',
            bottom: 10,
            left: '5%',
            containLabel: true
        },
        xAxis: {
            offset: 5,
            minInterval: 1
        },
        yAxis: {
            type: 'category',
            offset: 5,
            minInterval: 1,
            axisLabel: {
                formatter: function (value) {
                    return value.replace(/\s\(ID:[0-9]*\)/,'')
                }
            }
        },
        series: [
            {
                type: 'bar',
                encode: {
                    x: 'value',
                    y: 'name'
                },
                label: {
                    show: true,
                    position: 'right'
                },
                itemStyle: {
                    color: function(params) {
                        if (JSPC.helper.contains(['${BaseQuery.getChartLabel(BaseQuery.NO_DATA_LABEL)}', '${BaseQuery.getChartLabel(BaseQuery.NO_MATCH_LABEL)}', '${BaseQuery.getChartLabel(BaseQuery.NO_PROVIDER_LABEL)}', '${BaseQuery.getChartLabel(BaseQuery.NO_STARTDATE_LABEL)}'], params.name)) {
                            return JSPC.app.reporting.helper.series._color.redInactive
                        }
                        else if (JSPC.helper.contains(['${BaseQuery.getChartLabel(BaseQuery.NO_ENDDATE_LABEL)}'], params.name)) {
                            return JSPC.app.reporting.helper.series._color.ice
                        }
                        else {
                            return JSPC.app.reporting.helper.series._color.blue
                        }
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