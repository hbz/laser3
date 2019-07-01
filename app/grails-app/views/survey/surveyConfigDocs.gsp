<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[ params:params ]}"/>

<semui:controlButtons>
    <g:render template="actions" />
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>



<g:render template="nav" />

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}" />

<semui:messages data="${flash}" />



<br>


<h2 class="ui left aligned icon header">${message(code: 'surveyConfigs.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>


<br>

<p><b>${message(code: 'surveyConfigDocs.info')}</b></p>
<br>


<g:if test="${surveyConfigs}">

    <div class="ui grid">
        <div class="four wide column">
            <div class="ui vertical fluid menu">
                <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

                    <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                            controller="survey" action="surveyConfigDocs"
                            id="${config?.surveyInfo?.id}" params="[surveyConfigID: config?.id]">

                        <h5 class="ui header">${config?.getConfigNameShort()}</h5>
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
                <br><br>


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
                                <span data-tooltip="${message(code:'property.share.tooltip.on')}">
                                    <i class="green alternate share icon"></i>
                                </span>
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
