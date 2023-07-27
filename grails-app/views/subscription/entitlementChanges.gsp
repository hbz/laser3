<%@ page import="grails.converters.JSON; de.laser.PendingChangeConfiguration; de.laser.TitleInstancePackagePlatform; de.laser.Subscription;de.laser.License;de.laser.finance.CostItem;de.laser.PendingChange; de.laser.TitleChange; de.laser.IssueEntitlementChange; de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.RefdataValue;" %>

<laser:htmlStart message="myinst.menu.pendingChanges.label" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" type="subscription" visibleOrgRelations="${visibleOrgRelations}">
    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
        <laser:render template="iconSubscriptionIsChild"/>
    </g:if>
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<ui:anualRings object="${subscription}" controller="subscription" action="entitlementChanges" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<laser:render template="nav"/>

<g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
    <laser:render template="message"/>
</g:if>


<div class="ui top attached stackable tabular la-tab-with-js menu">
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
    <g:if test="${packages && params.tab != 'acceptedChanges' && changes && editable}">
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

    <div class="ui top attached stackable tabular la-tab-with-js menu">
        <%
            Set<String> eventTabs = []
            /*
            eventTabs.addAll(PendingChangeConfiguration.SETTING_KEYS)
            eventTabs.removeAll([PendingChangeConfiguration.PACKAGE_PROP, PendingChangeConfiguration.PACKAGE_DELETED])
            eventTabs << PendingChangeConfiguration.TITLE_DELETED
            */
            eventTabs << PendingChangeConfiguration.TITLE_REMOVED
            int currentCount = 0
        %>
        <g:each in="${eventTabs}" var="event">
            <%
                switch(event) {
                    case PendingChangeConfiguration.NEW_TITLE: currentCount = params.tab == 'acceptedChanges' ? newTitlesAccepted : newTitlesPending
                        break
                    case PendingChangeConfiguration.TITLE_STATUS_CHANGED: currentCount = params.tab == 'acceptedChanges' ? titlesStatusChangedAccepted : 0
                        break
                    case PendingChangeConfiguration.TITLE_DELETED: currentCount = params.tab == 'changes' ? titlesDeletedPending : 0
                        break
                    case PendingChangeConfiguration.TITLE_REMOVED: currentCount = params.tab == 'acceptedChanges' ? titlesRemovedAccepted : 0
                        break
                }
            %>
            <g:if test="${(event == PendingChangeConfiguration.TITLE_DELETED && params.tab == 'changes') || (event == PendingChangeConfiguration.TITLE_REMOVED && params.tab == 'acceptedChanges')
                    || !(event in [PendingChangeConfiguration.TITLE_DELETED, PendingChangeConfiguration.TITLE_STATUS_CHANGED])}">
                <g:link controller="subscription" action="entitlementChanges" id="${subscription.id}"
                        params="[eventType: event, tab: params.tab]"
                        class="item ${params.eventType == event ? 'active' : ''}">
                    <g:message code="subscription.packages.tabs.${event}"/>
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
                <g:sortableColumn property="tic.event" title="${message(code: 'profile.dashboard.changes.event')}"
                                  params="[tab: params.tab]"/>
                <g:if test="${params.tab == 'acceptedChanges' && params.eventType == PendingChangeConfiguration.NEW_TITLE}">
                    <g:sortableColumn property="actionDate" title="${message(code: 'default.date.label')}" params="[tab: params.tab]"/>
                    <th><g:message code="default.status.label"/></th>
                </g:if>
                <g:else>
                    <g:sortableColumn property="dateCreated" title="${message(code: 'default.date.label')}" params="[tab: params.tab]"/>
                    <g:if test="${editable && params.tab == 'changed'}">
                        <th><g:message code="default.action.label"/></th>
                    </g:if>
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
                        <g:set var="change" />
                        <g:set var="actionDate" />

                        <g:if test="${entry instanceof TitleChange}">
                            <g:set var="tipp" value="${entry.tipp}"/>
                            <g:set var="change" value="${entry}"/>
                            <g:set var="actionDate" value="${entry.dateCreated}"/>
                        </g:if>
                        <g:elseif test="${entry instanceof de.laser.IssueEntitlementChange}">
                            <g:set var="tipp" value="${entry.titleChange.tipp}"/>
                            <g:set var="change" value="${entry.titleChange}"/>
                            <g:set var="actionDate" value="${entry.actionDate}"/>
                        </g:elseif>
                        <g:elseif test="${entry instanceof de.laser.IssueEntitlement}">
                            <g:set var="tipp" value="${entry.tipp}"/>
                            <g:set var="change" value="${PendingChangeConfiguration.TITLE_REMOVED}"/>
                            <g:set var="actionDate" value="${entry.lastUpdated}"/>
                        </g:elseif>

                        <g:set var="ie" value="${IssueEntitlement.findByTippAndSubscription(tipp, subscription)}"/>

                        <g:if test="${tipp}">

                            <g:if test="${ie}">
                                <g:link controller="issueEntitlement" action="show" id="${ie.id}">${ie.tipp.name}</g:link>
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
                        <g:if test="${change instanceof TitleChange || change instanceof de.laser.IssueEntitlementChange}">
                            <g:if test="${change.field in PendingChange.REFDATA_FIELDS}">
                                <g:set var="oldValue" value="${change.oldRefVal.getI10n('value')}"/>
                                <g:set var="newValue" value="${change.newRefVal.getI10n('value')}"/>
                            </g:if>
                            <g:elseif test="${change.field in PendingChange.DATE_FIELDS}">
                                <g:set var="oldValue" value="${change.oldDateVal}"/>
                                <g:set var="newValue" value="${change.newDateVal}"/>
                            </g:elseif>
                            <g:else>
                                <g:set var="oldValue" value="${change.oldVal}"/>
                                <g:set var="newValue" value="${change.newVal}"/>
                            </g:else>

                            <g:if test="${change.event in [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_DELETED]}">
                                <g:message code="subscription.packages.${change.event}"/>
                            </g:if>
                        </g:if>
                        <g:elseif test="${change == PendingChangeConfiguration.TITLE_REMOVED}">
                            <g:message code="subscription.packages.${change}"/>
                        </g:elseif>
                        <g:else>
                            <g:if test="${oldValue != null || newValue != null}">
                                ${(message(code: 'tipp.' + change.field) ?: '') + ': ' + message(code: 'pendingChange.change', args: [oldValue, newValue])}
                            </g:if>
                            <g:elseif test="${change.field}">
                                ${message(code: 'tipp.' + change.field)}
                            </g:elseif>
                        </g:else>

                    </td>
                    <td>
                        <g:formatDate format="${message(code: 'default.date.format.noZ')}" date="${actionDate}"/>
                    </td>
                    <g:if test="${params.tab == 'acceptedChanges' && params.eventType == PendingChangeConfiguration.NEW_TITLE}">
                        <td>${entry.status.getI10n('value')}</td>
                    </g:if>

                    <g:elseif test="${editable && params.tab == 'changes'}">
                        <td>
                            <div class="ui buttons">
                                <g:link class="ui positive button" controller="pendingChange" action="acceptTitleChange"
                                        id="${entry.id}"
                                        params="[subId: subscription.id]"><g:message
                                        code="default.button.accept.label"/></g:link>
                                <div class="or" data-text="${message(code: 'default.or')}"></div>
                                <g:link class="ui negative button" controller="pendingChange" action="rejectTitleChange"
                                        id="${entry.id}"
                                        params="[subId: subscription.id]"><g:message
                                        code="default.button.reject.label"/></g:link>
                            </div>
                        </td>
                    </g:elseif>

                </tr>

            </g:each>
            </tbody>
        </table>

        <ui:paginate offset="${offset}" max="${max}" total="${num_change_rows}" params="${params}"/>
    </div>
</div>
<laser:htmlEnd />
