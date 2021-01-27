<tr>
    <td>${tableRowNr}</td>
    <td>
        <g:if test="${showGlobalUid}">
            <g:message code="globalUID.label" />
            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'globalUID.desc')}">
                <i class="question circle icon"></i>
            </span>
        </g:if>
        <g:else>
            ${id.ns.getI10n('name') ?: id.ns.ns}

            <g:if test="${id.ns.getI10n('description')}">
                <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${id.ns.getI10n('description')}">
                    <i class="question circle icon"></i>
                </span>
            </g:if>
        </g:else>
    </td>
    <td>
        <g:if test="${showGlobalUid}">
            ${orgInstance.globalUID}
        </g:if>
        <g:else>
            ${id.value}
                <g:if test="${id.getURL()}">
                    <semui:linkIcon href="${id.getURL()}" />
                </g:if>
        </g:else>
    </td>
    <g:if test="${ ! showGlobalUid}">
    <td>${id.note}</td>
    <td>
        <g:if test="${editable}">
            <button class="ui icon button" onclick="JSPC.app.IdContoller.editIdentifier(${id.id});"><i class="write icon"></i></button>
            <g:link controller="ajax"
                    action="deleteIdentifier"
                    class="ui button icon red js-open-confirm-modal"
                    params='${[owner: "${orgInstance.class.name}:${orgInstance.id}", target: "${id.class.name}:${id.id}"]}'
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.identifier", args: [id?.ns?.ns+": "+id.value])}"
                    role="button"
                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                <i class="trash alternate icon"></i>
            </g:link>
        </g:if>
    </td>
    </g:if>
</tr>
