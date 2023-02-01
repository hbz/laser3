<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<div id="pendingChangesWrapper">
<%--<div class="la-float-right">
        <g:if test="${packages}">
            <g:form controller="pendingChange" action="processAll">
                <g:select from="${packages}" noSelection="${['':message(code:'default.select.choose.label')]}" name="acceptChangesForPackages" class="ui select search multiple dropdown" optionKey="${{it.id}}" optionValue="${{it.pkg.name}}"/>
                <div class="ui buttons">
                    <g:submitButton class="ui button positive" name="acceptAll" value="${message(code:'pendingChange.takeAll')}"/>
                    <div class="or" data-text="${message(code:'default.or')}"></div>
                    <g:submitButton class="ui button negative" name="rejectAll" value="${message(code:'pendingChange.rejectAll')}"/>
                </div>
             </g:form>
        </g:if>
    </div>--%>
    <div class="ui internally celled grid">
        <div class="row">
            <div class="six wide column">
                <g:message code="profile.dashboard.changes.object"/>
            </div><!-- .column -->
            <div class="ten wide column">
                <g:message code="profile.dashboard.changes.event"/>
            </div><!-- .column -->
        </div>
        <g:each in="${pending}" var="entry">
            <div class="row">
                <%--${notification}--%>
                <div class="six wide column">
                    <g:if test="${entry.packageSubscription}">
                        <g:link controller="subscription" action="index" id="${entry.packageSubscription.id}">${entry.packageSubscription.name}</g:link>
                    </g:if>
                    <g:elseif test="${entry.costItemSubscription}">
                        <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.costItemSubscription.id]}">${entry.costItemSubscription.name}</g:link>
                    </g:elseif>
                    <g:elseif test="${entry.subscription}">
                        <div class="right aligned wide column">
                            <g:link controller="subscription" action="show" id="${entry.subscription.id}">
                                ${entry.subscription.name}
                            </g:link>
                        </div>
                    </g:elseif>
                </div><!-- .column -->
                <div class="ten wide column">
                    <g:if test="${entry.packageSubscription}">
                        <g:link controller="subscription" action="entitlementChanges" id="${entry.packageSubscription.id}" params="[tab: 'changes', eventType: entry.msgToken]">${raw(entry.eventString)}</g:link>
                    </g:if>
                    <g:else>
                        ${raw(entry.eventString)}
                    </g:else>
                    <g:if test="${editable}">
                        <g:if test="${entry.subscription}">
                            <div class="right aligned wide column">
                                <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: entry.subscription.source, targetObjectId: entry.subscription.target]}">
                                    <g:message code="myinst.copyMyElements"/>
                                </g:link>

                                <div class="ui grid">
                                    <div class="right aligned wide column">
                                        <g:link controller="pendingChange" action="accept" id="${entry.changeId}" class="ui icon positive button la-modern-button js-open-confirm-modal"
                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.accept")}"
                                                data-confirm-term-how="ok"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.check.universal')}">
                                                <i class="checkmark icon"></i>
                                            <!--${message(code: 'default.button.accept.label')}-->
                                        </g:link>
                                        <g:link controller="pendingChange" action="reject" id="${entry.changeId}" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.reject")}"
                                                data-confirm-term-how="ok"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="times icon"></i>
                                            <!--${message(code: 'default.button.reject.label')}-->
                                        </g:link>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                        <g:elseif test="${entry.costItem}">
                            <div class="right aligned wide column">
                                <g:link controller="finance" action="acknowledgeChange" id="${entry.changeId}" class="ui icon primary button la-modern-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.changes.acknowledge")}"
                                        data-confirm-term-how="ok"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.check.universal')}">
                                    ${message(code: 'pendingChange.acknowledge')}
                                </g:link>
                            </div>
                        </g:elseif>
                    </g:if>
                </div><!-- .column -->
            </div><!-- .row -->
        </g:each>
    </div><!-- .grid -->
    <div>
        <ui:paginate controller="myInstitution" action="dashboard" offset="${pendingOffset ? pendingOffset : '0'}" max="${max}" params="${[view:'PendingChanges']}" total="${pendingCount}"/>
    </div>
</div>
<div id="acceptedChangesWrapper">
    <div class="la-float-right">
        <%--<g:link action="changes" class="ui button"><g:message code="myinst.changes.submit.label"/></g:link>--%>
    </div>
    <div class="ui internally celled grid">
        <div class="row">
            <div class="six wide column">
                <g:message code="profile.dashboard.changes.object"/>
            </div><!-- .column -->
            <div class="ten wide column">
                <g:message code="profile.dashboard.changes.event"/>
            </div><!-- .column -->
        </div>
        <g:each in="${notifications}" var="notification">
            <div class="row">
                <%--${notification}--%>
                <div class="six wide column">
                    <g:if test="${notification.packageSubscription}">
                        <g:link controller="subscription" action="index" id="${notification.packageSubscription.id}">${notification.packageSubscription.name}</g:link>
                    </g:if>
                    <g:elseif test="${notification.costItemSubscription}">
                        <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:notification.costItemSubscription.id]}">${notification.costItemSubscription.name}</g:link>
                    </g:elseif>
                    <g:elseif test="${notification.subscription}">
                        <div class="right aligned wide column">
                            <g:link controller="subscription" action="show" id="${notification.subscription.id}">
                                ${notification.subscription.name}
                            </g:link>
                        </div>
                    </g:elseif>
                </div><!-- .column -->
                <div class="ten wide column">
                    <g:if test="${notification.packageSubscription}">
                        <g:link controller="subscription" action="entitlementChanges" id="${notification.packageSubscription.id}" params="[tab: 'acceptedChanges', eventType: notification.msgToken]">${raw(notification.eventString)}</g:link>
                    </g:if>
                    <g:else>
                        ${raw(notification.eventString)}
                    </g:else>

                    <g:if test="${notification.subscription}">
                        <div class="right aligned wide column">
                            <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: notification.subscription.source, targetObjectId: notification.subscription.target]}">
                                <g:message code="myinst.copyMyElements"/>
                            </g:link>
                        </div>
                    </g:if>
                </div><!-- .column -->
            </div><!-- .row -->
        </g:each>
    </div><!-- .grid -->
    <div>
        <ui:paginate controller="myInstitution" action="dashboard" offset="${acceptedOffset ? acceptedOffset : '0'}" max="${max}" params="${[view:'AcceptedChanges']}" total="${notificationsCount}"/>
    </div>
    <laser:script file="${this.getGroovyPageFileName()}">
        $("#pendingCount").text("${message(code: 'myinst.pendingChanges.label', args: [pendingCount])}");
        $("#notificationsCount").text("${message(code: 'myinst.acceptedChanges.label', args: [notificationsCount])}");
    </laser:script>
</div>