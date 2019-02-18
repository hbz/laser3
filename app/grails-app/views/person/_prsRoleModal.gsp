<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>
<laser:serviceInjection />

<semui:modal id="${tmplId}" message="${message}">

    <g:form class="ui form" url="[controller: 'person', action: 'addPersonRole', params: [id: personInstance.id]]" method="POST">

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
            <label>${tmplRoleType}</label>
            <laser:select class="ui dropdown search"
                          name="newPrsRoleType"
                          from="${roleTypeValues}"
                          optionKey="id"
                          optionValue="value" />

            <input type="hidden" name="roleType" value="${roleType}" />
        </div>
    </g:form>
</semui:modal>