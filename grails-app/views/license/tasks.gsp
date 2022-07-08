<laser:htmlStart message="task.plural" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:h1HeaderWithIcon>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </semui:h1HeaderWithIcon>

    <semui:anualRings object="${license}" controller="license" action="show" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />

    <semui:messages data="${flash}" />

    <laser:render template="/templates/tasks/tables" model="${[
            taskInstanceList: taskInstanceList,     taskInstanceCount: taskInstanceCount,
            myTaskInstanceList: myTaskInstanceList, myTaskInstanceCount: myTaskInstanceCount
    ]}"/>

%{--    <laser:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList,taskInstanceCount:taskInstanceCount]}"/>--}%
%{--    <laser:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList,taskInstanceCount:myTaskInstanceCount]}"/>--}%

<laser:htmlEnd />
