<%@ page import="de.laser.CustomerTypeService; de.laser.PendingChangeConfiguration; de.laser.IssueEntitlement; de.laser.SubscriptionController; de.laser.storage.RDStore; de.laser.Person; de.laser.Subscription; de.laser.FormService; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.I10nTranslation" %>
<laser:serviceInjection/>


    <g:if test="${!fromSurvey && !isRenewSub && !copyObject}">
        <laser:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject          : sourceObject,
                targetObject          : targetObject,
                allObjects_readRights : allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form controller="${controllerName}" action="${actionName}" id="${params.id}" data-confirm-id="copyElements_form"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">
        <table class="ui celled table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="six wide">
                    <div class="la-copyElements-th-flex-container">
                        <div class="la-copyElements-th-flex-item">
                            <g:if test="${sourceObject}">
                                <g:link controller="${sourceObject.getClass().getSimpleName().toLowerCase()}" action="index"
                                    id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link>
                            </g:if>
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
                                    <g:if test="${targetObject}">
                                        <g:link controller="${targetObject.getClass().getSimpleName().toLowerCase()}" action="index"
                                            id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link>
                                    </g:if>
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
            <tr data-element="subscription.takePackages">
                <g:set var="excludes" value="${[PendingChangeConfiguration.PACKAGE_PROP, PendingChangeConfiguration.PACKAGE_DELETED]}"/>
                <td data-element="source">
                    <strong>${message(code: 'subscription.packages.label')}: ${sourceObject.packages?.size()}</strong>
                    <g:each in="${sourceObject.packages?.sort { it.pkg.name.toLowerCase() }}" var="sp">
                        <div class="la-copyPack-container la-element">
                            <div data-oid="${genericOIDService.getOID(sp)}" class="la-copyPack-item">
                                <label>
                                    <i class="gift icon"></i>
                                    <g:link controller="package" action="show" target="_blank" id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                                    <ui:debugInfo>PkgId: ${sp.pkg.id}</ui:debugInfo>
                                    <g:if test="${sp.pkg.contentProvider}">(${sp.pkg.contentProvider.name})</g:if>
                                </label>
                                <div>
                                    <g:link controller="subscription" action="index" id="${sourceObject.id}"><strong>${message(code: 'issueEntitlement.countSubscription')}</strong> ${sp.getIssueEntitlementCountOfPackage()}</g:link>
                                </div>
                            </div>
                            %{--COPY:--}%

                            <div data-oid="${genericOIDService.getOID(sp)}">
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
                <td data-element="target">
                    <strong>${message(code: 'subscription.packages.label')}: ${targetObject?.packages?.size()}</strong>

                    <g:each in="${targetObject?.packages?.sort { it.pkg.name.toLowerCase() }}" var="sp">
                        <div class="la-copyPack-container la-element">
                            <div data-oid="${genericOIDService.getOID(sp)}" class="la-copyPack-item">
                                <label>
                                    <i class="gift icon"></i>
                                    <g:link controller="packageDetails" action="show" target="_blank" id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                                    <ui:debugInfo>PkgId: ${sp.pkg.id}</ui:debugInfo>
                                    <g:if test="${sp.pkg.contentProvider}">(${sp.pkg.contentProvider.name})</g:if>
                                </label>
                                <div>
                                    <g:link controller="subscription" action="index" id="${targetObject?.id}"><strong>${message(code: 'issueEntitlement.countSubscription')}</strong> ${sp.getIssueEntitlementCountOfPackage()}</g:link>
                                </div>
                            </div>

                            %{--DELETE--}%
                            <div data-oid="${genericOIDService.getOID(sp)}">
                                <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                    <g:checkBox name="subscription.deletePackageIds"
                                                value="${genericOIDService.getOID(sp)}"
                                                data-oid="${genericOIDService.getOID(sp)}" data-action="delete"
                                                checked="${false}"/>
                                </div>
                            </div>
                        </div>
                    </g:each>
                </td>
            </g:if>
            </tr>
            <tr data-element="subscription.takeTitleGroups">
                <td data-element="source">
                    <strong>${message(code: 'subscription.details.ieGroups')}: ${sourceObject.ieGroups?.size()}</strong>
                    <g:if test="${sourceObject.ieGroups}">
                        <g:each in="${sourceObject.ieGroups.sort { it.name }}" var="titleGroup">
                            <div class="la-copyPack-container la-element">
                                <div data-oid="${genericOIDService.getOID(titleGroup)}" class="la-copyPack-item">
                                    <g:link action="index" controller="subscription" id="${sourceObject.id}" params="[titleGroup: titleGroup.id]">
                                        <i class="grey icon object group la-popup-tooltip la-delay"
                                           data-content="${message(code: 'issueEntitlementGroup.label')}"></i> ${titleGroup.name}
                                    </g:link>
                                    <div class="ui accordion">
                                        <div class="title"><i class="dropdown icon"></i>
                                            ${message(code: 'issueEntitlementGroup.items.label')}: ${titleGroup.countCurrentTitles()}
                                        </div>

                                        <div class="content">
                                            <div class="ui list">
                                                <g:each in="${titleGroup.items?.sort { it.ie.tipp.sortname }}" var="item">
                                                    <div class="item">
                                                        <ui:listIcon hideTooltip="true" type="${item.ie.tipp.titleType}"/>
                                                        <strong>
                                                            <g:link controller="tipp" action="show" id="${item.ie.tipp.id}">${item.ie.tipp.name}</g:link>
                                                        </strong>
                                                    </div>
                                                </g:each>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div data-oid="${genericOIDService.getOID(titleGroup)}">
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
                <td data-element="target">
                    <strong>${message(code: 'subscription.details.ieGroups')}: ${targetObject?.ieGroups?.size()}</strong>

                    <g:if test="${targetObject?.ieGroups}">
                        <g:each in="${targetObject.ieGroups.sort { it.name }}" var="titleGroup">
                            <div class="la-copyPack-container la-element">
                                <div data-oid="${genericOIDService.getOID(titleGroup)}" class="la-copyPack-item">
                                    <g:link action="index" controller="subscription" id="${targetObject.id}" params="[titleGroup: titleGroup.id]">
                                        <i class="grey icon object group la-popup-tooltip la-delay"
                                           data-content="${message(code: 'issueEntitlementGroup.label')}"></i> ${titleGroup.name}
                                    </g:link>
                                    <div class="ui accordion">
                                        <div class="title"><i class="dropdown icon"></i>
                                            ${message(code: 'issueEntitlementGroup.items.label')}: ${titleGroup.countCurrentTitles()}
                                        </div>
                                        <div class="content">
                                            <div class="ui list">
                                                <g:each in="${titleGroup.items?.sort { it.ie.tipp.titleType }}" var="item">
                                                    <div class="item">
                                                        <ui:listIcon hideTooltip="true" type="${item.ie.tipp.titleType}"/>
                                                        <strong><g:link controller="tipp" action="show" id="${item.ie.tipp.id}">${item.ie.tipp.name}</g:link></strong>
                                                    </div>
                                                </g:each>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div data-oid="${genericOIDService.getOID(titleGroup)}">
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
                    <input type="submit" name="copyElementsSubmit" id="copyElementsSubmit" class="ui button js-click-control" value="${submitButtonText}"
                           data-confirm-id="copyElements"
                           data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                           data-confirm-term-how="delete" ${submitDisabled}/>
                </div>
            </div>
        </g:if>
        <g:elseif test="${copyObject}">
            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" name="copyElementsSubmit" id="copyElementsSubmit" class="ui button js-click-control"
                       value="${message(code: 'default.button.copy.label')}"/>
            </div>
        </g:elseif>
        <g:else>
            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitDisabled" value="${(sourceObject && targetObject) || processRunning ? '' : 'disabled'}"/>
                <input type="submit" name="copyElementsSubmit" id="copyElementsSubmit" class="ui button js-click-control"
                       data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete"
                       value="${submitButtonText}" ${submitDisabled}/>
            </div>
        </g:else>
    </g:form>

<g:if test="${!copyObject}">
    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.subCopyController = {

            checkboxes: {
                $takePackageIds:            $('input[name="subscription.takePackageIds"]'),
                $deletePackageIds:          $('input[name="subscription.deletePackageIds"]'),
                $takeTitleGroups:           $('input[name="subscription.takeTitleGroups"]'),
                $deleteTitleGroups:         $('input[name="subscription.deleteTitleGroups"]')
            },

            init: function (elem) {
                let scc = JSPC.app.subCopyController

                scc.checkboxes.$takePackageIds.change(function (event) { scc.takePackageIds(this); } ).trigger('change')
                scc.checkboxes.$deletePackageIds.change(function (event) { scc.deletePackageIds(this); } ).trigger('change')
                scc.checkboxes.$takeTitleGroups.change(function (event) { scc.takeTitleGroups(this); } ).trigger('change')
                scc.checkboxes.$deleteTitleGroups.change(function (event) { scc.deleteTitleGroups(this); } ).trigger('change')
            },

            takePackageIds: function (elem) {
                JSPC.app.subCopyController._handleTake(elem, 'takePackages', 'takePackageIds')
            },
            deletePackageIds: function (elem) {
                JSPC.app.subCopyController._handleDeleted(elem, 'takePackages')
            },

            takeTitleGroups: function (elem) {
                JSPC.app.subCopyController._handleTake(elem, 'takeTitleGroups', 'takeTitleGroups')
            },
            deleteTitleGroups: function (elem) {
                JSPC.app.subCopyController._handleDeleted(elem, 'takeTitleGroups')
            },

            _handleTake: function(elem, identifier, counterId) {
                if (elem.checked) {
                    $('.table tr[data-element="subscription.' + identifier + '"] td[data-element="source"] div.la-copyPack-item[data-oid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr[data-element="subscription.' + identifier + '"] td[data-element="target"] div.la-copyPack-item').addClass('willStay');
                } else {
                    $('.table tr[data-element="subscription.' + identifier + '"] td[data-element="source"] div.la-copyPack-item[data-oid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('subscription.' + counterId) < 1) {
                        $('.table tr[data-element="subscription.' + identifier + '"] td[data-element="target"] div.la-copyPack-item').removeClass('willStay');
                    }
                }
            },
            _handleDeleted: function(elem, identifier) {
                if (elem.checked) {
                    $('.table tr[data-element="subscription.' + identifier + '"] td[data-element="target"] div.la-copyPack-item[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr[data-element="subscription.' + identifier + '"] td[data-element="target"] div.la-copyPack-item[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
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




