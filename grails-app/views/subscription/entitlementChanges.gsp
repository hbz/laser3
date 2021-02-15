<%@page import="de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'myinst.pendingChanges.label')}</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="myinst.pendingChanges.label" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'myinst.pendingChanges.label')}</h1>

            <g:if test="${packages}">
                <g:form controller="pendingChange" action="processAll">
                    <g:select from="${packages}" noSelection="${['':message(code:'default.select.choose.label')]}" name="acceptChangesForPackages" class="ui select search multiple dropdown" optionKey="${{it.id}}" optionValue="${{it.pkg.name}}"/>
                    <g:submitButton class="ui button positive" name="acceptAll" value="${message(code:'pendingChange.takeAll')}"/>
                    <g:submitButton class="ui button negative" name="rejectAll" value="${message(code:'pendingChange.rejectAll')}"/>
                </g:form>
            </g:if>

        <table class="ui celled la-table table">
            <thead>
                <tr>
                    <th><g:message code="profile.dashboard.changes.object"/></th>
                    <th><g:message code="profile.dashboard.changes.event"/></th>
                    <th><g:message code="profile.dashboard.changes.action"/></th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${packageHistory}" var="entry">
                <tr>
                    <td>
                        <g:if test="${entry.tipp}">
                            ${entry.tipp.name}
                        </g:if>
                        <g:elseif test="${entry.tippCoverage}">
                            ${entry.tippCoverage.tipp.name}
                        </g:elseif>
                        <g:elseif test="${entry.tippCoverage}">
                            ${entry.priceItem.tipp.name}
                        </g:elseif>
                    </td>
                    <td>
                        ${message(code:'subscription.packages.'+entry.msgToken)}
                    </td>
                    <td>
                        <g:if test="${!accepted.contains(entry.id)}">
                            <div class="ui buttons">
                                <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.id}" params="[subId:subscription.id]"><g:message code="default.button.accept.label"/></g:link>
                                <%--
                                <div class="or" data-text="${message(code:'default.or')}"></div>
                                <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.id}"><g:message code="default.button.reject.label"/></g:link>
                                --%>
                            </div>
                        </g:if>
                    </td>
                </tr>

            </g:each>
            </tbody>
        </table>

    <semui:paginate offset="${offset}" max="${max}" total="${pendingCount}"/>

  </body>
</html>
