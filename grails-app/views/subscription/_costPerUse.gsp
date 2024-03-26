<%@ page import="de.laser.utils.DateUtils;" %>
<div class="ui segment" id="costPerUse">
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
    <table class="ui compact celled table">
        <thead>
        <tr>
            <th><g:message code="default.usage.reportType"/></th>
            <th><g:message code="default.usage.metricType"/></th>
            <th><g:message code="default.usage.allUsageGrid.header"/></th>
            <g:each in="${datePoints}" var="datePoint">
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
                            <g:message code="default.usage.costPerUse.result" args="${[formatNumber(number: costs.get('total'), type: "currency", currencySymbol:"EUR"), sums.get(metricType).total]}"/>

                        </td>
                        <g:each in="${datePoints}" var="datePoint">
                            <td>
                                <g:if test="${costs.containsKey(datePoint)}">
                                    <g:message code="default.usage.costPerUse.result" args="${[formatNumber(number: costs.get(datePoint), type:"currency", currencySymbol:"EUR"), sums.(metricType).get(datePoint)]}"/>
                                </g:if>
                                <g:else>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-tooltip="${message(code: 'default.usage.missingUsageInfo')}"><i class="exclamation triangle icon la-popup small"></i></span>
                                </g:else>
                            </td>
                        </g:each>
                    </tr>
                </g:each>
            </g:each>
        </tbody>
    </table>
</div>