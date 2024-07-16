<%@ page import="de.laser.ui.Icon; de.laser.workflow.WfChecklist; de.laser.PersonRole; de.laser.RefdataValue; de.laser.Person; de.laser.Contact; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.remote.ApiSource" %>
<laser:serviceInjection />
<g:set var="wekbAPI" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>
<table class="ui compact table">
    <g:each in="${vendorRoles}" var="role">
        <g:set var="cssId" value="prsLinksModal-${role.vendor.id}" />
        <tr>
            <td>
                <span class="la-flexbox la-minor-object">
                    <i class="${Icon.VENDOR} la-list-icon la-popup-tooltip la-delay" data-content="${message(code:'vendor.label')}"></i>
                </span>
                <g:link controller="vendor" action="show" id="${role.vendor.id}">
                    ${role.vendor.name}
                </g:link>
            </td>

            <td class="right aligned eight wide column">
                <g:if test="${editmode}">
                    <g:if test="${roleObject.showUIShareButton()}">
                        <g:if test="${role.isShared}">
                                <span>
                                    <g:link id="test" class="ui icon button la-modern-button green la-selectable-button la-popup-tooltip la-delay"
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:genericOIDService.getOID(roleObject), sharedObject:genericOIDService.getOID(role), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]}"
                                            data-position="top right" data-content="${message(code:'property.share.tooltip.on')}"
                                    >
                                        <i class="la-share icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <span>
                                    <g:link class="ui icon button blue la-modern-button la-selectable-button la-popup-tooltip la-delay  "
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:genericOIDService.getOID(roleObject), sharedObject:genericOIDService.getOID(role), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]}"
                                             data-position="top right" data-content="${message(code:'property.share.tooltip.off')}"
                                    >
                                        <i class="la-share slash icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>
                        <g:if test="${! role.isShared && ! role.sharedFrom}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.unlinkProviderAgency')}">
                                <g:link class="ui negative icon button la-modern-button la-selectable-button js-open-confirm-modal" controller="ajax" action="delVendorRole" id="${role.id}"
                                    data-confirm-tokenMsg = "${message(code:'confirm.dialog.unlink.provider-agency.subscription')}"
                                    data-confirm-term-how = "unlink"
                                    role="button"
                                    aria-label="${message(code:'ariaLabel.unlink.provider-agency.subscription')}">
                                    <i class="${Icon.CMD.UNLINK}"></i>
                                </g:link>
                            </span>
                        </g:if>

                        <g:if test="${!role.isShared && role.sharedFrom}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.sharedFrom')}">
                                <i class="grey alternate share icon"></i>
                            </span>
                        </g:if>
                        <%--
                        <g:if test="${showPersons}">
                                <button class="ui icon button blue la-modern-button la-selectable-button la-popup-tooltip la-delay" data-ui="modal" data-href="#${cssId}" data-content="${message(code:'subscription.details.addNewContact')}">
                                    <i class="${Icon.CMD.ADD}"></i>
                                </button>

                        <laser:render template="/templates/links/vendorLinksAsListAddPrsModal"
                                  model="['cssId': cssId,
                                          'orgRole': role,
                                          'roleObject': roleObject,
                                          parent: genericOIDService.getOID(roleObject),
                                          role: genericOIDService.getOID(modalPrsLinkRole)
                                  ]"/>
                        </g:if>
                        --%>
                    </g:if>
                </td>

            </tr>

        <g:if test="${workflowService.hasUserPerm_read() && WfChecklist.getAllChecklistsByOwnerAndObjAndStatus(contextService.getOrg(), role.vendor, RDStore.WF_WORKFLOW_STATUS_OPEN)}">
            <tr>
                <td colspan="2">
                    <span class="la-flexbox la-minor-object">
                        <i class="icon exclamation triangle orange" style="margin:.1rem .5rem 0 0"></i>
                        <g:link controller="vendor" action="workflows" id="${role.vendor.id}">${message(code:'workflow.vendor.someMore.info')}</g:link>
                    </span>
                </td>
            </tr>
        </g:if>

            <g:if test="${showPersons && (Person.getPublicByOrgAndFunc(role.vendor, 'General contact person') || (Person.getPublicByOrgAndFunc(role.vendor, 'Technical Support')) || (Person.getPublicByOrgAndFunc(role.vendor, 'Service Support')) || (Person.getPublicByOrgAndFunc(role.vendor, 'Metadata Contact')) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'General contact person', contextOrg) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Technical Support', contextOrg) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Service Support', contextOrg) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Metadata Contact', contextOrg) ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.vendor, roleObject, roleRespValue, contextOrg))}">
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
                                                    <i class="${Icon.ADDRESS_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                </div>
                                                <div class="thirteen wide column">
                                                    <div class="ui  label">
                                                        ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${func}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentType(
                                                    func,
                                                    RDStore.CCT_EMAIL
                                            )}" var="contact">
                                                        <laser:render template="/templates/cpa/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}" />
                                                    </g:each>
                                                </div>
                                            </div>
                                        </g:each>
                                        <%--<g:if test="${roleObject instanceof de.laser.Package}">--%>
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
                                                            <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + role.vendor.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="${Icon.ADDRESS_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                        </g:else>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui  label">
                                                            ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/templates/cpa/contact" model="${[
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
                                                            <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + role.vendor.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="${Icon.ADDRESS_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                        </g:else>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui  label">
                                                            ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/templates/cpa/contact" model="${[
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
                                                            <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + role.vendor.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="${Icon.ADDRESS_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                        </g:else>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui  label">
                                                            ${(RefdataValue.getByValueAndCategory('Metadata Contact', RDConstants.PERSON_FUNCTION)).getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentType(
                                                                func,
                                                                RDStore.CCT_EMAIL
                                                        )}" var="contact">
                                                            <laser:render template="/templates/cpa/contact" model="${[
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
                                                    <i class="${Icon.ADDRESS_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                </div>
                                                <div class="thirteen wide column">
                                                    <div class="ui  label">
                                                        ${(RefdataValue.getByValue(roleRespValue)).getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${resp}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                            resp,
                                                            [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                    )}" var="contact">
                                                        <laser:render template="/templates/cpa/contact" model="${[
                                                                contact             : contact,
                                                                tmplShowDeleteButton: false,
                                                                overwriteEditable   : false
                                                        ]}" />
                                                    </g:each>
                                                </div>
                                                <g:if test="${editmode}">
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.vendor, roleRespValue)}" />
                                                    <div class="two wide column">
                                                        <div class="ui icon buttons">
                                                            <g:link class="ui negative  button la-modern-button la-selectable-button js-open-confirm-modal" controller="ajax" action="delPrsRole" id="${prsRole?.id}"
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
                        <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'General contact person', contextOrg) ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Technical Support', contextOrg) ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Service Support', contextOrg) ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Metadata Contact', contextOrg) ||
                                Person.getPrivateByOrgAndObjectRespFromAddressbook(role.vendor, roleObject, roleRespValue, contextOrg)}">
                            <div class="ui segment la-timeLineSegment-contact">
                                <div class="la-timeLineGrid">
                                    <div class="ui grid">
                                        <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'General contact person', contextOrg)}" var="func">
                                            <div class="row">
                                                <div class="two wide column">
                                                    <i class="${Icon.ADDRESS_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                </div>
                                                <div class="thirteen wide column">
                                                    <div class="ui  label">
                                                        ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${func}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                            func,
                                                            [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                    )}" var="contact">
                                                        <laser:render template="/templates/cpa/contact" model="${[
                                                                contact             : contact,
                                                                tmplShowDeleteButton: false,
                                                                overwriteEditable   : false
                                                        ]}" />
                                                    </g:each>
                                                </div>
                                            </div>
                                        </g:each>
                                        <%--<g:if test="${roleObject instanceof de.laser.Package}">--%>
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Technical Support', contextOrg)}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="${Icon.ADDRESS_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui  label">
                                                            ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/templates/cpa/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Service Support', contextOrg)}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="${Icon.ADDRESS_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui  label">
                                                            ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/templates/cpa/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.vendor, 'Metadata Contact', contextOrg)}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="${Icon.ADDRESS_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                    </div>
                                                    <div class="thirteen wide column">
                                                        <div class="ui  label">
                                                            ${(RefdataValue.getByValueAndCategory('Metadata Contact', RDConstants.PERSON_FUNCTION)).getI10n('value')}
                                                        </div>
                                                        <div class="ui header">
                                                            ${func}
                                                        </div>
                                                        <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                                func,
                                                                [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                        )}" var="contact">
                                                            <laser:render template="/templates/cpa/contact" model="${[
                                                                    contact             : contact,
                                                                    tmplShowDeleteButton: false,
                                                                    overwriteEditable   : false
                                                            ]}" />
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </g:each>
                                        <%--</g:if>--%>
                                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.vendor, roleObject, roleRespValue, contextOrg)}" var="resp">
                                            <div class="row">
                                               <div class="two wide column">
                                                    <i class="${Icon.ADDRESS_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" ></i>
                                               </div>
                                                <div class="twelve wide column">
                                                    <div class="ui  label">
                                                        ${(RefdataValue.getByValue(roleRespValue)).getI10n('value')}
                                                    </div>
                                                    <div class="ui header">
                                                        ${resp}
                                                    </div>
                                                    <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                            resp,
                                                            [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                    )}" var="contact">
                                                        <laser:render template="/templates/cpa/contact" model="${[
                                                                contact             : contact,
                                                                tmplShowDeleteButton: false,
                                                                overwriteEditable   : false
                                                        ]}" />
                                                    </g:each>
                                                </div>
                                                <g:if test="${editmode}">
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.vendor, roleRespValue)}" />
                                                    <div class="two wide column">
                                                        <div class="ui icon buttons">
                                                            <g:link class="ui negative button la-modern-button la-selectable-button js-open-confirm-modal" controller="ajax" action="delPrsRole" id="${prsRole?.id}"
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



