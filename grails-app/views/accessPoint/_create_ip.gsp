<semui:form>
    <g:form action="create_ip" controller="accessPoint" id="${orgInstance.id}" method="post" class="ui form">

        <g:render template="name" model="${[nameOptions: availableOptions.collectEntries(),
            name: availableOptions.first().values().first(),
            accessMethod: accessMethod]}"/>
        <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.create.label')}"/>
    </g:form>
</semui:form>
