<%@ page import="de.laser.Org; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.auth.UserOrg;" %>
<laser:serviceInjection />

<div class="ui card">
    <div class="content">
    <h2 class="ui dividing header">${message(code: 'profile.membership.existing')}</h2>

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'profile.membership.org')}</th>
            <th>${message(code: 'profile.membership.role')}</th>
            <g:if test="${contextService.getUser().hasRole('ROLE_ADMIN')}">
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </g:if>
        </tr>
        </thead>
        <tbody>
        <%
            int affiCount = 0
            List comboOrgIds = Org.executeQuery('select c.fromOrg.id from Combo c where c.toOrg = :org', [org: contextService.getOrg()])
        %>
        <g:each in="${userInstance.affiliations}" var="aff">
            <g:if test="${controllerName == 'profile' || (contextService.getUser().hasRole('ROLE_ADMIN') || (aff.org.id == contextService.getOrg().id) || (aff.org.id in comboOrgIds))}">
                <% affiCount++ %>
                <tr>
                    <td>
                        <g:link controller="organisation" action="show" id="${aff.org.id}">${aff.org.name}</g:link>
                    </td>
                    <td>
                        <%
                            boolean check = ! instAdmService.isUserLastInstAdminForOrg(userInstance, aff.org) && (
                                    contextService.getUser().hasRole('ROLE_ADMIN') || (
                                        ( aff.org.id == contextService.getOrg().id || aff.org.id in comboOrgIds ) &&
                                                contextService.getUser().isAuthorizedComboInstAdmin(aff.org)
                                    )
                            )
                        %>
                        <g:if test="${check}">
                            <semui:xEditableRole owner="${aff}" field="formalRole" type="user" />
                        </g:if>
                        <g:else>
                            <g:message code="cv.roles.${aff.formalRole?.authority}"/>
                        </g:else>
                    </td>
                    <g:if test="${(contextService.getUser().hasRole('ROLE_ADMIN'))}">
                        <td class="x">
                            <g:if test="${! instAdmService.isUserLastInstAdminForOrg(userInstance, aff.org)}">
                                    <g:link controller="ajax" action="deleteThrough" params='${[contextOid:"${userInstance.class.name}:${userInstance.id}",contextProperty:"affiliations",targetOid:"${aff.class.name}:${aff.id}"]}'
                                            class="ui icon negative button la-modern-button">
                                        <i class="unlink icon"></i>
                                    </g:link>
                            </g:if>
                            <g:else>
                                    <span  class="la-popup-tooltip la-delay" data-content="${message(code:'user.affiliation.lastAdminForOrg2', args: [userInstance.getDisplayName()])}">
                                        <button class="ui icon negative button la-modern-button" disabled="disabled">
                                            <i class="unlink icon"></i>
                                        </button>
                                    </span>
                            </g:else>
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

    </div><!--.content-->
</div><!--.card-->