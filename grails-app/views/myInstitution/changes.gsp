<%@page import="de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange" %>

<laser:htmlStart message="myinst.menu.pendingChanges.label" serviceInjection="true" />

        <semui:breadcrumbs>
            <semui:crumb message="myinst.pendingChanges.label" class="active" />
        </semui:breadcrumbs>

        <semui:h1HeaderWithIcon message="myinst.menu.pendingChanges.label" />

            <g:if test="${packages}">
                <g:form controller="pendingChange" action="processAll">
                    <g:select from="${packages}" noSelection="${['':message(code:'default.select.choose.label')]}" name="acceptChangesForPackages" class="ui select search multiple dropdown" optionKey="${{it.id}}" optionValue="${{it.pkg.name}}"/>
                    <g:submitButton class="ui button positive" name="acceptAll" value="${message(code:'pendingChange.takeAll')}"/>
                    <g:submitButton class="ui button negative" name="rejectAll" value="${message(code:'pendingChange.rejectAll')}"/>
                </g:form>
            </g:if>

        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
                <tr>
                    <th><g:message code="profile.dashboard.changes.eventtype"/></th>
                    <th><g:message code="profile.dashboard.changes.objecttype"/></th>
                    <th><g:message code="profile.dashboard.changes.object"/></th>
                    <th><g:message code="profile.dashboard.changes.event"/></th>
                    <th><g:message code="default.date.label"/></th>
                    <th><g:message code="default.action.label"/></th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${pending}" var="entry">
                <g:set var="row" value="${pendingChangeService.printRow(PendingChange.get(entry.changeId))}" />
                <g:set var="event" value="${row.eventData}"/>
                <tr>
                    <td>
                        ${raw(row.eventIcon)}
                    </td>
                    <td>
                        ${raw(row.instanceIcon)}
                    </td>
                    <td>
                        <g:if test="${entry.subPkg}">
                            <g:link controller="subscription" action="index" id="${entry.subPkg.subscription.id}">${entry.subPkg.subscription.dropdownNamingConvention()}</g:link>
                        </g:if>
                        <g:elseif test="${entry.costItem}">
                            <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.costItem.sub.id]}">${entry.costItem.sub.dropdownNamingConvention()}</g:link>
                        </g:elseif>
                        <g:elseif test="${entry.subscription}">
                                <g:link controller="subscription" action="show" id="${entry.subscription.id}" >
                                    ${entry.subscription.name}
                                </g:link>
                        </g:elseif>
                    </td>
                    <td>
                        <g:if test="${entry.subPkg}">
                            <g:link controller="subscription" action="entitlementChanges" id="${entry.subPkg.subscription.id}" params="[tab: 'changes']">${raw(entry.eventString)}</g:link>
                        </g:if>
                        <g:else>
                            ${raw(entry.eventString)}
                        </g:else>

                        <g:if test="${entry.subscription}">
                            <div class="right aligned wide column">
                                <g:each in="${entry.subscription._getCalculatedPrevious()}" var="prev">
                                    <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(prev), targetObjectId: genericOIDService.getOID(entry.subscription)]}">
                                        <g:message code="myinst.copyMyElements"/>
                                    </g:link>
                                </g:each>
                            </div>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate format="${message(code: 'default.date.format.noZ')}" date="${entry.ts}"/>
                    </td>
                    <td>
                        <g:if test="${editable && entry.changeId && entry.subPkg}">
                            <div class="ui buttons">
                                <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.changeId}" params="[subId: entry.subPkg.subscription.id]"><g:message code="default.button.accept.label"/></g:link>
                                <div class="or" data-text="${message(code:'default.or')}"></div>
                                <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.changeId}" params="[subId: entry.subPkg.subscription.id]"><g:message code="default.button.reject.label"/></g:link>
                            </div>
                        </g:if>
                        <g:elseif test="${editable && entry.changeId}">
                            <div class="ui buttons">
                                <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.changeId}"><g:message code="default.button.accept.label"/></g:link>
                                <div class="or" data-text="${message(code:'default.or')}"></div>
                                <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.changeId}"><g:message code="default.button.reject.label"/></g:link>
                            </div>
                        </g:elseif>
                    </td>
                </tr>

            </g:each>
            </tbody>
        </table>

    <semui:paginate offset="${offset}" max="${max}" total="${pendingCount}"/>

<laser:htmlEnd />
