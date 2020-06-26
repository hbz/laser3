<%@ page import="com.k_int.kbplus.IssueEntitlement; com.k_int.kbplus.SubscriptionController; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription; com.k_int.kbplus.GenericOIDService; de.laser.AuditConfig "%>
<%@ page import="com.k_int.kbplus.SubscriptionController" %>
<laser:serviceInjection />

<semui:form>

    <g:if test="${controllerName != 'survey' && !isRenewSub}">
        <g:render template="selectSourceAndTargetSubscription" model="[
                sourceSubscription: sourceSubscription,
                targetSubscription: targetSubscription,
                allSubscriptions_readRights: allSubscriptions_readRights,
                allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
    </g:if>

    <g:form action="${actionName}" controller="${controllerName}" id="${params.id}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscription?.id, isRenewSub: isRenewSub]"
            method="post" class="ui form newLicence">
        <table class="ui celled table table-tworow la-table">
            <thead>
                <tr>
                    <th class="six wide">
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription.id}">${sourceSubscription.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <g:if test="${isConsortialSubs}">
                                <th class="center aligned">
                                    <g:message code="subscription.details.copyElementsIntoSubscription.share"/>
                                </th>
                    </g:if>
                    <th class="one wide center aligned"><input type="checkbox" name="checkAllCopyCheckboxes" data-action="copy" onClick="toggleAllCheckboxes(this)" checked />
                    <th class="six wide">
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned">
                        <g:if test="${targetSubscription}">
                            <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
                        </g:if>
                    </th>
                </tr>
            </thead>
            <tbody>
            <g:if test="${ ! isRenewSub}">
                <tr>
                    <td name="subscription.takeDates.source">
                        <div>
                            <b><i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}:</b>&nbsp
                            <g:formatDate date="${sourceSubscription.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                            ${sourceSubscription.endDate ? (' - ' + formatDate(date: sourceSubscription.endDate, format: message(code: 'default.date.format.notime'))) : ''}
                        </div>
                    </td>
                    <g:if test="${isConsortialSubs}">
                        <td class="center aligned">
                            <%-- TODO show that startDate and endDate are meani! --%>
                            <div class="ui checkbox la-toggle-radio la-share">
                                <input class="ui checkbox" type="checkbox" name="toggleShareStartDate" ${AuditConfig.getConfig(sourceSubscription,'startDate') ? 'checked': ''} />
                            </div>
                            <div class="ui checkbox la-toggle-radio la-share">
                                <input class="ui checkbox" type="checkbox" name="toggleShareEndDate" ${AuditConfig.getConfig(sourceSubscription,'endDate') ? 'checked': ''} />
                            </div>
                        </td>
                    </g:if>
                    %{--AKTIONEN:--}%
                    <td class="center aligned">
                        <g:if test="${sourceSubscription.startDate || sourceSubscription.endDate}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="subscription.takeDates" data-action="copy" checked="${true}" />
                            </div>
                        </g:if>
                    </td>

                    <td  name="subscription.takeDates.target">
                        <div>
                            <b><i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}:</b>&nbsp
                            <g:formatDate date="${targetSubscription?.startDate}" format="${message(code: 'default.date.format.notime')}"/>
                            ${targetSubscription?.endDate ? (' - ' + formatDate(date: targetSubscription?.endDate, format: message(code: 'default.date.format.notime'))) : ''}
                        </div>
                    </td>

                    <td>
                        <g:if test="${targetSubscription?.startDate || targetSubscription?.endDate}">
                            <div class="ui checkbox la-toggle-radio la-noChange">
                                <g:checkBox name="subscription.deleteDates" data-action="delete" />
                            </div>
                        </g:if>
                    </td>
                </tr>
                <tr>
                    <td name="subscription.takeStatus.source">
                        <div>
                            <b><i class="ellipsis vertical icon"></i>${message(code: 'subscription.status.label')}:</b>
                            ${sourceSubscription.status.getI10n('value')}
                        </div>
                    </td>
                    <g:if test="${isConsortialSubs}">
                        <td class="center aligned">
                            <div class="ui checkbox la-toggle-radio la-share">
                                <input class="ui checkbox" type="checkbox" name="toggleShareStatus" ${AuditConfig.getConfig(sourceSubscription,'status') ? 'checked': ''} />
                            </div>
                        </td>
                    </g:if>
                %{--AKTIONEN:--}%
                    <td class="center aligned">
                        <g:if test="${sourceSubscription.status}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="subscription.takeStatus" data-action="copy" checked="${true}" />
                            </div>
                        </g:if>
                    </td>

                    <td name="subscription.takeStatus.target">
                        <div>
                            <b><i class="ellipsis vertical icon"></i>${message(code: 'subscription.status.label')}:</b>
                            ${targetSubscription?.status?.getI10n('value')}
                        </div>
                    </td>

                    <td>
                        <g:if test="${targetSubscription?.status}">
                            <div class="ui checkbox la-toggle-radio la-noChange">
                                <g:checkBox name="subscription.deleteStatus" data-action="delete" />
                            </div>
                        </g:if>
                    </td>
                </tr>
            </g:if>

            <tr>
                <td name="subscription.takeKind.source">
                    <div>
                        <b><i class="image outline icon"></i>${message(code: 'subscription.kind.label')}:</b>
                        ${sourceSubscription.kind?.getI10n('value')}
                    </div>
                </td>
                <g:if test="${isConsortialSubs}">
                    <td class="center aligned">
                        <div class="ui checkbox la-toggle-radio la-share">
                            <input class="ui checkbox" type="checkbox" name="toggleShareKind" value="${sourceSubscription.kind}" ${AuditConfig.getConfig(sourceSubscription,'kind') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
                %{--AKTIONEN:--}%
                <td class="center aligned">
                    <g:if test="${sourceSubscription.kind}">
                        <div class="ui checkbox la-toggle-radio la-replace">
                            <g:checkBox name="subscription.takeKind" data-action="copy" checked="${true}" />
                        </div>
                    </g:if>
                </td>

                <td name="subscription.takeKind.target">
                    <div>
                        <b><i class="image outline icon"></i>${message(code: 'subscription.kind.label')}:</b>
                        ${targetSubscription?.kind?.getI10n('value')}
                    </div>
                </td>

                <td>
                    <g:if test="${targetSubscription?.kind}">
                        <div class="ui checkbox la-toggle-radio la-noChange">
                            <g:checkBox name="subscription.deleteKind" data-action="delete" />
                        </div>
                    </g:if>
                </td>
            </tr>
            <tr>
                <td name="subscription.takeForm.source">
                    <div>
                        <b><i class="dolly icon"></i>${message(code: 'subscription.form.label')}:</b>
                        ${sourceSubscription.form?.getI10n('value')}
                    </div>
                </td>
                <g:if test="${isConsortialSubs}">
                    <td class="center aligned">
                        <div class="ui checkbox la-toggle-radio la-share">
                            <input class="ui checkbox" type="checkbox" name="toggleShareForm" ${AuditConfig.getConfig(sourceSubscription,'form') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
                %{--AKTIONEN:--}%
                <td class="center aligned">
                    <g:if test="${sourceSubscription.form}">
                        <div class="ui checkbox la-toggle-radio la-replace">
                            <g:checkBox name="subscription.takeForm" data-action="copy" checked="${true}" />
                        </div>
                    </g:if>
                </td>

                <td name="subscription.takeForm.target">
                    <div>
                        <b><i class="dolly icon"></i>${message(code: 'subscription.form.label')}:</b>
                        ${targetSubscription?.form?.getI10n('value')}
                    </div>
                </td>

                <td>
                    <g:if test="${targetSubscription?.form}">
                        <div class="ui checkbox la-toggle-radio la-noChange">
                            <g:checkBox name="subscription.deleteForm" data-action="delete" />
                        </div>
                    </g:if>
                </td>
            </tr>
            <tr>
                <td name="subscription.takeResource.source">
                    <div>
                        <b><i class="box icon"></i>${message(code: 'subscription.resource.label')}:</b>
                        ${sourceSubscription.resource?.getI10n('value')}
                    </div>
                </td>
                <g:if test="${isConsortialSubs}">
                    <td class="center aligned">
                        <div class="ui checkbox la-toggle-radio la-share">
                            <input class="ui checkbox" type="checkbox" name="toggleShareResource" ${AuditConfig.getConfig(sourceSubscription,'resource') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
                %{--AKTIONEN:--}%
                <td class="center aligned">
                    <g:if test="${sourceSubscription.resource}">
                        <div class="ui checkbox la-toggle-radio la-replace">
                            <g:checkBox name="subscription.takeResource" data-action="copy" checked="${true}" />
                        </div>
                    </g:if>
                </td>

                <td name="subscription.takeResource.target">
                    <div>
                        <b><i class="box icon"></i>${message(code: 'subscription.resource.label')}:</b>
                        ${targetSubscription?.resource?.getI10n('value')}
                    </div>
                </td>

                <td>
                    <g:if test="${targetSubscription?.form}">
                        <div class="ui checkbox la-toggle-radio la-noChange">
                            <g:checkBox name="subscription.deleteResource" data-action="delete" />
                        </div>
                    </g:if>
                </td>
            </tr>
            <tr>
                <td name="subscription.takePublicForApi.source">
                    <div>
                        <b><i class="shipping fast icon"></i>${message(code: 'subscription.isPublicForApi.label')}:</b>
                        ${sourceSubscription.isPublicForApi ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                    </div>
                </td>
                <g:if test="${isConsortialSubs}">
                    <td class="center aligned">
                        <div class="ui checkbox la-toggle-radio la-share">
                            <input class="ui checkbox" type="checkbox" name="toggleSharePublicForApi" ${AuditConfig.getConfig(sourceSubscription,'isPublicForApi') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
                %{--AKTIONEN:--}%
                <td class="center aligned">
                    <div class="ui checkbox la-toggle-radio la-replace">
                        <g:checkBox name="subscription.takePublicForApi" data-action="copy" checked="${true}" />
                    </div>
                </td>

                <td name="subscription.takePublicForApi.target">
                    <div>
                        <b><i class="shipping fast icon"></i>${message(code: 'subscription.isPublicForApi.label')}:</b>
                        ${targetSubscription?.isPublicForApi ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                    </div>
                </td>

                <td>
                    <div class="ui checkbox la-toggle-radio la-noChange">
                        <g:checkBox name="subscription.deletePublicForApi" data-action="delete" />
                    </div>
                </td>
            </tr>
            <tr>
                <td name="subscription.takePerpetualAccess.source">
                    <div>
                        <b><i class="flag outline icon"></i>${message(code: 'subscription.hasPerpetualAccess.label')}:</b>
                        ${sourceSubscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                    </div>
                </td>
                <g:if test="${isConsortialSubs}">
                    <td class="center aligned">
                        <div class="ui checkbox la-toggle-radio la-share">
                            <input class="ui checkbox" type="checkbox" name="toggleSharePerpetualAccess" ${AuditConfig.getConfig(sourceSubscription,'hasPerpetualAccess') ? 'checked': ''} />
                        </div>
                    </td>
                </g:if>
                %{--AKTIONEN:--}%
                <td class="center aligned">
                    <div class="ui checkbox la-toggle-radio la-replace">
                        <g:checkBox name="subscription.takePerpetualAccess" data-action="copy" checked="${true}" />
                    </div>
                </td>

                <td name="subscription.takePerpetualAccess.target">
                    <div>
                        <b><i class="flag outline icon"></i>${message(code: 'subscription.hasPerpetualAccess.label')}:</b>
                        ${targetSubscription?.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                    </div>
                </td>

                <td>
                    <div class="ui checkbox la-toggle-radio la-noChange">
                        <g:checkBox name="subscription.deletePerpetualAccess" data-action="delete" />
                    </div>
                </td>
            </tr>
            <tr>
                <td name="subscription.takeLicenses.source">
                    <div>
                        <b><i class="balance scale icon"></i>${message(code: 'license.label')}:</b>
                        <g:each in="${sourceLicenses}" var="license">
                            <g:link controller="license" action="show" target="_blank" id="${license.id}">
                                <div data-oid="${GenericOIDService.getOID(license)}" class="la-multi-sources">
                                    <b><i class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:</b>
                                    ${license.reference}
                                    <br>
                                </div>
                            </g:link>
                        </g:each>
                    </div>
                </td>
                <g:if test="${isConsortialSubs}">
                    <td class="center aligned">

                    </td>
                </g:if>
                %{--AKTIONEN:--}%
                <td class="center aligned">
                    <g:each in="${sourceLicenses}" var="license">
                        <div class="ui checkbox la-toggle-radio la-replace">
                            <g:checkBox name="subscription.takeLicenses" data-action="copy" value="${GenericOIDService.getOID(license)}" checked="${true}" />
                        </div>
                    </g:each>
                </td>

                <td  name="subscription.takeLicenses.target">
                    <div>
                        <b><i class="balance scale icon"></i>${message(code: 'license.label')}:</b>
                        <g:each in="${targetLicenses}" var="license">
                            <div data-oid="${GenericOIDService.getOID(license)}">
                                <b><i class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:</b>
                                <g:link controller="license" action="show" target="_blank" id="${license.id}">
                                    ${license.reference}
                                </g:link>
                                <br>
                            </div>
                        </g:each>
                    </div>
                </td>

                <td>
                    <g:each in="${targetLicenses}" var="license">
                        <div class="ui checkbox la-toggle-radio la-noChange">
                            <g:checkBox name="subscription.deleteLicenses" data-action="delete" value="${GenericOIDService.getOID(license)}" checked="${false}"/>
                        </div>
                    </g:each>
                </td>
            </tr>
            <tr>
                <td  name="subscription.takeOrgRelations.source">
                    <div>
                        <g:if test="${ ! source_visibleOrgRelations}">
                            <b><i class="university icon"></i>&nbsp${message(code: 'subscription.organisations.label')}:</b>
                        </g:if>
                        <g:each in="${source_visibleOrgRelations}" var="source_role">
                            <g:if test="${source_role.org}">
                                <div data-oid="${GenericOIDService.getOID(source_role)}" class="la-multi-sources">
                                    <b><i class="university icon"></i>&nbsp${source_role?.roleType?.getI10n("value")}:</b>
                                    <g:link controller="organisation" action="show" target="_blank" id="${source_role.org.id}">
                                        ${source_role?.org?.name}
                                    </g:link>

                                <br>
                                </div>
                            </g:if>
                        </g:each>
                    </div>
                </td>
                <g:if test="${isConsortialSubs}">
                        <td class="center aligned">
                            <g:each in="${source_visibleOrgRelations}" var="source_role">
                                <g:if test="${source_role.org}">
                                    <div class="ui checkbox la-toggle-radio la-share">
                                        <input class="ui checkbox" type="checkbox" name="toggleShareOrgRoles" value="${source_role.class.name}:${source_role.id}" ${source_role.isShared ? 'checked': ''} />
                                    </div>
                                    <br>
                                </g:if>
                            </g:each>

                        </td>
                </g:if>

                </td>
                %{--AKTIONEN:--}%
                <td class="center aligned">
                    <g:each in="${source_visibleOrgRelations}" var="source_role">
                        <g:if test="${source_role.org}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="subscription.takeOrgRelations" data-action="copy" value="${GenericOIDService.getOID(source_role)}" checked="${true}" />
                            </div>
                        </g:if>
                    </g:each>
                </td>

                <td  name="subscription.takeOrgRelations.target">
                    <div>
                        <g:if test="${ ! target_visibleOrgRelations}">
                            <b><i class="university icon"></i>&nbsp${message(code: 'subscription.organisations.label')}:</b>
                        </g:if>
                        <g:each in="${target_visibleOrgRelations}" var="target_role">
                            <g:if test="${target_role.org}">
                                <div data-oid="${GenericOIDService.getOID(target_role)}">
                                    <b><i class="university icon"></i>&nbsp${target_role?.roleType?.getI10n("value")}:</b>
                                    <g:link controller="organisation" action="show" target="_blank" id="${target_role.org.id}">
                                        ${target_role?.org?.name}
                                    </g:link>
                                    <g:if test="${isConsortialSubs}">
                                            <div class="right aligned wide column">
                                                <g:if test="${target_role.isShared}">
                                                    <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                                        <i class="la-share icon la-js-editmode-icon"></i>
                                                    </span>

                                                </g:if>
                                                <g:else>
                                                    <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                                        <i class="la-share slash icon la-js-editmode-icon"></i>
                                                    </span>
                                                </g:else>

                                            </div>
                                    </g:if>
                                    <br>
                                </div>
                            </g:if>
                        </g:each>
                    </div>
                </td>
                <td>
                    <g:each in="${target_visibleOrgRelations}" var="target_role">
                        <g:if test="${target_role.org}">
                            <div class="ui checkbox la-toggle-radio la-noChange">
                                <g:checkBox name="subscription.deleteOrgRelations" data-action="delete" value="${GenericOIDService.getOID(target_role)}" checked="${false}"/>
                            </div>
                            <br/>
                        </g:if>
                    </g:each>
                </td>
            </tr>
            <tr>
                <td name="subscription.takeIdentifier.source">
                    <b><i class="barcode icon"></i>&nbsp${message(code: 'default.identifiers.label')}:</b><br />
                    <g:each in="${sourceIdentifiers}" var="ident">
                        <b>${ident.ns.ns}:</b>&nbsp${ident.value}<br />
                    </g:each>
                </td>
                <g:if test="${isConsortialSubs}">
                    <td class="center aligned">
                    </td>
                </g:if>

                %{--COPY:--}%
                <td class="center aligned">
                    <g:each in="${sourceIdentifiers}" var="ident">
                        <div data-id="${ident.id}" class="la-element">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="subscription.takeIdentifierIds" value="${ident.id}" data-action="copy"  />
                            </div>
                        </div>
                    </g:each>
                </td>
                <td name="subscription.takeIdentifier.target">
                    <b><i class="barcode icon"></i>&nbsp${message(code: 'default.identifiers.label')}:</b><br />
                    <g:each in="${targetIdentifiers}" var="ident">
                        <b>${ident.ns.ns}:</b>&nbsp${ident.value}<br />
                    </g:each>
                </td>
                %{--DELETE:--}%
                <td>
                    <g:each in="${targetIdentifiers}" var="ident">
                        <div data-id="${ident.id}" class="la-element">
                            <div class="ui checkbox la-toggle-radio la-noChange">
                                <g:checkBox name="subscription.deleteIdentifierIds" value="${ident.id}" data-action="delete" checked="${false}" />
                            </div>
                        </div>
                    </g:each>
                </td>
            </tr>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'subscription.details.copyElementsIntoSubscription.copyDeleteElements.button') }" />

        <g:if test="${controllerName == 'survey'}">
            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <g:set var="surveyConfig" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(Subscription.get(sourceSubscriptionId), true)}" />
                    <g:link action="renewalWithSurvey" id="${surveyConfig.surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" class="ui button js-click-control">
                        <g:message code="renewalWithSurvey.back"/>
                    </g:link>
                </div>
                <div class="eight wide field" style="text-align: right;">
                    <g:set var="submitDisabled" value="${(sourceSubscription && targetSubscription)? '' : 'disabled'}"/>
                    <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()"  ${submitDisabled}/>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitDisabled" value="${(sourceSubscription && targetSubscription)? '' : 'disabled'}"/>
                <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()" ${submitDisabled}/>
            </div>
        </g:else>
    </g:form>
</semui:form>

<r:script>

    var subCopyController = {

        checkboxes : {
            $takeDates: $('input:checkbox[name="subscription.takeDates"]'),
            $deleteDates: $('input:checkbox[name="subscription.deleteDates"]'),
            $takeStatus: $('input:checkbox[name="subscription.takeStatus"]'),
            $deleteStatus: $('input:checkbox[name="subscription.deleteStatus"]'),
            $takeKind: $('input:checkbox[name="subscription.takeKind"]'),
            $deleteKind: $('input:checkbox[name="subscription.deleteKind"]'),
            $takeForm: $('input:checkbox[name="subscription.takeForm"]'),
            $deleteForm: $('input:checkbox[name="subscription.deleteForm"]'),
            $takeResource: $('input:checkbox[name="subscription.takeResource"]'),
            $deleteResource: $('input:checkbox[name="subscription.deleteResource"]'),
            $takePublicForApi: $('input:checkbox[name="subscription.takePublicForApi"]'),
            $deletePublicForApi: $('input:checkbox[name="subscription.deletePublicForApi"]'),
            $takePerpetualAccess: $('input:checkbox[name="subscription.takePerpetualAccess"]'),
            $deletePerpetualAccess: $('input:checkbox[name="subscription.deletePerpetualAccess"]'),
            $takeLicenses: $('input:checkbox[name="subscription.takeLicenses"]'),
            $deleteLicenses: $('input:checkbox[name="subscription.deleteLicenses"]'),
            $takeOrgRelations: $('input:checkbox[name="subscription.takeOrgRelations"]'),
            $deleteOrgRelations: $('input:checkbox[name="subscription.deleteOrgRelations"]')
        },

        init: function() {
            var ref = subCopyController.checkboxes

            ref.$takeDates.change( function(event) {
                subCopyController.takeDates(this);
            }).trigger('change')

            ref.$deleteDates.change( function(event) {
                subCopyController.deleteDates(this);
            }).trigger('change')

            ref.$takeStatus.change( function(event) {
                subCopyController.takeStatus(this);
            }).trigger('change')

            ref.$deleteStatus.change( function(event) {
                subCopyController.deleteStatus(this);
            }).trigger('change')

            ref.$takeKind.change( function(event) {
                subCopyController.takeKind(this);
            }).trigger('change')

            ref.$deleteKind.change( function(event) {
                subCopyController.deleteKind(this);
            }).trigger('change')

            ref.$takeForm.change( function(event) {
                subCopyController.takeForm(this);
            }).trigger('change')

            ref.$deleteForm.change( function(event) {
                subCopyController.deleteForm(this);
            }).trigger('change')

            ref.$takeResource.change( function(event) {
                subCopyController.takeResource(this);
            }).trigger('change')

            ref.$deleteResource.change( function(event) {
                subCopyController.deleteResource(this);
            }).trigger('change')

            ref.$takePublicForApi.change( function(event) {
                subCopyController.takePublicForApi(this);
            }).trigger('change')

            ref.$deletePublicForApi.change( function(event) {
                subCopyController.deletePublicForApi(this);
            }).trigger('change')

            ref.$takePerpetualAccess.change( function(event) {
                subCopyController.takePerpetualAccess(this);
            }).trigger('change')

            ref.$deletePerpetualAccess.change( function(event) {
                subCopyController.deletePerpetualAccess(this);
            }).trigger('change')

            ref.$takeLicenses.change( function(event) {
                subCopyController.takeLicenses(this);
            }).trigger('change')

            ref.$deleteLicenses.change( function(event) {
                subCopyController.deleteLicenses(this);
            }).trigger('change')

            ref.$takeOrgRelations.change( function(event) {
                subCopyController.takeOrgRelations(this);
            }).trigger('change')

            ref.$deleteOrgRelations.change( function(event) {
                subCopyController.deleteOrgRelations(this);
            }).trigger('change')
        },

        takeDates: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeDates.source"] div').addClass('willStay');
                $('.table tr td[name="subscription.takeDates.target"] div').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeDates.source"] div').removeClass('willStay');
                $('.table tr td[name="subscription.takeDates.target"] div').removeClass('willBeReplaced');
            }
        },

        deleteDates: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeDates.target"] div').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takeDates.target"] div').removeClass('willBeReplacedStrong');
            }
        },

        takeStatus: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeStatus.source"] div').addClass('willStay');
                $('.table tr td[name="subscription.takeStatus.target"] div').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeStatus.source"] div').removeClass('willStay');
                $('.table tr td[name="subscription.takeStatus.target"] div').removeClass('willBeReplaced');
            }
        },

        deleteStatus: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeStatus.target"] div').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takeStatus.target"] div').removeClass('willBeReplacedStrong');
            }
        },

        takeKind: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeKind.source"] div').addClass('willStay');
                $('.table tr td[name="subscription.takeKind.target"] div').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeKind.source"] div').removeClass('willStay');
                $('.table tr td[name="subscription.takeKind.target"] div').removeClass('willBeReplaced');
            }
        },

        deleteKind: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeKind.target"] div').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takeKind.target"] div').removeClass('willBeReplacedStrong');
            }
        },

        takeForm: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeForm.source"] div').addClass('willStay');
                $('.table tr td[name="subscription.takeForm.target"] div').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeForm.source"] div').removeClass('willStay');
                $('.table tr td[name="subscription.takeForm.target"] div').removeClass('willBeReplaced');
            }
        },

        deleteForm: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeForm.target"] div').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takeForm.target"] div').removeClass('willBeReplacedStrong');
            }
        },

        takeResource: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeResource.source"] div').addClass('willStay');
                $('.table tr td[name="subscription.takeResource.target"] div').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takeResource.source"] div').removeClass('willStay');
                $('.table tr td[name="subscription.takeResource.target"] div').removeClass('willBeReplaced');
            }
        },

        deleteResource: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeResource.target"] div').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takeResource.target"] div').removeClass('willBeReplacedStrong');
            }
        },

        takePublicForApi: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takePublicForApi.source"] div').addClass('willStay');
                $('.table tr td[name="subscription.takePublicForApi.target"] div').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takePublicForApi.source"] div').removeClass('willStay');
                $('.table tr td[name="subscription.takePublicForApi.target"] div').removeClass('willBeReplaced');
            }
        },

        deletePublicForApi: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takePublicForApi.target"] div').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takePublicForApi.target"] div').removeClass('willBeReplacedStrong');
            }
        },

        takePerpetualAccess: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takePerpetualAccess.source"] div').addClass('willStay');
                $('.table tr td[name="subscription.takePerpetualAccess.target"] div').addClass('willBeReplaced');
            }
            else {
                $('.table tr td[name="subscription.takePerpetualAccess.source"] div').removeClass('willStay');
                $('.table tr td[name="subscription.takePerpetualAccess.target"] div').removeClass('willBeReplaced');
            }
        },

        deletePerpetualAccess: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takePerpetualAccess.target"] div').addClass('willBeReplacedStrong');
            }
            else {
                $('.table tr td[name="subscription.takePerpetualAccess.target"] div').removeClass('willBeReplacedStrong');
            }
        },

        takeLicenses: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeLicenses.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeLicenses.target"] div div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeLicenses.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeLicenses') < 1) {
                    $('.table tr td[name="subscription.takeLicenses.target"] div div').removeClass('willStay');
                }
            }
        },

        deleteLicenses: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeLicenses.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
            } else {
                $('.table tr td[name="subscription.takeLicenses.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
            }
        },

        takeOrgRelations: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeOrgRelations.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeOrgRelations.target"] div div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeOrgRelations.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeOrgRelations') < 1) {
                    $('.table tr td[name="subscription.takeOrgRelations.target"] div div').removeClass('willStay');
                }
            }
        },

        deleteOrgRelations: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeOrgRelations.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
            } else {
                $('.table tr td[name="subscription.takeOrgRelations.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
            }
        },

        getNumberOfCheckedCheckboxes: function(inputElementName) {
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

    subCopyController.init()
</r:script>


