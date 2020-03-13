<r:require module="chartist" />
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.timelineProfiler')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.timelineProfiler" class="active"/>
</semui:breadcrumbs>
<br>
    <h2 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.timelineProfiler')}</h2>

    <table class="ui celled la-table la-table-small table">
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
                        <strong>${ik}</strong> (${iv}) <br/>

                        <div id="ct-chart-${index}"></div>

                        <script>
                            $(document).ready(function(){

                                var chartData = {
                                    labels: [
                                        <% println '"' + globalTimelineDates.collect{ it.length() ? it.substring(0,5) : it }.join('","') + '"' %>
                                    ],
                                    series: [
                                        [<% println '"' + itemValue.join('","') + '"' %>]
                                    ]
                                };

                                new Chartist.Bar('#ct-chart-${index}', chartData, {
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
                            })
                        </script>
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