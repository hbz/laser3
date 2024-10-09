<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${orgType}">
    <div class="ui divided middle aligned selection list la-flex-list">
        <% List availableOrgTypeIds = availableOrgTypes.collect{ it.id } %>
            <div class="ui item">
                <div class="content la-space-right">
                    <strong>${orgType.getI10n("value")}</strong>
                </div>
                <g:if test="${editable}">
                    <g:if test="${orgType.id in availableOrgTypeIds}">
                        <div class="content la-space-right">
                            <div class="ui buttons">
                                <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.organisationtype.organisation", args: [orgType.getI10n('value')])}"
                                        data-confirm-term-how="delete"
                                        controller="organisation" action="deleteOrgType" params="[org: org.id, removeOrgType: orgType.id]"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                </g:if>
            </div>
    </div>
</g:if>