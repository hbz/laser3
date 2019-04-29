<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataValue;" %>
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

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'survey.label')}</h1>

<br>

<semui:messages data="${flash}"/>

<br>


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
                <dt>${message(code: 'surveyInfo.owner.label')}</dt>
                <dd>${surveyInfo?.owner}</dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.comment.label')}</dt>
                <dd>${surveyInfo?.comment}</dd>
            </dl>

        </div>
    </div>
</div>

<br>

<h3>
    <p>
        <g:message code="surveyResult.info" args="[surveyInfo?.owner]"/>
    </p>
</h3>

<br>

<h2 class="ui left aligned icon header">${message(code: 'surveyConfig.label')} <semui:totalNumber
        total="${surveyResults.size()}"/></h2>

<div class="ui grid">

    <semui:form>
        <div class="ui container">
            <div class="ui divided items">
                <g:each in="${surveyResults}" var="surveyResult" status="i">
                    <div class="item ">

                        <g:if test="${surveyResult.surveyConfig?.type == 'Subscription'}">
                            <div class="ui icon">
                                <i class="circular orange inverted icon folder open"></i>
                            </div>
                        </g:if>

                        <g:if test="${surveyResult.surveyConfig?.type == 'SurveyProperty'}">
                            <div class="ui icon">

                            </div>
                        </g:if>

                        <div class="content">

                            <g:if test="${surveyResult.surveyConfig?.type == 'Subscription'}">

                                <g:set var="childSub"
                                       value="${surveyResult.surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(institution)}"/>

                                <g:if test="${childSub}">
                                    <g:link controller="subscription" class="header" action="show"
                                            id="${childSub?.id}">${childSub?.dropdownNamingConvention()}</g:link>
                                </g:if>
                                <g:else>
                                    <a class="header">
                                        ${surveyResult.surveyConfig?.subscription?.dropdownNamingConvention()}
                                    </a>
                                </g:else>
                            </g:if>

                            <g:if test="${surveyResult.surveyConfig?.type == 'SurveyProperty'}">
                                <a class="header">${surveyResult.surveyConfig?.surveyProperty?.getI10n('name')}</a>
                            </g:if>

                            <div class="meta">
                                <div class="ui label blue">${com.k_int.kbplus.SurveyConfig.getLocalizedValue(surveyResult.surveyConfig?.type)}</div>

                            </div>

                            <div class="description">

                                <div class="ui ">

                                    <i class="circular info icon la-popup-tooltip la-delay"
                                       data-html="<div class='ui three column divided center aligned grid'>
                                                    <div class='column'>
                                                        <h4 class='ui header'>${g.message(code: 'surveyProperty.introduction.label')}</h4>
                                                        <p>${surveyResult.type?.introduction}</p>

                                                    </div>
                                                    <div class='column'>
                                                        <h4 class='ui header'>${g.message(code: 'surveyProperty.explain.label')}</h4>
                                                        <p>${surveyResult.type?.explain}</p>

                                                    </div>
                                                    <div class='column'>
                                                        <h4 class='ui header'>${g.message(code: 'surveyProperty.comment.label')}</h4>
                                                        <p>${surveyResult.type?.comment}</p>

                                                    </div>
                                        </div>"
                                       data-variation="wide"></i>

                                    ${surveyResult.type?.getI10n('name')}

                                    <g:if test="${surveyResult.type?.type == Integer.toString()}">
                                        <semui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                                    </g:if>
                                    <g:elseif test="${surveyResult.type?.type == String.toString()}">
                                        <semui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                                    </g:elseif>
                                    <g:elseif test="${surveyResult.type?.type == BigDecimal.toString()}">
                                        <semui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                                    </g:elseif>
                                    <g:elseif test="${surveyResult.type?.type == Date.toString()}">
                                        <semui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                                    </g:elseif>
                                    <g:elseif test="${surveyResult.type?.type == URL.toString()}">
                                        <semui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                                         overwriteEditable="${overwriteEditable}"
                                                         class="la-overflow la-ellipsis"/>
                                        <g:if test="${surveyResult.value}">
                                            <semui:linkIcon/>
                                        </g:if>
                                    </g:elseif>
                                    <g:elseif test="${surveyResult.type?.type == RefdataValue.toString()}">
                                        <semui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                                config="${surveyResult.type?.refdataCategory}"/>
                                    </g:elseif>
                                </div>
                            </div>

                            <div class="extra">

                                <br>
                                <g:message code="surveyResult.comment"/>: <semui:xEditable owner="${surveyResult}"
                                                                                           type="textarea"
                                                                                           field="comment"/>


                                <br>
                                <g:each in="${surveyResult.surveyConfig?.getCurrentDocs().sort { it?.owner?.title }}"
                                        var="docctx" status="s">

                                    <g:if test="${((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3))}">

                                        <g:link controller="docstore" id="${docctx.owner.uuid}"
                                                class="ui icon mini button ">
                                            <i class="download icon"></i>
                                            ${docctx.owner.title ?: docctx.owner.filename}
                                            (${docctx.owner?.type?.getI10n('value')})

                                        </g:link>
                                    </g:if>
                                </g:each>

                            </div>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>
    </semui:form>

</div>

<br>
<br>

</div>

</body>
</html>
