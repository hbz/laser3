<td>
    ${info[0]}
</td>
<td class="center aligned">
    <g:if test="${info.size() > 2 && info[1].size() > 0}">
        <span class="ui circular label la-popup-tooltip ${info[2]}">${info[1].size()}</span>
    </g:if>
    <g:else>
        ${info[1].size()}
    </g:else>
</td>
<td>
    <div style="overflow-y:scroll;scrollbar-color:grey white;max-height:14.25em">
        ${info[1].collect{ item -> item instanceof Long ? item : item.hasProperty('id') ? item.id : 'x'}.sort().join(', ')}
    </div>
</td>