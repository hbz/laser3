<tr>
    <td>${tableRowNr}</td>
    <td>${id?.ns?.ns}</td>
    <td>
        ${id.value}
        %{--<a href="<g:createLink url="" />" target="_blank"><i class="external alternate icon"></i></a>--}%
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
