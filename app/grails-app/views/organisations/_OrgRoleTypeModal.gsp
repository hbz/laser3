<g:if test="${editable}">
    <dl>
        <dt></dt>
        <dd>
            <br/>
            <a class="ui button" data-semui="modal" href="#OrgType"><g:message code="org.orgRoleType.add.label"
                                                                               default="Add new Organisation Type"/></a>
        </dd>
    </dl>
</g:if>

<semui:modal id="OrgType" message="org.orgRoleType.add.label">
    <g:form  class="ui form" url="[controller: 'organisations', action: 'addOrgType']"
            method="post">
        <input type="hidden" name="org" value="${Org.id}"/>
        <div class="field fieldcontain">
        <label><g:message code="org.orgRoleType.label" default="Organisation Type" />:</label>

        <g:select from="${com.k_int.kbplus.RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.loc('OrgRoleType', [en: 'Organisation Type'])).sort{it.getI10n('value')}}"
                  class="ui dropdown fluid"
                  optionKey="id"
                  optionValue="${{ it.getI10n('value') }}"
                  name="orgRoleType"
                  value=""/>
        </div>
    </g:form>
</semui:modal>