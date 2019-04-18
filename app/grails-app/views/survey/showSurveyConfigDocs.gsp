<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="survey" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'survey.label')}</h1>


<g:render template="steps"/>
<br>

<semui:messages data="${flash}"/>

<br>
<semui:form>
    <div class="ui grid">
        <div class="middle aligned row">
            <div class="two wide column">

                <g:link controller="survey" action="showSurveyConfig" id="${surveyInfo.id}"
                        class="ui huge button"><i class="angle left aligned icon"></i></g:link>

            </div>

            <div class="twelve wide column">

                <div class="la-inline-lists">
                    <div class="ui card">
                        <div class="content">

                            <div class="header">
                                <div class="ui grid">
                                    <div class="twelve wide column">
                                        ${message(code: 'showSurveyInfo.step.first.title')}
                                    </div>
                                </div>
                            </div>
                            <dl>
                                <dt>${message(code: 'surveyInfo.status.label', default: 'Survey Status')}</dt>
                                <dd>${surveyInfo.status?.getI10n('value')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</dt>
                                <dd>${surveyInfo.name}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.startDate.label')}</dt>
                                <dd><g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyInfo.startDate ?: null}"/></dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.endDate.label')}</dt>
                                <dd><g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyInfo.endDate ?: null}"/></dd>
                            </dl>

                            <dl>
                                <dt>${message(code: 'surveyInfo.type.label')}</dt>
                                <dd>${com.k_int.kbplus.RefdataValue.get(surveyInfo?.type?.id)?.getI10n('value')}</dd>
                            </dl>

                        </div>
                    </div>
                </div>
            </div>

            <div class="two wide column">
                <g:if test="${surveyConfigs.size() > 0}">

                    <g:link controller="survey" action="showSurveyParticipants" id="${surveyInfo.id}"
                            class="ui huge button"><i class="angle right icon"></i></g:link>

                </g:if>
            </div>
        </div>

    </div>

</semui:form>
<br>


<h2 class="ui left aligned icon header">${message(code: 'showSurveyConfig.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<br>

<p><b>${message(code: 'showSurveyConfigDocs.info')}</b></p>
<br>


<g:if test="${surveyConfigs}">

    <div class="ui grid">
        <div class="four wide column">
            <div class="ui vertical fluid menu">
                <g:each in="${surveyConfigs.sort{it.configOrder}}" var="config" status="i">

                    <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                            controller="survey" action="showSurveyConfigDocs"
                            id="${config?.surveyInfo?.id}" params="[surveyConfigID: config?.id]">

                        <h5 class="ui header">${config?.getConfigName()}</h5>
                        ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}


                        <div class="ui floating circular label">${config?.getCurrentDocs()?.size() ?: 0}</div>
                    </g:link>
                </g:each>
            </div>
        </div>

        <div class="twelve wide stretched column">

            <semui:form>

                <div class="four wide column">
                    <button type="button" class="ui icon button right floated" data-semui="modal"
                            data-href="#modalCreateDocument"><i class="plus icon"></i></button>
                    <g:render template="/templates/documents/modal"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>
                </div>


                <table class="ui celled la-table table license-documents">
                    <thead>
                    <tr>
                        <th></th>
                        <th>${message(code: 'showSurveyConfigDocs.docs.table.title', default: 'Title')}</th>
                        <th>${message(code: 'showSurveyConfigDocs.docs.table.fileName', default: 'File Name')}</th>
                        <th>${message(code: 'showSurveyConfigDocs.docs.table.type', default: 'Type')}</th>
                        <th>${message(code: 'default.actions', default: 'Actions')}</th>
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
                                <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">

                                    <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon button"><i
                                            class="download icon"></i></g:link>
                                    <g:if test="${editable && !docctx.sharedFrom}">
                                        <button type="button" class="ui icon button" data-semui="modal"
                                                href="#modalEditDocument_${docctx.id}"
                                                data-tooltip="${message(code: "template.documents.edit")}"><i
                                                class="pencil icon"></i></button>
                                        <g:link controller="${controllerName}" action="deleteDocuments"
                                                class="ui icon negative button js-open-confirm-modal"
                                                data-confirm-term-what="document"
                                                data-confirm-term-what-detail="${docctx.owner.title}"
                                                data-confirm-term-how="delete"
                                                params='[instanceId: "${surveyConfig.id}", deleteId: "${docctx.id}", redirectAction: "${redirect}"]'>
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

    </div>

</g:if>
<g:else>
    <p><b>${message(code: 'showSurveyConfig.noConfigList')}</b></p>
</g:else>


<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>

</body>
</html>
