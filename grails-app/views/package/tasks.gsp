<%@ page import="de.laser.Package" %>
<laser:htmlStart message="task.plural" />

    <semui:breadcrumbs>
        <semui:crumb controller="package" action="index" text="${message(code:'package.show.all')}" />
        <semui:crumb text="${packageInstance?.name}" id="${packageInstance?.id}" class="active"/>
    </semui:breadcrumbs>
    <semui:controlButtons>
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'json']}">JSON</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'xml']}">XML</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
        <laser:render template="actions" />
    </semui:controlButtons>
    <semui:modeSwitch controller="package" action="show" params="${params}"/>

    <semui:h1HeaderWithIcon text="${packageInstance?.name}" />

    <laser:render template="nav"/>

    <semui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,     taskInstanceCount: taskInstanceCount,
            myTaskInstanceList: myTaskInstanceList, myTaskInstanceCount: myTaskInstanceCount
    ]}"/>

%{--    <laser:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList,taskInstanceCount:taskInstanceCount]}"/>--}%
%{--    <laser:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList,taskInstanceCount:myTaskInstanceCount]}"/>--}%

<laser:htmlEnd />
