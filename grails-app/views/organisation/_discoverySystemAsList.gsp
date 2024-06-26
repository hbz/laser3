<%@ page import="de.laser.helper.Icons" %>
<g:if test="${config == 'discoverySystemFrontend'}">
    <div class="ui divided middle aligned selection list la-flex-list">
        <g:each in="${org.discoverySystemFrontends}" var="frontend">
            <div class="ui item">
                <div class="content la-space-right">
                    <strong>${frontend.frontend.getI10n("value")}</strong>
                </div>
                <g:if test="${editable}">
                    <div class="content la-space-right">
                        <div class="ui buttons">
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.frontend", args: [frontend.frontend.getI10n('value')])}"
                                           data-confirm-term-how="delete"
                                           controller="organisation" action="deleteDiscoverySystem" params="[id: org.id, oid: genericOIDService.getOID(frontend)]"
                                           role="button"
                                           aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icons.CMD_DELETE}"></i>
                            </g:link>
                        </div>
                    </div>
                </g:if>
            </div>
        </g:each>
    </div>
</g:if>
<g:elseif test="${config == 'discoverySystemIndex'}">
    <div class="ui divided middle aligned selection list la-flex-list">
        <g:each in="${org.discoverySystemIndices}" var="index">
            <div class="ui item">
                <div class="content la-space-right">
                    <strong>${index.index.getI10n("value")}</strong>
                </div>
                <g:if test="${editable}">
                    <div class="content la-space-right">
                        <div class="ui buttons">
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.frontend", args: [index.index.getI10n('value')])}"
                                           data-confirm-term-how="delete"
                                           controller="organisation" action="deleteDiscoverySystem" params="[id: org.id, oid: genericOIDService.getOID(index)]"
                                           role="button"
                                           aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icons.CMD_DELETE}"></i>
                            </g:link>
                        </div>
                    </div>
                </g:if>
            </div>
        </g:each>
    </div>
</g:elseif>