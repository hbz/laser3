<%@ page import="com.k_int.kbplus.SubscriptionController; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription; com.k_int.kbplus.GenericOIDService; de.laser.AuditConfig; com.k_int.kbplus.RefdataValue; de.laser.FormService;" %>
<laser:serviceInjection/>

<g:set var="copyElementsService" bean="copyElementsService"/>
<g:set var="formService" bean="formService"/>

<semui:form>

    <g:if test="${!fromSurvey && !isRenewSub}">
        <g:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject          : sourceObject,
                targetObject          : targetObject,
                allObjects_readRights : allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form action="${actionName}" controller="${controllerName}" id="${params.id}"
            params="[workFlowPart: workFlowPart, sourceObjectId: GenericOIDService.getOID(sourceObject), targetObjectId: targetObject?.id, isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <table class="ui celled table table-tworow la-table">
            <thead>
            <tr>
                <th class="six wide">
                    <g:if test="${sourceObject}"><g:link controller="subscription" action="show"
                                                               id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if>
                </th>
                <g:if test="${isConsortialObjects}">
                    <th class="center aligned">
                        <g:message code="copyElementsIntoObject.share"/>
                    </th>
                </g:if>
                <th class="one wide center aligned"><input type="checkbox" name="checkAllCopyCheckboxes"
                                                           data-action="copy" onClick="toggleAllCheckboxes(this)"
                                                           checked/>
                <th class="six wide">
                    <g:if test="${targetObject}"><g:link controller="subscription" action="show"
                                                               id="${targetObject?.id}">${targetObject?.dropdownNamingConvention()}</g:link></g:if>
                </th>
                <th class="one wide center aligned">
                    <g:if test="${targetObject}">
                        <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)"/>
                    </g:if>
                </th>
            </tr>
            </thead>
            <tbody>

            <g:each in="${copyElementsService.allowedProperties(sourceObject)}" var="objProperty">
                <tr>
                    <td name="object.take${objProperty}.source">
                        <div>
                            <b><semui:propertyIcon object="${sourceObject}" propertyName="${objProperty}"
                                                   showToolTipp="true"/> ${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.${objProperty}.label")}:</b>
                            <semui:showPropertyValue property="${sourceObject."${objProperty}"}"/>
                        </div>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                            <div class="ui checkbox la-toggle-radio la-share">
                                <g:checkBox name="object.toggleAudit" value="${objProperty}" class="ui checkbox" checked="${AuditConfig.getConfig(sourceObject, objProperty) ? 'true' : 'false'}"/>
                            </div>
                        </td>
                    </g:if>
                %{--AKTIONEN:--}%
                    <td class="center aligned">
                        <g:if test="${sourceObject.hasProperty("${objProperty}")}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="object.take" id="object.take${objProperty}" value="${objProperty}" data-action="copy"
                                            checked="${true}"/>
                            </div>
                        </g:if>
                    </td>

                    <td name="object.take${objProperty}.target">
                        <g:if test="${targetObject}">
                            <div>
                                <b><semui:propertyIcon object="${targetObject}" propertyName="${objProperty}"
                                                       showToolTipp="true"/> ${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.${objProperty}.label")}:</b>
                                <semui:showPropertyValue property="${targetObject?."${objProperty}"}"/>

                                <g:if test="${isConsortialObjects}">
                                    <div class="right aligned wide column">
                                    <%
                                        if (AuditConfig.getConfig(targetObject, objProperty)) {
                                            if (targetObject.isSlaved) {
                                                println '<span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue"></i></span>'
                                            } else {
                                                println '<span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                            }
                                        }
                                    %>
                                    </div>
                                </g:if>

                            </div>
                        </g:if>
                    </td>

                    <td>
                        <g:if test="${targetObject?.hasProperty("${objProperty}")}">
                            <div class="ui checkbox la-toggle-radio la-noChange">
                                <g:checkBox name="object.delete" id="object.delete${objProperty}" value="${objProperty}" data-action="delete"/>
                            </div>
                        </g:if>
                    </td>
                </tr>
            </g:each>

            <g:if test="${sourceLicenses}">
            <tr>
                <td name="object.takeLicenses.source">
                    <div>
                        <b><i class="balance scale icon"></i>${message(code: 'license.label')}:</b>
                        <g:each in="${sourceLicenses}" var="license">
                            <g:link controller="license" action="show" target="_blank" id="${license.id}">
                                <div data-oid="${GenericOIDService.getOID(license)}" class="la-multi-sources">
                                    <b><i class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:
                                    </b>
                                    ${license.reference}
                                    <br>
                                </div>
                            </g:link>
                        </g:each>
                    </div>
                </td>
                <g:if test="${isConsortialObjects}">
                    <td class="center aligned">

                    </td>
                </g:if>
            %{--AKTIONEN:--}%
                <td class="center aligned">
                    <g:each in="${sourceLicenses}" var="license">
                        <div class="ui checkbox la-toggle-radio la-replace">
                            <g:checkBox name="object.takeLicenses" data-action="copy"
                                        value="${GenericOIDService.getOID(license)}" checked="${true}"/>
                        </div>
                    </g:each>
                </td>

                <td name="object.takeLicenses.target">
                    <div>
                        <b><i class="balance scale icon"></i>${message(code: 'license.label')}:</b>
                        <g:each in="${targetLicenses}" var="license">
                            <div data-oid="${GenericOIDService.getOID(license)}">
                                <b><i class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:
                                </b>
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
                            <g:checkBox name="object.deleteLicenses" data-action="delete"
                                        value="${GenericOIDService.getOID(license)}" checked="${false}"/>
                        </div>
                    </g:each>
                </td>
            </tr>
            </g:if>
            <g:if test="${sourceObject.hasProperty("orgLinks") || sourceObject.hasProperty("orgRelations")}">
            <tr>
                <td name="object.takeOrgRelations.source">
                    <div>
                        <g:if test="${!source_visibleOrgRelations}">
                            <b><i class="university icon"></i>&nbsp${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.organisations.label")}:
                            </b>
                        </g:if>
                        <g:each in="${source_visibleOrgRelations}" var="source_role">
                            <g:if test="${source_role.org}">
                                <div data-oid="${GenericOIDService.getOID(source_role)}" class="la-multi-sources">
                                    <b><i class="university icon"></i>&nbsp${source_role.roleType.getI10n("value")}:</b>
                                    <g:link controller="organisation" action="show" target="_blank"
                                            id="${source_role.org.id}">
                                        ${source_role.org.name}
                                    </g:link>

                                    <br>
                                </div>
                            </g:if>
                        </g:each>
                    </div>
                </td>
                <g:if test="${isConsortialObjects}">
                    <td class="center aligned">
                        <g:each in="${source_visibleOrgRelations}" var="source_role">
                            <g:if test="${source_role.org}">
                                <div class="ui checkbox la-toggle-radio la-share">
                                    <input class="ui checkbox" type="checkbox" name="toggleShareOrgRoles"
                                           value="${source_role.class.name}:${source_role.id}" ${source_role.isShared ? 'checked' : ''}/>
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
                                <g:checkBox name="object.takeOrgRelations" data-action="copy"
                                            value="${GenericOIDService.getOID(source_role)}" checked="${true}"/>
                            </div>
                        </g:if>
                    </g:each>
                </td>

                <td name="object.takeOrgRelations.target">
                    <div>
                        <g:if test="${!target_visibleOrgRelations}">
                            <b><i class="university icon"></i>&nbsp${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.organisations.label")}:
                            </b>
                        </g:if>
                        <g:each in="${target_visibleOrgRelations}" var="target_role">
                            <g:if test="${target_role.org}">
                                <div data-oid="${GenericOIDService.getOID(target_role)}">
                                    <b><i class="university icon"></i>&nbsp${target_role.roleType.getI10n("value")}:</b>
                                    <g:link controller="organisation" action="show" target="_blank"
                                            id="${target_role.org.id}">
                                        ${target_role.org.name}
                                    </g:link>
                                    <g:if test="${isConsortialObjects}">
                                        <div class="right aligned wide column">
                                            <g:if test="${target_role.isShared}">
                                                <span data-position="top left" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'property.share.tooltip.on')}">
                                                    <i class="la-share icon la-js-editmode-icon"></i>
                                                </span>

                                            </g:if>
                                            <g:else>
                                                <span data-position="top left" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'property.share.tooltip.off')}">
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
                                <g:checkBox name="object.deleteOrgRelations" data-action="delete"
                                            value="${GenericOIDService.getOID(target_role)}" checked="${false}"/>
                            </div>
                            <br/>
                        </g:if>
                    </g:each>
                </td>
            </tr>
            </g:if>

            <g:if test="${sourceObject instanceof com.k_int.kbplus.Subscription}">
            <tr>
                <td name="subscription.takeSpecificSubscriptionEditors.source">
                    <div>
                        <b>
                            <i class="address card icon"></i>
                            ${message(code: 'subscription.specificSubscriptionEditors')}:
                        </b>
                        <g:each in="${source_visibleOrgRelations}" var="source_role">
                            <g:if test="${source_role.org}">
                                <g:if test="${Person.getPublicByOrgAndObjectResp(source_role.org, sourceObject, 'Specific subscription editor') ||
                                        Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}">

                                <%-- public --%>
                                    <g:each in="${Person.getPublicByOrgAndObjectResp(source_role.org, sourceObject, 'Specific subscription editor')}"
                                            var="resp">

                                        <div data-oid="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                             class="la-multi-sources">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'address.public')}"
                                                  data-position="top right">
                                                <i class="address card icon"></i>
                                            </span>
                                            <g:link controller="person" action="show"
                                                    id="${resp.id}">${resp}</g:link>
                                            (<b><i
                                                class="university icon"></i>&nbsp${source_role.roleType.getI10n("value")}:
                                        </b>
                                            <g:link controller="organisation" action="show" target="_blank"
                                                    id="${source_role.org.id}">${source_role.org.name}</g:link>)
                                        </div>
                                    </g:each>
                                <%-- public --%>
                                <%-- private --%>
                                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}"
                                            var="resp">
                                        <div data-oid="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                             class="la-multi-sources">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'address.private')}"
                                                  data-position="top right">
                                                <i class="address card outline icon"></i>
                                            </span>
                                            <g:link controller="person" action="show"
                                                    id="${resp.id}">${resp}</g:link>
                                            (<b><i
                                                class="university icon"></i>&nbsp${source_role.roleType.getI10n("value")}:
                                        </b>
                                            <g:link controller="organisation" action="show" target="_blank"
                                                    id="${source_role.org.id}">${source_role.org.name}</g:link>)
                                        </div>
                                    </g:each><%-- private --%>
                                </g:if>
                            </g:if>
                        </g:each>
                    </div>
                </td>
                <g:if test="${isConsortialObjects}">
                    <td class="center aligned">
                    </td>
                </g:if>


            %{--AKTIONEN:--}%
                <td class="center aligned">
                    <g:each in="${source_visibleOrgRelations}" var="source_role">
                        <g:if test="${source_role.org}">
                        <%-- public --%>
                            <g:if test="${Person.getPublicByOrgAndObjectResp(source_role.org, sourceObject, 'Specific subscription editor')}">
                                <g:each in="${Person.getPublicByOrgAndObjectResp(source_role.org, sourceObject, 'Specific subscription editor')}"
                                        var="resp">
                                    <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="subscription.takeSpecificSubscriptionEditors"
                                                    data-action="copy"
                                                    value="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                                    checked="${true}"/>
                                    </div>
                                </g:each>
                            </g:if><%-- public --%>
                        <%-- private --%>
                            <g:if test="${Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}">
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}"
                                        var="resp">
                                    <div class="ui checkbox la-toggle-radio la-replace">
                                        <g:checkBox name="subscription.takeSpecificSubscriptionEditors"
                                                    data-action="copy"
                                                    value="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                                    checked="${true}"/>
                                    </div>
                                </g:each>
                            </g:if><%-- private --%>
                        </g:if>
                    </g:each>
                </td>

                <td name="subscription.takeSpecificSubscriptionEditors.target">
                    <div>
                        <b>
                            <i class="address card icon"></i>
                            ${message(code: 'subscription.specificSubscriptionEditors')}:
                        </b>
                        <g:each in="${target_visibleOrgRelations}" var="target_role">
                            <g:if test="${target_role.org}">
                                <g:if test="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, targetObject, 'Specific subscription editor', contextService.getOrg()) ||
                                        Person.getPublicByOrgAndObjectResp(target_role.org, targetObject, 'Specific subscription editor')}">
                                <%-- public --%>
                                    <g:each in="${Person.getPublicByOrgAndObjectResp(target_role.org, targetObject, 'Specific subscription editor')}"
                                            var="resp">

                                        <div data-oid="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                             class="la-multi-sources">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'address.public')}"
                                                  data-position="top right">
                                                <i class="address card icon"></i>
                                            </span>
                                            <g:link controller="person" action="show"
                                                    id="${resp.id}">${resp}</g:link>
                                            (<b><i
                                                class="university icon"></i>&nbsp${target_role.roleType.getI10n("value")}:
                                        </b>
                                            <g:link controller="organisation" action="show" target="_blank"
                                                    id="${target_role.org.id}">${target_role.org.name}</g:link>)
                                        </div>
                                    </g:each>
                                <%-- public --%>
                                <%-- private --%>

                                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, targetObject, 'Specific subscription editor', contextService.getOrg())}"
                                            var="resp">
                                        <div data-oid="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                             class="la-multi-sources">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'address.private')}"
                                                  data-position="top right">
                                                <i class="address card outline icon"></i>
                                            </span>
                                            <g:link controller="person" action="show"
                                                    id="${resp.id}">${resp}</g:link>
                                            (<b><i
                                                class="university icon"></i>&nbsp${target_role.roleType.getI10n("value")}:
                                        </b>
                                            <g:link controller="organisation" action="show" target="_blank"
                                                    id="${target_role.org.id}">${target_role.org.name}</g:link>)
                                        </div>
                                    </g:each>
                                <%-- private --%>
                                </g:if>
                            </g:if>
                        </g:each>
                    </div>
                </td>
                <td>
                    <g:each in="${target_visibleOrgRelations}" var="target_role">
                        <g:if test="${target_role.org}">

                        <%-- public --%>
                            <g:if test="${Person.getPublicByOrgAndObjectResp(target_role.org, sourceObject, 'Specific subscription editor')}">
                                <g:each in="${Person.getPublicByOrgAndObjectResp(target_role.org, sourceObject, 'Specific subscription editor')}"
                                        var="resp">
                                    <div class="ui checkbox la-toggle-radio la-noChange">
                                        <g:checkBox name="subscription.deleteSpecificSubscriptionEditors"
                                                    data-action="delete"
                                                    value="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                                    checked="${false}"/>
                                    </div>
                                </g:each>
                            </g:if><%-- public --%>
                        <%-- private --%>
                            <g:if test="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}">
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}"
                                        var="resp">
                                    <div class="ui checkbox la-toggle-radio la-noChange">
                                        <g:checkBox name="subscription.deleteSpecificSubscriptionEditors"
                                                    data-action="delete"
                                                    value="${GenericOIDService.getOID(com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                                    checked="${false}"/>
                                    </div>
                                </g:each>
                            </g:if><%-- private --%>

                        </g:if>
                    </g:each>
                </td>
            </tr>
            </g:if>

            <g:if test="${sourceObject.hasProperty("ids")}">
                <tr>
                    <td name="object.takeIdentifier.source">
                        <b><i class="barcode icon"></i>&nbsp${message(code: 'default.identifiers.label')}:</b><br/>
                        <g:each in="${sourceIdentifiers}" var="ident">
                            <b>${ident.ns.ns}:</b>&nbsp${ident.value}<br/>
                        </g:each>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                        </td>
                    </g:if>

                %{--COPY:--}%
                    <td class="center aligned">
                        <g:each in="${sourceIdentifiers}" var="ident">
                            <div data-id="${ident.id}" class="la-element">
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="object.takeIdentifierIds" value="${ident.id}"
                                                data-action="copy"/>
                                </div>
                            </div>
                        </g:each>
                    </td>
                    <td name="object.takeIdentifier.target">
                        <b><i class="barcode icon"></i>&nbsp${message(code: 'default.identifiers.label')}:</b><br/>
                        <g:each in="${targetIdentifiers}" var="ident">
                            <b>${ident.ns.ns}:</b>&nbsp${ident.value}<br/>
                        </g:each>
                    </td>
                    %{--DELETE:--}%
                    <td>
                        <g:each in="${targetIdentifiers}" var="ident">
                            <div data-id="${ident.id}" class="la-element">
                                <div class="ui checkbox la-toggle-radio la-noChange">
                                    <g:checkBox name="object.deleteIdentifierIds" value="${ident.id}"
                                                data-action="delete" checked="${false}"/>
                                </div>
                            </div>
                        </g:each>
                    </td>
                </tr>
            </g:if>
            </tbody>
        </table>
        <g:set var="submitButtonText" value="${isRenewSub ?
                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep') :
                message(code: 'copyElementsIntoObject.copyDeleteElements.button')}"/>

        <g:if test="${fromSurvey && surveyConfig}">
            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <g:link controller="survey" action="renewalWithSurvey" id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]" class="ui button js-click-control">
                        <g:message code="renewalWithSurvey.back"/>
                    </g:link>
                </div>

                <div class="eight wide field" style="text-align: right;">
                    <g:set var="submitDisabled"
                           value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>
                    <input type="submit" class="ui button js-click-control" value="${submitButtonText}"
                           onclick="return jsConfirmation()" ${submitDisabled}/>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitDisabled" value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>
                <input type="submit" class="ui button js-click-control" value="${submitButtonText}"
                       onclick="return jsConfirmation()" ${submitDisabled}/>
            </div>
        </g:else>
    </g:form>
</semui:form>

<r:script>

    var subCopyController = {

        checkboxes : {
            $takeLicenses: $('input:checkbox[name="object.takeLicenses"]'),
            $deleteLicenses: $('input:checkbox[name="object.deleteLicenses"]'),
            $takeOrgRelations: $('input:checkbox[name="object.takeOrgRelations"]'),
            $deleteOrgRelations: $('input:checkbox[name="object.deleteOrgRelations"]'),
            $takeSpecificSubscriptionEditors: $('input:checkbox[name="subscription.takeSpecificSubscriptionEditors"]'),
            $deleteSpecificSubscriptionEditors: $('input:checkbox[name="subscription.deleteSpecificSubscriptionEditors"]')
        },

        init: function() {
            var ref = subCopyController.checkboxes

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

            ref.$takeSpecificSubscriptionEditors.change( function(event) {
                subCopyController.takeSpecificSubscriptionEditors(this);
            }).trigger('change')

            ref.$deleteSpecificSubscriptionEditors.change( function(event) {
                subCopyController.deleteSpecificSubscriptionEditors(this);
            }).trigger('change')

            $("input:checkbox[name^='object']").change(function() {
                subCopyController.checkCheckBoxesOfProperties(this);
            }).trigger('change');



        },

        takeLicenses: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeLicenses.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="object.takeLicenses.target"] div div').addClass('willStay');
            }
            else {
                $('.table tr td[name="object.takeLicenses.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('object.takeLicenses') < 1) {
                    $('.table tr td[name="object.takeLicenses.target"] div div').removeClass('willStay');
                }
            }
        },

        deleteLicenses: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeLicenses.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
            } else {
                $('.table tr td[name="object.takeLicenses.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
            }
        },

        takeOrgRelations: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeOrgRelations.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="object.takeOrgRelations.target"] div div').addClass('willStay');
            }
            else {
                $('.table tr td[name="object.takeOrgRelations.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('object.takeOrgRelations') < 1) {
                    $('.table tr td[name="object.takeOrgRelations.target"] div div').removeClass('willStay');
                }
            }
        },

        deleteOrgRelations: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="object.takeOrgRelations.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
            } else {
                $('.table tr td[name="object.takeOrgRelations.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
            }
        },

        takeSpecificSubscriptionEditors: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div').addClass('willStay');
            }
            else {
                $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                if (subCopyController.getNumberOfCheckedCheckboxes('subscription.takeSpecificSubscriptionEditors') < 1) {
                    $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div').removeClass('willStay');
                }
            }
        },

        deleteSpecificSubscriptionEditors: function(elem) {
            if (elem.checked) {
                $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
            } else {
                $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
            }
        },


        checkCheckBoxesOfProperties: function(elem) {
            var $input = $( elem );
            var checkBoxID = $input.attr("id");
            if (checkBoxID.includes("object.take")) {

                if ($input.prop( "checked" ) == true) {
                    $(".table tr td[name='object.take"+$input.attr("value")+".source'] div").addClass("willStay");
                    $(".table tr td[name='object.take"+$input.attr("value")+".target'] div").addClass("willBeReplaced");
                } else {
                    $(".table tr td[name='object.take"+$input.attr("value")+".source'] div").removeClass("willStay");
                    $(".table tr td[name='object.take"+$input.attr("value")+".target'] div").removeClass("willBeReplaced");
                }
            }
            if (checkBoxID.includes("object.delete")) {

                if ($input.prop( "checked" ) == true) {
                    $(".table tr td[name='object.take"+$input.attr("value")+".target'] div").addClass("willBeReplacedStrong");
                } else {
                    $(".table tr td[name='object.take"+$input.attr("value")+".target'] div").removeClass("willBeReplacedStrong");
                }
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


