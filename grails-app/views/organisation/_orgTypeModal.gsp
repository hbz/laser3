<g:if test="${editable}">
    <dl>
        <dt></dt>
        <dd>
            <a class="ui button" data-semui="modal" href="#orgType">
                <g:message code="org.orgType.add.label" />
            </a>
        </dd>
    </dl>
</g:if>

<semui:modal id="orgType" message="org.orgType.add.label">
    <g:form class="ui form" url="[controller: 'organisation', action: 'addOrgType']" method="post">
        <input type="hidden" name="org" value="${org.id}"/>
        <div class="field">
        <label><g:message code="org.orgType.label" />:</label>

        <g:select from="${availableOrgTypes}"
                  class="ui dropdown fluid"
                    id="orgTypeSelection"
                  optionKey="id"
                  optionValue="${{ it?.getI10n('value') }}"
                  name="orgType"
                  value=""/>
        </div>
    </g:form>
</semui:modal>