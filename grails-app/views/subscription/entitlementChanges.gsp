<%@ page import="grails.converters.JSON; de.laser.PendingChangeConfiguration; de.laser.TitleInstancePackagePlatform; de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange; de.laser.IssueEntitlement; de.laser.helper.RDStore; de.laser.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.menu.pendingChanges.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<h1 class="ui icon header la-noMargin-top"><semui:headerTitleIcon type="Subscription"/>
<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="iconSubscriptionIsChild"/>
</g:if>
<semui:xEditable owner="${subscription}" field="name"/>
</h1>
<semui:anualRings object="${subscription}" controller="subscription" action="entitlementChanges"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="nav"/>

<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <g:render template="message"/>
</g:if>


<div class="ui top attached stackable tabular menu">
    <g:link controller="subscription" action="entitlementChanges" id="${subscription.id}" params="[tab: 'changes', eventType: params.eventType]"
            class="item ${params.tab == "changes" ? 'active' : ''}">
        <g:message code="myinst.menu.pendingChanges.label"/>
        <span class="ui blue circular label">
            ${countPendingChanges}
        </span>
    </g:link>

    <g:link controller="subscription" action="entitlementChanges" id="${subscription.id}"
            params="[tab: 'acceptedChanges', eventType: params.eventType]"
            class="item ${params.tab == "acceptedChanges" ? 'active' : ''}">
        <g:message code="myinst.menu.acceptedChanges.label"/>
        <span class="ui blue circular label">
            ${countAcceptedChanges}
        </span>
    </g:link>

</div>

<div class="ui bottom attached tab active segment">
    <g:if test="${packages && params.tab != 'acceptedChanges' && changes}">
        <g:form controller="pendingChange" action="processAll">
            <g:hiddenField name="eventType" value="${params.eventType}"/>
            <g:select from="${packages}" noSelection="${['': message(code: 'default.select.choose.label')]}"
                      name="acceptChangesForPackages" class="ui select search multiple dropdown"
                      optionKey="${{ it.id }}"
                      optionValue="${{ it.pkg.name }}"/>
            <g:submitButton class="ui button positive" name="acceptAll"
                            value="${message(code: 'pendingChange.takeAll')}"/>
            <g:submitButton class="ui button negative" name="rejectAll"
                            value="${message(code: 'pendingChange.rejectAll')}"/>
        </g:form>
    </g:if>

    <div class="ui top attached stackable tabular menu">
        <%
            Set<String> eventTabs = PendingChangeConfiguration.SETTING_KEYS
            eventTabs.removeAll([PendingChangeConfiguration.PACKAGE_PROP, PendingChangeConfiguration.PACKAGE_DELETED])
            eventTabs << PendingChangeConfiguration.TITLE_REMOVED
            int currentCount = 0
        %>
        <g:each in="${eventTabs}" var="event">
            <%
                switch(event) {
                    case PendingChangeConfiguration.NEW_TITLE: currentCount = params.tab == 'acceptedChanges' ? newTitlesAccepted : newTitlesPending
                        break
                    case PendingChangeConfiguration.TITLE_UPDATED: currentCount = params.tab == 'acceptedChanges' ? titlesUpdatedAccepted : titlesUpdatedPending
                        break
                    case PendingChangeConfiguration.TITLE_DELETED: currentCount = params.tab == 'acceptedChanges' ? titlesDeletedAccepted : titlesDeletedPending
                        break
                    case PendingChangeConfiguration.TITLE_REMOVED: currentCount = params.tab == 'changes' ? titlesRemovedPending : 0
                        break
                    case PendingChangeConfiguration.NEW_COVERAGE: currentCount = params.tab == 'acceptedChanges' ? newCoveragesAccepted : newCoveragesPending
                        break
                    case PendingChangeConfiguration.COVERAGE_UPDATED: currentCount = params.tab == 'acceptedChanges' ? coveragesUpdatedAccepted : coveragesUpdatedPending
                        break
                    case  PendingChangeConfiguration.COVERAGE_DELETED: currentCount = params.tab == 'acceptedChanges' ? coveragesDeletedAccepted : coveragesDeletedPending
                        break
                }
            %>
            <g:if test="${(event == PendingChangeConfiguration.TITLE_REMOVED && params.tab == 'changes') || event != PendingChangeConfiguration.TITLE_REMOVED}">
                <g:link controller="subscription" action="entitlementChanges" id="${subscription.id}"
                        params="[eventType: event, tab: params.tab]"
                        class="item ${params.eventType == event ? 'active' : ''}">
                    <g:message code="subscription.packages.${event}"/>
                    <span class="ui circular label">
                        ${currentCount}
                    </span>
                </g:link>
            </g:if>
        </g:each>
    </div>
    <div class="ui bottom attached tab active segment">
        <g:set var="counter" value="${offset + 1}"/>
        <table class="ui celled la-js-responsive-table la-table table sortable">
            <thead>
            <tr>
                <th>${message(code: 'sidewide.number')}</th>
                <th><g:message code="profile.dashboard.changes.object"/></th>
                <g:sortableColumn property="msgToken" title="${message(code: 'profile.dashboard.changes.event')}"
                                  params="[tab: params.tab]"/>
                <g:sortableColumn property="ts" title="${message(code: 'default.date.label')}" params="[tab: params.tab]"/>
                <g:if test="${params.tab == 'acceptedChanges'}">
                    <th><g:message code="default.status.label"/></th>
                </g:if>
                <g:else>
                    <th><g:message code="profile.dashboard.changes.action"/></th>
                </g:else>
            </tr>
            </thead>
            <tbody>
            <g:each in="${changes}" var="entry">
                <tr>
                    <td>
                        ${counter++}
                    </td>
                    <td>

                        <g:set var="tipp" />

                        <g:if test="${entry.tipp}">
                            <g:set var="tipp" value="${entry.tipp}"/>
                        </g:if>
                        <g:if test="${entry.tippCoverage}">
                            <g:set var="tipp" value="${entry.tippCoverage.tipp}"/>
                        </g:if>
                    <%--<g:elseif test="${entry.priceItem}">
                        <g:set var="tipp" value="${entry.priceItem.tipp}"/>
                    </g:elseif>--%>
                        <g:elseif test="${entry.oid}">
                            <g:set var="object" value="${genericOIDService.resolveOID(entry.oid)}"/>
                            <g:if test="${object instanceof IssueEntitlement}">
                                <g:set var="tipp" value="${object.tipp}"/>
                            </g:if>
                            <g:if test="${object instanceof TitleInstancePackagePlatform}">
                                <g:set var="tipp" value="${object}"/>
                            </g:if>
                        </g:elseif>

                        <g:set var="ie" value="${tipp instanceof TitleInstancePackagePlatform ? IssueEntitlement.findByTippAndSubscription(tipp, subscription) : null}"/>

                        <g:if test="${tipp}">

                            <g:if test="${ie}">
                                <g:link controller="issueEntitlement" action="show" id="${ie.id}">${ie.name}</g:link>
                            </g:if>
                            <g:else>
                                ${tipp.name}
                            </g:else>


                            <div class="la-title">${message(code: 'default.details.label')}</div>

                            <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                    data-content="${message(code: 'laser')}"
                                    target="_blank"
                                    controller="tipp" action="show"
                                    id="${tipp.id}">
                                <i class="book icon"></i>
                            </g:link>

                            <g:each in="${apisources}" var="gokbAPI">
                                <g:if test="${tipp.gokbId}">
                                    <a role="button"
                                       class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                       data-content="${message(code: 'wekb')}"
                                       href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/public/tippContent/?id=' + tipp.gokbId : '#'}"
                                       target="_blank"><i class="la-gokb  icon"></i>
                                    </a>
                                </g:if>
                            </g:each>
                        </g:if>

                    </td>
                    <td>
                        <g:if test="${entry.targetProperty in PendingChange.REFDATA_FIELDS}">
                            <g:set var="oldValue" value="${RefdataValue.get(entry.oldValue)?.getI10n('value')}"/>
                            <g:set var="newValue" value="${RefdataValue.get(entry.newValue)?.getI10n('value')}"/>
                        </g:if>
                        <g:else>
                            <g:set var="oldValue" value="${entry.oldValue}"/>
                            <g:set var="newValue" value="${entry.newValue}"/>
                        </g:else>

                        <g:if test="${entry.msgToken == PendingChangeConfiguration.TITLE_DELETED}">
                            <g:message code="subscription.packages.${entry.msgToken}"/>
                        </g:if>
                        <g:elseif test="${entry.msgToken == PendingChangeConfiguration.COVERAGE_DELETED}">
                            <g:set var="oldValue" value="${JSON.parse(entry.oldValue)}"/>
                            <g:if test="${oldValue.startDate}">
                                ${oldValue.startDate} - ${oldValue.startVolume}/${oldValue.startIssue} – ${oldValue.endDate} - ${oldValue.endVolume}/${oldValue.endIssue}
                            </g:if>
                        </g:elseif>
                        <g:else>
                            <g:if test="${oldValue != null || newValue != null}">
                                ${(message(code: 'tipp.' + (entry.priceItem ? 'price.' : '') + entry.targetProperty) ?: '') + ': ' + message(code: 'pendingChange.change', args: [oldValue, newValue])}
                            </g:if>
                            <g:elseif test="${entry.targetProperty}">
                                ${message(code: 'tipp.' + (entry.priceItem ? 'price.' : '') + entry.targetProperty)}
                            </g:elseif>
                        </g:else>

                    </td>
                    <td>
                        <g:formatDate format="${message(code: 'default.date.format.noZ')}" date="${entry.ts}"/>
                    </td>
                    <g:if test="${params.tab == 'acceptedChanges'}">
                        <td>${entry.status.getI10n('value')}</td>
                    </g:if>
                    <g:else>
                        <td>
                            <div class="ui buttons">
                                <g:link class="ui positive button" controller="pendingChange" action="accept"
                                        id="${entry.id}"
                                        params="[subId: subscription.id]"><g:message
                                        code="default.button.accept.label"/></g:link>
                                <div class="or" data-text="${message(code: 'default.or')}"></div>
                                <g:link class="ui negative button" controller="pendingChange" action="reject"
                                        id="${entry.id}"
                                        params="[subId: subscription.id]"><g:message
                                        code="default.button.reject.label"/></g:link>
                            </div>
                        </td>
                    </g:else>

                </tr>

            </g:each>
            </tbody>
        </table>

        <semui:paginate offset="${offset}" max="${max}" total="${num_change_rows}" params="${params}"/>
    </div>


</div>
</body>
</html>
