
<div class="ui divided middle aligned selection list la-flex-list">
    <g:each in="${orgTypes.sort { it?.getI10n("value") }}" var="type">
        <div class="ui item">
            <div class="content la-space-right">
                <strong>${type?.getI10n("value")}</strong>
            </div>
            <g:if test="${editable}">
                <g:if test="${type.id in availableOrgTypes.collect{ it.id }}">
                    <div class="content la-space-right">
                        <div class="ui mini icon buttons">
                            <g:link role="button" class="ui negative button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.organisationtype.organisation", args: [type?.getI10n('value')])}"
                                    data-confirm-term-how="delete"
                                    controller="organisation" action="deleteOrgType" params="[org: org.id, removeOrgType: type.id]">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </div>
                    </div>
                </g:if>
            </g:if>
        </div>
    </g:each>
</div>