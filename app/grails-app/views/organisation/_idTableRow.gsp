<g:if test="${idList}">
    %{--<div class="ui divided middle aligned selection list la-flex-list">--}%
        <g:each in="${idList}" var="id">
            <tr>
                <td>${tableRowNr}</td>
                <td>${namespace}</td>
                <td>${id.value} <a href="<g:createLink url="" />" target="_blank"><i class="external alternate icon"></i></a></td>
                <td>${id.note}</td>
                <td>
                    <g:if test="${editable}">
                        TODO: EDIT
                        <g:link class="ui icon button" controller="person" action="show" id="${person?.id}">
                            <i class="write icon"></i>
                        </g:link>
                        <g:link controller="ajax" action="deleteIdentifier" class="ui icon mini negative button"
                                params='${[owner: "${orgInstance.class.name}:${orgInstance.id}", target: "${id.class.name}:${id.id}"]}'>
                            <i class="trash alternate icon"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    %{--</div>--}%
</g:if>
<g:else>
    <tr>
        <td>${tableRowNr}</td>
        <td>${namespace}</td>
        <td></td>
        <td></td>
        <td>
            <g:if test="${editable}">
                TODO: EDIT
                <g:link class="ui icon button" controller="person" action="show" id="${person?.id}">
                    <i class="write icon"></i>
                </g:link>
            </g:if>
        </td>
    </tr>
</g:else>
