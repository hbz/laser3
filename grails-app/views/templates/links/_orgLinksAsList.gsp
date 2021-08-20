<%@ page import="de.laser.PersonRole; de.laser.RefdataValue; de.laser.Person; de.laser.Contact; de.laser.helper.RDConstants; de.laser.helper.RDStore" %>
<laser:serviceInjection />
<table class="ui table">
    <g:each in="${roleLinks}" var="role">
        <g:if test="${role.org}">
            <g:set var="cssId" value="prsLinksModal-${role.org.id}-${role.roleType.id}" />
            <tr>
                <td>
                    <span class="la-flexbox la-minor-object">
                        <g:if test="${role.roleType.value=="Provider"}">
                            <i class="handshake outline icon"></i>
                        </g:if>
                        <g:elseif test="${role.roleType.value =="Agency"}">
                            <i class="shipping fast icon"></i>
                        </g:elseif>
                        <g:link controller="organisation" action="show" id="${role.org.id}">${role.org.name}</g:link>
                    </span>

                </td>

                <td class="right aligned eight wide column">
                    <g:if test="${editmode}">
                        <g:if test="${roleObject.showUIShareButton()}">
                            <g:if test="${role.isShared}">
                                <span class="la-js-editmode-container">
                                    <g:link id="test" class="ui icon button green la-selectable-button la-popup-tooltip la-delay"
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:genericOIDService.getOID(roleObject), sharedObject:genericOIDService.getOID(role), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]}"
                                            data-position="top right" data-content="${message(code:'property.share.tooltip.on')}"
                                    >
                                        <i class="la-share icon la-js-editmode-icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <span class="la-js-editmode-container">
                                    <g:link  class="ui icon button la-selectable-button la-popup-tooltip la-delay  "
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:genericOIDService.getOID(roleObject), sharedObject:genericOIDService.getOID(role), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]}"
                                             data-position="top right" data-content="${message(code:'property.share.tooltip.off')}"
                                    >
                                        <i class="la-share slash icon la-js-editmode-icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>
                        <g:if test="${! role.isShared && ! role.sharedFrom}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'subscription.details.unlinkProviderAgency')}">
                                <g:link class="ui negative icon button la-selectable-button js-open-confirm-modal" controller="ajax" action="delOrgRole" id="${role.id}"
                                    data-confirm-tokenMsg = "${message(code:'confirm.dialog.unlink.provider-agency.subscription')}"
                                    data-confirm-how = "unlink"
                                    role="button"
                                    aria-label="${message(code:'ariaLabel.unlink.provider-agency.subscription')}">
                                    <i class="unlink icon"></i>
                                </g:link>
                            </span>
                        </g:if>

                        <g:if test="${!role.isShared && role.sharedFrom}">
                            <span  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.sharedFrom')}">
                                <i class="green alternate share icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${showPersons}">
                                <button class="ui icon button la-selectable-button la-popup-tooltip la-delay" data-semui="modal" data-href="#${cssId}" data-content="${message(code:'subscription.details.addNewContact')}">
                                    <i class="address plus icon"></i>
                                </button>

                        <g:render template="/templates/links/orgLinksAsListAddPrsModal"
                                  model="['cssId': cssId,
                                          'orgRole': role,
                                          'roleObject': roleObject,
                                          parent: genericOIDService.getOID(roleObject),
                                          role: genericOIDService.getOID(modalPrsLinkRole)
                                  ]"/>
                        </g:if>
                    </g:if>
                </td>

            </tr>
            <g:if test="${showPersons && (Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                            Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextOrg) ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextOrg))}">
                <tr>

                    <td colspan="3">
                        <%-- public --%>
                        <g:if test="${ Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                                Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)  }">
                            <div class="ui divided middle list la-flex-list">
                                <g:each in="${Person.getPublicByOrgAndFunc(role.org, 'General contact person')}" var="func">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.public')}" data-position="top right">
                                            <i class="circular big address card icon"></i>
                                        </span>
                                        <div class="content">
                                            <g:link controller="organisation" action="${contextOrg.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM'] ? 'addressbook' : 'show'}" params="[id: role.org.id]">${func}</g:link> (${(RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION)).getI10n('value')})
                                            <g:each in="${Contact.findAllByPrsAndContentType(
                                                    func,
                                                    RDStore.CCT_EMAIL
                                            )}" var="email">
                                                <g:render template="/templates/cpa/contact" model="${[
                                                        contact             : email,
                                                        tmplShowDeleteButton: false,
                                                        overwriteEditable   : false
                                                ]}">

                                                </g:render>
                                            </g:each>
                                        </div>
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)}" var="resp">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.public')}" data-position="top right">
                                            <i class="circular big address card icon"></i>
                                        </span>
                                        <div class="content">
                                            <g:link controller="organisation" action="${contextOrg.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM'] ? 'addressbook' : 'show'}" params="[id: role.org.id]">${resp}</g:link> (${(RefdataValue.getByValue(roleRespValue)).getI10n('value')})
                                            <g:each in="${Contact.findAllByPrsAndContentType(
                                                    resp,
                                                    RDStore.CCT_EMAIL
                                            )}" var="email">
                                                <g:render template="/templates/cpa/contact" model="${[
                                                        contact             : email,
                                                        tmplShowDeleteButton: false,
                                                        overwriteEditable   : false
                                                ]}">

                                                </g:render>
                                            </g:each>

                                            <g:if test="${editmode}">
                                                <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
                                                <div class="ui mini icon buttons">
                                                    <g:link class="ui negative  button la-selectable-button js-open-confirm-modal" controller="ajax" action="delPrsRole" id="${prsRole?.id}"
                                                            data-confirm-tokenMsg = "${message(code:'template.orgLinks.delete.warn')}"
                                                            data-confirm-how = "unlink">
                                                        <i class="unlink icon"></i>
                                                    </g:link>
                                                </div>
                                            </g:if>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </g:if><%-- public --%>

                        <%-- private --%>
                        <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextOrg) ||
                                Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextOrg)}">
                            <div class="ui divided middle la-flex-list list">
                                <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextOrg)}" var="func">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" style="margin-top:30px; color: gray" data-content="${message(code:'address.private')}" data-position="top right">
                                            <i class="circular big address card outline icon" ></i>
                                        </span>
                                        <div class="content">
                                            <div class="ui label">${(RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION)).getI10n('value')}</div>
                                            <div class="ui header" style="margin-top:10px; font-size:1.1em"><g:link controller="organisation" action="${contextOrg.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM'] ? 'addressbook' : 'show'}" params="[id: role.org.id]">${func}</g:link></div>
                                            <g:each in="${Contact.findAllByPrsAndContentType(
                                                    func,
                                                    RDStore.CCT_EMAIL
                                            )}" var="email">
                                                <g:render template="/templates/cpa/contact" model="${[
                                                        contact             : email,
                                                        tmplShowDeleteButton: false,
                                                        overwriteEditable   : false
                                                ]}">

                                                </g:render>
                                            </g:each>
                                        </div>
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextOrg)}" var="resp">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" data-position="top right">
                                            <i class="circular big address card outline icon"></i>
                                        </span>
                                        <div class="content">
                                            <g:link controller="organisation" action="${contextOrg.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM'] ? 'addressbook' : 'show'}" params="[id: role.org.id]">${resp}</g:link> (${(RefdataValue.getByValue(roleRespValue)).getI10n('value')})
                                            <g:each in="${Contact.findAllByPrsAndContentType(
                                                    resp,
                                                    RDStore.CCT_EMAIL
                                            )}" var="email">
                                                <g:render template="/templates/cpa/contact" model="${[
                                                        contact             : email,
                                                        tmplShowDeleteButton: false,
                                                        overwriteEditable   : false
                                                ]}">

                                                </g:render>
                                            </g:each>

                                            <g:if test="${editmode}">
                                                <g:set var="prsRole" value="${PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
                                                <div class="ui mini icon buttons">
                                                    <g:link class="ui negative button la-selectable-button js-open-confirm-modal" controller="ajax" action="delPrsRole" id="${prsRole?.id}"
                                                            data-confirm-tokenMsg = "${message(code:'template.orgLinks.delete.warn')}"
                                                            data-confirm-how = "unlink">
                                                        <i class="unlink icon"></i>
                                                    </g:link>
                                                </div>
                                            </g:if>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </g:if><%-- private --%>

                    </td>
                </tr>
            </g:if>
        </g:if>
    </g:each>
</table>



