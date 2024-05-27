<laser:serviceInjection/>
<g:if test="${attributes}">
    <div class="ui divided middle aligned selection list la-flex-list">
        <g:each in="${attributes}" var="attribute">
            <div class="ui item">
                <div class="content la-space-right">
                    <strong>${attribute[field].getI10n("value")}</strong>
                </div>
                <g:if test="${editable}">
                    <g:if test="${attribute[field].id in availableAttributeIds}">
                        <div class="content la-space-right">
                            <div class="ui icon buttons">
                                <g:link class="ui negative button la-modern-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.subjectgroup.organisation", args: [attribute[field].getI10n('value')])}"
                                        data-confirm-term-how="delete"
                                        controller="${controllerName}" action="${deleteAction}" params="[id: owner.id, removeObjectOID: genericOIDService.getOID(attribute)]"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                </g:if>
            </div>
        </g:each>
    </div>
</g:if>