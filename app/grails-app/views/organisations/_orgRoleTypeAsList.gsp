
<div class="ui divided middle aligned selection list la-flex-list">
    <g:each in="${orgRoleTypes.sort { it?.getI10n("value") }}" var="type">
        <div class="ui item">
            <div class="content la-space-right">
                <strong>${type?.getI10n("value")}</strong>
            </div>
            <g:if test="${editable}">
                <g:if test="${type.id in availableOrgRoleTypes.collect{ it.id }}">
                    <div class="content la-space-right">
                        <div class="ui mini icon buttons">
                            <g:link class="ui negative button js-open-confirm-modal"
                                    data-confirm-term-what="organisationtype"
                                    data-confirm-term-what-detail="${type?.getI10n('value')}"
                                    data-confirm-term-where="organisation"
                                    data-confirm-term-how="delete"
                                    controller="organisations" action="deleteOrgRoleType" params="[org: org.id, removeOrgRoleType: type.id]">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </div>
                    </div>
                </g:if>
            </g:if>
        </div>
    </g:each>
</div>