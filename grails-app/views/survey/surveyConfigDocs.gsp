<%@ page import="de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.SurveyConfig; de.laser.helper.RDStore;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig?.getConfigNameShort()}"/>
    </g:if>
    <semui:crumb message="surveyConfigDocs.label" class="active"/>
</semui:breadcrumbs>


<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="surveyConfigDocs"/>
</h1>

<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:messages data="${flash}"/>

<br />


<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig?.type == 'Subscription'}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.id}">
            ${surveyConfig?.subscription?.name}
        </g:link>
    </g:if>
    <g:else>
        ${surveyConfig?.getConfigNameShort()}
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


                <div class="ui floating circular label">${config?.getCurrentDocs()?.size() ?: 0}</div>
            </g:link>
        </g:each>
    </div>
</div>
--}%
    <div class="sixteen wide stretched column">

        <semui:form>

            <div class="four wide column">
                <button type="button" class="ui icon button right floated" data-semui="modal"
                        data-href="#modalCreateDocument"><i class="plus icon"></i></button>
                <g:render template="/templates/documents/modal"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
            </div>
            <br /><br />


            <table class="ui celled la-table table license-documents">
                <thead>
                <tr>
                    <th></th>
                    <th>${message(code: 'surveyConfigDocs.docs.table.title')}</th>
                    <th>${message(code: 'surveyConfigDocs.docs.table.fileName')}</th>
                    <th>${message(code: 'surveyConfigDocs.docs.table.type')}</th>
                    <th>${message(code: 'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${surveyConfig?.getCurrentDocs()}" var="docctx" status="i">
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

                        <td class="x">
                            %{--//Vorerst alle Umfrage Dokumente als geteilt nur Kennzeichen--}%
                            <span class="la-popup-tooltip la-delay" data-content="${message(code: 'property.share.tooltip.on')}">
                                <i class="green alternate share icon"></i>
                            </span>
                            <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">

                                <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon button"><i
                                        class="download icon"></i></g:link>
                                <g:if test="${editable && !docctx.sharedFrom}">
                                    <button type="button" class="ui icon button la-popup-tooltip la-delay" data-semui="modal"
                                            href="#modalEditDocument_${docctx.id}"
                                            data-content="${message(code: "template.documents.edit")}"><i
                                            class="pencil icon"></i></button>
                                    <g:link controller="${controllerName}" action="deleteDocuments"
                                            class="ui icon negative button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                            data-confirm-term-how="delete"
                                            params='[surveyConfigID: surveyConfig.id, id: surveyInfo.id, deleteId: "${docctx.id}", redirectAction: "${actionName}"]'>
                                        <i class="trash alternate icon"></i>
                                    </g:link>
                                </g:if>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

            <g:each in="${surveyConfig?.getCurrentDocs()}" var="docctx">
                <g:render template="/templates/documents/modal"
                          model="${[ownobj: surveyConfig, owntp: surveyConfig, docctx: docctx, doc: docctx.owner]}"/>
            </g:each>
        </semui:form>
    </div>

</g:if>

</body>
</html>
