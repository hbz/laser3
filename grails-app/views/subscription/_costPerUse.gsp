<%@ page import="de.laser.utils.DateUtils;" %>
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
            <tr>
                <g:each in="${costPerMetric.getValue()}" var="cost">
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
                </g:each>
            </tr>
        </tbody>
    </table>
</g:each>
<table class="ui compact celled table">
    <thead>
        <tr>
            <th colspan="0"><g:message code="default.usage.reportType"/></th>
            <th><g:message code="default.usage.allUsageGrid.header"/></th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${sums}" var="reportSums">
            <tr>
                <td>${reportSums.getKey()}</td>
                <td>
                    <g:each in="${reportSums.getValue().sumsPerMetric}" var="sum">
                        ${sum.getKey()}<br>
                    </g:each>
                </td>
                <td>
                    <g:each in="${reportSums.getValue().sumsPerMetric}" var="sum">
                        ${sum.getValue()}<br>
                    </g:each>
                </td>
                <td>
                    <table>
                        <g:if test="${reportSums.getValue().containsKey('countsPerYop')}">
                            <tr>
                                <g:each in="${reportSums.getValue().countsPerYop.keySet()}" var="yop">
                                    <th>${yop}</th>
                                </g:each>
                            </tr>
                            <tr>
                                <g:each in="${reportSums.getValue().countsPerYop.keySet()}" var="yop">
                                    <td>
                                        <g:each in="${reportSums.getValue().countsPerYop.get(yop)}" var="metric">
                                            ${metric.getValue()}
                                        </g:each>
                                    </td>
                                </g:each>
                            </tr>
                        </g:if>
                        <g:else>
                            <g:set var="currMonth" value="${GregorianCalendar.getInstance()}"/>
                            <g:set var="currMonthInner" value="${GregorianCalendar.getInstance()}"/>
                            <g:each in="${monthsInRing}" var="month" status="monthCtr">
                                <%
                                    currMonth.setTime(month)
                                %>
                                <g:if test="${currMonth.get(Calendar.MONTH) == Calendar.JANUARY}">
                                    <tr>
                                </g:if>
                                <th>${DateUtils.getSDF_yyyyMM().format(month)}</th>
                                <g:if test="${currMonth.get(Calendar.MONTH) == Calendar.DECEMBER || monthCtr == monthsInRing.size()-1}">
                                    </tr>
                                    <g:each in="${monthsInRing}" var="monthInner" status="monthInnerCtr">
                                        <%
                                            currMonthInner.setTime(monthInner)
                                        %>
                                        <g:if test="${currMonthInner.get(Calendar.YEAR) == currMonth.get(Calendar.YEAR)}">
                                            <g:if test="${currMonthInner.get(Calendar.MONTH) == Calendar.JANUARY}">
                                                <tr>
                                            </g:if>
                                            <td>
                                                <g:if test="${reportSums.getValue().countsPerMonth.get(monthInner)}">
                                                    <g:each in="${reportSums.getValue().countsPerMonth.get(monthInner)}" var="metric">
                                                        ${metric.getValue()}<br>
                                                    </g:each>
                                                </g:if>
                                                <g:else>
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                          data-tooltip="${message(code: 'default.usage.missingUsageInfo')}">
                                                        <i class="exclamation triangle icon la-popup small"></i>
                                                    </span>
                                                </g:else>
                                            </td>
                                            <g:if test="${currMonthInner.get(Calendar.MONTH) == Calendar.DECEMBER || monthInnerCtr == monthsInRing.size()-1}">
                                                </tr>
                                            </g:if>
                                        </g:if>
                                    </g:each>
                                </g:if>
                            </g:each>
                        </g:else>
                    </table>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>
<table class="ui compact celled table">
    <thead>
        <tr>
            <th colspan="2"><g:message code="default.usage.reportType"/></th>
            <g:each in="${allYears}" var="year">
                <th>${year}</th>
            </g:each>
        </tr>
    </thead>
    <tbody>
        <g:each in="${allYearSums}" var="row">
            <tr>
                <td>${row.getKey()}</td>
                <td>
                    <g:each in="${row.getValue().metrics}" var="metric">
                        ${metric}<br>
                    </g:each>
                </td>
                <g:each in="${row.getValue().countsPerYear}" var="yearSum">
                    <td>
                        <g:each in="${row.getValue().metrics}" var="metric">
                            ${yearSum.getValue().get(metric)}<br>
                        </g:each>
                    </td>
                </g:each>
            </tr>
        </g:each>
    </tbody>
</table>