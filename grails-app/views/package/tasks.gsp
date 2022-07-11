<%@ page import="de.laser.Package" %>
<laser:htmlStart message="task.plural" />

    <ui:breadcrumbs>
        <ui:crumb controller="package" action="index" text="${message(code:'package.show.all')}" />
        <ui:crumb text="${packageInstance?.name}" id="${packageInstance?.id}" class="active"/>
    </ui:breadcrumbs>
    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'json']}">JSON</g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'xml']}">XML</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
        <laser:render template="actions" />
    </ui:controlButtons>
    <ui:modeSwitch controller="package" action="show" params="${params}"/>

    <ui:h1HeaderWithIcon text="${packageInstance?.name}" />

    <laser:render template="nav"/>

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,     taskInstanceCount: taskInstanceCount,
            myTaskInstanceList: myTaskInstanceList, myTaskInstanceCount: myTaskInstanceCount
    ]}"/>

<laser:htmlEnd />
