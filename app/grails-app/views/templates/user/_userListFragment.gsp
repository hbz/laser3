<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserRole;com.k_int.kbplus.UserSettings" %>
<laser:serviceInjection/>

<table class="ui sortable celled la-table compact table">
    <thead>
    <tr>
        <%--<g:sortableColumn property="u.username" params="${params}" title="${message(code: 'user.name.label')}" />
        <g:sortableColumn property="u.display" params="${params}" title="${message(code: 'user.display.label')}" />
        <g:sortableColumn property="uo.org.instname" params="${params}" title="${message(code: 'user.instname.label')}" />
        --%>
        <th>${message(code:'user.username.label')}</th>
        <th>${message(code:'user.displayName.label')}</th>
        <th>${message(code:'user.email')}</th>
        <th>
            <g:if test="${showAllAffiliations}">
                <g:message code="user.org"/>
            </g:if>
            <g:elseif test="${!showAllAffiliations}">
                <g:message code="profile.membership.role"/>
            </g:elseif>
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

                    <g:if test="${! UserRole.findByUserAndRole(us, Role.findByAuthority('ROLE_USER'))}">
                        <span  class="la-popup-tooltip la-delay" data-content="Dieser Account besitzt keine ROLE_USER-Rechte." data-position="top right">
                            <i class="icon minus circle red"></i>
                        </span>
                    </g:if>
                </td>
                <td>${us.getDisplayName()}</td>
                <td>${us.email}</td>
                <td>
                    <g:each in="${us.getAuthorizedAffiliations()}" var="affi">
                        <g:set var="uoId" value="${affi.id}"/><%-- ERMS-2370 fix this for count>1 --%>
                        <g:if test="${showAllAffiliations}">
                            ${affi.org?.getDesignation()} <span>(${affi.formalRole.authority})</span> <br />
                        </g:if>
                        <g:elseif test="${!showAllAffiliations}">
                            <g:if test="${affi.org.id == orgInstance.id}">
                                <g:message code="cv.roles.${affi.formalRole.authority}"/>
                            </g:if>
                            <%-- int affiCount = 0 %>
                            <g:each in="${us.getAuthorizedAffiliations()}" var="affi">
                                <g:if test="${affi.org.id == contextService.getOrg().id}">
                                    ${affi.org?.getDesignation()} <span>(${affi.formalRole.authority})</span> <br />
                                    <% affiCount++ %>
                                </g:if>
                            </g:each>
                            <g:if test="${affiCount != us.getAuthorizedAffiliations().size()}">
                                und ${us.getAuthorizedAffiliations().size() - affiCount} weitere ..
                            </g:if>--%>
                        </g:elseif>
                    </g:each>
                </td>
                <td>
                    <g:if test="${modifyAccountEnability}">
                        <semui:xEditableBoolean owner="${us}" field="enabled"/>
                    </g:if>
                    <g:elseif test="${!modifyAccountEnability}">
                        <g:if test="${! us.enabled}">
                            <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'user.disabled.text')}">
                                <i class="icon minus circle red"></i>
                            </span>
                        </g:if>
                    </g:elseif>
                </td>
                <td class="x">
                    <g:if test="${editable && (instAdmService.isUserEditableForInstAdm(us, editor) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                        <g:link controller="${controllerName}" action="${editLink}" id="${us.id}" class="ui icon button"><i class="write icon"></i></g:link>
                        <g:if test="${showAffiliationDeleteLink}">

                        <g:if test="${!instAdmService.isUserLastInstAdminForOrg(us, orgInstance) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
                            <g:link class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.user.organisation", args: [us.displayName,us.getSettingsValue(UserSettings.KEYS.DASHBOARD)?.name ])}"
                                    data-confirm-term-how="delete"
                                    controller="organisation"
                                    action="processAffiliation"
                                    params="${[assoc:uoId, id:orgInstance?.id, cmd:'delete']}"
                                    data-content="${message(code:'profile.membership.delete.button')}" data-position="top left" >
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <span  class="la-popup-tooltip la-delay" data-content="${message(code:'user.affiliation.lastAdminForOrg', args: [us.getDisplayName()])}">
                                <button class="ui icon negative button" disabled="disabled">
                                    <i class="trash alternate icon"></i>
                                </button>
                            </span>
                        </g:else>

                        </g:if>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>
