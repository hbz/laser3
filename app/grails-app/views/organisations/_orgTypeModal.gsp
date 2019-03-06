<g:if test="${editable}">
    <dl>
        <dt></dt>
        <dd>
            <br/>
            <a class="ui button" data-semui="modal" href="#orgType">
                <g:message code="org.orgType.add.label" default="Add new Organisation Type"/>
            </a>
        </dd>
    </dl>
</g:if>

<semui:modal id="orgType" message="org.orgType.add.label">
    <g:form class="ui form" url="[controller: 'organisations', action: 'addOrgType']" method="post">
        <input type="hidden" name="org" value="${org.id}"/>
        <div class="field fieldcontain">
        <label><g:message code="org.orgType.label" default="Organisation Type" />:</label>

        <g:select from="${availableOrgTypes}"
                  class="ui dropdown fluid"
                  optionKey="id"
                  optionValue="${{ it?.getI10n('value') }}"
                  name="orgType"
                  value=""/>
        </div>
    </g:form>
</semui:modal>