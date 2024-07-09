<%@page import="de.laser.helper.Icons; de.laser.Subscription; de.laser.FormService" %>
<laser:serviceInjection/>
<g:if test="${wrapper == 'altname'}">
    <div class="ui item" data-objId="${objOID}">
        <div class="content la-space-right">
            <g:if test="${ownObj.provider || ownObj.vendor}">
                <ui:xEditable
                        data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                        data_confirm_term_how="ok"
                        class="js-open-confirm-modal-xEditable"
                        owner="${ownObj}" field="${field}" overwriteEditable="${overwriteEditable}"/>
            </g:if>
            <g:else>
                <ui:xEditable owner="${ownObj}" field="${field}" overwriteEditable="${overwriteEditable}"/>
            </g:else>
        </div>
        <div class="content la-space-right">
            <g:if test="${ownObj.subscription && showConsortiaFunctions}">
                <ui:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                         controller="ajax"
                         action="toggleAlternativeNameAuditConfig"
                         params='[ownerId                                  : "${ownObj.subscription.id}",
                                  ownerClass                               : "${ownObj.subscription.class}",
                                  showConsortiaFunctions                   : true,
                                  (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                         ]'
                         data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [ownObj.name])}"
                         data-confirm-term-how="inherit"
                         id="${ownObj.id}"
                         data-content="${message(code:'property.audit.off.tooltip')}"
                         role="button"
                >
                    <i class="icon la-thumbtack slash"></i>
                </ui:link>
            </g:if>
            <g:elseif test="${ownObj.license && showConsortiaFunctions}">
                <ui:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                         controller="ajax"
                         action="toggleAlternativeNameAuditConfig"
                         params='[ownerId                                  : "${ownObj.license.id}",
                                  ownerClass                               : "${ownObj.license.class}",
                                  showConsortiaFunctions                   : true,
                                  (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                         ]'
                         data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.altname", args: [ownObj.name])}"
                         data-confirm-term-how="inherit"
                         id="${ownObj.id}"
                         data-content="${message(code:'property.audit.off.tooltip')}"
                         role="button"
                >
                    <i class="icon la-thumbtack slash"></i>
                </ui:link>
            </g:elseif>
            <div class="ui buttons">
                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: ownObj.id]"
                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.${wrapper}", args: [ownObj.name])}"
                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${objOID}')">
                    <i class="${Icons.CMD.DELETE}"></i>
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
                    <i class="${Icons.CMD.DELETE}"></i>
                </ui:remoteLink>
            </div>
        </div>
    </div>
</g:elseif>
