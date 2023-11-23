<%@ page import="de.laser.GenericOIDService; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.auth.Role;de.laser.auth.UserRole;de.laser.UserSetting" %>
<laser:serviceInjection/>

<table class="ui sortable celled la-js-responsive-table la-table compact table">
    <thead>
    <tr>
        <th>${message(code:'user.username.label')}</th>
        <th>${message(code:'user.displayName.label')}</th>
        <th>${message(code:'user.email')}</th>
        <th>
            <g:if test="${showAllAffiliations}">
                <g:message code="user.org"/>
            </g:if>
            <g:else>
                <g:message code="default.role.label"/>
            </g:else>
        </th>
        <th>${message(code:'user.enabled.label')}</th>
        <th class="la-action-info">${message(code:'default.actions.label')}</th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${users}" var="us">
            <tr>
                <td>
                    ${fieldValue(bean: us, field: "username")}

                    <g:if test="${! UserRole.findByUser(us)}">
                        <span class="la-popup-tooltip la-delay" data-content="Dieser Account besitzt keine ROLE." data-position="top right">
                            <i class="icon minus circle red"></i>
                        </span>
                    </g:if>
                </td>
                <td>${us.getDisplayName()}</td>
                <td>${us.email}</td>
                <td>
                        <g:if test="${showAllAffiliations}">
                            ${us.formalOrg?.getDesignation()} <g:if test="${us.formalRole}"><span>(${us.formalRole.authority})</span></g:if>
                        </g:if>
                        <g:else>
                            <g:if test="${us.formalOrg?.id == orgInstance.id}">
                                <g:message code="cv.roles.${us.formalRole?.authority}"/>
                            </g:if>
                        </g:else>
                </td>
                <td>
                    <g:if test="${modifyAccountEnability}">
                        <ui:xEditableBoolean owner="${us}" field="enabled"/>
                    </g:if>
                    <g:else>
                        <g:if test="${! us.enabled}">
                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'user.disabled.text')}">
                                <i class="icon minus circle red"></i>
                            </span>
                        </g:if>
                    </g:else>
                </td>
                <td class="x">
                    <%
                        boolean check = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN');
                        if (! check) {
                            check = editable && userService.isUserEditableForInstAdm(us, editor);
                        }
                    %>

                    <g:if test="${check}">

                        <g:if test="${controllerName == 'user'}">
                            <g:link controller="${controllerName}" action="${editLink}" params="${[id: us.id]}" class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </g:if>
                        <g:if test="${controllerName == 'myInstitution'}">
                            <g:link controller="${controllerName}" action="${editLink}" params="${[uoid: genericOIDService.getOID(us)]}" class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </g:if>
                        <g:if test="${controllerName == 'organisation'}">
                            <g:link controller="${controllerName}" action="${editLink}" id="${orgInstance.id}" params="${[uoid: genericOIDService.getOID(us)]}" class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </g:if>

                        <%
                            boolean check2 = false
                            if (controllerName == 'user') {
                                check2 = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN');
                            }
                            else {
                                check2 = ! us.isLastInstAdminOf(orgInstance);
                            }
                        %>
                        <g:if test="${check2}">

                            <g:if test="${us.id == editor.id}">
                                <g:link controller="profile" action="delete" class="ui icon negative button la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i></g:link>
                            </g:if>
                            <g:elseif test="${controllerName == 'user'}">
                                <g:link controller="${controllerName}" action="${deleteLink}" params="${[id: us.id]}" class="ui icon negative button la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i></g:link>
                            </g:elseif>
                            <g:elseif test="${controllerName == 'myInstitution'}">
                                <g:link controller="${controllerName}" action="${deleteLink}" params="${[uoid: genericOIDService.getOID(us)]}" class="ui icon negative button la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i></g:link>
                            </g:elseif>
                            <g:elseif test="${controllerName == 'organisation'}">
                                <g:link controller="${controllerName}" action="${deleteLink}" id="${orgInstance.id}" params="${[uoid: genericOIDService.getOID(us)]}" class="ui icon negative button la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i></g:link>
                            </g:elseif>

                        </g:if>
                        <g:else>
                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'user.affiliation.lastAdminForOrg1', args: [us.getDisplayName()])}">
                                <button class="ui icon negative button la-modern-button" disabled="disabled"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
                                </button>
                            </span>
                        </g:else>

                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>
