<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection/>
<!doctype html>



<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'currentSurveys.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="currentSurveys.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>


<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerTitleIcon
        type="Survey"/>${message(code: 'currentSurveys.label')}
<semui:totalNumber total="${surveysCount}"/>
</h1>

<semui:messages data="${flash}"/>

<g:render template="/templates/filter/javascript"/>
<semui:filter showFilterButton="true">
    <g:form action="currentSurveysConsortia" controller="survey" method="post" class="ui small form"
            params="[tab: params.tab]">
        <div class="three fields">
            <div class="field">
                <label for="name">${message(code: 'surveyInfo.name.label')}
                </label>

                <div class="ui input">
                    <input type="text" id="name" name="name"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.name}"/>
                </div>
            </div>


            <div class="field fieldcontain">
                <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                                  placeholder="filter.placeholder" value="${params.startDate}"/>
            </div>


            <div class="field fieldcontain">
                <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                                  placeholder="filter.placeholder" value="${params.endDate}"/>
            </div>

        </div>

        <div class="three fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.type.label')}</label>
                <laser:select class="ui dropdown" name="type"
                              from="${RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.SURVEY_TYPE)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.type}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <label>${message(code: 'menu.my.providers')}</label>
                <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${providers.sort { it.name }}" var="provider">
                        <option <%=(params.list('filterPvd').contains(provider.id.toString())) ? 'selected="selected"' : ''%>
                        value="${provider.id}" ">
                        ${provider.name}
                        </option>
                    </g:each>
                </select>

            </div>

            <div class="field">
                <label>${message(code: 'menu.my.subscriptions')}</label>
                <select id="filterSub" name="filterSub" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${subscriptions.sort { it }}" var="sub">
                        <option <%=(params.list('filterSub').contains(sub)) ? 'selected="selected"' : ''%>
                        value="${sub}" ">
                        ${sub}
                        </option>
                    </g:each>
                </select>

            </div>

        </div>

        <div class="three fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.status.label')}</label>
                <select id="filterStatus" name="filterStatus" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.SURVEY_STATUS)}"
                            var="status">
                        <option <%=(params.list('filterStatus').contains(status.id.toString())) ? 'selected="selected"' : ''%>
                                value="${status.id}" title="${status.getI10n('value')}">
                            ${status.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label>${message(code: 'surveyInfo.options')}</label>

                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkMandatory">${message(code: 'surveyInfo.isMandatory.label')}</label>
                            <input id="checkMandatory" name="mandatory" type="checkbox"
                                   <g:if test="${params.mandatory}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubSurveyUseForTransfer">${message(code: 'surveyConfig.subSurveyUseForTransfer.label')}</label>
                            <input id="checkSubSurveyUseForTransfer" name="checkSubSurveyUseForTransfer" type="checkbox"
                                   <g:if test="${params.checkSubSurveyUseForTransfer}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <g:link controller="survey" action="currentSurveysConsortia"
                        class="ui reset primary button">${message(code: 'default.button.reset.label')}</g:link>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>

        </div>
    </g:form>
</semui:filter>

<semui:form>

    <g:if test="${surveys}">

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>

                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>

                <g:sortableColumn params="${params}" property="surInfo.name"
                                  title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>
                <th rowspan="2" scope="col">${message(code: 'surveyInfo.type.label')}</th>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                  property="surInfo.startDate"
                                  title="${message(code: 'default.startDate.label')}"/>
                <th rowspan="2" scope="col">${message(code: 'surveyInfo.status.label')}</th>
                <th rowspan="2" scope="col">${message(code: 'surveyInfo.isMandatory.label')}</th>
                <th rowspan="2" scope="col">${message(code: 'surveyProperty.plural.label')}</th>


                <th rowspan="2" scope="col">${message(code: 'surveyConfigDocs.label')}</th>

                <th rowspan="2" scope="col">${message(code: 'surveyParticipants.label')}</th>


                <th rowspan="2" scope="col">${message(code: 'surveyCostItems.label')}</th>



                <th rowspan="2" scope="col">${message(code: 'surveyInfo.finished')}</th>
                <th rowspan="2" scope="col"></th>

            </tr>
            <tr>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                  property="surInfo.endDate"
                                  title="${message(code: 'default.endDate.label')}"/>
            </tr>
            </thead>
            <g:each in="${surveys}" var="survey" status="i">

                <g:set var="surveyInfo"
                       value="${survey[0]}"/>

                <g:set var="surveyConfig"
                       value="${survey[1]}"/>


                <g:set var="participantsFinish"
                       value="${com.k_int.kbplus.SurveyResult.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).participant?.flatten()?.unique { a, b -> a.id <=> b.id }}"/>

                <g:set var="participantsTotal"
                       value="${surveyConfig?.orgs}"/>

                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>

                    <td>
                        <div class="la-flexbox">
                            <g:link controller="survey" action="show" id="${surveyInfo?.id}" class="ui ">
                                ${surveyConfig?.getSurveyName()}
                            </g:link>
                        </div>
                    </td>

                    <td class="center aligned">
                        ${surveyInfo.type.getI10n('value')} (${surveyInfo.isSubscriptionSurvey ? message(code: 'subscriptionSurvey.label') : message(code: 'generalSurvey.label')})
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyInfo?.startDate}"/>
                        <br>
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyInfo?.endDate}"/>
                    </td>

                    <td class="center aligned">
                        ${surveyInfo.status.getI10n('value')}
                    </td>

                    <td class="center aligned">
                        <g:if test="${surveyInfo.isMandatory}">
                            <i class='check green icon'></i>
                        </g:if>
                    </td>

                    <td class="center aligned">

                        <g:if test="${surveyConfig && !surveyConfig.pickAndChoose}">
                                <g:link controller="survey" action="show" id="${surveyInfo?.id}"
                                        params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                    <div class="ui circular ${surveyConfig?.configFinish ? "green" : ""} label">
                                        ${surveyConfig?.surveyProperties?.size() ?: 0}
                                    </div>
                                </g:link>
                        </g:if>

                    </td>

                        <td class="center aligned">
                            <g:if test="${surveyConfig}">
                                <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo?.id}"
                                        params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                    <div class="ui circular label">
                                        ${surveyConfig?.getCurrentDocs()?.size() ?: 0}
                                    </div>
                                </g:link>
                            </g:if>
                        </td>


                    <td class="center aligned">
                        <g:if test="${surveyConfig}">
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular ${participantsFinish?.size() == participantsTotal?.size() ? "green" : surveyConfig?.configFinish ? "yellow" : ""} label">
                                    ${participantsFinish?.size() ?: 0} / ${surveyConfig?.orgs?.size() ?: 0}
                                </div>
                            </g:link>
                        </g:if>
                    </td>


                    <td class="center aligned">
                        <g:if test="${surveyConfig && surveyConfig.type == 'Subscription' && !surveyConfig.pickAndChoose && surveyInfo.type == de.laser.helper.RDStore.SURVEY_TYPE_RENEWAL}">
                            <g:link controller="survey" action="surveyCostItems" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular ${surveyConfig?.costItemsFinish ? "green" : ""} label">
                                    ${surveyConfig?.getSurveyConfigCostItems()?.size() ?: 0}
                                </div>
                            </g:link>
                        </g:if>
                    </td>

                    <td class="center aligned">
                        <g:if test="${surveyConfig && surveyConfig?.type == 'Subscription' && !surveyConfig?.pickAndChoose}">
                            <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]"
                                    class="ui icon">
                                <div class="ui circular ${(participantsFinish?.size() == participantsTotal?.size()) ? "green" : (participantsFinish?.size() > 0) ? "yellow" : ""} label">
                                    <g:if
                                            test="${participantsFinish && participantsTotal}">
                                        <g:formatNumber
                                                number="${(participantsFinish?.size() / participantsTotal?.size()) * 100}"
                                                minFractionDigits="2"
                                                maxFractionDigits="2"/>%
                                    </g:if>
                                    <g:else>
                                        0%
                                    </g:else>
                                </div>
                            </g:link>
                        </g:if>
                        <g:if test="${surveyConfig && surveyConfig?.type == 'Subscription' && surveyConfig?.pickAndChoose}">

                            <g:set var="participantsTitleSurveyFinish"
                                   value="${com.k_int.kbplus.SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig)}"/>

                            <g:set var="participantsTitleSurveyTotal"
                                   value="${com.k_int.kbplus.SurveyOrg.findAllBySurveyConfig(surveyConfig)}"/>
                            <g:link controller="survey" action="surveyTitlesEvaluation" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]"
                                    class="ui icon">
                                <div class="ui circular ${(participantsTitleSurveyFinish.size() == participantsTitleSurveyTotal.size()) ? "green" : (participantsTitleSurveyFinish.size() > 0) ? "yellow" : ""} label">
                                    <g:if
                                            test="${participantsTitleSurveyFinish && participantsTitleSurveyTotal}">
                                        <g:formatNumber
                                                number="${(participantsTitleSurveyFinish.size() / participantsTitleSurveyTotal.size()) * 100}"
                                                minFractionDigits="2"
                                                maxFractionDigits="2"/>%
                                    </g:if>
                                    <g:else>
                                        0%
                                    </g:else>
                                </div>
                            </g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:link controller="survey" action="show" id="${surveyInfo?.id}" class="ui button icon">
                            <i class="pencil icon"></i>
                        </g:link>
                        %{--<g:if test="${surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING]}">
                            <g:link class="ui icon negative button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.survey", args: [surveyConfig?.getSurveyName()])}"
                                    data-confirm-term-how="delete"
                                    controller="survey" action="deleteSurveyInfo"
                                    id="${surveyInfo?.id}">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:if>--}%
                    </td>


                </tr>

            </g:each>
        </table>

    </g:if>
    <g:else>
        <g:if test="${filterSet}">
            <br><strong><g:message code="filter.result.empty.object"
                                   args="${[message(code: "survey.plural")]}"/></strong>
        </g:if>
        <g:else>
            <br><strong><g:message code="result.empty.object" args="${[message(code: "survey.plural")]}"/></strong>
        </g:else>
    </g:else>

</semui:form>

<g:if test="${surveysCount}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${surveysCount}"/>
</g:if>

</body>
</html>
