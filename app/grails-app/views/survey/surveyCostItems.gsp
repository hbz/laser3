<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br>

<div class="ui icon info message">
    <i class="info icon"></i>

    ${message(code: 'surveyParticipants.info')}
</div>

<br>

<h2 class="ui left aligned icon header">${message(code: 'surveyConfigs.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<br>



<g:if test="${surveyConfigs}">
    <div class="ui grid">
        <div class="four wide column">
            <div class="ui vertical fluid menu">
                <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

                    <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                            controller="survey" action="surveyCostItems"
                            id="${config?.surveyInfo?.id}" params="[surveyConfigID: config?.id]">

                        <h5 class="ui header">${config?.getConfigName()}</h5>
                        ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}


                        <div class="ui floating circular label">${config?.orgs?.size() ?: 0}</div>
                    </g:link>
                </g:each>
            </div>
        </div>

        <div class="twelve wide stretched column">
            <div class="ui top attached tabular menu">
                <a class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                   data-tab="selectedSubParticipants">${message(code: 'surveyParticipants.selectedSubParticipants')}
                    <div class="ui floating circular label">${selectedSubParticipants.size() ?: 0}</div>
                </a>

                <a class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                   data-tab="selectedParticipants">${message(code: 'surveyParticipants.selectedParticipants')}
                    <div class="ui floating circular label">${selectedParticipants.size() ?: 0}</div></a>

            </div>

            <div class="ui bottom attached tab segment ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                 data-tab="selectedSubParticipants">

                <div>
                    <h2 class="ui left aligned icon header">${message(code: 'surveyParticipants.selectedSubParticipants')}<semui:totalNumber
                            total="${selectedSubParticipants?.size()}"/></h2>
                    <br>
                    <semui:filter>
                        <g:form action="surveyCostItems" method="post" class="ui form"
                                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedSubParticipants']">
                            <g:render template="/templates/filter/orgFilter"
                                      model="[
                                              tmplConfigShow      : [['name', 'libraryType'], ['federalState', 'libraryNetwork', 'property'], ['customerType']],
                                              tmplConfigFormFilter: true,
                                              useNewLayouter      : true
                                      ]"/>
                        </g:form>
                    </semui:filter>



                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList       : selectedSubParticipants,
                                      tmplConfigShow: ['sortname', 'name', 'libraryType', 'surveySubInfo', 'surveySubCostItem']
                              ]"/>

                    <br/>

                    <g:if test="${selectedSubParticipants && editable}">
                        <input type="submit" class="ui button"
                               value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
                    </g:if>

                </div>

            </div>


            <div class="ui bottom attached tab segment ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                 data-tab="selectedParticipants">

                <div>
                    <h2 class="ui left aligned icon header">${message(code: 'surveyParticipants.selectedParticipants')}<semui:totalNumber
                            total="${selectedParticipants?.size()}"/></h2>
                    <br>

                    <semui:filter>
                        <g:form action="surveyCostItems" method="post" class="ui form"
                                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedParticipants']">
                            <g:render template="/templates/filter/orgFilter"
                                      model="[
                                              tmplConfigShow      : [['name', 'libraryType'], ['federalState', 'libraryNetwork', 'property'], ['customerType']],
                                              tmplConfigFormFilter: true,
                                              useNewLayouter      : true
                                      ]"/>
                        </g:form>
                    </semui:filter>



                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList       : selectedParticipants,
                                      tmplConfigShow: ['sortname', 'name', 'libraryType', 'surveySubCostItem']
                              ]"/>

                    <br/>

                    <g:if test="${selectedParticipants && editable}">
                        <input type="submit" class="ui button"
                               value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
                    </g:if>

                </div>

            </div>

        </div>
    </div>
</g:if>
<g:else>
    <p><b>${message(code: 'surveyConfigs.noConfigList')}</b></p>
</g:else>

<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>

</body>
</html>
