<%@page import="de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.PendingChangeConfiguration; de.laser.Platform; de.laser.SubscriptionPackage" %>
<laser:serviceInjection />

<div class="ui card">
    <div class="content">
        <h2 class="ui header">${message(code: 'subscription.packages.label')}</h2>
        <table class="ui three column table">
            <g:each in="${subscription.packages}" var="sp">
                <% String buttonColor = ""
                    if(sp.pendingChangeConfig.size() > 0) {
                        buttonColor = "green"
                }%>
                <tr>
                    <td colspan="2">
                        <g:link controller="package" action="show" id="${sp.pkg.id}">${sp.pkg.name}</g:link>

                        <g:if test="${sp.pkg.contentProvider}">
                            (${sp.pkg.contentProvider.name})
                        </g:if>
                    </td>
                </tr>
                <tr>
                    <td style="border-top: none">
                        <div class="ui top aligned divided relaxed list">
                            <div class="item">
                                <div class="content">
                                    <strong>${message(code: 'subscription.details.linkAccessPoint.platform.label')}</strong>
                                </div>
                            </div>
                            <div class="item">

                                <div class="content">
                                    <g:if test="${packageService.getCountOfNonDeletedTitles(sp.pkg) > 0}">
                                        <g:each in="${Platform.executeQuery('select distinct tipp.platform from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg',[pkg:sp.pkg])}" var="platform">
                                            <g:if test="${platform}">
                                                <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                                                <semui:linkIcon href="${platform.primaryUrl?.startsWith('http') ? platform.primaryUrl : 'http://' + platform.primaryUrl}"/>
                                            </g:if>
                                        </g:each>
                                    </g:if>
                                    <g:elseif test="${sp.pkg.nominalPlatform}">
                                        <g:link controller="platform" action="show" id="${sp.pkg.nominalPlatform.id}">${sp.pkg.nominalPlatform.name}</g:link>
                                        <semui:linkIcon href="${sp.pkg.nominalPlatform.primaryUrl?.startsWith('http') ? sp.pkg.nominalPlatform.primaryUrl : 'http://' + sp.pkg.nominalPlatform.primaryUrl}"/>
                                    </g:elseif>
                                </div>
                            </div>
                        </div>
                    </td>
                    <td style="border-top: none" class="right aligned">
                        <g:if test="${editmode}">
                            <button id="pendingChangeConfigurationToggle${sp.id}"
                                    class="ui icon blue button la-modern-button ${buttonColor} la-js-dont-hide-button la-popup-tooltip la-delay"
                                    data-content="${message(code:'subscription.packages.config.header')}">
                                <i class="ui angle double down icon"></i>
                            </button>
                            <laser:script file="${this.getGroovyPageFileName()}">
                                $("#pendingChangeConfigurationToggle${sp.id}").on('click', function() {
                                $("#pendingChangeConfiguration${sp.id}").transition('slide down');
                                if ($("#pendingChangeConfiguration${sp.id}").hasClass('visible')) {
                                    $(this).html('<i class="ui angle double down icon"></i>');
                                } else {
                                    $(this).html('<i class="ui angle double up icon"></i>');
                                }
                            })
                            </laser:script>
                            <g:link controller="subscription"
                                    action="unlinkPackage"
                                    extaContentFlag="false"
                                    params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y']}"
                                    data-confirm-messageUrl="${createLink(controller:'subscription', action:'unlinkPackage', params:[subscription: sp.subscription.id, package: sp.pkg.id])}"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name])}"
                                    data-confirm-term-how="delete"
                                    class="ui icon negative button la-modern-button js-open-confirm-modal la-popup-tooltip la-delay"
                                    role="button"
                                    aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                <i aria-hidden="true" class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
                <g:if test="${editmode}">
                    <tr>
                        <td style="border-top: none" colspan="3">
                            <div id="pendingChangeConfiguration${sp.id}" class="ui hidden">
                                <h5 class="ui header">
                                    <g:message code="subscription.packages.config.label" args="${[sp.pkg.name]}"/>
                                </h5>
                                <g:form controller="subscription" action="setupPendingChangeConfiguration" params="[id:sp.subscription.id,pkg:sp.pkg.id]">
                                    <table class="ui table stackable la-noSticky">
                                        <thead>
                                        <g:if test="${contextCustomerType == 'ORG_CONSORTIUM'}">
                                            <tr>
                                                <th></th>
                                                <th></th>
                                                <th></th>
                                                <th class="control-label la-border-left" colspan="2">
                                                    <g:message code="subscription.details.linkPackage.children.label"/>
                                                </th>
                                            </tr>
                                        </g:if>
                                        <tr>
                                            <th class="control-label"><g:message code="subscription.packages.changeType.label"/></th>
                                            <th class="control-label">
                                                <g:message code="subscription.packages.setting.label"/>
                                            </th>
                                            <th class="control-label">
                                                <span data-tooltip="${message(code:"subscription.packages.notification.label")}">
                                                    <i class="ui large icon bullhorn"></i>
                                                </span>
                                            </th>
                                            <g:if test="${contextCustomerType == 'ORG_CONSORTIUM'}">
                                                <th class="control-label la-border-left" >
                                                    <span data-tooltip="${message(code:'subscription.packages.auditable')}">
                                                        <i class="ui large icon thumbtack"></i>
                                                    </span>
                                                </th>
                                                <th class="control-label">
                                                    <span data-tooltip="${message(code:'subscription.packages.notification.auditable')}">
                                                        <i class="ui large icon bullhorn"></i>
                                                    </span>
                                                </th>
                                            </g:if>
                                        </tr>
                                        </thead>
                                    <g:set var="excludes" value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                                                    PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                                    <g:each in="${PendingChangeConfiguration.SETTING_KEYS}" var="settingKey">
                                        <%
                                            PendingChangeConfiguration pcc = sp.getPendingChangeConfig(settingKey)
                                        %>
                                        <tr>
                                            <th class="control-label">
                                                <g:message code="subscription.packages.${settingKey}"/>
                                            </th>
                                            <td>
                                                <g:if test="${!(settingKey in excludes)}">
                                                    <g:if test="${editmode}">
                                                        <laser:select class="ui dropdown"
                                                                      name="${settingKey}!§!setting" from="${RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)}"
                                                                      optionKey="id" optionValue="value"
                                                                      value="${(pcc && pcc.settingValue) ? pcc.settingValue.id : RDStore.PENDING_CHANGE_CONFIG_PROMPT.id}"
                                                        />
                                                    </g:if>
                                                    <g:else>
                                                        ${(pcc && pcc.settingValue) ? pcc.settingValue.getI10n("value") : RDStore.PENDING_CHANGE_CONFIG_PROMPT.getI10n("value")}
                                                    </g:else>
                                                </g:if>
                                            </td>
                                            <td>
                                                <g:if test="${editmode}">
                                                    <g:checkBox class="ui checkbox" name="${settingKey}!§!notification" checked="${pcc?.withNotification}"/>
                                                </g:if>
                                                <g:else>
                                                    ${(pcc && pcc.withNotification) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                </g:else>
                                            </td>
                                            <g:if test="${contextCustomerType == 'ORG_CONSORTIUM'}">
                                                <td class="la-border-left">
                                                    <g:if test="${!(settingKey in excludes)}">
                                                        <g:if test="${editmode}">
                                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!auditable" checked="${pcc ? auditService.getAuditConfig(subscription, settingKey) : false}"/>
                                                        </g:if>
                                                        <g:else>
                                                            ${pcc && auditService.getAuditConfig(subscription, settingKey) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                        </g:else>
                                                    </g:if>
                                                </td>
                                                <td>
                                                    <g:if test="${!(settingKey in excludes)}">
                                                        <g:if test="${editmode}">
                                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!notificationAudit" checked="${pcc ? auditService.getAuditConfig(subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX) : false}"/>
                                                        </g:if>
                                                        <g:else>
                                                            ${pcc && auditService.getAuditConfig(subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                        </g:else>
                                                    </g:if>
                                                </td>
                                            </g:if>
                                        </tr>
                                    </g:each>
                                    <tr>
                                        <th class="control-label">
                                            <g:message code="subscription.packages.freezeHolding"/> <span data-tooltip="${message(code: 'subscription.packages.freezeHolding.expl')}"><i class="ui question circle icon"></i></span>
                                        </th>
                                        <td>
                                            <g:if test="${editmode}">
                                                <g:checkBox class="ui checkbox" name="freezeHolding" checked="${sp.freezeHolding}"/>
                                            </g:if>
                                            <g:else>
                                                ${sp.freezeHolding ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                            </g:else>
                                        </td>
                                        <td></td>
                                        <g:if test="${contextCustomerType == 'ORG_CONSORTIUM'}">
                                            <td class="la-border-left">
                                                <g:if test="${editmode}">
                                                    <g:checkBox class="ui checkbox" name="freezeHoldingAudit" checked="${auditService.getAuditConfig(subscription, SubscriptionPackage.FREEZE_HOLDING)}"/>
                                                </g:if>
                                                <g:else>
                                                    ${auditService.getAuditConfig(subscription, SubscriptionPackage.FREEZE_HOLDING) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                </g:else>
                                            </td>
                                            <td></td>
                                        </g:if>
                                    </tr>
                                    <g:if test="${editmode}">
                                        <tr>
                                            <td colspan="2" class="control-label"><g:submitButton class="ui button btn-primary" name="${message(code:'subscription.packages.submit.label')}"/></td>
                                            <g:set var="now" value="${new Date()}"/>
                                            <td colspan="2" class="control-label">
                                                <g:if test="${subscription.endDate < now}">
                                                    <%
                                                        boolean disabled = false
                                                        Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
                                                        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
                                                        threadArray.each { Thread thread ->
                                                            if (thread.name == 'PackageTransfer_' + subscription.id) {
                                                                disabled = true
                                                            }
                                                        }
                                                    %>
                                                    <g:if test="${!disabled}">
                                                        <g:link controller="subscription" action="resetHoldingToSubEnd" class="ui button negative la-modern-button js-open-confirm-modal la-popup-tooltip la-delay"
                                                                params="[id: subscription.id, subPkg: sp.id]"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.resetSubToEndDate")}"
                                                                data-confirm-term-how="ok">
                                                            <g:message code="subscription.packages.resetToSubEnd.label"/>
                                                        </g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <g:link class="ui disabled button negative la-modern-button la-popup-tooltip la-delay"
                                                                data-content="${message(code: 'subscription.packages.resetToSubEnd.threadRunning')}">
                                                            <g:message code="subscription.packages.resetToSubEnd.label"/>
                                                        </g:link>
                                                    </g:else>
                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:if>
                                    </table>
                                </g:form>
                            </div><!-- .content -->
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </table>
    </div><!-- .content -->
</div>
