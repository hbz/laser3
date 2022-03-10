
<table class="ui table la-js-responsive-table la-table compact">
    <thead>
        <tr>
            <th>Class.Field</th>
            <th>Annotation</th>
            <th>Integritätsprüfung</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${result}" var="cls">
            <g:each in="${cls.value}" var="entry">
                <tr>
                    <td>
                        <strong>${cls.key}</strong>.${entry.field}
                    </td>
                    <td>
                        ${entry.cat}
                    </td>
                    <td>
                        <!-- ${entry.rdc} -->
                        <g:each in="${entry.check}" var="check">
                            <g:if test="${check.value != true}">
                                <div class="ui label red la-popup-tooltip la-delay" data-content="RDC: ${check.value.desc} (${check.value.id})">${check.key}</div>
                            </g:if>
                            <g:else>
                                <div class="ui label green">${check.key}</div>
                            </g:else>
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </g:each>
    </tbody>
</table>