<g:if test="${editable}">
    <a class="ui button" data-ui="modal" href="#subjectGroup">
        <g:message code="org.subjectGroup.add.label"/>
    </a>

    <ui:modal id="subjectGroup" message="org.subjectGroup.add.label">
        <g:form class="ui form" url="[controller: 'organisation', action: 'addSubjectGroup',id:org.id]" method="post">
            <div class="field">
            <label for="subjectGroupSelection"><g:message code="org.subjectGroup.label"/>:</label>

            <g:select from="${availableSubjectGroups}"
                      class="ui dropdown fluid"
                      id="subjectGroupSelection"
                      optionKey="id"
                      optionValue="${{ it?.getI10n('value') }}"
                      name="subjectGroup"
                      value=""/>
            </div>
        </g:form>
    </ui:modal>
</g:if>
