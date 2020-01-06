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

    <h3 class="ui header">Aktivität</h3>

    <table class="ui celled la-table la-table-small table">
        <thead>
            <tr>
                <th>Zeitraum</th>
                <th></th>
                <th>min(user)</th>
                <th>max(user)</th>
                <th>avg(user)</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${activity}" var="item" status="index">
                <tr>
                    <td>
                        ${item.key}
                    </td>
                    <td colspan="4">
                        <%
                            List labels  = (0..23).collect{ "${it < 10 ? '0' + it : it}:00:00" }
                            List series1 = (0..23).collect{ 0 }
                            List series2 = (0..23).collect{ 0 }

                            item.value.each{ val ->
                                int indexOf = labels.findIndexOf{it == val[0]}
                                if (indexOf >= 0) {
                                    labels.putAt(indexOf, val[0])
                                    series1.putAt(indexOf, val[4])
                                    series2.putAt(indexOf, val[3])
                                }
                            }
                            labels.add("")
                            series1.add(series1[0])
                            series2.add(series2[0])
                        %>

                        <div id="ct-chart-${index}"></div>

                        <script>
                            $(document).ready(function(){

                                var chartData = {
                                    labels: [
                                        <% println '"' + labels.collect{ it.length() ? it.substring(0,3) + 'xx' : it }.join('","') + '"' %>
                                    ],
                                    series: [
                                        [<% println '"' + series1.join('","') + '"' %>],
                                        [<% println '"' + series2.join('","') + '"' %>]
                                    ]
                                };

                                new Chartist.Line('#ct-chart-${index}', chartData, {
                                    low: 0,
                                    showArea: true,
                                    showPoint: false,
                                    lineSmooth: Chartist.Interpolation.simple({
                                        divisor: 2
                                    }),
                                    fullWidth: true,
                                    chartPadding: {
                                        right: 20
                                    },
                                    axisY: {
                                        onlyInteger: true
                                    }
                                }, {});
                            })
                        </script>
                    </td>
                </tr>
                <%--
                <tr>
                    <td>
                        ${item.key}
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${slot[1]} - ${slot[2]}</span> <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${slot[3]}</span> <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${slot[4]}</span> <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${item.value}" var="slot">
                            <span>${((double) slot[5]).round(2)}</span> <br/>
                        </g:each>
                    </td>
                </tr>
                --%>
            </g:each>
        </tbody>
    </table>

</body>
</html>