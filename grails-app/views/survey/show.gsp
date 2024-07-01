<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.survey.SurveyConfig; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.survey.SurveyResult" %>
<laser:htmlStart message="surveyShow.label" serviceInjection="true"/>

<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>surveyInfo.dateCreated: ${surveyInfo.dateCreated}</p>
        <p>surveyInfo.lastUpdated: ${surveyInfo.lastUpdated}</p>
    </div>
</ui:debugInfo>


<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
<ui:xEditable owner="${surveyInfo}" field="name"/>
</ui:h1HeaderWithIcon>
<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
<ui:linkWithIcon icon="${Icons.SUBSCRIPTION} bordered inverted orange la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

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

                        <g:if test="${surveyInfo.type != RDStore.SURVEY_TYPE_TITLE_SELECTION}">
                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.packageSurvey.label')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="packageSurvey"/>
                                    </g:if><g:else>
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="packageSurvey" overwriteEditable="false"/>
                                    </g:else>

                                    </dd>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.vendorSurvey.label')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="vendorSurvey"/>
                                    </g:if><g:else>
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="vendorSurvey" overwriteEditable="false"/>
                                    </g:else>

                                </dd>
                            </dl>

                            <dl>
                                <dt class="control-label">${message(code: 'surveyconfig.invoicingInformation.label')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="invoicingInformation"/>
                                    </g:if><g:else>
                                        <ui:xEditableBoolean owner="${surveyConfig}" field="invoicingInformation" overwriteEditable="false"/>
                                    </g:else>
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

                            <dl>
                                <dt class="control-label">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.new')}</dt>
                                <dd>
                                    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_IN_PROCESSING.id, RDStore.SURVEY_READY.id]}">
                                        <ui:xEditable owner="${surveyInfo.surveyConfigs[0]}" field="issueEntitlementGroupName"/>
                                    </g:if><g:else>
                                        <ui:xEditable owner="${surveyInfo.surveyConfigs[0]}" field="issueEntitlementGroupName" overwriteEditable="false"/>
                                    </g:else>
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
                <g:if test="${surveyConfig.subscription}">

                    <laser:render template="/templates/survey/subscriptionSurveyForOwner" model="[surveyConfig: surveyConfig,
                                                                                          subscription: surveyConfig.subscription,
                                                                                          tasks: tasks,
                                                                                          visibleProviders: providerRoles]"/>

                </g:if>


                <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">
                    <laser:render template="/templates/survey/generalSurveyForOwner"/>
                </g:if>

            </g:if>
            <g:else>
                <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
            </g:else>
        </div>

        <br />
        <br />

        <g:form action="setSurveyWorkFlowInfos" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, setSurveyWorkFlowInfo: 'setSurveyConfigFinish']">

            <div class="ui right floated compact segment">
                <div class="ui checkbox">
                    <input type="checkbox" onchange="this.form.submit()"
                           name="configFinish" ${surveyConfig.configFinish ? 'checked' : ''}>
                    <label><g:message code="surveyconfig.configFinish.label"/></label>
                </div>
            </div>

        </g:form>



    </div><!-- .twelve -->

</div><!-- .grid -->


<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#finishProcess').progress();
    $('#finishProcess2').progress();
</laser:script>

<laser:htmlEnd />
