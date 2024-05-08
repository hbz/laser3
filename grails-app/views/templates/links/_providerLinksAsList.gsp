<%@ page import="de.laser.PersonRole; de.laser.RefdataValue; de.laser.Person; de.laser.Contact; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.remote.ApiSource" %>
<laser:serviceInjection />
<g:set var="wekbAPI" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>
<table class="ui compact table">
    <g:each in="${providerLinks}" var="role">
            <g:set var="cssId" value="prsLinksModal-${role.provider.id}-${role.roleType.id}" />
            <tr>
                <td>
                    <span class="la-flexbox la-minor-object">
                        <g:if test="${role.roleType.value == RDStore.OR_SUBSCRIPTION_CONSORTIA.value}">
                            <i class="la-list-icon la-popup-tooltip la-delay la-consortia icon" data-content="${message(code:'consortium')}"></i>
                        </g:if>
                        <g:elseif test="${role.roleType.value==RDStore.OR_PROVIDER.value}">
                            <i class="la-list-icon la-popup-tooltip la-delay handshake outline icon" data-content="${message(code:'default.provider.label')}"></i>
                        </g:elseif>
                        <g:link controller="provider" action="show" id="${role.provider.id}">
                            ${role.provider.name}
                        </g:link>
                    </span>

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
                                <g:link class="ui negative icon button la-modern-button la-selectable-button js-open-confirm-modal" controller="ajax" action="delOrgRole" id="${role.id}"
                                    data-confirm-tokenMsg = "${message(code:'confirm.dialog.unlink.provider-agency.subscription')}"
                                    data-confirm-term-how = "unlink"
                                    role="button"
                                    aria-label="${message(code:'ariaLabel.unlink.provider-agency.subscription')}">
                                    <i class="unlink icon"></i>
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
                                    <i class="address plus icon"></i>
                                </button>

                        <laser:render template="/templates/links/providerLinksAsListAddPrsModal"
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
            <g:if test="${showPersons && (Person.getPublicByOrgAndFunc(role.provider, 'General contact person') || (Person.getPublicByOrgAndFunc(role.provider, 'Technical Support')) || (Person.getPublicByOrgAndFunc(role.provider, 'Service Support')) || (Person.getPublicByOrgAndFunc(role.provider, 'Metadata Contact')) ||
                            Person.getPublicByOrgAndObjectResp(role.provider, roleObject, roleRespValue) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'General contact person', contextOrg) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Technical Support', contextOrg) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Service Support', contextOrg) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Metadata Contact', contextOrg) ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.provider, roleObject, roleRespValue, contextOrg))}">
                <tr>
                    <td colspan="3" style="padding-bottom:0;">
                        <%-- public --%>
                        <g:if test="${ Person.getPublicByOrgAndFunc(role.provider, 'General contact person') ||
                                Person.getPublicByOrgAndFunc(role.provider, 'Technical Support') ||
                                Person.getPublicByOrgAndFunc(role.provider, 'Service Support') ||
                                Person.getPublicByOrgAndFunc(role.provider, 'Metadata Contact') ||
                                Person.getPublicByOrgAndObjectResp(role.provider, roleObject, roleRespValue)  }">
                            <div class="ui segment la-timeLineSegment-contact">
                                <div class="la-timeLineGrid">
                                    <div class="ui grid">
                                        <g:each in="${Person.getPublicByOrgAndFunc(role.provider, 'General contact person')}" var="func">
                                            <div class="row">
                                                <div class="two wide column">
                                                    <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
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
                                            if(role.provider.gokbId) {
                                                contactsExWekb = true
                                                techSupports.addAll(Person.getPublicByOrgAndFunc(role.provider, 'Technical Support', role.provider))
                                                serviceSupports.addAll(Person.getPublicByOrgAndFunc(role.provider, 'Service Support', role.provider))
                                                metadataContacts.addAll(Person.getPublicByOrgAndFunc(role.provider, 'Metadata Contact', role.provider))
                                            }
                                            else {
                                                techSupports.addAll(Person.getPublicByOrgAndFunc(role.provider, 'Technical Support'))
                                                serviceSupports.addAll(Person.getPublicByOrgAndFunc(role.provider, 'Service Support'))
                                                metadataContacts.addAll(Person.getPublicByOrgAndFunc(role.provider, 'Metadata Contact'))
                                            }
                                        %>
                                            <g:each in="${techSupports}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <g:if test="${contactsExWekb}">
                                                            <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + role.provider.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
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
                                                            <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + role.provider.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
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
                                                            <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + role.provider.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                        </g:if>
                                                        <g:else>
                                                            <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
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
                                        <g:each in="${Person.getPublicByOrgAndObjectResp(role.provider, roleObject, roleRespValue)}" var="resp">
                                            <div class="row">
                                                <div class="two wide column">
                                                    <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
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
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.provider, roleRespValue)}" />
                                                    <div class="two wide column">
                                                        <div class="ui icon buttons">
                                                            <g:link class="ui negative  button la-modern-button la-selectable-button js-open-confirm-modal" controller="ajax" action="delPrsRole" id="${prsRole?.id}"
                                                                    data-confirm-tokenMsg = "${message(code:'template.orgLinks.delete.warn')}"
                                                                    data-confirm-how = "unlink">
                                                                <i class="unlink icon"></i>
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
                        <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'General contact person', contextOrg) ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Technical Support', contextOrg) ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Service Support', contextOrg) ||
                                Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Metadata Contact', contextOrg) ||
                                Person.getPrivateByOrgAndObjectRespFromAddressbook(role.provider, roleObject, roleRespValue, contextOrg)}">
                            <div class="ui segment la-timeLineSegment-contact">
                                <div class="la-timeLineGrid">
                                    <div class="ui grid">
                                        <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'General contact person', contextOrg)}" var="func">
                                            <div class="row">
                                                <div class="two wide column">
                                                    <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
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
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Technical Support', contextOrg)}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
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
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Service Support', contextOrg)}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
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
                                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.provider, 'Metadata Contact', contextOrg)}" var="func">
                                                <div class="row">
                                                    <div class="two wide column">
                                                        <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
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
                                        <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.provider, roleObject, roleRespValue, contextOrg)}" var="resp">
                                            <div class="row">
                                               <div class="two wide column">
                                                    <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" ></i>
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
                                                    <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.provider, roleRespValue)}" />
                                                    <div class="two wide column">
                                                        <div class="ui icon buttons">
                                                            <g:link class="ui negative button la-modern-button la-selectable-button js-open-confirm-modal" controller="ajax" action="delPrsRole" id="${prsRole?.id}"
                                                                    data-confirm-tokenMsg = "${message(code:'template.orgLinks.delete.warn')}"
                                                                    data-confirm-how = "unlink">
                                                                <i class="unlink icon"></i>
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



