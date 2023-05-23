<%@ page import="de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.myInstitution.base.BaseQuery" %>
<g:if test="${data && chart == BaseConfig.CHART_PIE}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${labels.tooltip}',
            show: false
        },
        dataset: {
            source: [
                ['id', 'name', 'value'],
                <%
                    if (objectReference) {
                        data.each { it ->
                            if (it[0] instanceof String) {
                                print "['${objectReference}:${it[0].replaceAll("'", BaseQuery.SQM_MASK)}', '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}]," // property
                            } else {
                                print "['${objectReference}:${it[0]}', '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}]," // rdv
                            }
                        } // workaround : XYZ
                    }
                    else {
                        data.each { it -> print "['${it[0]}', '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}]," }
                    }
                %>
            ]
        },
        toolbox: JSPC.app.reporting.helper._pie.toolbox,
        tooltip: {
            trigger: 'item',
            formatter (params) {
                var str = JSPC.app.reporting.current.chart.option.title.text
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, params.name, params.data[2])
                return '<div style="max-width:800px;word-break:break-word;white-space:pre-wrap;">' + str + '</div>' /* erms-4787 */
           }
        },
        legend: {
            bottom: 0,
            left: 'center',
            z: 1,
            formatter: function (value) {
                if (JSPC.app.reporting.current.myCountsToggle) {
                    var row = JSPC.app.reporting.current.chart.option.dataset.source.filter(row => row[1] === value)
                    if (row) { value = value + ': ' + row[0][2] }
                }
                var v = value.replace(/\s\(ID:[0-9]*\)/,'')
                if (v.length > 80) { v = v.substring(0, 55) + '[..]' + v.substring(v.length - 20) }  /* erms-4787 */
                return v
            }
        },
        series: [
            {
                type: 'pie',
                radius: [0, '70%'],
                center: ['50%', '45%'],
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
                        if (JSPC.helper.contains(['${BaseQuery.getChartLabel(BaseQuery.NO_DATA_LABEL)}'], params.name)) {
                            return JSPC.app.reporting.helper.series._color.redInactiveSolid
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
        dataset: {
            source: [
                ['id', 'name', 'value'],
                <%
                    if (objectReference) {
                        data.reverse().each { it ->
                            if (it[0] instanceof String) {
                                print "['${objectReference}:${it[0].replaceAll("'", BaseQuery.SQM_MASK)}', '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}]," // property
                            } else {
                                print "['${objectReference}:${it[0]}', '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}]," // rdv
                            }
                        } // workaround : XYZ
                    }
                    else {
                        data.reverse().each { it -> print "['${it[0]}', '${it[1].replaceAll("'", BaseQuery.SQM_MASK)}', ${it[2]}]," }
                    }
                %>
            ]
        },
        toolbox: JSPC.app.reporting.helper.toolbox,
        tooltip: {
            trigger: 'item',
            formatter (params) {
                var str = JSPC.app.reporting.current.chart.option.title.text
                str += JSPC.app.reporting.helper.tooltip.getEntry(params.marker, params.name, params.data[2])
                return '<div style="max-width:800px;word-break:break-word;white-space:pre-wrap;">' + str + '</div>' /* erms-4787 */
           }
        },
        grid:  {
            top: 10,
            right: '5%',
            bottom: 10,
            left: '5%',
            containLabel: true
        },
        xAxis: { },
        yAxis: {
            type: 'category',
            axisLabel: {
                formatter: function(v) {
                    if (v.length > 80) { v = v.substring(0, 55) + '[..]' + v.substring(v.length - 20) }  /* erms-4787 */
                    return v
                }
            },
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
                        if (JSPC.helper.contains(['${BaseQuery.getChartLabel(BaseQuery.NO_DATA_LABEL)}'], params.name)) {
                            return JSPC.app.reporting.helper.series._color.redInactive
                        } else {
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