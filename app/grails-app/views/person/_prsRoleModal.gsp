<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>
<laser:serviceInjection />

<semui:modal id="personRoleFormModal" text="${message(code: 'person.function_new.label')}">

    <g:form class="ui form" id="create_personRole" url="[controller: 'person', action: 'addPersonRole', params: [id: personInstance.id]]" method="POST">

        <div class="field">
            <label>Einrichtung</label>
            <g:select class="ui dropdown search"
                          name="newPrsRoleOrg"
                          from="${Org.findAll()}"
                          optionKey="id"
                          optionValue="name"
                value="${contextService.getOrg()?.id}"
            />
        </div>

        <div class="field">
            <label>Funktion</label>
            <laser:select class="ui dropdown search"
                          name="newPrsRoleType"
                          from="${PersonRole.getAllRefdataValues('Person Function')}"
                          optionKey="id"
                          optionValue="value" />
        </div>
    </g:form>
</semui:modal>