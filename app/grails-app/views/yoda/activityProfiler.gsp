<r:require module="chartist" />
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.activityProfiler')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.activityProfiler" class="active"/>
</semui:breadcrumbs>
<br>
    <h2 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.activityProfiler')}</h2>

    <table class="ui celled la-table la-table-small table">
        <thead>
            <tr class="center aligned">
                <th>Zeitraum</th>
                <th colspan="4">Benutzer online (min/max)</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${activity}" var="itemKey,itemValue" status="index">
                <tr>
                    <td class="center aligned">
                        ${itemKey}
                    </td>
                    <td colspan="4">
                        <div id="ct-chart-${index}"></div>

                        <script>
                            $(document).ready(function(){

                                <g:if test="${index == 0}">
                                    setTimeout( function(){ console.log(".."); window.dispatchEvent(new Event('resize')) }, 99);
                                </g:if>

                                var chartData = {
                                    labels: [
                                        <% println '"' + labels.collect{ it.length() ? it.substring(0,3) + '00' : it }.join('","') + '"' %>
                                    ],
                                    series: [
                                        [<% println '"' + itemValue[0].join('","') + '"' %>],
                                        [<% println '"' + itemValue[1].join('","') + '"' %>]
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
                                            <g:if test="${index == 0}">
                                                style: 'stroke-width: 26px'
                                            </g:if><g:else>
                                                style: 'stroke-width: 20px'
                                            </g:else>
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
        #ct-chart-0 .ct-series-b .ct-bar { stroke: darkgreen; }

        .ct-series-a .ct-bar { stroke: #98b500; }
        .ct-series-b .ct-bar { stroke: orange; }
    </style>
</body>
</html>