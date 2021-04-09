<%@ page import="de.laser.reporting.myInstitution.base.BaseQuery" %>
<g:if test="${! data}">
    JSPC.app.reporting.current.chart.option = {}
    $("#reporting-modal-nodata").modal('show');
</g:if>
<g:else>
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${tooltipLabel}',
            show: false
        },
        dataset: {
            source: [
                ['id', 'name', 'value'],
                <%
                    if (objectReference) {
                        data.reverse().each { it -> print "['${objectReference}:${it[0]}', '${it[1]}', ${it[2]}]," } // workaround : XYZ
                    }
                    else {
                        data.reverse().each { it -> print "['${it[0]}', '${it[1]}', ${it[2]}]," }
                    }
                %>
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
                        if (JSPC.helper.contains(['${BaseQuery.NO_DATA_LABEL}'], params.name)) {
                            return JSPC.app.reporting.helper.series.color.redInactive
                        } else {
                            return JSPC.app.reporting.helper.series.color.blue
                        }
                    }
                }
            }
        ]
    };
    JSPC.app.reporting.current.chart.details = <%
        if (objectReference) {
            print dataDetails.each { dd -> dd.id = objectReference + ':' + dd. id } as grails.converters.JSON // workaround : XYZ
        }
        else {
            print dataDetails as grails.converters.JSON
        }
        %>
</g:else>