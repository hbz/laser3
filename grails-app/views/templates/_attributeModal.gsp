<g:if test="${editable}">
    <a class="ui button" data-ui="modal" href="#subjectGroup">
        <g:message code="${buttonText}"/>
    </a>

    <ui:modal id="subjectGroup" message="org.subjectGroup.add.label">
        <g:form class="ui form" url="[controller: 'organisation', action: 'addSubjectGroup',id:org.id]" method="post">
            <div class="field">
            <label for="attributeSelection"><g:message code="${label}"/>:</label>

            <g:select from="${availableAttributes}"
                      class="ui dropdown fluid"
                      id="attributeSelection"
                      optionKey="id"
                      optionValue="${{ it?.getI10n('value') }}"
                      name="subjectGroup"
                      value=""/>
            </div>
        </g:form>
    </ui:modal>
</g:if>
