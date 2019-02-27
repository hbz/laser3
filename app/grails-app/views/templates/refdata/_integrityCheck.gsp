
<table class="ui table la-table la-table-small">
    <thead>
        <tr>
            <td>Class.Field</td>
            <td>Annotation</td>
            <td></td>
        </tr>
    </thead>
    <tbody>
        <g:each in="${result}" var="cls">
            <g:each in="${cls.value}" var="entry">
                <tr>
                    <td>
                        ${cls.key}.${entry.field}
                    </td>
                    <td>
                        ${entry.cat}
                    </td>
                    <td>
                        <!-- ${entry.rdc} -->
                        <g:each in="${entry.check}" var="check">
                            <g:if test="${check.value != true}">
                                <span class="ui label red" data-tooltip="RDC: ${check.value.desc} (${check.value.id})">${check.key}</span>
                            </g:if>
                            <g:else>
                                <span class="ui label green">${check.key}</span>
                            </g:else>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </g:each>
    </tbody>
</table>