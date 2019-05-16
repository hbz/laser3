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
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<semui:messages data="${flash}"/>


<div id="collapseableSubDetails" class="ui stackable grid">
    <div class="twelve wide column">

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


            <div class="clear-fix"></div>
        </div>


        <div class="ui grid">
            <div class="sixteen wide column">
                <g:if test="${surveyConfigs}">
                    <div class="ui styled fluid accordion">

                        <g:each in="${surveyConfigs}" var="config" status="i">

                            <div class="title active"><i class="dropdown icon"></i>

                            ${config?.getConfigName()}

                            <div class="ui label circular ${(config?.type == 'Subscription') ? 'black' : 'blue'}">${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}</div>

                            <g:if test="${config?.type != 'Subscription'}">
                                ${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(config?.surveyProperty?.type)}</b>

                                <g:if test="${config?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(config?.surveyProperty?.refdataCategory)}"
                                            var="refdataValue">
                                        <g:set var="refdataValues"
                                               value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                    </g:each>
                                    <br>
                                    (${refdataValues.join('/')})
                                </g:if>
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
                                                    <th>${message(code: 'showSurveyConfigDocs.docs.table.title', default: 'Title')}</th>
                                                    <th>${message(code: 'showSurveyConfigDocs.docs.table.fileName', default: 'File Name')}</th>
                                                    <th>${message(code: 'showSurveyConfigDocs.docs.table.type', default: 'Type')}</th>
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
                                            class="dropdown icon"></i>${message(code: 'surveyConfig.orgIDs.label')}

                                        <div class="ui circular label">${config?.orgIDs?.size() ?: 0}</div>
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
                                                        <th>${message(code: 'surveyProperty.name.label')}</th>
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
                                                                ${message(code: 'showSurveyConfig.surveyPropToSub')}
                                                                <br>
                                                                <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(prop?.surveyProperty?.type)}</b>

                                                                <g:if test="${prop?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                                                    <g:set var="refdataValues" value="${[]}"/>
                                                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(prop?.surveyProperty?.refdataCategory)}"
                                                                            var="refdataValue">
                                                                        <g:set var="refdataValues"
                                                                               value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                                                    </g:each>
                                                                    <br>
                                                                    (${refdataValues.join('/')})
                                                                </g:if>
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
                    <p><b>${message(code: 'showSurveyConfig.noConfigList')}</b></p>
                </g:else>
            </div>
        </div>

    </div><!-- .twelve -->

    <aside class="four wide column la-sidekick">
        <g:render template="asideSurvey" model="${[ownobj: surveyInfo, owntp: 'survey']}"/>
    </aside><!-- .four -->

</div><!-- .grid -->


<div id="magicArea"></div>

</body>
</html>
