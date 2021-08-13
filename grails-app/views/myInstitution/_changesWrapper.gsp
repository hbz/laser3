<laser:serviceInjection/>
<g:if test="${editable}">
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
                <div class="seven wide column">
                    <g:message code="profile.dashboard.changes.event"/>
                </div><!-- .column -->
                <div class="three wide column">
                    <g:message code="profile.dashboard.changes.action"/>
                </div><!-- .column -->
            </div>
            <g:each in="${pending}" var="entry">
                <div class="row">
                    <%--${entry}--%>
                    <div class="six wide column">
                        <g:if test="${entry.subPkg}">
                            <g:link controller="subscription" action="index" id="${entry.subPkg.subscription.id}">${entry.subPkg.subscription.dropdownNamingConvention()}</g:link>
                        </g:if>
                        <g:elseif test="${entry.costItem}">
                            <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.costItem.sub.id]}">${entry.costItem.sub.dropdownNamingConvention()}</g:link>
                        </g:elseif>
                        <g:elseif test="${entry.subscription}">
                            <div class="right aligned wide column">
                                <g:link controller="subscription" action="show" id="${entry.subscription._getCalculatedPrevious().id}">
                                    ${entry.subscription._getCalculatedPrevious().dropdownNamingConvention(institution)}
                                </g:link>
                            </div>
                        </g:elseif>
                    </div><!-- .column -->
                    <div class="seven wide column">
                        <g:if test="${entry.subPkg}">
                            <g:link controller="subscription" action="entitlementChanges" id="${entry.subPkg.subscription.id}" params="[tab: 'changes', eventType: entry.msgToken]">${raw(entry.eventString)}</g:link>
                        </g:if>
                        <g:else>
                            ${raw(entry.eventString)}
                        </g:else>

                        <g:if test="${entry.subscription}">
                            <div class="right aligned wide column">
                                <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(entry.subscription._getCalculatedPrevious()), targetObjectId: genericOIDService.getOID(entry.subscription)]}">
                                    <g:message code="myinst.copyMyElements"/>
                                </g:link>
                            </div>
                        </g:if>
                    </div><!-- .column -->
                    <div class="three wide column">
                        <g:if test="${entry.changeId}">
                            <div class="ui buttons">
                                <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.changeId}"><g:message code="default.button.accept.label"/></g:link>
                                <div class="or" data-text="${message(code:'default.or')}"></div>
                                <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.changeId}"><g:message code="default.button.reject.label"/></g:link>
                            </div>
                        </g:if>
                    </div><!-- .column -->
                </div><!-- .row -->
            </g:each>
        </div><!-- .grid -->
    </div>
</g:if>
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
        <g:each in="${notifications}" var="entry">
            <div class="row">
                <%--${entry}--%>
                <div class="six wide column">
                    <g:if test="${entry.subPkg}">
                        <g:link controller="subscription" action="index" id="${entry.subPkg.subscription.id}">${entry.subPkg.subscription.dropdownNamingConvention()}</g:link>
                    </g:if>
                    <g:elseif test="${entry.costItem}">
                        <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.costItem.sub.id]}">${entry.costItem.sub.dropdownNamingConvention()}</g:link>
                    </g:elseif>
                </div><!-- .column -->
                <div class="ten wide column">
                    <g:if test="${entry.subPkg}">
                        <g:link controller="subscription" action="entitlementChanges" id="${entry.subPkg.subscription.id}" params="[tab: 'acceptedChanges', eventType: entry.msgToken]">${raw(entry.eventString)}</g:link>
                    </g:if>
                    <g:else>
                        ${raw(entry.eventString)}
                    </g:else>

                    <g:if test="${entry.subscription}">
                        <div class="right aligned wide column">
                            <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(entry.subscription._getCalculatedPrevious()), targetObjectId: genericOIDService.getOID(entry.subscription)]}">
                                <g:message code="myinst.copyMyElements"/>
                            </g:link>
                        </div>
                    </g:if>
                </div><!-- .column -->
            </div><!-- .row -->
        </g:each>
    </div><!-- .grid -->
    <div>
        <semui:paginate offset="${acceptedOffset ? acceptedOffset : '0'}" max="${max}" params="${[view:'AcceptedChanges']}" total="${notifications.size()}"/>
    </div>
    <laser:script file="${this.getGroovyPageFileName()}">
        $("#pendingCount").text("${message(code: 'myinst.pendingChanges.label', args: [pending.size()])}");
        $("#notificationsCount").text("${message(code: 'myinst.acceptedChanges.label', args: [notifications.size()])}");
    </laser:script>
</div>