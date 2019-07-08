<%@ page import="com.k_int.kbplus.Org;com.k_int.kbplus.auth.UserOrg" %>
<laser:serviceInjection />

<div class="column wide sixteen">
    <h4 class="ui dividing header">${message(code: 'profile.membership.existing')}</h4>
    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th>${message(code: 'profile.membership.org', default:'Organisation')}</th>
            <th>${message(code: 'profile.membership.role', default:'Role')}</th>
            <th>${message(code: 'profile.membership.status', default:'Status')}</th>
            <th>${message(code: 'profile.membership.date', default:'Date Requested / Actioned')}</th>
            <th class="la-action-info">${message(code:'default.actions')}</th>
        </tr>
        </thead>
        <tbody>

        <%
            int affiCount = 0
            List comboOrgIds = []

            if (contextService.getOrg()) {
                comboOrgIds = Org.executeQuery(
                        'select c.fromOrg.id from Combo c where c.toOrg = :org', [org: contextService.getOrg()]
                )
            }
        %>
        <g:each in="${userInstance.affiliations}" var="aff">
            <g:if test="${tmplProfile || (editor.hasRole('ROLE_ADMIN') || (aff.org.id == contextService.getOrg().id) || (aff.org.id in comboOrgIds))}">
                <% affiCount++ %>
                <tr>
                    <td>
                        <g:link controller="organisation" action="show" id="${aff.org.id}">${aff.org.name}</g:link>
                    </td>
                    <td>
                        <g:message code="cv.roles.${aff.formalRole?.authority}"/>
                    </td>
                    <td>
                        <g:message code="cv.membership.status.${aff.status}"/>
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${aff.dateRequested}"/>
                        /
                        <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${aff.dateActioned}"/>
                    </td>
                    <td class="x">
                        <g:if test="${tmplProfile}">
                            <g:link class="ui button" controller="profile" action="processCancelRequest" params="${[assoc:aff.id]}">${message(code:'default.button.revoke.label', default:'Revoke')}</g:link>
                        </g:if>
                        <g:if test="${tmplUserEdit}">
                            <g:if test="${editor.hasRole('ROLE_ADMIN') || (aff.org.id == contextService.getOrg().id) || (aff.org.id in comboOrgIds)}">
                                <g:link controller="ajax" action="deleteThrough" params='${[contextOid:"${userInstance.class.name}:${userInstance.id}",contextProperty:"affiliations",targetOid:"${aff.class.name}:${aff.id}"]}'
                                        class="ui icon negative button">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:if>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
        <g:if test="${affiCount != userInstance.affiliations?.size()}">
            <tr>
                <td colspan="5">
                    und ${userInstance.affiliations.size() - affiCount} weitere ..
                </td>
            </tr>
        </g:if>
        </tbody>
    </table>
</div><!--.column-->