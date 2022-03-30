<%@ page import="de.laser.PendingChangeConfiguration; de.laser.IssueEntitlement; de.laser.SubscriptionController; de.laser.helper.RDStore; de.laser.Person; de.laser.Subscription; de.laser.FormService;" %>
<laser:serviceInjection/>

<g:set var="formService" bean="formService"/>

<semui:form>

    <g:if test="${!fromSurvey && !isRenewSub && !copyObject}">
        <g:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject          : sourceObject,
                targetObject          : targetObject,
                allObjects_readRights : allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form controller="${controllerName}" action="${actionName}" id="${params.id}" data-confirm-id="copyElements_form"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">
        <table class="ui celled table table-tworow la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="six wide">
                    <div class="la-copyElements-th-flex-container">
                        <div class="la-copyElements-th-flex-item">
                            <g:if test="${sourceObject}"><g:link
                                    controller="${sourceObject.getClass().getSimpleName().toLowerCase()}" action="index"
                                    id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if>
                        </div>

                        <div>
                            <input type="checkbox" data-action="copy" onClick="JSPC.app.toggleAllCheckboxes(this)" checked/>
                        </div>
                    </div>
                </th>
                <g:if test="${!copyObject}">
                        <th class="six wide">
                            <div class="la-copyElements-th-flex-container">
                                <div class="la-copyElements-th-flex-item">
                                    <g:if test="${targetObject}"><g:link
                                            controller="${targetObject.getClass().getSimpleName().toLowerCase()}" action="index"
                                            id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link></g:if>
                                </div>

                                <div>
                                    <input class="setDeletionConfirm" type="checkbox" data-action="delete" onClick="JSPC.app.toggleAllCheckboxes(this)"/>
                                </div>
                            </div>
                        </th>
                </g:if>
            </tr>
            </thead>
            <tbody class="top aligned">
            <tr>
                <g:set var="excludes"
                       value="${[PendingChangeConfiguration.PACKAGE_PROP,
                                 PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                <td name="subscription.takePackages.source">
                    <strong>${message(code: 'subscription.packages.label')}: ${sourceObject.packages?.size()}</strong>
                    <g:each in="${sourceObject.packages?.sort { it.pkg.name.toLowerCase() }}" var="sp">
                        <div class="la-copyPack-container la-element">
                            <div data-pkgoid="${genericOIDService.getOID(sp)}" class="la-copyPack-item">
                                <label>
                                    <i class="gift icon"></i>
                                    <g:link controller="package" action="show" target="_blank"
                                            id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                                    <semui:debugInfo>PkgId: ${sp.pkg.id}</semui:debugInfo>
                                    <g:if test="${sp.pkg.contentProvider}">(${sp.pkg.contentProvider.name})</g:if>
                                </label>

                                <div>
                                    <g:link controller="subscription" action="index" id="${sourceObject.id}"><strong>${message(code: 'issueEntitlement.countSubscription')}</strong> ${sp.getIssueEntitlementCountOfPackage()}</g:link>
                                </div>
                                <%--
                                <g:set var="ies" value="${sp.getIssueEntitlementsofPackage()}"/>
                                <div class="ui accordion">
                                    <div class="title">
                                        <i class="dropdown icon"></i> ${message(code: 'issueEntitlement.countSubscription')} </strong>${ies.size()}
                                    </div>

                                    <div class="content">
                                        <div class="ui list">
                                            <g:if test="${ies}">
                                                <g:each in="${ies}" var="ie">
                                                    <g:if test="${ie.tipp.status == RDStore.TIPP_STATUS_DELETED}">
                                                        <div class="item willBeReplaced willBeReplacedStrong">
                                                            <semui:listIcon hideTooltip="true" type="${ie.tipp.titleType}"/>
                                                            <strong><g:link controller="tipp" action="show" id="${ie.tipp.id}">${ie.name}</g:link></strong>
                                                        </div>
                                                        <i><g:message code="issueEntitlement.missingSource"/></i>
                                                    </g:if>
                                                    <g:else>
                                                        <div class="item">
                                                            <semui:listIcon hideTooltip="true" type="${ie.tipp.titleType}"/>
                                                            <strong><g:link controller="tipp" action="show" id="${ie.tipp.id}">${ie.name}</g:link></strong>
                                                        </div>
                                                    </g:else>
                                                    <semui:debugInfo>Tipp PkgId: ${ie.tipp.pkg.id}, Tipp ID: ${ie.tipp.id}</semui:debugInfo>
                                                </g:each>
                                            </g:if>
                                        </div>
                                    </div>
                                </div>
                                --%>

                                <g:set var="packageSettings"
                                       value="${PendingChangeConfiguration.findAllBySubscriptionPackage(sp)}"/>

                                <g:if test="${packageSettings}">
                                    <div class="la-copyPack-container la-element">
                                        <div class="ui accordion">
                                            <div class="title"><i
                                                    class="dropdown icon"></i> ${message(code: 'subscription.packages.config.header')} </strong>
                                            </div>

                                            <div class="content">
                                                <ul>
                                                    <g:each in="${packageSettings}"
                                                            var="pcc">
                                                        <g:if test="${!(pcc.settingKey in [PendingChangeConfiguration.NEW_PRICE,
                                                                                           PendingChangeConfiguration.PRICE_UPDATED,
                                                                                           PendingChangeConfiguration.PRICE_DELETED])}">
                                                            <li class="la-copyPack-item">
                                                                <g:message
                                                                        code="subscription.packages.${pcc.settingKey}"/>: ${pcc.settingValue ? pcc.settingValue.getI10n('value') : RDStore.PENDING_CHANGE_CONFIG_PROMPT.getI10n('value')} (<g:message
                                                                    code="subscription.packages.notification.label"/>: ${pcc.withNotification ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')})
                                                                <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')}">
                                                                    <g:if test="${!(pcc.settingKey in excludes)}">
                                                                        <g:if test="${auditService.getAuditConfig(sourceObject, pcc.settingKey)}">
                                                                            <span data-tooltip="${message(code: 'subscription.packages.auditable')}"><i
                                                                                    class="ui thumbtack icon"></i></span>
                                                                        </g:if>
                                                                    </g:if>
                                                                </g:if>
                                                            </li>
                                                        </g:if>
                                                    </g:each>
                                                </ul>
                                            </div>
                                        </div>

                                        <div class="ui checkbox la-toggle-radio la-replace">
                                            <g:checkBox name="subscription.takePackageSettings"
                                                        value="${genericOIDService.getOID(sp)}" data-pkgid="${sp.id}"
                                                        data-action="copy" checked="${true}"/>
                                        </div>
                                    </div>
                                </g:if>
                            </div>
                            %{--COPY:--}%

                            <div data-pkgoid="${genericOIDService.getOID(sp)}">
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="subscription.takePackageIds"
                                                value="${genericOIDService.getOID(sp)}" data-pkgid="${sp.id}"
                                                data-action="copy" checked="${true}"/>
                                </div>
                                <br />
                            </div>
                        </div>
                    </g:each>
                </td>


            <g:if test="${!copyObject}">
                <td name="subscription.takePackages.target">
                    <strong>${message(code: 'subscription.packages.label')}: ${targetObject?.packages?.size()}</strong>

                    <g:each in="${targetObject?.packages?.sort { it.pkg.name.toLowerCase() }}" var="sp">
                        <div class="la-copyPack-container la-element">
                            <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}" class="la-copyPack-item">
                                <label>
                                    <i class="gift icon"></i>
                                    <g:link controller="packageDetails" action="show" target="_blank"
                                            id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                                    <semui:debugInfo>PkgId: ${sp.pkg.id}</semui:debugInfo>
                                    <g:if test="${sp.pkg.contentProvider}">(${sp.pkg.contentProvider.name})</g:if>
                                </label>

                                <div>
                                    <g:link controller="subscription" action="index" id="${targetObject?.id}"><strong>${message(code: 'issueEntitlement.countSubscription')}</strong> ${sp.getIssueEntitlementCountOfPackage()}</g:link>
                                </div>
                                <%--
                                <g:set var="ies" value="${sp.getIssueEntitlementsofPackage()}"/>

                                <div class="ui accordion">
                                    <div class="title"><i
                                            class="dropdown icon"></i> ${message(code: 'issueEntitlement.countSubscription')} </strong>${ies.size()}
                                    </div>

                                    <div class="content">
                                        <div class="ui list">
                                            <g:if test="${ies}">
                                                <g:each in="${ies}" var="ie">
                                                    <div class="item">
                                                        <semui:listIcon hideTooltip="true"
                                                                        type="${ie.tipp.titleType}"/>
                                                        <strong><g:link controller="tipp" action="show"
                                                                        id="${ie.tipp.id}">${ie.name}</g:link></strong>
                                                        <semui:debugInfo>Tipp PkgId: ${ie.tipp.pkg.id}, Tipp ID: ${ie.tipp.id}</semui:debugInfo>
                                                    </div>
                                                </g:each>
                                            </g:if>
                                        </div>
                                    </div>
                                </div>
                                --%>
                                <g:set var="packageSettings"
                                       value="${PendingChangeConfiguration.findAllBySubscriptionPackage(sp)}"/>

                                <g:if test="${packageSettings}">
                                    <div class="la-copyPack-container la-element">
                                        <div class="ui accordion">
                                            <div class="title"><i
                                                    class="dropdown icon"></i> ${message(code: 'subscription.packages.config.header')} </strong>
                                            </div>

                                            <div class="content">
                                                <ul>
                                                    <g:each in="${packageSettings}"
                                                            var="pcc">
                                                        <g:if test="${!(pcc.settingKey in [PendingChangeConfiguration.NEW_PRICE,
                                                                                           PendingChangeConfiguration.PRICE_UPDATED,
                                                                                           PendingChangeConfiguration.PRICE_DELETED])}">
                                                            <li class="la-copyPack-item">
                                                                <g:message
                                                                        code="subscription.packages.${pcc.settingKey}"/>: ${pcc.settingValue ? pcc.settingValue.getI10n('value') : RDStore.PENDING_CHANGE_CONFIG_PROMPT.getI10n('value')} (<g:message
                                                                    code="subscription.packages.notification.label"/>: ${pcc.withNotification ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')})
                                                                <g:if test="${accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR')}">
                                                                    <g:if test="${!(pcc.settingKey in excludes)}">
                                                                        <g:if test="${auditService.getAuditConfig(targetObject, pcc.settingKey)}">
                                                                            <span data-tooltip="${message(code: 'subscription.packages.auditable')}"><i
                                                                                    class="ui thumbtack icon"></i></span>
                                                                        </g:if>
                                                                    </g:if>
                                                                </g:if>
                                                            </li>
                                                        </g:if>
                                                    </g:each>
                                                </ul>
                                            </div>
                                        </div>
                                        <g:if test="${sp.pendingChangeConfig}">
                                            <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                                <g:checkBox name="subscription.deletePackageSettings"
                                                            value="${genericOIDService.getOID(sp)}"
                                                            data-pkgoid="${genericOIDService.getOID(sp.pkg)}"
                                                            data-action="delete" checked="${false}"/>
                                            </div>
                                        </g:if>
                                    </div>
                                </g:if>

                            </div>

                            %{--DELETE--}%
                            <div data-pkgoid="${genericOIDService.getOID(sp.pkg)}">
                                <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                    <g:checkBox name="subscription.deletePackageIds"
                                                value="${genericOIDService.getOID(sp)}"
                                                data-pkgoid="${genericOIDService.getOID(sp.pkg)}" data-action="delete"
                                                checked="${false}"/>
                                </div>
                            </div>
                        </div>
                    </g:each>
                </td>
            </g:if>
            </tr>
            <tr>
                <td name="subscription.takeTitleGroups.source">
                    <strong>${message(code: 'subscription.details.ieGroups')}: ${sourceObject.ieGroups?.size()}</strong>
                    <g:if test="${sourceObject.ieGroups}">
                        <g:each in="${sourceObject.ieGroups.sort { it.name }}" var="titleGroup">
                            <div class="la-copyPack-container la-element">
                                <div data-oid="${genericOIDService.getOID(titleGroup)}" class="la-copyPack-item">
                                    <g:link action="index" controller="subscription" id="${sourceObject.id}"
                                            params="[titleGroup: titleGroup.id]">
                                        <i class="grey icon object group la-popup-tooltip la-delay"
                                           data-content="${message(code: 'issueEntitlementGroup.label')}"></i> ${titleGroup.name}
                                    </g:link>
                                    <div class="ui accordion">
                                        <div class="title"><i class="dropdown icon"></i>
                                            ${message(code: 'issueEntitlementGroup.items.label')}: ${titleGroup.items?.size()}
                                        </div>

                                        <div class="content">
                                            <div class="ui list">
                                                <g:each in="${titleGroup.items?.sort { it.ie.sortname }}"
                                                        var="item">
                                                    <div class="item">
                                                        <semui:listIcon hideTooltip="true" type="${item.ie.tipp.titleType}"/>
                                                        <strong>
                                                            <g:link controller="tipp" action="show" id="${item.ie.tipp.id}">${item.ie.name}</g:link>
                                                        </strong>
                                                    </div>
                                                </g:each>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div data-titleGroupoid="${genericOIDService.getOID(titleGroup)}">
                                    <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="subscription.takeTitleGroups"
                                                    value="${genericOIDService.getOID(titleGroup)}"
                                                    data-action="copy"
                                                    checked="${true}"/>
                                    </div>
                                </div>
                            </div>
                        </g:each>
                    </g:if>
                </td>
                <g:if test="${!copyObject}">
                <td name="subscription.takeTitleGroups.target">
                    <strong>${message(code: 'subscription.details.ieGroups')}: ${targetObject?.ieGroups?.size()}</strong>

                    <g:if test="${targetObject?.ieGroups}">
                        <g:each in="${targetObject.ieGroups.sort { it.name }}" var="titleGroup">
                            <div class="la-copyPack-container la-element">
                                <div data-oid="${genericOIDService.getOID(titleGroup)}" class="la-copyPack-item">
                                    <g:link action="index" controller="subscription" id="${targetObject.id}"
                                            params="[titleGroup: titleGroup.id]">
                                        <i class="grey icon object group la-popup-tooltip la-delay"
                                           data-content="${message(code: 'issueEntitlementGroup.label')}"></i> ${titleGroup.name}
                                    </g:link>
                                    <div class="ui accordion">
                                        <div class="title"><i
                                                class="dropdown icon"></i>
                                            ${message(code: 'issueEntitlementGroup.items.label')}: ${titleGroup.items?.size()}
                                        </div>

                                        <div class="content">
                                            <div class="ui list">
                                                <g:each in="${titleGroup.items?.sort { it.ie.tipp.titleType }}"
                                                        var="item">
                                                    <div class="item">
                                                        <semui:listIcon hideTooltip="true"
                                                                        type="${item.ie.tipp.titleType}"/>
                                                        <strong><g:link controller="tipp" action="show"
                                                                        id="${item.ie.tipp.id}">${item.ie.name}</g:link></strong>
                                                    </div>
                                                </g:each>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div data-titleGroupoid="${genericOIDService.getOID(titleGroup)}">
                                    <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                        <g:checkBox name="subscription.deleteTitleGroups"
                                                    value="${genericOIDService.getOID(titleGroup)}"
                                                    data-action="delete"
                                                    checked="${false}"/>
                                    </div>
                                </div>
                            </div>
                        </g:each>
                    </g:if>  
                </div>
                </td>
            </g:if>
            </tr>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub ?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'copyElementsIntoObject.copyPackagesAndIEs.button')}"/>

        <g:if test="${fromSurvey && surveyConfig}">
            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <g:link controller="survey" action="renewalEvaluation" id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]" class="ui button js-click-control">
                        <g:message code="renewalEvaluation.back"/>
                    </g:link>
                </div>

                <div class="eight wide field" style="text-align: right;">
                    <g:set var="submitDisabled" value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>
                    <input type="submit" id="copyElementsSubmit" class="ui button js-click-control" value="${submitButtonText}"
                           data-confirm-id="copyElements"
                           data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                           data-confirm-term-how="delete" ${submitDisabled}/>
                </div>
            </div>
        </g:if>
        <g:elseif test="${copyObject}">
            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" class="ui button js-click-control"
                       value="${message(code: 'default.button.copy.label')}"/>
            </div>
        </g:elseif>
        <g:else>
            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitDisabled" value="${(sourceObject && targetObject) || processRunning ? '' : 'disabled'}"/>
                <input type="submit" id="copyElementsSubmit" class="ui button js-click-control"
                       data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete"
                       value="${submitButtonText}" ${submitDisabled}/>
            </div>
        </g:else>
    </g:form>
</semui:form>
<g:if test="${!copyObject}">
    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.subCopyController = {

            checkboxes: {
                $takePackageIds: $('input[name="subscription.takePackageIds"]'),
                $takePackageSettings: $('input[name="subscription.takePackageSettings"]'),
                $deletePackageIds: $('input[name="subscription.deletePackageIds"]'),
                $deletePackageSettings: $('input[name="subscription.deletePackageSettings"]'),
                $takeEntitlementIds: $('input[name="subscription.takeEntitlementIds"]'),
                $deleteEntitlementIds: $('input[name="subscription.deleteEntitlementIds"]'),
                $takeTitleGroups: $('input[name="subscription.takeTitleGroups"]'),
                $deleteTitleGroups: $('input[name="subscription.deleteTitleGroups"]')
            },

            init: function (elem) {
                var ref = JSPC.app.subCopyController.checkboxes

                ref.$takePackageIds.change(function (event) {
                    JSPC.app.subCopyController.takePackageIds(this);
                }).trigger('change')

                ref.$takePackageSettings.change(function (event) {
                    JSPC.app.subCopyController.takePackageSettings(this);
                }).trigger('change')

                ref.$deletePackageIds.change(function (event) {
                    JSPC.app.subCopyController.deletePackageIds(this);
                }).trigger('change')

                ref.$deletePackageSettings.change(function (event) {
                    JSPC.app.subCopyController.deletePackageSettings(this);
                }).trigger('change')

                ref.$takeEntitlementIds.change(function (event) {
                    JSPC.app.subCopyController.takeEntitlementIds(this);
                }).trigger('change')

                ref.$deleteEntitlementIds.change(function (event) {
                    JSPC.app.subCopyController.deleteEntitlementIds(this);
                }).trigger('change')

                ref.$takeTitleGroups.change(function (event) {
                    JSPC.app.subCopyController.takeTitleGroups(this);
                }).trigger('change')

                ref.$deleteTitleGroups.change(function (event) {
                    JSPC.app.subCopyController.deleteTitleGroups(this);
                }).trigger('change')
            },

            takePackageIds: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="subscription.takePackages.source"] div[data-pkgid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="subscription.takePackages.target"] div').addClass('willStay');
                } else {
                    $('.table tr td[name="subscription.takePackages.source"] div[data-pkgid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('subscription.takePackageIds') < 1) {
                        $('.table tr td[name="subscription.takePackages.target"] div').removeClass('willStay');
                    }
                }
            },

            deletePackageIds: function (elem) {
                var pkgOid = $(elem).attr('data-pkgid'); // FEHLER dk !?
                //var pkgOid = $(elem).attr('data-pkgoid'); // dk
                $('[name="subscription.deletePackageSettings"]').filter('[data-pkgoid="' + pkgOid + '"]').change();
                if (elem.checked) {
                    $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"]').addClass('willBeReplacedStrong');
                    $('.table tr td[name="subscription.takeEntitlements.target"] div[data-pkgoid="' + pkgOid + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"]').removeClass('willBeReplacedStrong');
                    $('.table tr td[name="subscription.takeEntitlements.target"] div[data-pkgoid="' + pkgOid + '"]').removeClass('willBeReplacedStrong');
                }
            },

            takePackageSettings: function (elem) {
                var pkgOid = $(elem).attr('data-pkgid'); // FEHLER dk !?
                //var pkgOid = $(elem).attr('data-pkgoid'); // dk
                if (elem.checked) {
                    $('.table tr td[name="subscription.takePackages.source"] div[data-pkgoid="' + elem.value + '"] div.la-copyPack-container').addClass('willStay');
                    $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + elem.value + '"] div.la-copyPack-container').addClass('willStay');
                } else {
                    $('.table tr td[name="subscription.takePackages.source"] div[data-pkgoid="' + elem.value + '"] div.la-copyPack-container').removeClass('willStay');
                    $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + elem.value + '"] div.la-copyPack-container').removeClass('willStay');
                }
            },

            deletePackageSettings: function (elem) {
                var pkgOid = $(elem).attr('data-pkgid'); // FEHLER dk !?
                //var pkgOid = $(elem).attr('data-pkgoid'); // dk
                if (elem.checked) {
                    $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"] div.la-copyPack-container').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="subscription.takePackages.target"] div[data-pkgoid="' + pkgOid + '"] div.la-copyPack-container').removeClass('willBeReplacedStrong');
                }
            },

            takeEntitlementIds: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieoid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="subscription.takeEntitlements.target"] div').addClass('willStay');
                } else {
                    $('.table tr td[name="subscription.takeEntitlements.source"] div[data-ieoid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('subscription.takeEntitlementIds') < 1) {
                        $('.table tr td[name="subscription.takeEntitlements.target"] div').removeClass('willStay');
                    }
                }
            },

            deleteEntitlementIds: function (elem) {
                var ieoid = elem.value // FEHLER dk !?
                //var ieoid = $(elem).attr('data-ieoid'); // dk
                if (elem.checked) {
                    $('.table tr td[name="subscription.takeEntitlements.target"] div[data-ieoid="' + ieoid + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="subscription.takeEntitlements.target"] div[data-ieoid="' + ieoid + '"]').removeClass('willBeReplacedStrong');
                }
            },

            takeTitleGroups: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="subscription.takeTitleGroups.source"] div[data-oid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="subscription.takeTitleGroups.target"] div').addClass('willStay');
                } else {
                    $('.table tr td[name="subscription.takeTitleGroups.source"] div[data-oid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('subscription.takeTitleGroups') < 1) {
                        $('.table tr td[name="subscription.takeTitleGroups.target"] div').removeClass('willStay');
                    }
                }
            },

            deleteTitleGroups: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="subscription.takeTitleGroups.target"] div [data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="subscription.takeTitleGroups.target"] div [data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
                }
            },

            getNumberOfCheckedCheckboxes: function (inputElementName) {
                var checkboxes = document.querySelectorAll('input[name="' + inputElementName + '"]');
                var numberOfChecked = 0;
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked) {
                        numberOfChecked++;
                    }
                }
                return numberOfChecked;
            }
        }

        JSPC.app.subCopyController.init()
    </laser:script>
</g:if>




