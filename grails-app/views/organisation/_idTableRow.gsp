<%@page import="de.laser.remote.Wekb; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.IdentifierNamespace"%>
<tr>
    <td>${tableRowNr}</td>
    <td>
        <g:if test="${showGlobalUid}">
            <g:message code="laserID.label" />
            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'laserID.desc')}">
                <i class="${Icon.TOOLTIP.HELP}"></i>
            </span>
        </g:if>
        <g:elseif test="${showWekbId}">
            <g:message code="org.wekbId.label" />
            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'org.wekbId.desc')}">
                <i class="${Icon.TOOLTIP.HELP}"></i>
            </span>
        </g:elseif>
        <g:else>
            ${id.ns.getI10n('name') ?: id.ns.ns}

            <g:if test="${id.ns.getI10n('description')}">
                <span data-position="top left" class="la-popup-tooltip" data-content="${id.ns.getI10n('description')}">
                    <i class="${Icon.TOOLTIP.HELP}"></i>
                </span>
            </g:if>
        </g:else>
    </td>
    <td>
        <g:if test="${showGlobalUid}">
            ${orgInstance.globalUID}
        </g:if>
        <g:elseif test="${showWekbId}">
            ${orgInstance.gokbId}
            <ui:linkWithIcon href="${Wekb.getURL()}/public/orgContent/${orgInstance.gokbId}" />
        </g:elseif>
        <g:else>
            ${id.value == IdentifierNamespace.UNKNOWN ? "" : id.value}
            <g:if test="${id.getURL()}">
                <ui:linkWithIcon href="${id.getURL()}" />
            </g:if>
        </g:else>
    </td>
    <g:if test="${ ! showGlobalUid && ! showWekbId}">
        <td>${id.note}</td>
        <td>
            <g:if test="${editable}">
                <button class="${Btn.MODERN.SIMPLE}" onclick="JSPC.app.IdContoller.editIdentifier(${id.id});"
                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                </button>
                <g:link controller="ajax"
                        action="deleteIdentifier"
                        class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                        params='${[owner: "${orgInstance.class.name}:${orgInstance.id}", target: "${id.class.name}:${id.id}"]}'
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.identifier", args: [id?.ns?.ns+": "+id.value])}"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                    <i class="${Icon.CMD.DELETE}"></i>
                </g:link>
            </g:if>
        </td>
    </g:if>
    <g:else>
        <td></td>
        <td></td>
    </g:else>
</tr>
