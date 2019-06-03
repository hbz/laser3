<h2 class="ui left aligned icon header"><g:message code="surveyEvaluation.participants"/><semui:totalNumber
        total="${participants?.size()}"/></h2>
<br>

<semui:form>

    <h4><g:message code="surveyParticipants.hasAccess"/></h4>

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
        <g:each in="${participants.findAll { it?.hasAccessOrg() }.sort { it?.sortname }}" var="participant" status="i">
            <tr>
                <td>
                    ${i + 1}
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
                            params="[participant: participant?.id]" class="ui icon button"><i
                            class="chart bar icon"></i></g:link>

                </td>
            </tr>

        </g:each>
        </tbody>
    </table>

    <h4><g:message code="surveyParticipants.hasNotAccess"/></h4>


    <g:set var="surveyParticipantsHasNotAccess" value="${participants.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

    <div class="four wide column">
        <button type="button" class="ui icon button right floated" data-semui="modal"
                data-href="#copyEmailaddresses_evaluationParticipantsView"><g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/></button>
    </div>

    <g:render template="../templates/copyEmailaddresses"
              model="[orgList: surveyParticipantsHasNotAccess ?: null, modalID: 'copyEmailaddresses_evaluationParticipantsView']"/>

    <br>
    <br>

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
        <g:each in="${surveyParticipantsHasNotAccess}" var="participant" status="i">
            <tr>
                <td>
                    ${i + 1}
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
                            params="[participant: participant?.id]" class="ui icon button"><i
                            class="chart bar icon"></i></g:link>

                </td>
            </tr>

        </g:each>
        </tbody>
    </table>

</semui:form>