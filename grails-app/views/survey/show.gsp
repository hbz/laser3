<%@ page import="de.laser.SurveyOrg; de.laser.SurveyConfig; de.laser.helper.RDStore; de.laser.SurveyResult" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'surveyShow.label')}</title>

</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" controller="survey" action="show"
                    params="${params + [exportXLSX: true, surveyConfigID: surveyConfig.id]}">${message(code: 'survey.exportSurvey')}</g:link>
        </semui:exportDropdownItem>
        <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
        <semui:exportDropdownItem>
            <g:link class="item" controller="survey" action="show"
                    params="${params + [exportXLSX: true, surveyCostItems: true]}">${message(code: 'survey.exportSurveyCostItems')}</g:link>
        </semui:exportDropdownItem>
        </g:if>
    </semui:exportDropdown>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name" overwriteEditable="${surveyInfo.isSubscriptionSurvey ? false : editable}"/>
</h1>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="show"/>

<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:messages data="${flash}"/>


<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">
            <div class="ui two doubling stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.startDate.label')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                    <semui:xEditable owner="${surveyInfo}" field="startDate" type="date"/>
                                </g:if><g:else>
                                    <semui:xEditable owner="${surveyInfo}" field="startDate" type="date" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.endDate.label')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id, RDStore.SURVEY_SURVEY_STARTED.id]}">
                                    <semui:xEditable owner="${surveyInfo}" field="endDate" type="date"/>
                                </g:if><g:else>
                                    <semui:xEditable owner="${surveyInfo}" field="endDate" type="date" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.comment.label')}</dt>
                            <dd><semui:xEditable owner="${surveyInfo}" field="comment" type="text"/></dd>

                        </dl>

                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'default.status.label')}</dt>
                            <dd>
                                ${surveyInfo.status.getI10n('value')}
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.type.label')}</dt>
                            <dd>
                                <div class="ui label survey-${surveyInfo.type.value}">
                                    ${surveyInfo.type.getI10n('value')}
                                </div>
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.isMandatory.label')}</dt>
                            <dd>
                                ${surveyInfo.isMandatory ? message(code: 'refdata.Yes') : message(code: 'refdata.No')}
                            </dd>

                        </dl>

                        <g:if test="${surveyInfo.isSubscriptionSurvey && surveyInfo.surveyConfigs.size() >= 1}">
                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.subSurveyUseForTransfer.label')}</dt>
                                <dd>
                                    ${surveyInfo.surveyConfigs[0].subSurveyUseForTransfer ? message(code: 'refdata.Yes') : message(code: 'refdata.No')}
                                </dd>

                            </dl>

                        </g:if>

                        <g:if test="${surveyInfo.type == RDStore.SURVEY_TYPE_TITLE_SELECTION}">
                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.pickAndChoosePerpetualAccess.label')}</dt>
                                <dd>
                                    ${surveyInfo.surveyConfigs[0].pickAndChoosePerpetualAccess ? message(code: 'refdata.Yes') : message(code: 'refdata.No')}
                                </dd>

                            </dl>

                        </g:if>

                    </div>
                </div>
            </div>
            <g:if test="${surveyInfo.type == RDStore.SURVEY_TYPE_TITLE_SELECTION}">
                <g:set var="finish"
                       value="${SurveyOrg.findAllByFinishDateIsNotNullAndSurveyConfig(surveyConfig).size()}"/>
                <g:set var="total"
                       value="${SurveyOrg.findAllBySurveyConfig(surveyConfig).size()}"/>

                <g:set var="finishProcess" value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
                <g:if test="${finishProcess > 0 || surveyInfo.status?.id == RDStore.SURVEY_SURVEY_STARTED.id}">
                    <div class="ui card">

                        <div class="content">
                            <div class="ui indicating progress" id="finishProcess" data-percent="${finishProcess}">
                                <div class="bar">
                                </div>

                                <div class="label"
                                     style="background-color: transparent"><g:formatNumber number="${finishProcess}"
                                                                                           type="number"
                                                                                           maxFractionDigits="2"
                                                                                           minFractionDigits="2"/>% <g:message
                                        code="surveyInfo.finished"/></div>
                            </div>
                        </div>
                    </div>
                </g:if>
            </g:if>
            <g:else>
                <g:set var="finish"
                       value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).size()}"/>
                <g:set var="total"
                       value="${SurveyOrg.findAllBySurveyConfig(surveyConfig).size()}"/>

                <g:set var="finishProcess" value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
                <g:if test="${finishProcess > 0 || surveyInfo.status?.id == RDStore.SURVEY_SURVEY_STARTED.id}">
                    <div class="ui card">

                        <div class="content">
                            <div class="ui indicating progress" id="finishProcess2" data-percent="${finishProcess}">
                                <div class="bar">
                                </div>

                                <div class="label"
                                     style="background-color: transparent"><g:formatNumber number="${finishProcess}"
                                                                                           type="number"
                                                                                           maxFractionDigits="2"
                                                                                           minFractionDigits="2"/>% <g:message
                                        code="surveyInfo.finished"/></div>
                            </div>
                        </div>
                    </div>
                </g:if>

            </g:else>

            <br />
            <g:if test="${surveyConfig}">
                <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">

                    <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig: surveyConfig,
                                                                costItemSums: costItemSums,
                                                                subscription: surveyConfig.subscription,
                                                                tasks: tasks,
                                                                visibleOrgRelations: visibleOrgRelations,
                                                                properties: properties]"/>
                </g:if>


                <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                    <g:render template="/templates/survey/generalSurvey" model="[surveyConfig: surveyConfig,
                                                                    costItemSums: costItemSums,
                                                                    subscription: surveyConfig.subscription,
                                                                    tasks: tasks,
                                                                    visibleOrgRelations: visibleOrgRelations,
                                                                    properties: properties]"/>
                </g:if>

            </g:if>
            <g:else>
                <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
            </g:else>
        </div>

        <br />
        <br />

        <g:form action="surveyConfigFinish" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

            <div class="ui right floated compact segment">
                <div class="ui checkbox">
                    <input type="checkbox" onchange="this.form.submit()"
                           name="configFinish" ${surveyConfig.configFinish ? 'checked' : ''}>
                    <label><g:message code="surveyconfig.configFinish.label"/></label>
                </div>
            </div>

        </g:form>

    </div><!-- .twelve -->

%{-- <aside class="four wide column la-sidekick">
     <g:render template="asideSurvey" model="${[ownobj: surveyInfo, owntp: 'survey']}"/>
 </aside><!-- .four -->
--}%
</div><!-- .grid -->


<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#finishProcess').progress();
    $('#finishProcess2').progress();
</laser:script>

</body>
</html>
