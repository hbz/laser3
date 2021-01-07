<%@ page import="de.laser.Org; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.auth.UserOrg;" %>
<laser:serviceInjection />

<div class="column wide sixteen">
    <h2 class="ui dividing header">${message(code: 'profile.membership.existing')}</h2>
    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th>${message(code: 'profile.membership.org')}</th>
            <th>${message(code: 'profile.membership.role')}</th>
            <th>${message(code: 'default.status.label')}</th>
            <th>${message(code: 'profile.membership.date')}</th>
            <g:if test="${tmplUserEdit}">
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </g:if>
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
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${aff.dateRequested}"/>
                        /
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${aff.dateActioned}"/>
                    </td>

                        %{--<g:if test="${tmplProfile}">
                            <td class="x">
                            <g:link class="ui button" controller="profile" action="processCancelRequest" params="${[assoc:aff.id]}">${message(code:'default.button.revoke.label')}</g:link>
                            </td>
                        </g:if>--}%
                        <g:if test="${tmplUserEdit}">
                            <td class="x">
                            <g:if test="${(editor.hasRole('ROLE_ADMIN') || (aff.org.id == contextService.getOrg().id) || (aff.org.id in comboOrgIds))}">
                                <g:if test="${!instAdmService.isUserLastInstAdminForOrg(userInstance, aff.org) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
                                    <g:link controller="ajax" action="deleteThrough" params='${[contextOid:"${userInstance.class.name}:${userInstance.id}",contextProperty:"affiliations",targetOid:"${aff.class.name}:${aff.id}"]}'
                                            class="ui icon negative button">
                                        <i class="trash alternate icon"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <span  class="la-popup-tooltip la-delay" data-content="${message(code:'user.affiliation.lastAdminForOrg', args: [userInstance.getDisplayName()])}">
                                        <button class="ui icon negative button" disabled="disabled">
                                            <i class="trash alternate icon"></i>
                                        </button>
                                    </span>
                                </g:else>
                            </g:if>
                            </td>
                        </g:if>
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