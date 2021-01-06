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
                    <th><g:message code="profile.dashboard.changes.eventtype"/></th>
                    <th><g:message code="profile.dashboard.changes.objecttype"/></th>
                    <th><g:message code="profile.dashboard.changes.object"/></th>
                    <th><g:message code="profile.dashboard.changes.event"/></th>
                    <th><g:message code="profile.dashboard.changes.action"/></th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${pending}" var="entry">
                <g:set var="row" value="${pendingChangeService.printRow(entry.change)}" />
                <g:set var="event" value="${row.eventData}"/>
                <tr>
                    <td>
                        ${raw(row.eventIcon)}
                    </td>
                    <td>
                        ${raw(row.instanceIcon)}
                    </td>
                    <td>
                        <g:if test="${entry.change.subscription}">
                            <g:link controller="subscription" action="index" id="${entry.target.id}">${entry.target.dropdownNamingConvention()}</g:link>
                        </g:if>
                        <g:elseif test="${entry.change.costItem}">
                            <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.target.sub.id]}">${entry.target.sub.dropdownNamingConvention()}</g:link>
                        </g:elseif>
                    </td>
                    <td>
                        ${raw(row.eventString)}

                        <g:if test="${entry.change.msgToken == "pendingChange.message_SU_NEW_01"}">
                            <div class="right aligned wide column">
                                <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(entry.change.subscription)]}">
                                    <g:message code="myinst.copyMyElements"/>
                                </g:link>
                            </div>
                        </g:if>
                    </td>
                    <td>
                        <div class="ui buttons">
                            <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.change.id}"><g:message code="default.button.accept.label"/></g:link>
                            <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.change.id}"><g:message code="default.button.reject.label"/></g:link>
                        </div>
                    </td>
                </tr>

            </g:each>
            </tbody>
        </table>

    <semui:paginate offset="${offset}" max="${max}" total="${pendingCount}"/>

  </body>
</html>
