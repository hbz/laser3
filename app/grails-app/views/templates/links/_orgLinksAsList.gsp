<%@ page import="com.k_int.kbplus.Person;com.k_int.kbplus.RefdataValue;de.laser.helper.RDConstants" %>
<laser:serviceInjection />

<table class="ui three column table">
    <g:each in="${roleLinks}" var="role">
        <g:if test="${role.org}">
            <g:set var="cssId" value="prsLinksModal-${role.org.id}-${role.roleType.id}" />

            <tr>
                <th scope="row" class="control-label la-js-dont-hide-this-card">${role?.roleType?.getI10n("value")}</th>
                <td>
                    <g:link controller="organisation" action="show" id="${role.org.id}">${role?.org?.name}</g:link>
                </td>

                <td class="right aligned">
                    <g:if test="${editmode}">
                        <g:if test="${roleObject?.showUIShareButton()}">
                            <g:if test="${role?.isShared}">
                                <span class="la-js-editmode-container">
                                    <g:link id="test" class="ui icon button green la-selectable-button la-popup-tooltip la-delay test"
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:"${roleObject.class.name}:${roleObject.id}", sharedObject:"${role.class.name}:${role.id}"]}"
                                            data-position="top right" data-content="${message(code:'property.share.tooltip.on')}"
                                    >
                                        <i class="la-share icon la-js-editmode-icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <span class="la-js-editmode-container">
                                    <g:link  class="ui icon button la-selectable-button la-popup-tooltip la-delay test "
                                            controller="ajax" action="toggleShare"
                                            params="${[owner:"${roleObject.class.name}:${roleObject.id}", sharedObject:"${role.class.name}:${role.id}"]}"
                                             data-position="top right" data-content="${message(code:'property.share.tooltip.off')}"
                                    >
                                        <i class="la-share slash icon la-js-editmode-icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>
                        <g:if test="${! role.isShared && ! role.sharedFrom}">
                            <g:link class="ui negative icon button la-selectable-button js-open-confirm-modal" controller="ajax" action="delOrgRole" id="${role.id}"
                                    data-confirm-tokenMsg = "${message(code:'confirm.dialog.unlink.provider-agency.subscription')}"
                                    data-confirm-how = "unlink">
                                <i class="unlink icon"></i>
                            </g:link>
                        </g:if>

                        <g:if test="${!role.isShared && role.sharedFrom}">
                            <span  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.sharedFrom')}">
                                <i class="green alternate share icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${showPersons}">
                         <button class="ui icon button la-selectable-button" data-semui="modal" data-href="#${cssId}">
                             <i class="address plus icon"></i>
                         </button>
                        <g:render template="/templates/links/orgLinksAsListAddPrsModal"
                                  model="['cssId': cssId,
                                          'orgRole': role,
                                          'roleObject': roleObject,
                                          parent: roleObject.class.name + ':' + roleObject.id,
                                          role: modalPrsLinkRole.class.name + ':' + modalPrsLinkRole.id
                                  ]"/>
                        </g:if>
                    </g:if>
                </td>

            </tr>
            <g:if test="${showPersons && (Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                            Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue) ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextService.getOrg()) ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextService.getOrg()))}">
                <tr>
                    <td></td>
                    <td>
                        <%-- public --%>
                        <g:if test="${ Person.getPublicByOrgAndFunc(role.org, 'General contact person') ||
                                Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)  }">
                            <div class="ui list">
                                <g:each in="${Person.getPublicByOrgAndFunc(role.org, 'General contact person')}" var="func">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.public')}" data-position="top right">
                                            <i class="address card icon"></i>
                                        </span>
                                        <div class="content">
                                            <g:link controller="person" action="show" id="${func.id}">${func}</g:link>
                                            (${(RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION)).getI10n('value')})
                                        </div>
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPublicByOrgAndObjectResp(role.org, roleObject, roleRespValue)}" var="resp">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.public')}" data-position="top right">
                                            <i class="address card icon"></i>
                                        </span>
                                        <div class="content">
                                            <g:link controller="person" action="show" id="${resp.id}">${resp}</g:link>
                                            (${(RefdataValue.getByValue(roleRespValue)).getI10n('value')})

                                            <g:if test="${editmode}">
                                                <g:set var="prsRole" value="${com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
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
                        <g:if test="${ Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextService.getOrg()) ||
                                Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextService.getOrg())}">
                            <div class="ui list">
                                <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(role.org, 'General contact person', contextService.getOrg())}" var="func">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" data-position="top right">
                                            <i class="address card outline icon"></i>
                                        </span>
                                        <div class="content">
                                            <g:link controller="person" action="show" id="${func.id}">${func}</g:link>
                                            (${(RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION)).getI10n('value')})
                                        </div>
                                    </div>
                                </g:each>
                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(role.org, roleObject, roleRespValue, contextService.getOrg())}" var="resp">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code:'address.private')}" data-position="top right">
                                            <i class="address card outline icon"></i>
                                        </span>
                                        <div class="content">
                                            <g:link controller="person" action="show" id="${resp.id}">${resp}</g:link>
                                            (${(RefdataValue.getByValue(roleRespValue)).getI10n('value')})

                                            <g:if test="${editmode}">
                                                <g:set var="prsRole" value="${com.k_int.kbplus.PersonRole.getByPersonAndOrgAndRespValue(resp, role.org, roleRespValue)}" />
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
                    <td></td>
                </tr>
            </g:if>
        </g:if>
    </g:each>
</table>



