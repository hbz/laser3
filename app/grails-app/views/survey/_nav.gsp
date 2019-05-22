<laser:serviceInjection />

<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="survey" action="show" params="${[id:params.id]}" message="surveyShow.label" />

    <semui:subNavItem controller="survey" action="surveyConfigs" params="${[id:params.id]}" message="surveyConfigs.label" />

    <semui:subNavItem controller="survey" action="surveyConfigDocs" params="${[id:params.id]}" message="surveyConfigDocs.label" />

    <semui:subNavItem controller="survey" action="surveyParticipants" params="${[id:params.id]}" message="surveyParticipants.label" />

    <semui:subNavItem controller="survey" action="surveyCostItems" params="${[id:params.id]}" message="surveyCostItems.label" />

    <semui:subNavItem controller="survey" action="surveyEvaluation" params="${[id:params.id]}" message="surveyEvaluation.label" />


</semui:subNav>
