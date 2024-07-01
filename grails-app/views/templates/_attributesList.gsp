<%@ page import="de.laser.helper.Icons" %>
<laser:serviceInjection/>
<g:if test="${attributes}">
    <div class="ui divided middle aligned selection list la-flex-list">
        <g:each in="${attributes}" var="attribute">
            <%
                String outputString = attribute[field]
                if(attribute[field] instanceof de.laser.RefdataValue)
                    outputString = attribute[field].getI10n("value")
            %>
            <div class="ui item">
                <div class="content la-space-right">
                    <strong>${outputString}</strong>
                </div>
                <g:if test="${editable}">
                    <g:if test="${attribute[field].id in availableAttributeIds}">
                        <div class="content la-space-right">
                            <div class="ui icon buttons">
                                <g:link class="ui negative button la-modern-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.subjectgroup.organisation", args: [outputString])}"
                                        data-confirm-term-how="delete"
                                        controller="${controllerName}" action="${deleteAction}" params="[id: ownObj.id, field: field, removeObjectOID: genericOIDService.getOID(attribute)]"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icons.CMD_DELETE}"></i>
                                </g:link>
                            </div>
                        </div>
                    </g:if>
                </g:if>
            </div>
        </g:each>
    </div>
</g:if>