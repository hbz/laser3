<%@ page import="com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserRole" %>
<laser:serviceInjection/>
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
        <td>
            <g:if test="${showAllAffiliations}">
                <g:each in="${us.getAuthorizedAffiliations()}" var="affi">
                    ${affi.org?.getDesignation()} <span>(${affi.formalRole.authority})</span> <br />
                </g:each>
            </g:if>
            <g:elseif test="${!showAllAffiliations}">
                <% int affiCount = 0 %>
                <g:each in="${us.getAuthorizedAffiliations()}" var="affi">
                    <g:if test="${affi.org.id == contextService.getOrg().id}">
                        ${affi.org?.getDesignation()} <span>(${affi.formalRole.authority})</span> <br />
                        <% affiCount++ %>
                    </g:if>
                </g:each>
                <g:if test="${affiCount != us.getAuthorizedAffiliations().size()}">
                    und ${us.getAuthorizedAffiliations().size() - affiCount} weitere ..
                </g:if>
            </g:elseif>
        </td>
        <td>
            <g:if test="${modifyAccountEnability}">
                <semui:xEditableBoolean owner="${us}" field="enabled"/>
            </g:if>
            <sec:ifNotGranted roles="ROLE_YODA">
                <g:if test="${! us.enabled}">
                    <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'user.disabled.text')}">
                        <i class="icon minus circle red"></i>
                    </span>
                </g:if>
            </sec:ifNotGranted>
        </td>
        <td class="x">
            <g:if test="${editor.hasRole('ROLE_ADMIN') || instAdmService.isUserEditableForInstAdm(us, contextService.getUser())}">
                <g:link action="edit" id="${us.id}" class="ui icon button"><i class="write icon"></i></g:link>
            </g:if>
        </td>
    </tr>
</g:each>