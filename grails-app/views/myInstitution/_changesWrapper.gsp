<%@ page import="de.laser.UserSetting; de.laser.ui.Icon; de.laser.ui.Btn; de.laser.PendingChangeConfiguration; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<div id="pendingChangesWrapper">
    <g:render template="/myInstitution/dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_CHANGES, user: contextService.getUser()]}" />

    <div class="ui card">
        <div class="ui top attached label">
            Filter: <g:message code="profile.dashboard.changes.event"/>
        </div>
        <div class="content">
            <div class="ui form" id="pc-tab-filter">
                <div class="grouped fields">
                    <g:each in="${pending.event.unique()}" var="filter" status="fi">
                        <div class="field">
                            <div class="ui checkbox">
                                <g:set var="fid" value="pc-tab-filter-${fi}" />
                                <input type="checkbox" id="${fid}" name="${filter.replace('pendingChange.message_', '')}" checked="checked">
                                <label for="${fid}"><g:message code="${filter}" args="${['alt', 'neu']}"/></label>
                            </div>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
    </div>

    <table class="ui table very compact">
        <thead>
        <tr>
            <th class="six wide"> <g:message code="profile.dashboard.changes.object"/> </th>
            <th class="four wide"> <g:message code="profile.dashboard.changes.event"/> </th>
            <th class="four wide center aligned"> todo </th>
            <th class="two wide center aligned"> <g:message code="profile.dashboard.changes.ts"/> </th>
        </tr>
        </thead>
        <tbody>

        <g:each in="${pending}" var="entry">
            <g:set var="event" value="${entry.event}"/>

            <tr class="row" data-id="${entry.changeId}" data-f1="${event.replace('pendingChange.message_', '')}">
                <td>
                    <g:if test="${event in PendingChangeConfiguration.TITLE_CHANGES}">
                        <g:link controller="subscription" action="index" id="${entry.subscription.id}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:if>
                    <g:elseif test="${event in PendingChangeConfiguration.COST_ITEM_CHANGES}">
                        <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.subscription.id]}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:elseif>
                    <g:elseif test="${event in [PendingChangeConfiguration.NEW_SUBSCRIPTION, PendingChangeConfiguration.SUBSCRIPTION_RENEWED]}">
                        <g:link controller="subscription" action="show" id="${entry.subscription.id}">${entry.subscription.name}</g:link>
                    </g:elseif>
                </td>
                <td>
                    <g:if test="${event in PendingChangeConfiguration.TITLE_CHANGES}">
                        <g:link controller="subscription" action="entitlementChanges" id="${entry.subscription.id}" params="[tab: 'changes', eventType: event]"><g:message code="${event}" args="${[entry.count]}"/></g:link>
                    </g:if>
                    <g:else>
                        <g:message code="${event}" args="${entry.args}"/>
                    </g:else>
                </td>
                <td>
                    <g:if test="${editable}">
                        <g:if test="${event == PendingChangeConfiguration.NEW_SUBSCRIPTION}">
                            <div class="ui compact grid">
                                <div class="ten wide column">
                                    <g:link class="${Btn.SIMPLE} small" controller="subscription" action="copyMyElements" target="_blank"
                                            params="${[sourceObjectId: entry.source, targetObjectId: entry.target]}">
                                        <g:message code="myinst.copyMyElements"/>
                                    </g:link>
                                </div>
                                <div class="six wide column right aligned">
                                    <ui:remoteLink controller="pendingChange" action="accept" id="${entry.changeId}" params="${[xhr:true]}"
                                                   class="${Btn.MODERN.POSITIVE_CONFIRM}"
                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.accept")}"
                                                   data-confirm-term-how="ok"
                                                   data-done="JSPC.app.dashboard.removePendingChangeEntry('positive', ${entry.changeId})"
                                                   role="button"
                                                   aria-label="${message(code: 'ariaLabel.check.universal')}">
                                        <i class="${Icon.SYM.YES}"></i>
                                    </ui:remoteLink>
                                    <ui:remoteLink controller="pendingChange" action="reject" id="${entry.changeId}" params="${[xhr:true]}"
                                            class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.reject")}"
                                            data-confirm-term-how="ok"
                                            data-done="JSPC.app.dashboard.removePendingChangeEntry('negative', ${entry.changeId})"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.SYM.NO}"></i>
                                    </ui:remoteLink>
                                </div>
                            </div>
                        </g:if>
                        <g:elseif test="${event == PendingChangeConfiguration.SUBSCRIPTION_RENEWED}">
                            <div class="right aligned">
                                <ui:remoteLink controller="pendingChange" action="accept" id="${entry.changeId}" params="${[xhr:true]}"
                                               class="${Btn.MODERN.POSITIVE_CONFIRM}"
                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.accept")}"
                                               data-confirm-term-how="ok"
                                               data-done="JSPC.app.dashboard.removePendingChangeEntry('positive', ${entry.changeId})"
                                               role="button"
                                               aria-label="${message(code: 'ariaLabel.check.universal')}">
                                    <i class="${Icon.SYM.YES}"></i>
                                </ui:remoteLink>
                                <ui:remoteLink controller="pendingChange" action="reject" id="${entry.changeId}" params="${[xhr:true]}"
                                               class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.reject")}"
                                               data-confirm-term-how="ok"
                                               data-done="JSPC.app.dashboard.removePendingChangeEntry('negative', ${entry.changeId})"
                                               role="button"
                                               aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.SYM.NO}"></i>
                                </ui:remoteLink>
                            </div>
                        </g:elseif>
                        <g:elseif test="${event in PendingChangeConfiguration.COST_ITEM_CHANGES}">
                            <div class="right aligned">
                                    <ui:remoteLink controller="finance" action="acknowledgeChange" id="${entry.changeId}" params="${[xhr:true]}"
                                            class="${Btn.MODERN.SIMPLE_CONFIRM} primary"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.acknowledge")}"
                                            data-confirm-term-how="ok"
                                            data-done="JSPC.app.dashboard.removePendingChangeEntry('warning', ${entry.changeId})"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.check.universal')}">
                                        ${message(code: 'pendingChange.acknowledge')}
                                    </ui:remoteLink>
                            </div>
                        </g:elseif>
                    </g:if>
                </td>
                <td class="center aligned">
                    <g:formatDate date="${entry._ts}" format="${message(code:'default.date.format.notime')}"/>
                </td>
            </tr>
        </g:each>

        </tbody>
    </table>
</div>

<div id="acceptedChangesWrapper">
    <g:render template="/myInstitution/dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_CHANGES, user: contextService.getUser()]}" />

    <div class="ui card">
        <div class="ui top attached label">
            Filter: <g:message code="profile.dashboard.changes.event"/>
        </div>
        <div class="content">
            <div class="ui form" id="ac-tab-filter">
                <div class="grouped fields">
                    <g:each in="${notifications.event.unique()}" var="filter" status="fi">
                        <div class="field">
                            <div class="ui checkbox">
                                <g:set var="fid" value="ac-tab-filter-${fi}" />
                                <input type="checkbox" id="${fid}" name="${filter.replace('pendingChange.message_', '')}" checked="checked">
                                <label for="${fid}"><g:message code="${filter}" args="${['alt', 'neu']}"/></label>
                            </div>
                        </div>
                    </g:each>
                </div>
            </div>
        </div>
    </div>

    <table class="ui table very compact">
        <thead>
            <tr>
                <th class="seven column"> <g:message code="profile.dashboard.changes.object"/> </th>
                <th class="seven column"> <g:message code="profile.dashboard.changes.event"/> </th>
                <th class="two column"> <g:message code="profile.dashboard.changes.actionDate"/> </th>
            </tr>
        </thead>
        <tbody>

        <g:each in="${notifications}" var="entry">
            <g:set var="event" value="${entry.event}"/>

            <tr class="row" data-f2="${event.replace('pendingChange.message_', '')}">
                <td>
                    <g:if test="${event in PendingChangeConfiguration.TITLE_CHANGES}">
                        <g:link controller="subscription" action="index" id="${entry.subscription.id}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:if>
                    <g:elseif test="${event in PendingChangeConfiguration.COST_ITEM_CHANGES}">
                        <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.subscription.id]}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:elseif>
                    <g:elseif test="${event in [PendingChangeConfiguration.NEW_SUBSCRIPTION, PendingChangeConfiguration.SUBSCRIPTION_RENEWED]}">
                        <g:link controller="subscription" action="show" id="${entry.subscription.id}">${entry.subscription.name}</g:link>
                    </g:elseif>
                </td>
                <td>
                    <g:if test="${event in PendingChangeConfiguration.TITLE_CHANGES}">
                        <g:link controller="subscription" action="entitlementChanges" id="${entry.subscription.id}" params="[tab: 'acceptedChanges', eventType: event]"><g:message code="${event}" args="${[entry.count]}"/></g:link>
                    </g:if>
                    <g:else>
                        <g:message code="${event}" args="${entry.args}"/>
                    </g:else>
                    <%--<g:if test="${event == PendingChangeConfiguration.NEW_SUBSCRIPTION}">
                        <div class="right aligned wide column">
                            <g:link class="${Btn.SIMPLE}" controller="subscription" action="copyMyElements" params="${[sourceObjectId: entry.source, targetObjectId: entry.target]}">
                                <g:message code="myinst.copyMyElements"/>
                            </g:link>
                        </div>
                    </g:if>--%>
                </td>
                <td>
%{--                    <g:if test="${! entry._status}">--}%
%{--                        ???--}%
%{--                    </g:if>--}%
%{--                    <g:elseif test="${RDStore.PENDING_CHANGE_ACCEPTED.id == entry._status.id}">--}%
%{--                        <i class="${Icon.SYM.YES} green"></i>--}%
%{--                    </g:elseif>--}%
%{--                    <g:elseif test="${RDStore.PENDING_CHANGE_REJECTED.id == entry._status.id}">--}%
%{--                        <i class="${Icon.SYM.NO} red"></i>--}%
%{--                    </g:elseif>--}%
%{--                    <g:else>--}%
%{--                        ${entry._status.getI10n('value')}--}%
%{--                    </g:else>--}%

                    <g:formatDate date="${entry._actionDate}" format="${message(code:'default.date.format.noZ')}"/>
                </td>
            </tr>
        </g:each>

        </tbody>
    </table>

    <laser:script file="${this.getGroovyPageFileName()}">
        $("#pendingCount").text("${pendingCount}")
        $("#notificationsCount").text("${notificationsCount}")

        JSPC.app.dashboard.removePendingChangeEntry = function (state, id) {
            let $e = $('#pendingChangesWrapper .row[data-id=' + id + ']')
            $e.addClass(state).fadeOut(1000)
            setTimeout(() => { $e.remove() }, 1200)
        }

        $('#pendingChangesWrapper #pc-tab-filter .checkbox').checkbox({
            onChange: function() {
                let n = $(this).attr('name')
                $('#pendingChangesWrapper .row[data-f1=' + n + ']').toggle()
            }
        })
        $('#acceptedChangesWrapper #ac-tab-filter .checkbox').checkbox({
            onChange: function() {
                let n = $(this).attr('name')
                $('#acceptedChangesWrapper .row[data-f2=' + n + ']').toggle()
            }
        })
    </laser:script>
</div>