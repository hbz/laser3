<g:if test="${wrapper == 'altname'}">
    <div class="ui item" data-objId="${objOID}">
        <div class="content la-space-right">
            <ui:xEditable owner="${ownObj}" field="${field}" overwriteEditable="${overwriteEditable}"/>
        </div>
        <div class="content la-space-right">
            <div class="ui buttons">
                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: ownObj.id]"
                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.${wrapper}", args: [ownObj.name])}"
                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${objOID}')">
                    <i class="trash alternate outline icon"></i>
                </ui:remoteLink>
            </div>
        </div>
    </div>
</g:if>
<g:elseif test="${wrapper in ['frontend', 'index']}">
    <div class="ui item" data-objId="${objOID}">
        <div class="content la-space-right">
            <ui:xEditableRefData owner="${ownObj}" field="${field}" config="${config}" overwriteEditable="${overwriteEditable}"/>
        </div>
        <div class="content la-space-right">
            <div class="ui buttons">
                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: wrapper, objId: ownObj.id]"
                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.${wrapper}", args: [ownObj[field].getI10n('value')])}"
                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${objOID}')">
                    <i class="trash alternate outline icon"></i>
                </ui:remoteLink>
            </div>
        </div>
    </div>
</g:elseif>
