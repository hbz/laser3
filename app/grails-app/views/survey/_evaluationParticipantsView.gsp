<h2 class="ui left aligned icon header"><g:message code="surveyEvaluation.participants"/><semui:totalNumber
        total="${participants?.size()}"/></h2>
<br>

<semui:form>
    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>
            <th>
                ${message(code: 'org.sortname.label')}
            </th>
            <th>
                ${message(code: 'org.name.label')}
            </th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${participants}" var="participant" status="i">
            <tr>
                <td>
                    ${i+1}
                </td>
                <td>
                    ${participant.sortname}
                </td>
                <td>
                    <g:link controller="organisation" action="show" id="${participant.id}">
                        ${fieldValue(bean: participant, field: "name")}
                    </g:link>
                </td>
                <td>

                    <g:link controller="survey" action="evaluationParticipantInfo" id="${surveyInfo.id}"
                            params="[surveyConfigID: config?.id]" class="ui icon button"><i
                            class="chart bar icon"></i></g:link>

                </td>
        </tr>

        </g:each>
        </tbody>
    </table>

</semui:form>