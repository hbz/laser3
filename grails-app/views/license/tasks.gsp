<laser:htmlStart message="task.plural" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon>
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>

    <ui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <ui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,     taskInstanceCount: taskInstanceCount,
            myTaskInstanceList: myTaskInstanceList, myTaskInstanceCount: myTaskInstanceCount
    ]}"/>

%{--    <laser:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList,taskInstanceCount:taskInstanceCount]}"/>--}%
%{--    <laser:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList,taskInstanceCount:myTaskInstanceCount]}"/>--}%

<laser:htmlEnd />
