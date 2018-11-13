<g:if test="${editable}">
    <dl>
        <dt></dt>
        <dd>
            <br/>
            <a class="ui button" data-semui="modal" href="#orgRoleType">
                <g:message code="org.orgRoleType.add.label" default="Add new Organisation Type"/>
            </a>
        </dd>
    </dl>
</g:if>

<semui:modal id="orgRoleType" message="org.orgRoleType.add.label">
    <g:form class="ui form" url="[controller: 'organisations', action: 'addOrgRoleType']" method="post">
        <input type="hidden" name="org" value="${org.id}"/>
        <div class="field fieldcontain">
        <label><g:message code="org.orgRoleType.label" default="Organisation Type" />:</label>

        <g:select from="${availableOrgRoleTypes}"
                  class="ui dropdown fluid"
                  optionKey="id"
                  optionValue="${{ it?.getI10n('value') }}"
                  name="orgRoleType"
                  value=""/>
        </div>
    </g:form>
</semui:modal>