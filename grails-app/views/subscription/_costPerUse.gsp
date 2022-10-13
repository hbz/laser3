<%@ page import="de.laser.utils.DateUtils;" %>
<table class="ui compact celled table">
    <thead>
        <tr>
            <th colspan="2"><g:message code="default.usage.reportType"/></th>
            <th><g:message code="default.usage.allUsageGrid.header"/></th>
            <g:each in="${monthsInRing}" var="month">
                <th>${DateUtils.getSDF_yyyyMM().format(month)}</th>
            </g:each>
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
                <g:each in="${monthsInRing}" var="month">
                    <td>
                        <g:if test="${reportSums.getValue().countsPerMonth.get(month)}">
                            <g:each in="${reportSums.getValue().countsPerMonth.get(month)}" var="metric">
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
                </g:each>
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