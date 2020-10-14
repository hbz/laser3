<g:if test="${editable}">
    <a class="ui button" data-semui="modal" href="#subjectGroup">
        <g:message code="org.subjectGroup.add.label"/>
    </a>

    <semui:modal id="subjectGroup" message="org.subjectGroup.add.label">
        <g:form class="ui form" url="[controller: 'organisation', action: 'addSubjectGroup']" method="post">
            <input type="hidden" name="id" value="${org.id}"/>
            <div class="field fieldcontain">
            <label><g:message code="org.subjectGroup.label"/>:</label>

            <g:select from="${availableSubjectGroups}"
                      class="ui dropdown fluid"
                      id="subjectGroupSelection"
                      optionKey="id"
                      optionValue="${{ it?.getI10n('value') }}"
                      name="subjectGroup"
                      value=""/>
            </div>
        </g:form>
    </semui:modal>
</g:if>
