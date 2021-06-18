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
                ['id', 'name', 'value'],
                <% data.each{ it -> print "[${it[0]}, '${it[1]}', ${it[2]}]," } %>
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
        legend: {
            orient: 'vertical',
            left: 'left',
            formatter: function (value) {
                return value.replace(/\s\(ID:[0-9]*\)/,'')
            }
        },
        series: [
            {
                type: 'pie',
                radius: '70%',
                center: ['65%', '50%'],
                minAngle: 1,
                minShowLabelAngle: 1,
                encode: {
                    itemName: 'name',
                    value: 'value',
                    id: 'id'
                },
                emphasis: JSPC.app.reporting.helper.series.pie.emphasis,
                label: {
                    formatter: function (obj) {
                        return obj.name.replace(/\s\(ID:[0-9]*\)/,'')
                    }
                }
            }
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
                        if (JSPC.helper.contains(['${BaseQuery.NO_DATA_LABEL}', '${BaseQuery.NO_PROVIDER_LABEL}'], params.name)) {
                            return JSPC.app.reporting.helper.series.color.redInactive
                        }
                        else if (JSPC.helper.contains(['${BaseQuery.NO_STARTDATE_LABEL}', '${BaseQuery.NO_ENDDATE_LABEL}'], params.name)) {
                            return JSPC.app.reporting.helper.series.color.ice
                        }
                        else {
                            return JSPC.app.reporting.helper.series.color.blue
                        }
                    }
                }
            }
        ]
    };
</g:elseif>