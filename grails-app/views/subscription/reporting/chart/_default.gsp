<%@ page import="de.laser.reporting.report.myInstitution.base.BaseQuery" %>
<g:if test="${data}">
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
        xAxis: { },
        yAxis: { type: 'category' },
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
</g:if>
<g:elseif test="${data != null && data.isEmpty()}">
    JSPC.app.reporting.current.chart.statusCode = 204
</g:elseif>
<g:else>
    JSPC.app.reporting.current.chart.statusCode = 500
</g:else>