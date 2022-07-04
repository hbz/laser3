<%@ page import="de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')} (${message(code: 'surveyConfigDocs.label')})</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig?.getConfigNameShort()}"/>
    </g:if>
    <semui:crumb message="surveyConfigDocs.label" class="active"/>
</semui:breadcrumbs>


<semui:controlButtons>
    <laser:render template="actions"/>
</semui:controlButtons>

<semui:headerWithIcon type="Survey">
<semui:xEditable owner="${surveyInfo}" field="name"/>
</semui:headerWithIcon>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="surveyConfigDocs"/>


<laser:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.subscription.name}
        </g:link>
    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>: ${message(code: 'surveyConfigDocs.label')}
</h2>
<br />

%{--<p><strong>${message(code: 'surveyConfigDocs.info')}</strong></p>
<br />--}%


<g:if test="${surveyConfig}">

    <div class="ui grid">
%{--<div class="four wide column">
    <div class="ui vertical fluid menu">
        <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

            <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                    controller="survey" action="surveyConfigDocs"
                    id="${config?.surveyInfo?.id}" params="[surveyConfigID: config?.id]">

                <h5 class="ui header">${config?.getConfigNameShort()}</h5>
                ${SurveyConfig.getLocalizedValue(config?.type)}


                <div class="ui floating circular label">${config?.getCurrentDocs().size() ?: 0}</div>
            </g:link>
        </g:each>
    </div>
</div>
--}%
    <div class="sixteen wide stretched column">

        <semui:form>

            <div class="four wide column">
                <button type="button" class="ui icon button blue la-modern-button right floated" data-semui="modal"
                        data-href="#modalCreateDocument"><i class="plus icon"></i></button>
                <laser:render template="/templates/documents/modal"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
            </div>
            <br /><br />


            <table class="ui celled la-js-responsive-table la-table table license-documents">
                <thead>
                <tr>
                    <th>${message(code:'sidewide.number')}</th>
                    <th>${message(code: 'default.title.label')}</th>
                    <th>${message(code: 'surveyConfigDocs.docs.table.fileName')}</th>
                    <th>${message(code: 'surveyConfigDocs.docs.table.type')}</th>
                    <th>${message(code: 'property.share.tooltip.sharedFrom')}</th>
                    <th>${message(code: 'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${surveyConfig.getCurrentDocs()}" var="docctx" status="i">
                    <tr>
                        <td>${i + 1}</td>
                        <td>
                            ${docctx.owner.title}
                        </td>
                        <td>
                            ${docctx.owner.filename}
                        </td>
                        <td>
                            ${docctx.owner?.type?.getI10n('value')}
                        </td>
                        <td>
                            %{--//Vorerst alle Umfrage Dokumente als geteilt nur Kennzeichen--}%
                            <span class="la-popup-tooltip la-delay" data-content="${message(code: 'property.share.tooltip.on')}">
                                <i class="green alternate share icon"></i>
                            </span>
                        </td>
                        <td class="x">
                            <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">

                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue button la-modern-button" target="_blank"><i
                                        class="download icon"></i></g:link>
                                <g:if test="${editable && !docctx.sharedFrom}">
                                    <button type="button" class="ui icon blue button la-modern-button la-popup-tooltip la-delay" data-semui="modal"
                                            href="#modalEditDocument_${docctx.id}"
                                            data-content="${message(code: "template.documents.edit")}"
                                            aria-label="${message(code: 'ariaLabel.change.universal')}">
                                        <i class="pencil icon"></i></button>
                                    <g:link controller="${controllerName}" action="deleteDocuments"
                                            class="ui icon negative button la-modern-button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                            data-confirm-term-how="delete"
                                            params='[surveyConfigID: surveyConfig.id, id: surveyInfo.id, deleteId: "${docctx.id}", redirectAction: "${actionName}"]'
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="trash alternate outline icon"></i>
                                    </g:link>
                                </g:if>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

            <g:each in="${surveyConfig.getCurrentDocs()}" var="docctx">
                <laser:render template="/templates/documents/modal"
                          model="${[ownobj: surveyConfig, owntp: surveyConfig, docctx: docctx, doc: docctx.owner]}"/>
            </g:each>
        </semui:form>
    </div>

</g:if>

</body>
</html>
