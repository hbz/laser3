<%@ page import="de.laser.ExportClickMeService; de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyResult.label')})" />
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
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />

<g:if test="${surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
    <div class="ui segment">
        <strong>${message(code: 'surveyEvaluation.notOpenSurvey')}</strong>
    </div>
</g:if>
<g:else>
<div class="ui top attached stackable tabular la-tab-with-js menu">

    <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
        <ui:bubble float="true" count="${participantsFinishTotal}"/>
    </g:link>

    <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
        <ui:bubble float="true" count="${participantsNotFinishTotal}"/>
    </g:link>

    <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsView']">
        ${message(code: 'surveyEvaluation.participantsView')}
        <ui:bubble float="true" count="${participantsTotal}"/>
    </g:link>

</div>
<div class="ui bottom attached tab segment active">

    <div id="chartWrapper" style="width:100%; min-height:500px"></div>

    <g:if test="${surveyConfig.pickAndChoose}">
        <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'finishedDate', 'surveyTitlesCount', 'uploadTitleListDoc', 'surveyProperties', 'commentOnlyForOwner', 'downloadTitleList']}"/>
    </g:if>
    <g:else>
        <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties']}"/>

        <g:if test="${surveyConfig.packageSurvey}">
            <g:set var="tmplConfigShowList"
                   value="${tmplConfigShowList <<  ['surveyPackages', 'surveyCostItemsPackages',]}"/>
        </g:if>

        <g:if test="${surveyConfig.vendorSurvey}">
            <g:set var="tmplConfigShowList" value="${tmplConfigShowList << ['surveyVendor']}"/>
        </g:if>

        <g:if test="${surveyConfig.subscriptionSurvey}">
            <g:set var="tmplConfigShowList"
                   value="${tmplConfigShowList << ['surveySubscriptions', 'surveyCostItemsSubscriptions']}"/>
        </g:if>

        <g:set var="tmplConfigShowList"
               value="${tmplConfigShowList << ['commentOnlyForOwner']}"/>

        <g:set var="tmplConfigShowList"
               value="${tmplConfigShowList.flatten()}"/>
    </g:else>

    <laser:render template="evaluationParticipantsView" model="[showCheckboxForParticipantsHasAccess: false,
                                                                showCheckboxForParticipantsHasNoAccess: false,
                                                                tmplConfigShow   : tmplConfigShowList,
                                                                showIcons: params.tab == 'participantsView']"/>
</div>


<laser:script file="${this.getGroovyPageFileName()}">
    let chartDom = $('#chartWrapper')[0];
    let surveyEvChart = echarts.init(chartDom);
    let option;

    option = {
        tooltip: {
                   trigger: 'axis',

                    formatter: function (params) {
                      var tar = params[0];
                      return 'Click for Export:' + '<br/>' + tar.name + '<br/>' + tar.value[1];
                    }
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
                    saveAsImage: {},
                    mySortToggle: {
                        title: 'Sort',
                        icon: 'image://${resource(dir:'images', file:'loading.gif', absolute:true)}',
                        onclick: function (){
                            window.location.replace("<g:createLink controller="survey" action="surveyEvaluation" params="[id: params.id, surveyConfigID: surveyConfig.id, tab: params.tab]"/>"+'&chartSort=X');
                        }
                    },
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
  yAxis: {  name: "${g.message(code: 'surveyProperty.plural.label')}", type: 'category' },
  series: [
    {
      type: 'bar',
      encode: {
        x: 'value',
        y: 'property'
      },
      barWidth: '25%'
    }
  ]
};
surveyEvChart.setOption(option);
surveyEvChart.on('click', function(params) {
   $.ajax({
            url: "<g:createLink controller="clickMe" action="exportClickMeModal" params="${params + [exportController: 'survey', exportAction: 'surveyEvaluation', clickMeType: ExportClickMeService.SURVEY_EVALUATION, id: params.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]}"/>"+'&chartFilter=' + encodeURIComponent(params.name)
        }).done( function (data) {
            $('.ui.dimmer.modals > #exportClickMeModal').remove();
            $('#dynamicModalContainer').empty().html(data);

            $('#dynamicModalContainer .ui.modal').modal({
               onShow: function () {
                    r2d2.initDynamicUiStuff('#exportClickMeModal');
                    r2d2.initDynamicXEditableStuff('#exportClickMeModal');
                    $("html").css("cursor", "auto");
                },
                detachable: true,
                autofocus: false,
                closable: false,
                transition: 'scale',
                onApprove : function() {
                    $(this).find('#exportClickMeModal .ui.form').submit();
                    return false;
                }
            }).modal('show');
        });
});
</laser:script>

    </g:else>
<laser:htmlEnd />
