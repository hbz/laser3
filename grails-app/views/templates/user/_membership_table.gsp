<%@ page import="de.laser.storage.RDStore; de.laser.Org; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

<div class="ui card">
    <div class="content">
    <h2 class="ui dividing header">${message(code: 'profile.membership.existing')}</h2>

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'profile.membership.org')}</th>
            <th>${message(code: 'default.role.label')}</th>
            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </g:if>
        </tr>
        </thead>
        <tbody>
        <%
            List comboOrgIds = Org.executeQuery('select c.fromOrg.id from Combo c where c.toOrg = :org and c.type = :type', [org: contextService.getOrg(), type: RDStore.COMBO_TYPE_CONSORTIUM])
        %>

        <g:if test="${userInstance.formalOrg}">
            <g:if test="${controllerName == 'profile' || (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || (userInstance.isFormal()) || (userInstance.formalOrg.id in comboOrgIds))}">
                <tr>
                    <td>
                        <g:link controller="organisation" action="show" id="${userInstance.formalOrg.id}">${userInstance.formalOrg.name}</g:link>
                    </td>
                    <td>
                        <%
                            boolean check = ! userInstance.isLastInstAdminOf(userInstance.formalOrg) && (
                                    SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || (
                                        ( userInstance.isFormal() || userInstance.formalOrg.id in comboOrgIds ) &&
                                                contextService.getUser().isComboInstAdminOf(userInstance.formalOrg)
                                    )
                            )
                        %>
                        <g:if test="${check}">
                            <ui:xEditableRole owner="${userInstance}" field="formalRole" type="user" />
                        </g:if>
                        <g:else>
                            <g:message code="cv.roles.${userInstance.formalRole?.authority}"/>
                        </g:else>
                    </td>
                    <g:if test="${(SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                        <td class="x">
                            <g:if test="${! userInstance.isLastInstAdminOf(userInstance.formalOrg)}">
                                    <g:link controller="ajax" action="unsetAffiliation"
                                            params='${[key:"${userInstance.id}:${userInstance.formalOrg.id}:${userInstance.formalRole.id}"]}'
                                            class="ui icon negative button la-modern-button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code:'confirm.dialog.unlink.user.affiliation')}"
                                            data-confirm-term-how="unlink"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="unlink icon"></i>
                                    </g:link>
                            </g:if>
                            <g:else>
                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'user.affiliation.lastAdminForOrg2', args: [userInstance.getDisplayName()])}">
                                    <button class="ui icon negative button la-modern-button" disabled="disabled">
                                        <i class="unlink icon"></i>
                                    </button>
                                </span>
                            </g:else>
                        </td>
                    </g:if>
                </tr>
            </g:if>
        </g:if>

        </tbody>
    </table>

    </div><!--.content-->
</div><!--.card-->