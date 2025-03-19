<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.GenericOIDService; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.auth.Role;de.laser.auth.UserRole;de.laser.UserSetting" %>
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
        <g:if test="${showUserStatus}">
            <th class="center aligned"><i class="exclamation triangle icon grey"></i></th>
        </g:if>
        <th>${message(code:'user.enabled.label')}</th>
        <th class="center aligned">
            <ui:optionsIcon />
        </th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${users}" var="us">
            <g:set var="noLogin" value="${us.accountExpired || us.accountLocked}"/>
            <tr>
                <td>
                    ${fieldValue(bean: us, field: "username")}
                    <g:if test="${! UserRole.findByUser(us)}">
                        <label class="ui icon label la-popup-tooltip" data-content="Dieser Account besitzt keine ROLE." data-position="top right">
                            <i class="minus circle icon red"></i>
                        </label>
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
                <g:if test="${showUserStatus}">
                    <td class="center aligned">
                        <g:if test="${us.accountExpired}">
                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'user.accountExpired.label')}">
                                <i class="${Icon.TOOLTIP.IMPORTANT} large red"></i>
                            </span>
                        </g:if>
                        <g:if test="${us.accountLocked}">
                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'user.accountLocked.label')}">
                                <i class="${Icon.TOOLTIP.IMPORTANT} large yellow"></i>
                            </span>
                        </g:if>
                    </td>
                </g:if>
                <td>
                    <g:if test="${contextService.getUser().isYoda()}">
                        <ui:xEditableBoolean owner="${us}" field="enabled"/>
                    </g:if>
                    <g:else>
                        <g:if test="${! us.enabled}">
                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'user.accountDisabled.label')}">
                                <i class="icon minus circle red"></i>
                            </span>
                        </g:if>
                    </g:else>
                </td>
                <td class="x">
                    <%
                        boolean check = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
                        if (! check) {
                            check = editable && userService.isUserEditableForInstAdm(us)
                        }
                    %>

                    <g:if test="${check}">

                        <g:if test="${controllerName == 'user'}">
                            <g:link controller="${controllerName}" action="${editLink}" params="${[id: us.id]}" class="${Btn.MODERN.SIMPLE}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                            </g:link>
                        </g:if>
                        <g:if test="${controllerName == 'myInstitution'}">
                            <g:link controller="${controllerName}" action="${editLink}" params="${[uoid: genericOIDService.getOID(us)]}" class="${Btn.MODERN.SIMPLE}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                            </g:link>
                        </g:if>
                        <g:if test="${controllerName == 'organisation'}">
                            <g:link controller="${controllerName}" action="${editLink}" id="${orgInstance.id}" params="${[uoid: genericOIDService.getOID(us)]}" class="${Btn.MODERN.SIMPLE}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
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

                            <g:if test="${us.id == contextService.getUser().id}">
                                <g:link controller="profile" action="delete" class="${Btn.MODERN.NEGATIVE}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icon.CMD.DELETE}"></i></g:link>
                            </g:if>
                            <g:elseif test="${controllerName == 'user'}">
                                <g:link controller="${controllerName}" action="${deleteLink}" params="${[id: us.id]}" class="${Btn.MODERN.NEGATIVE}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icon.CMD.DELETE}"></i></g:link>
                            </g:elseif>
                            <g:elseif test="${controllerName == 'myInstitution'}">
                                <g:link controller="${controllerName}" action="${deleteLink}" params="${[uoid: genericOIDService.getOID(us)]}" class="${Btn.MODERN.NEGATIVE}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icon.CMD.DELETE}"></i></g:link>
                            </g:elseif>
                            <g:elseif test="${controllerName == 'organisation'}">
                                <g:link controller="${controllerName}" action="${deleteLink}" id="${orgInstance.id}" params="${[uoid: genericOIDService.getOID(us)]}" class="${Btn.MODERN.NEGATIVE}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icon.CMD.DELETE}"></i></g:link>
                            </g:elseif>

                        </g:if>
                        <g:else>
                            <span class="la-popup-tooltip" data-content="${message(code:'user.affiliation.lastAdminForOrg1', args: [us.getDisplayName()])}">
                                <button class="${Btn.MODERN.NEGATIVE}" disabled="disabled"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </button>
                            </span>
                        </g:else>

                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${users}">
    <ui:paginate params="${params}" max="${max}" total="${total}" />
</g:if>