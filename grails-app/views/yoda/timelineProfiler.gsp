<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.timelineProfiler')}</title>
    <asset:stylesheet src="chartist.css"/><laser:javascript src="chartist.js"/>%{-- dont move --}%
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.timelineProfiler" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.timelineProfiler')}</h1>

    <table class="ui celled la-table compact table">
        <thead>
            <tr class="center aligned">
                <th>Timeline</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${globalTimelineOrder}" var="ik,iv" status="index">
                <g:set var="itemValue" value="${globalTimeline[ik]}" />

                <tr>
                    <td>
                        <strong>${ik}</strong> (${iv}) <br />

                        <div id="ct-chart-${index}"></div>

                        <laser:script file="${this.getGroovyPageFileName()}">
                                JSPC.app.chartData_${index} = {
                                    labels: [
                                        <% println '"' + globalTimelineDates.collect{ it.length() ? it.substring(0,5) : it }.join('","') + '"' %>
                                    ],
                                    series: [
                                        [<% println '"' + itemValue.join('","') + '"' %>]
                                    ]
                                };

                                new Chartist.Bar('#ct-chart-${index}', JSPC.app.chartData_${index}, {
                                    stackBars: true,
                                    fullWidth: true,
                                    chartPadding: {
                                        right: 20
                                    },
                                    axisY: {
                                        onlyInteger: true
                                    }
                                }).on('draw', function(data) {
                                    if(data.type === 'bar') {
                                        data.element.attr({
                                            style: 'stroke-width: 20px'
                                        });
                                    }
                                });
                        </laser:script>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
    <style>
        .ct-series-a .ct-bar { stroke: #98b500; }
        .ct-series-a .ct-slice-pie { fill: #98b500; }
    </style>
</body>
</html>