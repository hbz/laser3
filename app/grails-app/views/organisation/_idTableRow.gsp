<tr>
    <td>${tableRowNr}</td>
    <td>
        ${id.ns.getI10n('name') ?: id.ns.ns}

        <g:if test="${id.ns.getI10n('description')}">
            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${id.ns.getI10n('description')}">
                <i class="question circle icon"></i>
            </span>
        </g:if>
    </td>
    <td>
        ${id.value}
        <g:if test="${id.getURL()}">
            <a href="${id.getURL()}" target="_blank"><i class="external alternate icon"></i></a>
        </g:if>
    </td>
    <td>${id.note}</td>
    <td>
        <g:if test="${editable}">
            <button class="ui icon button" onclick="IdContoller.editIdentifier(${id.id});"><i class="write icon"></i></button>
            <g:link controller="ajax"
                    action="deleteIdentifier"
                    class="ui button icon red js-open-confirm-modal"
                    params='${[owner: "${orgInstance.class.name}:${orgInstance.id}", target: "${id.class.name}:${id.id}"]}'
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.identifier", args: [id?.ns?.ns+": "+id.value])}"
                    data-confirm-term-how="delete"
            >
                <i class="trash alternate icon"></i>
            </g:link>
        </g:if>
    </td>
</tr>
