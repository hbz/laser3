<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>
<laser:serviceInjection />

<semui:modal id="${tmplId}" message="${message}">

    <g:form class="ui form" url="[controller: 'person', action: 'addPersonRole', params: [id: personInstance.id]]" method="POST">

        <div class="field">
            <label for="newPrsRoleOrg">Einrichtung</label>
            <g:select class="ui dropdown search"
                      id="newPrsRoleOrg" name="newPrsRoleOrg"
                          from="${Org.findAll().sort{ it.name ? it.name?.toLowerCase() : it.sortname?.toLowerCase() }}"
                          optionKey="id"
                          optionValue="${{ it.name ?: it.sortname }}"
                value="${personOrg?.id}"
            />
        </div>

        <div class="field">
            <label for="newPrsRoleType">${tmplRoleType}</label>
            <laser:select class="ui dropdown search"
                          id="newPrsRoleType" name="newPrsRoleType"
                          from="${roleTypeValues}"
                          optionKey="id"
                          optionValue="value" />

            <input type="hidden" name="roleType" value="${roleType}" />
        </div>
    </g:form>
</semui:modal>