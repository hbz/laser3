<%@ page import="de.laser.addressbook.PersonRole; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.workflow.WfChecklist; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.remote.Wekb" %>
<laser:serviceInjection />

<table class="ui compact table">
    <g:each in="${vendorRoles}" var="role">
        <g:set var="cssId" value="prsLinksModal-${role.vendor.id}" />
        <tr>
            <td>
                <span class="la-flexbox la-minor-object">
                    <i class="${Icon.VENDOR} la-list-icon la-popup-tooltip" data-content="${message(code:'vendor.label')}"></i>

                    <g:link controller="vendor" action="show" id="${role.vendor.id}">
                        ${role.vendor.name}
                    </g:link>
                </span>
            </td>

            <td class="right aligned eight wide column">
                <g:if test="${editmode}">
                    <g:if test="${roleObject.showUIShareButton()}">
                        <g:if test="${role.isShared}">
                                <span>
                                    <g:link id="test" class="${Btn.MODERN.POSITIVE_TOOLTIP} la-selectable-button"
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:genericOIDService.getOID(roleObject), sharedObject:genericOIDService.getOID(role), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]}"
                                            data-position="top right" data-content="${message(code:'property.share.tooltip.on')}"
                                    >
                                        <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <span>
                                    <g:link class="${Btn.MODERN.SIMPLE_TOOLTIP} la-selectable-button"
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:genericOIDService.getOID(roleObject), sharedObject:genericOIDService.getOID(role), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]}"
                                             data-position="top right" data-content="${message(code:'property.share.tooltip.off')}"
                                    >
                                        <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>
                        <g:if test="${! role.isShared && ! role.sharedFrom}">
                            <span class="la-popup-tooltip" data-content="${message(code:'subscription.details.unlinkProviderAgency')}">
                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button" controller="ajax" action="delVendorRole" id="${role.id}"
                                    data-confirm-tokenMsg = "${message(code:'confirm.dialog.unlink.provider-agency.subscription')}"
                                    data-confirm-term-how = "unlink"
                                    role="button"
                                    aria-label="${message(code:'ariaLabel.unlink.provider-agency.subscription')}">
                                    <i class="${Icon.CMD.UNLINK}"></i>
                                </g:link>
                            </span>
                        </g:if>

                        <g:if test="${!role.isShared && role.sharedFrom}">
                            <span class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.sharedFrom')}">
                                <i class="${Icon.SIG.SHARED_OBJECT_ON} grey"></i>
                            </span>
                        </g:if>

                        <g:if test="${showPersons}">
                            <button class="${Btn.MODERN.SIMPLE_TOOLTIP} la-selectable-button" data-ui="modal" data-href="#${cssId}" data-content="${message(code:'subscription.details.addNewContact')}">
                                <i class="${Icon.CMD.ADD}"></i>
                            </button>

                            <laser:render template="/templates/links/linksAsListAddPrsModal"
                                  model="[cssId: cssId,
                                          relation: role,
                                          roleObject: roleObject,
                                          parent: genericOIDService.getOID(roleObject),
                                          role: modalPrsLinkRole
                                  ]"/>
                        </g:if>
                    </g:if>
                </td>

            </tr>

        <g:if test="${workflowService.hasREAD() && WfChecklist.getAllChecklistsByOwnerAndObjAndStatus(contextService.getOrg(), role.vendor, RDStore.WF_WORKFLOW_STATUS_OPEN)}">
            <tr>
                <td colspan="2">
                    <span class="la-flexbox la-minor-object">
                        <i class="${Icon.UI.WARNING} circle orange large" style="margin:.1rem .5rem 0 0"></i>
                        <g:link controller="vendor" action="workflows" id="${role.vendor.id}">${message(code:'workflow.vendor.someMore.info')}</g:link>
                    </span>
                </td>
            </tr>
        </g:if>

            <g:if test="${showPersons && (Person.getPublicByOrgAndFunc(role.vendor, 'General contact person') || (Person.getPublicByOrgAndFunc(role.vendor, 'Technical Support')) || (Person.getPublicByOrgAndFunc(role.vendor, 'Service Support')) || (Person.getPublicByOrgAndFunc(role.vendor, 'Metadata Contact')) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'General contact person') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Technical Support') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Service Support') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Metadata Contact') ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.vendor, roleObject, roleRespValue))}">
                <tr>
                    <td colspan="3" style="padding-bottom:0;">
                        <%-- public --%>
                        <g:if test="${ Person.getPublicByOrgAndFunc(role.vendor, 'General contact person') ||
                                Person.getPublicByOrgAndFunc(role.vendor, 'Technical Support') ||
                                Person.getPublicByOrgAndFunc(role.vendor, 'Service Support') ||
                                Person.getPublicByOrgAndFunc(role.vendor, 'Metadata Contact') ||
                                Person.getPublicByOrgAndObjectResp(role.vendor, roleObject, roleRespValue)  }">
                            <div class="ui segment la-timeLineSegment-contact">
                                <div class="la-timeLineGrid">
                                    <div class="ui grid">
                                        <g:each in="${Person.getPublicByOrgAndFunc(role.vendor, 'General contact person')}" var="func">
                                            <div class="row">
                                                <div class="two wide column">
                                                    <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                </div>
                                                <div class="thirteen wide column">
                                                    <div class="ui label">
                                                        ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${func}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentType(
                                                    func,
                                                    RDStore.CCT_EMAIL
                                            )}" var="contact">
                                                        <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}" />
                                                    </g:each>
                                                </div>
                                            </div>
                                        </g:each>
                                        <%--<g:if test="${roleObject instanceof de.laser.wekb.Package}">--%>
                                        <%
                                            Set<Person> techSupports = [], serviceSupports = [], metadataContacts = []
                                            boolean contactsExWekb = false
                                            if(role.vendor.gokbId)
                                                contactsExWekb = true
                                            techSupports.addAll(Person.getPublicByOrgAndFunc(role.vendor, 'Technical Support'))
                                            serviceSupports.addAll(Person.getPublicByOrgAndFunc(role.vendor, 'Service Support'))
                                            metadataContacts.addAll(Person.getPublicByOrgAndFunc(role.vendor, 'Metadata Contact'))
                                        %>
                                            <g:each in="${techSupports}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <g:if test="${contactsExWekb}">
                                                            <a target="_blank" href="${Wekb.getURL() + '/public/orgContent/' + role.vendor.gokbId}"><i class="${Icon.WEKB} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                        </g:else>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui label">
                                                            ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/addressbook/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                            <g:each in="${serviceSupports}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <g:if test="${contactsExWekb}">
                                                            <a target="_blank" href="${Wekb.getURL() + '/public/orgContent/' + role.vendor.gokbId}"><i class="${Icon.WEKB} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                        </g:else>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui label">
                                                            ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/addressbook/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                            <g:each in="${metadataContacts}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <g:if test="${contactsExWekb}">
                                                            <a target="_blank" href="${Wekb.getURL() + '/public/orgContent/' + role.vendor.gokbId}"><i class="${Icon.WEKB} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                        </g:else>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui label">
                                                            ${(RefdataValue.getByValueAndCategory('Metadata Contact', RDConstants.PERSON_FUNCTION)).getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentType(
                                                                func,
                                                                RDStore.CCT_EMAIL
                                                        )}" var="contact">
                                                            <laser:render template="/addressbook/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                        <%--</g:if>--%>
                                        <g:each in="${Person.getPublicByOrgAndObjectResp(role.vendor, roleObject, roleRespValue)}" var="resp">
                                            <div class="row">
                                                <div class="two wide column">
                                                    <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                </div>
                                                <div class="thirteen wide column">
                                                    <div class="ui label">
                                                        ${(RefdataValue.getByValue(roleRespValue)).getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${resp}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                            resp,
                                                            [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                    )}" var="contact">
                                                        <laser:render template="/addressbook/contact" model="${[
                                                                contact             : contact,
                                                                tmplShowDeleteButton: false,
                                                                overwriteEditable   : false
                                                        ]}" />
                                                    </g:each>
                                                </div>
                                                <g:if test="${editmode}">
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.vendor, roleRespValue)}" />
                                                    <div class="two wide column">
                                                        <div class="ui buttons">
                                                            <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button" controller="addressbook" action="deletePersonRole" id="${prsRole.id}"
                                                                    data-confirm-tokenMsg = "${message(code:'template.orgLinks.delete.warn')}"
                                                                    data-confirm-how = "unlink">
                                                                <i class="${Icon.CMD.UNLINK}"></i>
                                                            </g:link>
                                                        </div>
                                                    </div>
                                                </g:if>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                        <%-- public --%>

                        <%-- private --%>
                        <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'General contact person') ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Technical Support') ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Service Support') ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Metadata Contact') ||
                                Person.getPrivateByOrgAndObjectRespFromAddressbook(role.vendor, roleObject, roleRespValue)}">
                            <div class="ui segment la-timeLineSegment-contact">
                                <div class="la-timeLineGrid">
                                    <div class="ui grid">
                                        <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'General contact person')}" var="func">
                                            <div class="row">
                                                <div class="two wide column">
                                                    <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                </div>
                                                <div class="thirteen wide column">
                                                    <div class="ui label">
                                                        ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${func}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                            func,
                                                            [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                    )}" var="contact">
                                                        <laser:render template="/addressbook/contact" model="${[
                                                                contact             : contact,
                                                                tmplShowDeleteButton: false,
                                                                overwriteEditable   : false
                                                        ]}" />
                                                    </g:each>
                                                </div>
                                            </div>
                                        </g:each>
                                        <%--<g:if test="${roleObject instanceof de.laser.wekb.Package}">--%>
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Technical Support')}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui label">
                                                            ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/addressbook/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Service Support')}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui label">
                                                            ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/addressbook/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Metadata Contact')}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui label">
                                                            ${(RefdataValue.getByValueAndCategory('Metadata Contact', RDConstants.PERSON_FUNCTION)).getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/addressbook/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                        <%--</g:if>--%>
                                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.vendor, roleObject, roleRespValue)}" var="resp">
                                            <div class="row">
                                               <div class="two wide column">
                                                    <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}" ></i>
                                               </div>
                                                <div class="twelve wide column">
                                                    <div class="ui label">
                                                        ${(RefdataValue.getByValue(roleRespValue)).getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${resp}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                            resp,
                                                            [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                    )}" var="contact">
                                                        <laser:render template="/addressbook/contact" model="${[
                                                                contact             : contact,
                                                                tmplShowDeleteButton: false,
                                                                overwriteEditable   : false
                                                        ]}" />
                                                    </g:each>
                                                </div>
                                                <g:if test="${editmode}">
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.vendor, roleRespValue)}" />
                                                    <div class="two wide column">
                                                        <div class="ui buttons">
                                                            <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button" controller="addressbook" action="deletePersonRole" id="${prsRole.id}"
                                                                    data-confirm-tokenMsg = "${message(code:'template.orgLinks.delete.warn')}"
                                                                    data-confirm-how = "unlink">
                                                                <i class="${Icon.CMD.UNLINK}"></i>
                                                            </g:link>
                                                        </div>
                                                    </div>
                                                </g:if>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </g:if>
                        <%-- private --%>
                    </td>
                </tr>
            </g:if>
    </g:each>
</table>



