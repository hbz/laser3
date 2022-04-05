<%@ page import="de.laser.Org; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.Person; de.laser.PersonRole" %>
<laser:serviceInjection />

<semui:modal id="${tmplId}" message="${message}">

    <g:form class="ui form" url="[controller: 'person', action: 'addPersonRole', params: [id: personInstance.id]]" method="POST">
        <input type="hidden" name="redirect" value="true" />

        <div class="field">
            <label for="newPrsRoleOrg_${tmplId}">Einrichtung</label>
            <g:select class="ui dropdown search"
                      id="newPrsRoleOrg_${tmplId}" name="newPrsRoleOrg"
                          from="${Org.findAll("from Org o order by lower(o.name)")}"
                          optionKey="id"
                          optionValue="${{ it.name ?: it.sortname ?: it.shortname }}"
                value="${presetOrgId}"
            />
        </div>

        <div class="field">
            <label for="newPrsRoleType_${tmplId}">${tmplRoleType}</label>
            <laser:select class="ui dropdown search"
                          id="newPrsRoleType_${tmplId}" name="newPrsRoleType"
                          from="${roleTypeValues}"
                          optionKey="id"
                          optionValue="value"
                          value="${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS?.id}"
                          />

            <input type="hidden" name="roleType" value="${roleType}" />
        </div>
    </g:form>
</semui:modal>