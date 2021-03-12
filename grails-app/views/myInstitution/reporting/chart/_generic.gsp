<%@ page import="de.laser.reporting.myInstitution.GenericConfig" %>
<g:if test="${! data}">
    JSPC.app.reporting.current.chart.option = {}
    $("#reporting-modal-nodata").modal('show');
</g:if>
<g:elseif test="${chart == GenericConfig.CHART_PIE}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${tooltipLabel}',
            show: false
        },
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
                str += '<br/>' + params.marker + params.name + '&nbsp;&nbsp;&nbsp;<strong>' + params.data[2] + '</strong>'
                return str
           }
        },
        legend: {
            orient: 'vertical',
            left: 'left'
        },
        series: [
            {
                type: 'pie',
                radius: '70%',
                center: ['65%', '50%'],
                encode: {
                    itemName: 'name',
                    value: 'value',
                    id: 'id'
                },
                emphasis: JSPC.app.reporting.helper.series.pie.emphasis
            }
        ]
    };
    JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>
<g:elseif test="${chart == GenericConfig.CHART_BAR}">
    JSPC.app.reporting.current.chart.option = {
        title: {
            text: '${tooltipLabel}',
            show: false
        },
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
                str += '<br/>' + params.marker + params.name + '&nbsp;&nbsp;&nbsp;<strong>' + params.data[2] + '</strong>'
                return str
           }
        },
        grid:  {
            top: 10,
            bottom: 10,
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
                }
            }
        ]
    };
    JSPC.app.reporting.current.chart.details = <%= dataDetails as grails.converters.JSON %>
</g:elseif>