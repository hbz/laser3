<%@ page import="de.laser.addressbook.PersonRole; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:serviceInjection />
<table class="ui compact table">
    <g:each in="${roleLinks.sort{it.roleType.id}}" var="role">
        <g:if test="${role.org}">
            <g:set var="cssId" value="prsLinksModal-${role.org.id}-${role.roleType.id}" />
            <tr>
                <td>
                    <span class="la-flexbox la-minor-object">
                        <g:if test="${role.roleType.value == RDStore.OR_SUBSCRIPTION_CONSORTIUM.value}">
                            <i class="${Icon.AUTH.ORG_CONSORTIUM} la-list-icon la-popup-tooltip" data-content="${message(code:'consortium')}"></i>
                        </g:if>
                        <g:link controller="organisation" action="show" id="${role.org.id}">
                            ${role.org.name}
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
            <g:if test="${showPersons && (Person.getPublicByOrgAndFunc(role.org, 'General contact person') || (Person.getPublicByOrgAndFunc(role.org, 'Technical Support')) || (Person.getPublicByOrgAndFunc(role.org, 'Service Support')) || (Person.getPublicByOrgAndFunc(role.org, 'Metadata Contact')) ||
                            Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Technical Support') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Service Support') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Metadata Contact') ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue))}">
                <tr>
                    <td colspan="3" style="padding-bottom:0;">
                        <%-- public --%>
                        <g:if test="${ Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                                Person.getPublicByOrgAndFunc(role.org, 'Technical Support') ||
                                Person.getPublicByOrgAndFunc(role.org, 'Service Support') ||
                                Person.getPublicByOrgAndFunc(role.org, 'Metadata Contact') ||
                                Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)  }">
                            <div class="ui segment la-timeLineSegment-contact">
                                <div class="la-timeLineGrid">
                                    <div class="ui grid">
                                        <g:each in="${Person.getPublicByOrgAndFunc(role.org, 'General contact person')}" var="func">
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
                                            Set<Person> techSupports = Person.getPublicByOrgAndFunc(role.org, 'Technical Support'), serviceSupports = Person.getPublicByOrgAndFunc(role.org, 'Service Support'), metadataContacts = Person.getPublicByOrgAndFunc(role.org, 'Metadata Contact')
                                        %>
                                            <g:each in="${techSupports}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
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
                                                        <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
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
                                                        <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
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
                                        <g:each in="${Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)}" var="resp">
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
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
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
                        <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person') ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Technical Support') ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Service Support') ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Metadata Contact') ||
                                Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue)}">
                            <div class="ui segment la-timeLineSegment-contact">
                                <div class="la-timeLineGrid">
                                    <div class="ui grid">
                                        <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person')}" var="func">
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
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Technical Support')}" var="func">
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
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Service Support')}" var="func">
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
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'Metadata Contact')}" var="func">
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
                                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue)}" var="resp">
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
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
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
        </g:if>
    </g:each>
</table>



