<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig; de.laser.storage.RDStore;" %>

<g:if test="${selectedSubParticipantsCount == 0}">
    <div class="four wide column">

        <g:link action="actionSurveyParticipants"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab, actionSurveyParticipants: 'addSubMembersToSurvey']"
                class="${Btn.SIMPLE} right floated">
            <g:message code="surveyParticipants.addSubMembersToSurvey"/>
        </g:link>
        <br/>
        <br/>

    </div>
</g:if>

<br />

<ui:filter>
    <g:form action="surveyParticipants" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
        <laser:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['subStatus'], surveyConfig.subscription ? ['hasSubscription'] : []],
                          tmplConfigFormFilter: true
                  ]"/>
    </g:form>
</ui:filter>
<g:if test="${consortiaMembers.size() == 0 && consortiaMembersCount > 0}">
    <strong><g:message code="default.search.empty"/></strong>
</g:if>
<g:else>
    <br/>

    <h3 class="ui icon header la-clear-before la-noMargin-top">
        <span class="ui circular label">${consortiaMembers.size()}</span> <g:message code="surveyParticipants.consortiaMembers"/>
    </h3>

    <br/><br/>
    <g:form action="actionSurveyParticipants" controller="survey" method="post" class="ui form" enctype="multipart/form-data"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab, actionSurveyParticipants: 'addSurveyParticipants']">

        <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList          : consortiaMembers,
                              tmplShowCheckbox : (surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]),
                              tmplDisableOrgIds: surveyConfig.orgs?.org?.id,
                              tmplConfigShow   : ['lineNumber', 'sortname', 'name', 'libraryType', (surveyConfig.subscription ? 'surveySubInfo' : '')]
                      ]"/>

        <br/>
        <g:if test="${surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]}">

            <div class="ui message">
                <div class="header">${message(code: 'surveyParticipants.addParticipants.option.selectMembersWithFile.info')}</div>

                <br>
                ${message(code: 'surveyParticipants.addParticipants.option.selectMembersWithFile.text')}

                <br>
                <g:link class="item" controller="profile" action="importManuel" target="_blank">${message(code: 'help.technicalHelp.uploadFile.manuel')}</g:link>
                <br>

                <g:link controller="survey" action="templateForSurveyParticipantsBulkWithUpload" params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]">
                    <p>${message(code:'myinst.financeImport.template')}</p>
                </g:link>

                <div class="ui action input">
                    <input type="text" readonly="readonly"
                           placeholder="${message(code: 'template.addDocument.selectFile')}">
                    <input type="file" name="selectMembersWithImport" accept="text/tab-separated-values,.txt,.csv"
                           style="display: none;">
                    <div class="${Btn.SIMPLE_ICON}">
                        <i class="${Icon.CMD.ATTACHMENT}"></i>
                    </div>
                </div>
            </div><!-- .message -->


            <input type="submit" class="${Btn.SIMPLE}" value="${message(code: 'default.button.add.label')}"/>
        </g:if>

    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.action .icon.button').click(function () {
             $(this).parent('.action').find('input:file').click();
         });

         $('input:file', '.ui.action.input').on('change', function (e) {
             var name = e.target.files[0].name;
             $('input:text', $(e.target).parent()).val(name);
         });
    </laser:script>

</g:else>
