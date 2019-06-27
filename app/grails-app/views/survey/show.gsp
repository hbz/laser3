<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyShow.label')}</title>

</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>

    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:messages data="${flash}"/>


<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">
            <div class="ui two stackable cards">
                <div class="ui card la-time-card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.startDate.label')}</dt>
                            <dd><semui:xEditable owner="${surveyInfo}" field="startDate" type="date"/></dd>

                        </dl>
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.endDate.label')}</dt>
                            <dd><semui:xEditable owner="${surveyInfo}" field="endDate" type="date"/></dd>

                        </dl>

                    </div>
                </div>

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">${message(code: 'surveyInfo.status.label')}</dt>
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

                    </div>
                </div>
            </div>

            <g:set var="finish"
                   value="${com.k_int.kbplus.SurveyResult.findAllBySurveyConfigInListAndFinishDateIsNotNull(s?.surveyConfigs).size()}"/>
            <g:set var="total"
                   value="${com.k_int.kbplus.SurveyResult.findAllBySurveyConfigInList(s?.surveyConfigs).size()}"/>

            <g:set var="finishProcess" value="${(finish != 0 && total != 0) ? (finish / total) * 100 : 0}"/>
            <g:if test="${finishProcess > 0 || surveyInfo?.status?.id == de.laser.helper.RDStore.SURVEY_SURVEY_STARTED.id}">
                <div class="ui card">

                    <div class="content">
                        <div class="ui indicating progress" id="finishProcess" data-value="${finishProcess}"
                             data-total="100">
                            <div class="bar">
                                <div class="progress">${finishProcess}</div>
                            </div>

                            <div class="label"
                                 style="background-color: transparent">${finishProcess}% <g:message
                                    code="surveyInfo.finish"/></div>
                        </div>
                    </div>
                </div>
            </g:if>
            <br>

            <g:if test="${surveyConfigs}">
                <div class="ui styled fluid accordion">

                    <g:each in="${surveyConfigs}" var="config" status="i">

                        <div class="title active" style="background-color: ${(config?.configFinish && config?.costItemsFinish) ? 'lime' : ''}"><i
                            class="dropdown icon"></i>

                        ${config?.getConfigName()}

                        <div class="ui label circular ${(config?.type == 'Subscription') ? 'black' : 'blue'}">${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}</div>

                        <g:if test="${config?.type != 'Subscription'}">
                            ${message(code: 'surveyProperty.type.label')}: ${config?.surveyProperty?.getLocalizedType()}</br>

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
                                                        %{--//Vorerst alle Umfrage Dokumente als geteilt nur Kennzeichen--}%
                                                        <span data-tooltip="${message(code:'property.share.tooltip.on')}">
                                                            <i class="green alternate share icon"></i>
                                                        </span>
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
                                <g:if test="${config?.type == 'SurveyProperty'}">
                                    <div class="title"><i
                                            class="dropdown icon"></i>${message(code: 'surveyParticipants.label')}

                                        <div class="ui circular label">${config?.orgs?.size() ?: 0}</div>
                                    </div>

                                    <div class="content">
                                        <g:render template="allParticipants" model="[surveyConfig: config]"/>
                                    </div>
                                </g:if>

                                <g:if test="${config?.type == 'Subscription'}">

                                    <g:set var="costItems"
                                           value="${com.k_int.kbplus.CostItem.findAllBySurveyOrgInList(config?.orgs)}"/>

                                    <div class="title"
                                         style="background-color: ${config?.costItemsFinish ? 'lime' : ''}"><i
                                            class="dropdown icon"></i>${message(code: 'surveyParticipants.label')}

                                        <div class="ui circular label">${config?.orgs?.size() ?: 0}</div>
                                    </div>

                                    <div class="content compact">

                                        <g:render template="/templates/filter/orgFilterTable"
                                                  model="[orgList       : config?.orgs.org,
                                                          tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveyCostItem'],
                                                          tableID       : 'costTable',
                                                          surveyConfig  : config,
                                                          editable      : false
                                                  ]"/>
                                    </div>


                                    <div class="title"
                                         style="background-color: ${config?.configFinish ? 'lime' : ''}"><i
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

                                                            <g:if test="${prop?.surveyProperty?.getI10n('explain')}">
                                                                <span class="la-long-tooltip"
                                                                      data-position="right center" data-variation="tiny"
                                                                      data-tooltip="${prop?.surveyProperty?.getI10n('explain')}">
                                                                    <i class="question circle icon"></i>
                                                                </span>
                                                            </g:if>

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
</r:script>

</body>
</html>
