<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyOrg; com.k_int.kbplus.SurveyResult;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
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
<semui:surveyStatus object="${surveyInfo}"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:messages data="${flash}"/>


<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">
            <div class="ui two stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.startDate.label')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">}"
                                    <semui:xEditable owner="${surveyInfo}" field="startDate" type="date"/>
                                </g:if><g:else>
                                    <semui:xEditable owner="${surveyInfo}" field="startDate" type="date" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.endDate.label')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id, RDStore.SURVEY_SURVEY_STARTED.id]}">}"
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
                                ${surveyInfo.type.getI10n('value')}
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
                                <dt class="control-label">${message(code: 'surveyConfig.subSurveyUseForTransfer.label')}</dt>
                                <dd>
                                    ${surveyInfo.surveyConfigs[0].subSurveyUseForTransfer ? message(code: 'refdata.Yes') : message(code: 'refdata.No')}
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
                       value="${SurveyResult.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).size()}"/>
                <g:set var="total"
                       value="${SurveyResult.findAllBySurveyConfig(surveyConfig).size()}"/>

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

            <br>
            <g:if test="${surveyConfig}">
                <g:if test="${surveyConfig.type == "Subscription"}">

                    <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig: surveyConfig,
                                                                costItemSums: costItemSums,
                                                                subscriptionInstance: surveyConfig.subscription,
                                                                tasks: tasks,
                                                                visibleOrgRelations: visibleOrgRelations,
                                                                properties: properties]"/>
                </g:if>

                <g:if test="${surveyConfig.type == "IssueEntitlementsSurvey"}">

                    <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig: surveyConfig,
                                                                                      subscriptionInstance: surveyConfig.subscription,
                                                                                      tasks: tasks,
                                                                                      visibleOrgRelations: visibleOrgRelations,
                                                                                      properties: properties]"/>
                </g:if>

                <g:if test="${surveyConfig.type == "GeneralSurvey"}">

                    <g:render template="/templates/survey/generalSurvey" model="[surveyConfig: surveyConfig,
                                                                    costItemSums: costItemSums,
                                                                    subscriptionInstance: surveyConfig.subscription,
                                                                    tasks: tasks,
                                                                    visibleOrgRelations: visibleOrgRelations,
                                                                    properties: properties]"/>
                </g:if>

            </g:if>
            <g:else>
                <p><b>${message(code: 'surveyConfigs.noConfigList')}</b></p>
            </g:else>
        </div>

    </div><!-- .twelve -->

%{-- <aside class="four wide column la-sidekick">
     <g:render template="asideSurvey" model="${[ownobj: surveyInfo, owntp: 'survey']}"/>
 </aside><!-- .four -->
--}%
</div><!-- .grid -->


<div id="magicArea"></div>
<r:script>
    $(document).ready(function () {
        $('#finishProcess').progress();
    });

    $(document).ready(function () {
        $('#finishProcess2').progress();
    });
</r:script>

</body>
</html>
