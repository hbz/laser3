<%@ page import="de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyPackagesEvaluation.label')})" serviceInjection="true"/>
<laser:javascript src="echarts.js"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
%{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}" />--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
%{--    <ui:crumb message="surveyResult.label" class="active"/>--}%
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="bordered inverted orange clipboard la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.subscription}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.getConfigNameShort()}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyPackagesEvaluation.label')}
</h2>

<g:if test="${surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
    <div class="ui segment">
        <strong>${message(code: 'surveyEvaluation.notOpenSurvey')}</strong>
    </div>
</g:if>
<g:else>
<div class="ui top attached stackable tabular la-tab-with-js menu">

    <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
            controller="survey" action="surveyPackagesEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
        <span class="ui floating blue circular label">${participantsFinishTotal}</span>
    </g:link>

    <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
            controller="survey" action="surveyPackagesEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
        <span class="ui floating blue circular label">${participantsNotFinishTotal}</span>
    </g:link>

    <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
            controller="survey" action="surveyPackagesEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsView']">
        ${message(code: 'surveyEvaluation.participantsView')}
        <span class="ui floating blue circular label">${participantsTotal}</span>
    </g:link>

</div>
<div class="ui bottom attached tab segment active">

    <div id="chartWrapper" style="width:100%; min-height:500px"></div>

    <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyPackages', 'surveyCostItemsPackages', 'commentOnlyForOwner']}"/>

    <laser:render template="evaluationParticipantsView" model="[showCheckboxForParticipantsHasAccess: false,
                                                                showCheckboxForParticipantsHasNoAccess: false,
                                                        tmplConfigShow   : tmplConfigShowList]"/>
</div>




<laser:script file="${this.getGroovyPageFileName()}">
    let chartDom = $('#chartWrapper')[0];
    let surveyEvChart = echarts.init(chartDom);
    let option;

    option = {
        tooltip: {
                                        trigger: 'axis'
                                    },
        title: {
            text: '<g:message code="surveyInfo.evaluation"/>'
            },
         grid: {
                left: '4%',
                right: '10%',
                containLabel: true
            },
            toolbox: {
                feature: {
                    saveAsImage: {}
                }
            },
            dataset: {
            source: [
    <g:each in="${charts}" var="data">
        [
        <g:each in="${data}" var="value">
            <%
                print '"'
                print value
                print '",'
            %>
        </g:each>
        ],
    </g:each>
    ]
  },
  xAxis: {name: "${g.message(code: 'surveyEvaluation.participants')}", max: "${participants.size()}"},
  yAxis: {  name: "${g.message(code: 'surveyPackages.selectedPackages')}", type: 'category' },
  series: [
    {
      type: 'bar',
      encode: {
        x: 'value',
        y: 'property'
      },
      barWidth: '50%'
    }
  ]
};
surveyEvChart.setOption(option);
</laser:script>
</g:else>
<laser:htmlEnd />
