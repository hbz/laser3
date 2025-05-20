<%@ page import="de.laser.ui.Icon; de.laser.utils.DateUtils;" %>

<div class="ui segment" id="costPerUse">
    <g:if test="${selectedPeriodNotCovered}">
        <ui:msg class="info" showIcon="true" text="${selectedPeriodNotCovered}" />
    </g:if>
    <div id="chartWrapper" style="width:100%; min-height:500px"></div>
    <%--
    <g:each in="${costPerUse}" var="costPerMetric">
        <table class="ui compact celled table">
            <thead>
                <tr>
                    <th colspan="2">
                        <g:if test="${costPerUse.size() > 1}">
                            <g:if test="${costPerMetric.getKey() == 'consortialData'}">
                                <g:message code="subscription.details.costPerUse.headerConsortial"/>
                            </g:if>
                            <g:elseif test="${costPerMetric.getKey() == 'ownData'}">
                                <g:message code="subscription.details.costPerUse.headerOwn"/>
                            </g:elseif>
                        </g:if>
                        <g:else>
                            <g:message code="subscription.details.costPerUse.header"/>
                        </g:else>
                    </th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${costPerMetric.getValue()}" var="cost">
                    <tr>
                        <%
                            String costString, metricType = cost.getKey()
                            if((metricType.contains('ft_') || metricType in ['sectioned_html', 'toc', 'abstract', 'reference', 'data_set', 'audio', 'video', 'image', 'podcast']) || metricType.matches('\\w+_Requests')) {
                                costString = message(code: 'default.usage.pricePerDownload')
                            }
                            else if(metricType in ['search_reg', 'search_fed'] || metricType.contains('Searches')) {
                                costString = message(code: 'default.usage.pricePerSearch')
                            }
                            else if(metricType == 'result_click') {
                                costString = message(code: 'default.usage.pricePerClick')
                            }
                            else if(metricType == 'record_view' || metricType.matches('\\w+_Investigations')) {
                                costString = message(code: 'default.usage.pricePerView')
                            }
                        %>
                        <td>${metricType}</td>
                        <td>${costString}: ${cost.getValue()}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:each>
    --%>
    <div class="ui bulleted list">
        <g:each in="${costPerUse}" var="institutionalUsage">
            <g:if test="${institutionalUsage.getKey() == 'consortialData'}">
                Zugrunde liegen:
                <g:each in="${consortialCosts}" var="yearMap">
                    <div class="item">für ${yearMap.getKey()}:
                    <g:formatNumber type="currency" number="${yearMap.getValue().total}" currencySymbol="EUR"/> als Gesamtkosten
                    davon anteilig <g:formatNumber number="${yearMap.getValue().partial}" type="currency" currencySymbol="EUR"/> pro Monat
                    </div>
                </g:each>
                </div>
            </g:if>
            <g:elseif test="${institutionalUsage.getKey() == 'ownData'}">
                Zugrunde liegen:
                <g:each in="${ownCosts}" var="yearMap">
                    <div class="item">für ${yearMap.getKey()}:
                    <g:formatNumber type="currency" number="${yearMap.getValue().total}" currencySymbol="EUR"/> als Gesamtkosten
                    davon anteilig <g:formatNumber number="${yearMap.getValue().partial}" type="currency" currencySymbol="EUR"/> pro Monat
                    </div>
                </g:each>
            </g:elseif>
        </g:each>
    </div>

    <g:each in="${yearsInRing}" var="currYear">
        <table class="ui compact celled table">
            <thead>
            <tr>
                <th><g:message code="default.usage.reportType"/></th>
                <th><g:message code="default.usage.metricType"/></th>
                <th><g:message code="default.usage.allUsageGrid.header"/></th>
                <g:each in="${datePoints.get(currYear)}" var="datePoint">
                    <th>
                        ${datePoint}
                    </th>
                </g:each>
            </tr>
            </thead>
            <tbody>
            <g:each in="${costPerUse}" var="institutionalUsage">
                <g:each in="${institutionalUsage.getValue()}" var="costPerMetric">
                    <%
                        String costString, metricType = costPerMetric.getKey()
                        if((metricType.contains('ft_') || metricType in ['sectioned_html', 'toc', 'abstract', 'reference', 'data_set', 'audio', 'video', 'image', 'podcast']) || metricType.matches('\\w+_Requests')) {
                            costString = message(code: 'default.usage.pricePerDownload')
                        }
                        else if(metricType in ['search_reg', 'search_fed'] || metricType.contains('Searches')) {
                            costString = message(code: 'default.usage.pricePerSearch')
                        }
                        else if(metricType == 'result_click') {
                            costString = message(code: 'default.usage.pricePerClick')
                        }
                        else if(metricType == 'record_view' || metricType.matches('\\w+_Investigations')) {
                            costString = message(code: 'default.usage.pricePerView')
                        }
                    %>
                    <g:set var="costs" value="${costPerMetric.getValue()}"/>
                    <tr>
                        <td>${params.reportType.toUpperCase()}</td>
                        <td>
                            ${costString} (${metricType})
                        </td>
                        <td>
                            <g:message code="default.usage.costPerUse.result" args="${[formatNumber(number: costs.get(currYear+'-total'), type: "currency", currencySymbol:"EUR"), sums.get(metricType).get(currYear+'-total')]}"/>
                        </td>
                        <g:each in="${datePoints.get(currYear)}" var="datePoint">
                            <td>
                                <g:if test="${costs.containsKey(datePoint)}">
                                    <g:message code="default.usage.costPerUse.result" args="${[formatNumber(number: costs.get(datePoint), type:"currency", currencySymbol:"EUR"), sums.(metricType).get(datePoint)]}"/>
                                </g:if>
                                <g:else>
                                    <span class="la-long-tooltip la-popup-tooltip" data-tooltip="${message(code: 'default.usage.missingUsageInfo')}"><i class="${Icon.TOOLTIP.IMPORTANT} la-popup small"></i></span>
                                </g:else>
                            </td>
                        </g:each>
                    </tr>
                </g:each>
            </g:each>
            </tbody>
        </table>
    </g:each>

    <laser:script file="${this.getGroovyPageFileName()}">
        function format(data) {
            data = parseFloat(data);
            return data.toLocaleString('de-DE', {style: 'currency', currency: 'EUR'});
        }

        let chartDom = $('#chartWrapper')[0];
        let cpuChart = echarts.init(chartDom);
        let option;

        option = {
            title: {
                text: '<g:message code="default.usage.costPerUse.chartTitle"/>'
            },
            tooltip: {
                trigger: 'item',
                formatter: function(params) {
                    let val;
                    if(params.seriesIndex % 2 === 0) //index 0 or 2
                        val = format(params.value);
                    else val = params.value;
                    return params.seriesName+': '+val;
                }
            },
            legend: {
                data: ['Kosten pro Nutzung', 'Nutzung']
            },
            grid: {
                left: '4%',
                right: '3%',
                containLabel: true
            },
            toolbox: {
                feature: {
                    saveAsImage: {},
                    dataView: {}
                }
            },
            xAxis: {
                type: 'category',
                data: [<g:applyCodec encodeAs="none">'${yearsInRing.collect { String yr -> "${yr} gesamt"}.join("','")}', <g:each in="${datePoints.values()}" var="dpyAxis" status="dpyCt">'${dpyAxis.join("','")}'<g:if test="${dpyCt < datePoints.values().size()-1}">,</g:if></g:each></g:applyCodec>]
            },
            yAxis: [
                {
                    name: 'Kosten pro Nutzung in EUR',
                    type: 'value'
                },
                {
                    name: 'Nutzung',
                    alignTicks: true,
                    type: 'value'
                }
            ],
            series: [
                //series 1-n: metrics
                //series 11: cost
                //series 12: usage
                <g:set var="institutionalUsage" value="${costPerUse.entrySet()[0]}"/> //currently restricted! For each setting, I will need an own graph!
                <g:each in="${institutionalUsage.getValue()}" var="costPerMetric">
                    <%
                        metricType = costPerMetric.getKey()
                        Map<String, Object> costs = costPerMetric.getValue()
                        if((metricType.contains('ft_') || metricType in ['sectioned_html', 'toc', 'abstract', 'reference', 'data_set', 'audio', 'video', 'image', 'podcast']) || metricType.matches('\\w+_Requests')) {
                            costString = message(code: 'default.usage.pricePerDownload')
                        }
                        else if(metricType in ['search_reg', 'search_fed'] || metricType.contains('Searches')) {
                            costString = message(code: 'default.usage.pricePerSearch')
                        }
                        else if(metricType == 'result_click') {
                            costString = message(code: 'default.usage.pricePerClick')
                        }
                        else if(metricType == 'record_view' || metricType.matches('\\w+_Investigations')) {
                            costString = message(code: 'default.usage.pricePerView')
                        }
                    %>
                    {
                        name: <% print "'${costString} (${metricType})'" %>,
                        type: 'bar',
                        data: <% print '['
                            yearsInRing.each { String year ->
                                print costs.get(year+'-total')+','
                            }
                            yearsInRing.eachWithIndex { String year, int h ->
                                SortedSet datePointsYear = datePoints.get(year)
                                datePointsYear.eachWithIndex { String datePoint, int i ->
                                    if(costs.containsKey(datePoint))
                                        print costs.get(datePoint)
                                    else print 0.0
                                    if(i < datePointsYear.size()-1)
                                        print ','
                                }
                                if(h < yearsInRing.size()-1)
                                    print ','
                            }
                        print ']'%>
                    },
                    {
                        name: <% print "'Nutzung (${metricType})'" %>,
                        type: 'bar',
                        yAxisIndex: 1,
                        data: <% print '['
                            yearsInRing.each { String year ->
                                print sums.get(metricType).get(year+'-total')+','
                            }
                            yearsInRing.eachWithIndex { String year, int h ->
                                SortedSet datePointsYear = datePoints.get(year)
                                datePointsYear.eachWithIndex { String datePoint, int i ->
                                    if(sums.get(metricType).containsKey(datePoint))
                                        print sums.get(metricType).get(datePoint)
                                    else print 0
                                    if(i < datePointsYear.size()-1)
                                        print ','
                                }
                                if(h < yearsInRing.size()-1)
                                    print ','
                            }
                        print ']'%>
                    },
                </g:each>
                /*
                {
                    name: 'Kosten pro Nutzung in EUR (Preis pro Download/Total_Item_Requests)',
                    type: 'bar',
                    stack: 'Kosten',
                    data: [150.25, 186.52, 78.39, 100.17, 1803.03, 360.61, 1081.82, 150.25, 284.69, 75.13, 360.61, 150.25]
                },
                {
                    name: 'Kosten pro Nutzung in EUR (Preis pro Download/Unique_Title_Requests)',
                    type: 'bar',
                    stack: 'Kosten',
                    data: [270.45, 318.18, 300.51, 338.07, 1803.03, 901.52, 1352.27, 540.91, 450.76, 216.36, 772.73, 450.76]
                },
                {
                    name: 'Nutzung (Total_Item_Requests)',
                    yAxisIndex: 1,
                    type: 'bar',
                    stack: 'Nutzung',
                    data: [36, 29, 69, 54, 3, 15, 5, 36, 19, 72, 15, 36]
                },
                {
                    name: 'Nutzung (Unique_Title_Requests)',
                    yAxisIndex: 1,
                    type: 'bar',
                    stack: 'Nutzung',
                    data: [20, 17, 18, 16, 3, 6, 4, 10, 12, 25, 7, 12]
                }
                */
            ]
        };
        cpuChart.setOption(option);
    </laser:script>
</div>