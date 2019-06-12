<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig" %>
<%@ page import="com.k_int.kbplus.Org;" %>
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
    <semui:crumb controller="survey" action="currentSurveysConsortia" message="currentSurveys.label"/>
    <semui:crumb message="survey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${message(code: 'survey.label')}</h1>



<br>

<semui:messages data="${flash}"/>

<br>

<div class="ui grid">
    <div class="middle aligned row">
        <div class="two wide column">

            <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
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
                        <dl>
                            <dt>${message(code: 'surveyInfo.comment.label')}</dt>
                            <dd>${surveyInfo?.comment}</dd>
                        </dl>

                    </div>
                </div>
            </div>
        </div>

    </div>

</div>


<br>

<h2 class="ui left aligned icon header">${message(code: 'surveyConfigs.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<br>

<div class="ui grid">
    <div class="sixteen wide column">
        <g:if test="${surveyConfigs}">
            <div class="ui styled fluid accordion">

                <g:each in="${surveyConfigs}" var="config" status="i">

                    <div class="title active"><i class="dropdown icon"></i>

                    ${config?.getConfigNameShort()}

                    <div class="ui label circular ${(config?.type == 'Subscription') ? 'black' : 'blue'}">${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}</div>

                    <g:if test="${config?.type != 'Subscription'}">
                        ${message(code: 'surveyProperty.type.label')}: ${config?.surveyProperty?.getLocalizedType()}</b>

                    </g:if>

                    </div>

                    <div class="content active">
                        <div class="accordion transition visible"
                             style="display: block !important;">

                            <div class="title"><i
                                    class="dropdown icon"></i>${message(code: 'surveyConfig.documents.label')}

                                <div class="ui circular label">${config?.documents.size() ?: 0}</div>
                            </div>

                            <div class="content">
                                <g:if test="${config?.documents}">
                                    <table class="ui celled la-table table license-documents">
                                        <thead>
                                        <tr>
                                            <th></th>
                                            <th>${message(code: 'surveyConfigDocs.docs.table.title', default: 'Title')}</th>
                                            <th>${message(code: 'surveyConfigDocs.docs.table.fileName', default: 'File Name')}</th>
                                            <th>${message(code: 'surveyConfigDocs.docs.table.type', default: 'Type')}</th>
                                            <th>${message(code: 'default.actions', default: 'Actions')}</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <g:each in="${config?.getCurrentDocs()}" var="docctx" status="s">
                                            <tr>
                                                <td>${s + 1}</td>
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

                                                        <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                                class="ui icon button"><i
                                                                class="download icon"></i></g:link>
                                                    </g:if>
                                                </td>
                                            </tr>

                                        </g:each>
                                        </tbody>
                                    </table>
                                </g:if>

                            </div>

                            <div class="title"><i
                                    class="dropdown icon"></i>${message(code: 'surveyConfig.orgs.label')}

                                <div class="ui circular label">${config?.orgs?.size() ?: 0}</div>
                            </div>

                            <div class="content">
                                <g:render template="allParticipants" model="[surveyConfig: config]"/>
                            </div>

                            <g:if test="${config?.type == 'Subscription'}">
                                <div class="title"><i
                                        class="dropdown icon"></i>${message(code: 'surveyProperty.plural.label')}

                                    <div class="ui circular label">${config?.surveyProperties?.size() ?: 0}</div>
                                </div>

                                <div class="content">

                                    <g:if test="${config?.surveyProperties}">
                                        <table class="ui celled sortable table la-table">
                                            <thead>
                                            <tr>
                                                <th>${message(code: 'surveyProperty.name')}</th>
                                                <th>${message(code: 'surveyProperty.type.label')}</th>
                                            </tr>

                                            </thead>
                                            <g:each in="${config?.surveyProperties.sort {
                                                it?.surveyProperty?.getI10n('name')
                                            }}" var="prop" status="x">
                                                <tr>

                                                    <td>
                                                        ${prop?.surveyProperty?.getI10n('name')}
                                                    </td>
                                                    <td>
                                                        ${message(code: 'surveyConfigs.surveyPropToSub')}
                                                        <br>
                                                        <b>${message(code: 'surveyProperty.type.label')}: ${prop?.surveyProperty?.getLocalizedType()}</b>

                                                    </td>
                                                </tr>
                                            </g:each>
                                        </table>
                                    </g:if>

                                </div>
                            </g:if>
                        </div>
                    </div>

                </g:each>
            </div>
        </g:if>
        <g:else>
            <p><b>${message(code: 'surveyConfigs.noConfigList')}</b></p>
        </g:else>
    </div>
</div>



<br>
<g:if test="${editable}">
    <g:link controller="survey" action="processOpenSurvey" id="${surveyInfo.id}"
            class="ui button">${message(code: 'openSurvey.button')}</g:link>
</g:if>

<r:script>
    $(document).ready(function () {
        $('.ui.accordion')
                .accordion()
        ;
    });
</r:script>

</body>
</html>
