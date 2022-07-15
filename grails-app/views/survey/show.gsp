<%@ page import="de.laser.survey.SurveyConfig; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.survey.SurveyResult" %>
<laser:htmlStart message="surveyShow.label" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" controller="survey" action="show"
                    params="${params + [exportXLSX: true, surveyConfigID: surveyConfig.id]}">${message(code: 'survey.exportSurvey')}</g:link>
        </ui:exportDropdownItem>
        <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
        <ui:exportDropdownItem>
            <g:link class="item" controller="survey" action="show"
                    params="${params + [exportXLSX: true, surveyCostItems: true]}">${message(code: 'survey.exportSurveyCostItems')}</g:link>
        </ui:exportDropdownItem>
        </g:if>
    </ui:exportDropdown>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
<ui:xEditable owner="${surveyInfo}" field="name" overwriteEditable="${surveyInfo.isSubscriptionSurvey ? false : editable}"/>
</ui:h1HeaderWithIcon>
<survey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="show"/>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<ui:messages data="${flash}"/>

<g:if test="${surveyLinksMessage}">
    <ui:msg class="negative">
        <div class="ui bulleted list">
        <g:each in="${surveyLinksMessage}" var="msg">
            <div class="item">${msg}</div>
        </g:each>
        </div>
    </ui:msg>
</g:if>


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
                                    <ui:xEditable owner="${surveyInfo}" field="startDate" type="date"/>
                                </g:if><g:else>
                                    <ui:xEditable owner="${surveyInfo}" field="startDate" type="date" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.endDate.label')}</dt>
                            <dd>
                                <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id, RDStore.SURVEY_SURVEY_STARTED.id]}">
                                    <ui:xEditable owner="${surveyInfo}" field="endDate" type="date"/>
                                </g:if><g:else>
                                    <ui:xEditable owner="${surveyInfo}" field="endDate" type="date" overwriteEditable="false"/>
                                </g:else>
                            </dd>

                        </dl>

                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.comment.label')}</dt>
                            <dd><ui:xEditable owner="${surveyInfo}" field="comment" type="text"/></dd>

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

                    <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig: surveyConfig,
                                                                costItemSums: costItemSums,
                                                                subscription: surveyConfig.subscription,
                                                                tasks: tasks,
                                                                visibleOrgRelations: visibleOrgRelations,
                                                                properties: properties]"/>
                </g:if>


                <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                    <laser:render template="/templates/survey/generalSurvey" model="[surveyConfig: surveyConfig,
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

        <g:form action="setSurveyConfigFinish" method="post" class="ui form"
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
     <laser:render template="asideSurvey" model="${[ownobj: surveyInfo, owntp: 'survey']}"/>
 </aside><!-- .four -->
--}%
</div><!-- .grid -->


<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#finishProcess').progress();
    $('#finishProcess2').progress();
</laser:script>

<laser:htmlEnd />
