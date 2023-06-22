<%@ page import="de.laser.CustomerTypeService; de.laser.Subscription; de.laser.License; de.laser.PersonRole; de.laser.Person; de.laser.SubscriptionController; de.laser.storage.RDStore; de.laser.AuditConfig; de.laser.RefdataValue; de.laser.FormService;" %>
<laser:serviceInjection/>

<g:set var="copyElementsService" bean="copyElementsService"/>

<ui:greySegment>

    <g:if test="${!fromSurvey && !isRenewSub && !copyObject}">
        <laser:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject          : sourceObject,
                targetObject          : targetObject,
                allObjects_readRights : allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form action="${actionName}" controller="${controllerName}" id="${params.id}" data-confirm-id="copyElements_form"
            params="[workFlowPart: workFlowPart, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey, copyObject: copyObject]"
            method="post" class="ui form newLicence">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <g:if test="${copyObject}">
            <div class="ui big form">
                <div class="field required">
                    <label>${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.name.label")} <g:message code="messageRequiredField" /></label>
                    <input required type="text" name="name" value="" placeholder=""/>
                </div>
            </div>
        </g:if>

        <table class="ui celled table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="six wide">
                    <g:if test="${sourceObject}"><g:link
                            controller="${sourceObject.getClass().getSimpleName().toLowerCase()}" action="show"
                            id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if>
                </th>
                <g:if test="${isConsortialObjects}">
                    <th class="center aligned">
                        <g:if test="${isConsortialObjects}">
                            <g:message code="copyElementsIntoObject.share"/> / <g:message
                                code="copyElementsIntoObject.audit"/>
                        </g:if>
                        <g:else>
                            <g:message code="copyElementsIntoObject.share"/>
                        </g:else>
                    </th>
                </g:if>
            <th class="one wide center aligned" data-label="${message(code:'responsive.table.selectElement')}"><input type="checkbox"
                                                       data-action="copy" onClick="JSPC.app.toggleAllCheckboxes(this)"
                                                       checked/>
                <g:if test="${!copyObject}">
                    <th class="six wide">
                        <g:if test="${targetObject}"><g:link
                                controller="${targetObject.getClass().getSimpleName().toLowerCase()}" action="show"
                                id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link></g:if>
                    </th>
                    <th class="one wide center aligned" data-label="${message(code:'responsive.table.selectElement')}">
                        <g:if test="${targetObject}">
                            <input class="setDeletionConfirm" type="checkbox" data-action="delete" onClick="JSPC.app.toggleAllCheckboxes(this)"/>
                        </g:if>
                    </th>
                </g:if>
            </tr>
            </thead>
            <tbody>


            <g:each in="${copyElementsService.allowedProperties(sourceObject)}" var="objProperty">
                <tr>
                    <td name="copyObject.take${objProperty}.source">
                        <div>
                            <strong><ui:propertyIcon object="${sourceObject}" propertyName="${objProperty}"
                                                   showToolTipp="true"/> ${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.${objProperty}.label")}:</strong>
                            <ui:showPropertyValue property="${sourceObject."${objProperty}"}"/>
                        </div>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">

                                <div class="ui checkbox la-toggle-radio la-inherit">
                                    <g:checkBox name="copyObject.toggleAudit" value="${objProperty}"
                                                checked="${AuditConfig.getConfig(sourceObject, objProperty) ? 'true' : 'false'}"/>
                                </div>

                        </td>
                    </g:if>
                %{--AKTIONEN:--}%
                    <td class="center aligned">
                        <g:if test="${sourceObject.hasProperty("${objProperty}") && !isRenewSub}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="copyObject.take" id="copyObject.take${objProperty}"
                                            value="${objProperty}" data-action="copy"
                                            checked="${true}"/>
                            </div>
                        </g:if>
                    </td>
                    <g:if test="${!copyObject}">
                        <td name="copyObject.take${objProperty}.target">
                            <g:if test="${targetObject}">
                                <div>
                                    <strong><ui:propertyIcon object="${targetObject}" propertyName="${objProperty}"
                                                           showToolTipp="true"/> ${message(code: "${targetObject.getClass().getSimpleName().toLowerCase()}.${objProperty}.label")}:</strong>
                                    <ui:showPropertyValue property="${targetObject."${objProperty}"}"/>

                                    <g:if test="${isConsortialObjects}">
                                        <div class="right aligned wide column">
                                            <%
                                                if (AuditConfig.getConfig(targetObject, objProperty)) {
                                                    if (targetObject.isSlaved) {
                                                        println '<span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
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
                            <g:if test="${targetObject.hasProperty("${objProperty}") && !isRenewSub}">
                                <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                    <g:checkBox name="copyObject.delete" id="copyObject.delete${objProperty}"
                                                value="${objProperty}" data-action="delete"/>
                                </div>
                            </g:if>
                        </td>
                    </g:if>
                </tr>
            </g:each>

            <g:if test="${copyObject && contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC) && sourceObject.hasProperty("instanceOf") && sourceObject instanceof Subscription}">
                <tr>
                    <td>
                        <div>
                            <strong><i class="clipboard icon"></i>${message(code: 'subscription.linktoSubscription')}:</strong>
                            <g:if test="${sourceObject.instanceOf}">
                                <g:link controller="subscription" action="show" target="_blank"
                                        id="${sourceObject.instanceOf.id}">${sourceObject.instanceOf}</g:link>
                            </g:if>
                            <g:else>
                                ${message(code: 'subscription.linktoSubscriptionEmpty')}
                            </g:else>
                        </div>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                        </td>
                    </g:if>
                %{--AKTIONEN:--}%
                    <td class="center aligned">
                        <div class="ui checkbox la-toggle-radio la-replace">
                            <g:checkBox name="copyObject.copylinktoSubscription" data-action="copy"
                                        checked="${true}"/>
                        </div>
                    </td>
                </tr>
            </g:if>

            <g:if test="${copyObject && contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC) && sourceObject.hasProperty("instanceOf") && sourceObject instanceof License}">
                <tr>
                    <td>
                        <div>
                            <strong><i class="clipboard icon"></i>${message(code: 'license.linktoLicense')}:</strong>
                            <g:if test="${sourceObject.instanceOf}">
                                <g:link controller="license" action="show" target="_blank"
                                        id="${sourceObject.instanceOf.id}">${sourceObject.instanceOf}</g:link>
                            </g:if>
                            <g:else>
                                ${message(code: 'license.linktoLicenseEmpty')}
                            </g:else>
                        </div>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                        </td>
                    </g:if>
                %{--AKTIONEN:--}%
                    <td class="center aligned">
                        <div class="ui checkbox la-toggle-radio la-replace">
                            <g:checkBox name="copyObject.copylinktoLicense" data-action="copy"
                                        checked="${true}"/>
                        </div>
                    </td>
                </tr>
            </g:if>

            <g:if test="${sourceLicenses}">
                <tr>
                    <td name="copyObject.takeLicenses.source">
                        <div>
                            <strong><i class="balance scale icon"></i>${message(code: 'license.label')}:</strong>
                            <g:each in="${sourceLicenses}" var="license">
                                <g:link controller="license" action="show" target="_blank" id="${license.id}">
                                    <div data-oid="${genericOIDService.getOID(license)}" class="la-multi-sources">
                                        <strong><i class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:
                                        </strong>
                                        ${license.reference}
                                        <br />
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
                                <g:checkBox name="copyObject.takeLicenses" data-action="copy"
                                            value="${genericOIDService.getOID(license)}" checked="${true}"/>
                            </div>
                        </g:each>
                    </td>
                    <g:if test="${!copyObject}">
                        <td name="copyObject.takeLicenses.target">
                            <div>
                                <strong><i class="balance scale icon"></i>${message(code: 'license.label')}:</strong>
                                <g:each in="${targetLicenses}" var="license">
                                    <div data-oid="${genericOIDService.getOID(license)}">
                                        <strong><i class="balance scale icon"></i>&nbsp${license.licenseCategory?.getI10n("value")}:
                                        </strong>
                                        <g:link controller="license" action="show" target="_blank" id="${license.id}">
                                            ${license.reference}
                                        </g:link>
                                        <br />
                                    </div>
                                </g:each>
                            </div>
                        </td>

                        <td>
                            <g:each in="${targetLicenses}" var="license">
                                <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                    <g:checkBox name="copyObject.deleteLicenses" data-action="delete"
                                                value="${genericOIDService.getOID(license)}" checked="${false}"/>
                                </div>
                            </g:each>
                        </td>
                    </g:if>
                </tr>
            </g:if>
            <g:if test="${sourceObject.hasProperty("orgRelations")}">
                <tr>
                    <td name="copyObject.takeOrgRelations.source">
                        <div>
                <g:if test="${!source_visibleOrgRelations}">
                    <strong><i class="university icon"></i>&nbsp${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.organisations.label")}:
                    </strong>
                </g:if>
                <g:each in="${source_visibleOrgRelations}" var="source_role">
                    <g:if test="${source_role.org}">
                        <div data-oid="${genericOIDService.getOID(source_role)}" class="la-multi-sources">
                            <strong><i class="university icon"></i>&nbsp${source_role.roleType.getI10n("value")}:</strong>
                            <g:link controller="organisation" action="show" target="_blank"
                                    id="${source_role.org.id}">
                                ${source_role.org.name}
                            </g:link>

                            <br />
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
                                <br />
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
                                <g:checkBox name="copyObject.takeOrgRelations" data-action="copy"
                                            value="${genericOIDService.getOID(source_role)}" checked="${true}"/>
                            </div>
                            <br />
                        </g:if>
                    </g:each>
                </td>
                <g:if test="${!copyObject}">
                    <td name="copyObject.takeOrgRelations.target">
                        <div>
                            <g:if test="${!target_visibleOrgRelations}">
                                <strong><i class="university icon"></i>&nbsp${message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.organisations.label")}:
                                </strong>
                            </g:if>
                            <g:each in="${target_visibleOrgRelations}" var="target_role">
                                <g:if test="${target_role.org}">
                                    <div data-oid="${genericOIDService.getOID(target_role)}">
                                        <strong><i class="university icon"></i>&nbsp${target_role.roleType.getI10n("value")}:
                                        </strong>
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
                                    </div>
                                    <br />
                                </g:if>
                            </g:each>
                        </div>
                    </td>
                    <td>
                        <g:each in="${target_visibleOrgRelations}" var="target_role">
                            <g:if test="${target_role.org}">
                                <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                    <g:checkBox name="copyObject.deleteOrgRelations" data-action="delete"
                                                value="${genericOIDService.getOID(target_role)}" checked="${false}"/>
                                </div>
                                <br />
                            </g:if>
                        </g:each>
                    </td>
                </g:if>
                </tr>
            </g:if>

            <g:if test="${sourceObject instanceof Subscription}">
                <tr>
                    <td name="subscription.takeSpecificSubscriptionEditors.source">
                        <div>
                            <strong>
                                <i class="address card icon"></i>
                                ${message(code: 'subscription.specificSubscriptionEditors')}:
                            </strong>
                            <g:each in="${source_visibleOrgRelations}" var="source_role">
                                <g:if test="${source_role.org}">
                                    <g:if test="${Person.getPublicByOrgAndObjectResp(source_role.org, sourceObject, 'Specific subscription editor') ||
                                            Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}">

                                    <%-- public --%>
                                        <g:each in="${Person.getPublicByOrgAndObjectResp(source_role.org, sourceObject, 'Specific subscription editor')}"
                                                var="resp">

                                            <div data-oid="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                                 class="la-multi-sources">
                                                <span class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'address.public')}"
                                                      data-position="top right">
                                                    <i class="address card icon"></i>
                                                </span>
                                                ${resp}
                                                (<strong><i
                                                    class="university icon"></i>&nbsp${source_role.roleType.getI10n("value")}:
                                            </strong>
                                                <g:link controller="organisation" action="show" target="_blank"
                                                        id="${source_role.org.id}">${source_role.org.name}</g:link>)
                                            </div>
                                            <br />
                                        </g:each>
                                    <%-- public --%>
                                    <%-- private --%>
                                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}"
                                                var="resp">
                                            <div data-oid="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                                 class="la-multi-sources">
                                                <span class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'address.private')}"
                                                      data-position="top right">
                                                    <i class="address card outline icon"></i>
                                                </span>
                                                ${resp}
                                                (<strong><i
                                                    class="university icon"></i>&nbsp${source_role.roleType.getI10n("value")}:
                                            </strong>
                                                <g:link controller="organisation" action="show" target="_blank"
                                                        id="${source_role.org.id}">${source_role.org.name}</g:link>)
                                            </div>
                                            <br />
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
                                                        value="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                                        checked="${true}"/>
                                        </div>
                                        <br />
                                    </g:each>
                                </g:if><%-- public --%>
                            <%-- private --%>
                                <g:if test="${Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}">
                                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(source_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}"
                                            var="resp">
                                        <div class="ui checkbox la-toggle-radio la-replace">
                                            <g:checkBox name="subscription.takeSpecificSubscriptionEditors"
                                                        data-action="copy"
                                                        value="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, source_role.org, 'Specific subscription editor'))}"
                                                        checked="${true}"/>
                                        </div>
                                        <br />
                                    </g:each>
                                </g:if><%-- private --%>
                            </g:if>
                        </g:each>
                    </td>
                    <g:if test="${!copyObject}">
                        <td name="subscription.takeSpecificSubscriptionEditors.target">
                            <div>
                                <strong>
                                    <i class="address card icon"></i>
                                    ${message(code: 'subscription.specificSubscriptionEditors')}:
                                </strong>
                                <g:each in="${target_visibleOrgRelations}" var="target_role">
                                    <g:if test="${target_role.org}">
                                        <g:if test="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, targetObject, 'Specific subscription editor', contextService.getOrg()) ||
                                                Person.getPublicByOrgAndObjectResp(target_role.org, targetObject, 'Specific subscription editor')}">
                                        <%-- public --%>
                                            <g:each in="${Person.getPublicByOrgAndObjectResp(target_role.org, targetObject, 'Specific subscription editor')}"
                                                    var="resp">

                                                <div data-oid="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                                     class="la-multi-sources">
                                                    <span class="la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'address.public')}"
                                                          data-position="top right">
                                                        <i class="address card icon"></i>
                                                    </span>
                                                    ${resp}
                                                    (<strong><i
                                                        class="university icon"></i>&nbsp${target_role.roleType.getI10n("value")}:
                                                </strong>
                                                    <g:link controller="organisation" action="show" target="_blank"
                                                            id="${target_role.org.id}">${target_role.org.name}</g:link>)
                                                </div>
                                                <br />
                                            </g:each>
                                        <%-- public --%>
                                        <%-- private --%>

                                            <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, targetObject, 'Specific subscription editor', contextService.getOrg())}"
                                                    var="resp">
                                                <div data-oid="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                                     class="la-multi-sources">
                                                    <span class="la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'address.private')}"
                                                          data-position="top right">
                                                        <i class="address card outline icon"></i>
                                                    </span>
                                                    ${resp}
                                                    (<strong><i
                                                        class="university icon"></i>&nbsp${target_role.roleType.getI10n("value")}:
                                                </strong>
                                                    <g:link controller="organisation" action="show" target="_blank"
                                                            id="${target_role.org.id}">${target_role.org.name}</g:link>)
                                                </div>
                                                <br />
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
                                            <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                                <g:checkBox name="subscription.deleteSpecificSubscriptionEditors"
                                                            data-action="delete"
                                                            value="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                                            checked="${false}"/>
                                            </div>
                                            <br />
                                        </g:each>
                                    </g:if><%-- public --%>
                                <%-- private --%>
                                    <g:if test="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}">
                                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(target_role.org, sourceObject, 'Specific subscription editor', contextService.getOrg())}"
                                                var="resp">
                                            <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                                <g:checkBox name="subscription.deleteSpecificSubscriptionEditors"
                                                            data-action="delete"
                                                            value="${genericOIDService.getOID(PersonRole.getByPersonAndOrgAndRespValue(resp, target_role.org, 'Specific subscription editor'))}"
                                                            checked="${false}"/>
                                            </div>
                                            <br />
                                        </g:each>
                                    </g:if><%-- private --%>

                                </g:if>
                            </g:each>
                        </td>
                    </g:if>
                </tr>
            </g:if>

            <g:if test="${sourceObject.hasProperty("ids")}">
                <tr>
                    <td name="copyObject.takeIdentifier.source">
                        <div>
                            <strong><i class="barcode icon"></i>&nbsp${message(code: 'default.identifiers.label')}:</strong><br />
                            <g:each in="${sourceIdentifiers}" var="ident">
                                <div data-oid="${genericOIDService.getOID(ident)}">
                                <strong>${ident.ns.ns}:</strong>&nbsp${ident.value}<br />
                                </div>
                            </g:each>
                        </div>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                            <div class="ui checkbox la-toggle-radio la-inherit">
                                <g:each in="${sourceIdentifiers}" var="ident">
                                    <g:checkBox name="copyObject.toggleAudit" value="${genericOIDService.getOID(ident)}"
                                                checked="${AuditConfig.getConfig(ident) ? 'true' : 'false'}"/>
                                </g:each>
                            </div>
                        </td>
                    </g:if>

                %{--COPY:--}%
                    <td class="center aligned">
                        <g:each in="${sourceIdentifiers}" var="ident">
                            <div class="la-element">
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="copyObject.takeIdentifierIds" value="${genericOIDService.getOID(ident)}"
                                                data-action="copy"/>
                                </div>
                            </div>
                            <br />
                        </g:each>
                    </td>
                    <g:if test="${!copyObject}">
                        <td name="copyObject.takeIdentifier.target">
                            <div>
                            <strong><i class="barcode icon"></i>&nbsp${message(code: 'default.identifiers.label')}:</strong><br />
                            <g:each in="${targetIdentifiers}" var="ident">
                                <div data-oid="${genericOIDService.getOID(ident)}">
                                    <strong>${ident.ns.ns}:</strong>&nbsp${ident.value}<br />
                                </div>
                                <%
                                    if (AuditConfig.getConfig(ident)) {
                                        println '<span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                    }
                                %>
                            </g:each>
                            </div>
                        </td>
                    %{--DELETE:--}%
                        <td>
                            <g:each in="${targetIdentifiers}" var="ident">
                                <div class="la-element">
                                    <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                        <g:checkBox name="copyObject.deleteIdentifierIds" value="${genericOIDService.getOID(ident)}"
                                                    data-action="delete" checked="${false}"/>
                                    </div>
                                </div>
                                <br />
                            </g:each>
                        </td>
                    </g:if>
                </tr>
            </g:if>
            <g:if test="${sourceLinks}">
                <tr>
                    <td name="copyObject.takeLink.source">
                        <div>
                            <strong><i class="exchange icon"></i>&nbsp
                                <g:if test="${sourceObject instanceof Subscription}">${message(code: 'subscription.linkedObjects')}</g:if>
                                <g:elseif test="${sourceObject instanceof License}">${message(code: 'license.linkedObjects')}</g:elseif>:
                            </strong><br />
                            <g:each in="${sourceLinks}" var="link">
                                <%
                                    int perspectiveIndex
                                    if(sourceObject in [link.sourceSubscription, link.sourceLicense])
                                        perspectiveIndex = 0
                                    else if(sourceObject in [link.destinationSubscription, link.destinationLicense])
                                        perspectiveIndex = 1
                                %>
                                <div data-oid="${genericOIDService.getOID(link)}">
                                    <strong>${link.linkType.getI10n("value").split("\\|")[perspectiveIndex].replace('...','')}:</strong>&nbsp${link.getOther(sourceObject).dropdownNamingConvention()}<br />
                                </div>
                            </g:each>
                        </div>
                    </td>
                    <g:if test="${isConsortialObjects}">
                        <td class="center aligned">
                        </td>
                    </g:if>

                %{--COPY:--}%
                    <td class="center aligned">
                        <g:each in="${sourceLinks}" var="link">
                            <div class="la-element">
                                <div class="ui checkbox la-toggle-radio la-replace">
                                    <g:checkBox name="copyObject.takeLinks" value="${genericOIDService.getOID(link)}"
                                                data-action="copy"/>
                                </div>
                            </div>
                            <br />
                        </g:each>
                    </td>
                    <g:if test="${!copyObject}">
                        <td name="copyObject.takeLink.target">
                            <div>
                                <strong><i class="exchange icon"></i>&nbsp
                                    <g:if test="${sourceObject instanceof Subscription}">${message(code: 'subscription.linkedObjects')}</g:if>
                                    <g:elseif test="${sourceObject instanceof License}">${message(code: 'license.linkedObjects')}</g:elseif>:
                                </strong><br />
                                <g:each in="${targetLinks}" var="link">
                                    <%
                                        //perspectiveIndex already defined for source links
                                        if(targetObject in [link.sourceSubscription, link.sourceLicense])
                                            perspectiveIndex = 0
                                        else if(targetObject in [link.destinationSubscription, link.destinationLicense])
                                            perspectiveIndex = 1
                                    %>
                                    <div data-oid="${genericOIDService.getOID(link)}">
                                        <strong>${link.linkType.getI10n("value").split("\\|")[perspectiveIndex].replace('...','')}:</strong>&nbsp${link.getOther(targetObject).dropdownNamingConvention()}<br />
                                    </div>
                                </g:each>
                            </div>
                        </td>
                    %{--DELETE:--}%
                        <td>
                            <g:each in="${targetLinks}" var="link">
                                <div class="la-element">
                                    <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                        <g:checkBox name="copyObject.deleteLinks" value="${genericOIDService.getOID(link)}"
                                                    data-action="delete" checked="${false}"/>
                                    </div>
                                </div>
                                <br />
                            </g:each>
                        </td>
                    </g:if>
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
                    <g:link controller="survey" action="renewalEvaluation" id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]" class="ui button js-click-control">
                        <g:message code="renewalEvaluation.back"/>
                    </g:link>
                </div>

                <div class="eight wide field" style="text-align: right;">
                    <g:set var="submitDisabled"
                           value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>
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
                <g:set var="submitDisabled" value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>
                <input type="submit" id="copyElementsSubmit" class="ui button js-click-control" value="${submitButtonText}"
                       data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete" ${submitDisabled}/>
            </div>
        </g:else>
    </g:form>
</ui:greySegment>

<g:if test="${!copyObject}">
    <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.subCopyController = {

            checkboxes: {
                $takeLicenses: $('input:checkbox[name="copyObject.takeLicenses"]'),
                $deleteLicenses: $('input:checkbox[name="copyObject.deleteLicenses"]'),
                $takeOrgRelations: $('input:checkbox[name="copyObject.takeOrgRelations"]'),
                $deleteOrgRelations: $('input:checkbox[name="copyObject.deleteOrgRelations"]'),
                $takeSpecificSubscriptionEditors: $('input:checkbox[name="subscription.takeSpecificSubscriptionEditors"]'),
                $deleteSpecificSubscriptionEditors: $('input:checkbox[name="subscription.deleteSpecificSubscriptionEditors"]'),
                $takeIdentifier: $('input:checkbox[name="copyObject.takeIdentifierIds"]'),
                $deleteIdentifier: $('input:checkbox[name="copyObject.deleteIdentifierIds"]'),
                $takeLinks: $('input:checkbox[name="copyObject.takeLinks"]'),
                $deleteLinks: $('input:checkbox[name="copyObject.deleteLinks"]')
            },

            init: function () {
                var ref = JSPC.app.subCopyController.checkboxes

                ref.$takeLicenses.change(function (event) {
                    JSPC.app.subCopyController.takeLicenses(this);
                }).trigger('change');

                ref.$deleteLicenses.change(function (event) {
                    JSPC.app.subCopyController.deleteLicenses(this);
                }).trigger('change');

                ref.$takeOrgRelations.change(function (event) {
                    JSPC.app.subCopyController.takeOrgRelations(this);
                }).trigger('change');

                ref.$deleteOrgRelations.change(function (event) {
                    JSPC.app.subCopyController.deleteOrgRelations(this);
                }).trigger('change');

                ref.$takeSpecificSubscriptionEditors.change(function (event) {
                    JSPC.app.subCopyController.takeSpecificSubscriptionEditors(this);
                }).trigger('change');

                ref.$deleteSpecificSubscriptionEditors.change(function (event) {
                    JSPC.app.subCopyController.deleteSpecificSubscriptionEditors(this);
                }).trigger('change');

                ref.$takeSpecificSubscriptionEditors.change(function (event) {
                    JSPC.app.subCopyController.takeSpecificSubscriptionEditors(this);
                }).trigger('change');

                ref.$deleteSpecificSubscriptionEditors.change(function (event) {
                    JSPC.app.subCopyController.deleteSpecificSubscriptionEditors(this);
                }).trigger('change');

                ref.$takeIdentifier.change(function (event) {
                    JSPC.app.subCopyController.takeIdentifier(this);
                }).trigger('change');

                ref.$deleteIdentifier.change(function (event) {
                    JSPC.app.subCopyController.deleteIdentifier(this);
                }).trigger('change');

                ref.$takeLinks.change(function (event) {
                    JSPC.app.subCopyController.takeLinks(this);
                }).trigger('change');

                ref.$deleteLinks.change(function (event) {
                    JSPC.app.subCopyController.deleteLinks(this);
                }).trigger('change');

                $("input:checkbox[name^='copyObject']").change(function () {
                    JSPC.app.subCopyController.checkCheckBoxesOfProperties(this);
                }).trigger('change');

                console.log($('[data-action="delete"]:checked'));
                if($('[data-action="delete"]:checked').length > 0){
                    $("#copyElementsSubmit").addClass("js-open-confirm-modal");
                    r2d2.initDynamicUiStuff('form');
                }
            },

            takeLicenses: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeLicenses.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="copyObject.takeLicenses.target"] div div').addClass('willStay');
                } else {
                    $('.table tr td[name="copyObject.takeLicenses.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.takeLicenses') < 1) {
                        $('.table tr td[name="copyObject.takeLicenses.target"] div div').removeClass('willStay');
                    }
                }
            },

            deleteLicenses: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeLicenses.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="copyObject.takeLicenses.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
                }
            },

            takeOrgRelations: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeOrgRelations.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="copyObject.takeOrgRelations.target"] div div').addClass('willStay');
                } else {
                    $('.table tr td[name="copyObject.takeOrgRelations.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.takeOrgRelations') < 1) {
                        $('.table tr td[name="copyObject.takeOrgRelations.target"] div div').removeClass('willStay');
                    }
                }
            },

            deleteOrgRelations: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeOrgRelations.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="copyObject.takeOrgRelations.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
                }
            },

            takeSpecificSubscriptionEditors: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div').addClass('willStay');
                } else {
                    $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('subscription.takeSpecificSubscriptionEditors') < 1) {
                        $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div').removeClass('willStay');
                    }
                }
            },

            deleteSpecificSubscriptionEditors: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="subscription.takeSpecificSubscriptionEditors.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
                }
            },

            takeIdentifier: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeIdentifier.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="copyObject.takeIdentifier.target"] div div').addClass('willStay');
                } else {
                    $('.table tr td[name="copyObject.takeIdentifier.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.takeIdentifierIds') < 1) {
                        $('.table tr td[name="copyObject.takeIdentifier.target"] div div').removeClass('willStay');
                    }
                }
            },

            deleteIdentifier: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeIdentifier.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="copyObject.takeIdentifier.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
                }
            },

            takeLinks: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeLink.source"] div div[data-oid="' + elem.value + '"]').addClass('willStay');
                    $('.table tr td[name="copyObject.takeLink.target"] div div').addClass('willStay');
                } else {
                    $('.table tr td[name="copyObject.takeLink.source"] div div[data-oid="' + elem.value + '"]').removeClass('willStay');
                    if (JSPC.app.subCopyController.getNumberOfCheckedCheckboxes('copyObject.takeLinks') < 1) {
                        $('.table tr td[name="copyObject.takeLink.target"] div div').removeClass('willStay');
                    }
                }
            },

            deleteLinks: function (elem) {
                if (elem.checked) {
                    $('.table tr td[name="copyObject.takeLink.target"] div div[data-oid="' + elem.value + '"]').addClass('willBeReplacedStrong');
                } else {
                    $('.table tr td[name="copyObject.takeLink.target"] div div[data-oid="' + elem.value + '"]').removeClass('willBeReplacedStrong');
                }
            },


            checkCheckBoxesOfProperties: function (elem) {
                var $input = $(elem);
                var checkBoxID = $input.attr("id");
                if (checkBoxID.includes("copyObject.take")) {

                    if ($input.prop("checked") == true) {
                        $(".table tr td[name='copyObject.take" + $input.attr("value") + ".source'] div").addClass("willStay");
                        $(".table tr td[name='copyObject.take" + $input.attr("value") + ".target'] div").addClass("willBeReplaced");
                    } else {
                        $(".table tr td[name='copyObject.take" + $input.attr("value") + ".source'] div").removeClass("willStay");
                        $(".table tr td[name='copyObject.take" + $input.attr("value") + ".target'] div").removeClass("willBeReplaced");
                    }
                }
                if (checkBoxID.includes("copyObject.delete")) {

                    if ($input.prop("checked") == true) {
                        $(".table tr td[name='copyObject.take" + $input.attr("value") + ".target'] div").addClass("willBeReplacedStrong");
                    } else {
                        $(".table tr td[name='copyObject.take" + $input.attr("value") + ".target'] div").removeClass("willBeReplacedStrong");
                    }
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


