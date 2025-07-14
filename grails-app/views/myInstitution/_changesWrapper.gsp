<%@ page import="de.laser.UserSetting; de.laser.ui.Icon; de.laser.ui.Btn; de.laser.PendingChangeConfiguration; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<div id="pendingChangesWrapper">
    <g:render template="/myInstitution/dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_CHANGES, user: contextService.getUser()]}" />

    <div class="ui internally celled compact grid">
        <div class="row">
            <div class="six wide column center aligned">
                <strong><g:message code="profile.dashboard.changes.object"/></strong>
            </div>
            <div class="eight wide column center aligned">
                <strong><g:message code="profile.dashboard.changes.event"/></strong>
            </div>
            <div class="two wide column center aligned">
                <strong><g:message code="profile.dashboard.changes.ts"/></strong>
            </div>
        </div>
        <g:each in="${pending}" var="entry">
            <g:set var="event" value="${entry.event}"/>
            <div class="row">
                <div class="six wide column">
                    <g:if test="${event in PendingChangeConfiguration.TITLE_CHANGES}">
                        <g:link controller="subscription" action="index" id="${entry.subscription.id}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:if>
                    <g:elseif test="${event in PendingChangeConfiguration.COST_ITEM_CHANGES}">
                        <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.subscription.id]}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:elseif>
                    <g:elseif test="${event in [PendingChangeConfiguration.NEW_SUBSCRIPTION, PendingChangeConfiguration.SUBSCRIPTION_RENEWED]}">
                        <g:link controller="subscription" action="show" id="${entry.subscription.id}">${entry.subscription.name}</g:link>
                    </g:elseif>
                </div><!-- .column -->
                <div class="eight wide column">
                    <g:if test="${event in PendingChangeConfiguration.TITLE_CHANGES}">
                        <g:link controller="subscription" action="entitlementChanges" id="${entry.subscription.id}" params="[tab: 'changes', eventType: event]"><g:message code="${event}" args="${[entry.count]}"/></g:link>
                    </g:if>
                    <g:else>
                        <g:message code="${event}" args="${entry.args}"/>
                    </g:else>
                    <g:if test="${editable}">
                        <g:if test="${event == PendingChangeConfiguration.NEW_SUBSCRIPTION}">
                            <div class="ui compact grid">
                                <div class="eight wide column">
                                    <g:link class="${Btn.SIMPLE}" controller="subscription" action="copyMyElements" params="${[sourceObjectId: entry.source, targetObjectId: entry.target]}">
                                        <g:message code="myinst.copyMyElements"/>
                                    </g:link>
                                </div>
                                <div class="eight wide column right aligned">
                                    <g:link controller="pendingChange" action="accept" id="${entry.changeId}" class="${Btn.MODERN.POSITIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.accept")}"
                                        data-confirm-term-how="ok"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.check.universal')}">
                                        <i class="${Icon.SYM.YES}"></i>
                                    </g:link>
                                    <g:link controller="pendingChange" action="reject" id="${entry.changeId}" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.reject")}"
                                            data-confirm-term-how="ok"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.SYM.NO}"></i>
                                    </g:link>
                                </div>
                            </div>
                        </g:if>
                        <g:elseif test="${event == PendingChangeConfiguration.SUBSCRIPTION_RENEWED}">
                            <div class="ui compact grid">
                                <div class="sixteen wide column right aligned">
                                    <g:link controller="pendingChange" action="accept" id="${entry.changeId}" class="${Btn.MODERN.POSITIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.accept")}"
                                        data-confirm-term-how="ok"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.check.universal')}">
                                        <i class="${Icon.SYM.YES}"></i>
                                    </g:link>
                                    <g:link controller="pendingChange" action="reject" id="${entry.changeId}" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.reject")}"
                                            data-confirm-term-how="ok"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.SYM.NO}"></i>
                                    </g:link>
                                </div>
                            </div>
                        </g:elseif>
                        <g:elseif test="${event in PendingChangeConfiguration.COST_ITEM_CHANGES}">
                            <div class="ui compact grid">
                                <div class="sixteen wide column right aligned">
                                    <g:link controller="finance" action="acknowledgeChange" id="${entry.changeId}" class="${Btn.MODERN.SIMPLE_CONFIRM} primary"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.acknowledge")}"
                                            data-confirm-term-how="ok"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.check.universal')}">
                                        ${message(code: 'pendingChange.acknowledge')}
                                    </g:link>
                                </div>
                            </div>
                        </g:elseif>
                    </g:if>
                </div><!-- .column -->
                <div class="two wide column center aligned">
                    <g:formatDate date="${entry._ts}" format="${message(code:'default.date.format.notime')}"/>
                </div><!-- .column -->
            </div><!-- .row -->
        </g:each>
    </div><!-- .grid -->
</div>
<div id="acceptedChangesWrapper">
    <g:render template="/myInstitution/dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_CHANGES, user: contextService.getUser()]}" />

    <div class="ui internally celled compact grid">
        <div class="row">
            <div class="six wide column center aligned">
                <strong><g:message code="profile.dashboard.changes.object"/></strong>
            </div>
            <div class="eight wide column center aligned">
                <strong><g:message code="profile.dashboard.changes.event"/></strong>
            </div>
            <div class="two wide column center aligned">
                <strong><g:message code="profile.dashboard.changes.actionDate"/></strong>
            </div>
        </div>
        <g:each in="${notifications}" var="entry">
            <g:set var="event" value="${entry.event}"/>
            <div class="row">
                <%--${notification}--%>
                <div class="six wide column">
                    <g:if test="${event in PendingChangeConfiguration.TITLE_CHANGES}">
                        <g:link controller="subscription" action="index" id="${entry.subscription.id}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:if>
                    <g:elseif test="${event in PendingChangeConfiguration.COST_ITEM_CHANGES}">
                        <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.subscription.id]}">${entry.subscription.name} (${entry.subscription.status.getI10n("value")})</g:link>
                    </g:elseif>
                    <g:elseif test="${event in [PendingChangeConfiguration.NEW_SUBSCRIPTION, PendingChangeConfiguration.SUBSCRIPTION_RENEWED]}">
                        <g:link controller="subscription" action="show" id="${entry.subscription.id}">${entry.subscription.name}</g:link>
                    </g:elseif>
                </div><!-- .column -->
                <div class="eight wide column">
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
                </div><!-- .column -->
                <div class="two wide column center aligned">
                    <g:formatDate date="${entry._actionDate}" format="${message(code:'default.date.format.noZ')}"/>
                </div><!-- .column -->
            </div><!-- .row -->
        </g:each>
    </div><!-- .grid -->

    <laser:script file="${this.getGroovyPageFileName()}">
        $("#pendingCount").text("${pendingCount}")
        $("#notificationsCount").text("${notificationsCount}")
    </laser:script>
</div>