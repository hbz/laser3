<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyParticipants.label')})" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
    <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}" />
        <ui:crumb class="active" text="${message(code: 'default.add.label', args: [message(code: 'surveyParticipants.label')])}"/>
    </g:if>
%{--    <ui:crumb message="surveyParticipants.label" class="active"/>--}%
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

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />

<g:if test="${surveyConfig}">
    <div class="ui grid">
        <div class="sixteen wide stretched column">
            <ui:filter>
                <g:form action="addSurveyParticipants" method="post" class="ui form"
                        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
                    <laser:render template="/templates/filter/orgFilter"
                                  model="[
                                          tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['hasSubscription', 'subRunTimeMultiYear'], ['subscriptionAdjustDropdown']],
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
                    <ui:bubble count="${consortiaMembers.size()}" grey="true"/> <g:message code="surveyParticipants.consortiaMembers"/>
                </h3>

                <br/><br/>
                <g:form action="actionSurveyParticipants" controller="survey" method="post" class="ui form" enctype="multipart/form-data"
                        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab, actionSurveyParticipants: 'addSurveyParticipants']">

                    <laser:render template="/templates/filter/orgFilterTable"
                                  model="[orgList          : consortiaMembers,
                                          tmplShowCheckbox : (surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]),
                                          tmplDisableOrgIds: surveyConfig.orgs?.org?.id,
                                          tmplConfigShow   : ['lineNumber', 'sortname', 'name', 'libraryType', (surveyConfig.subscription || params.subs ? 'surveySubInfo' : '')]
                                  ]"/>

                    <br/>
                    <g:if test="${surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]}">

                        <div class="ui message">
                            <div class="header">${message(code: 'surveyParticipants.addParticipants.option.selectMembersWithFile.info')}</div>

                            <br>
                            ${message(code: 'surveyParticipants.addParticipants.option.selectMembersWithFile.text')}

                            <br>
                            <g:link class="item" controller="public" action="manual" id="fileImport" target="_blank">${message(code: 'help.technicalHelp.fileImport')}</g:link>
                            <br>

                            <g:link controller="survey" action="templateForSurveyParticipantsBulkWithUpload" params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]">
                                <p>${message(code:'myinst.financeImport.template')}</p>
                            </g:link>

                            <div class="ui action input">
                                <input type="text" readonly="readonly"
                                       placeholder="${message(code: 'template.addDocument.selectFile')}">
                                <input type="file" name="selectMembersWithImport" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                                       style="display: none;">
                                <div class="${Btn.ICON.SIMPLE}">
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

        </div>
    </div>
</g:if>
<g:else>
    <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
</g:else>

<laser:htmlEnd />
