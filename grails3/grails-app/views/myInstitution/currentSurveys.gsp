<%@ page import="de.laser.RefdataCategory; de.laser.helper.RDStore; de.laser.SurveyResult; de.laser.SurveyConfig; de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem;" %>
<laser:serviceInjection/>
<!doctype html>

<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'currentSurveys.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="currentSurveys.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="currentSurveys"
                    params="${params + [exportXLSX: true]}">${message(code: 'survey.exportSurveys')}</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="currentSurveys"
                    params="${params + [exportXLSX: true, surveyCostItems: true]}">${message(code: 'survey.exportSurveyCostItems')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>${institution.name} - ${message(code: 'currentSurveys.label')}
<semui:totalNumber total="${countSurveys.values().sum { it }}"/>
</h1>

<semui:messages data="${flash}"/>

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="currentSurveys" controller="myInstitution" method="post" class="ui small form" params="[tab: params.tab ]">
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


            <div class="field">
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${params.validOn}" />
            </div>

            <div class="field">
                <label>${message(code: 'default.valid_onYear.label')}</label>
                <select id="validOnYear" name="validOnYear" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <option value="all" <%=("all" in params.list('validOnYear')) ? 'selected="selected"' : ''%>>
                        ${message(code: 'default.select.all.label')}
                    </option>

                    <g:each in="${surveyYears}" var="surveyYear">
                        <option <%=(params.list('validOnYear').contains(surveyYear.toString())) ? 'selected="selected"' : ''%>
                                value="${surveyYear}" title="${surveyYear}">
                            ${surveyYear}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>

        <div class="three fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.owner.label')}</label>
                <g:select class="ui dropdown" name="owner"
                              from="${allConsortia}"
                              optionKey="id"
                              optionValue="name"
                              value="${params.owner}"
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

            <g:render template="/templates/properties/genericFilter" model="[propList: propList, hideFilterProp: true, label:message(code: 'subscription.property.search')]"/>

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
                            <label for="checkNoMandatory">${message(code: 'surveyInfo.isNotMandatory.label')}</label>
                            <input id="checkNoMandatory" name="noMandatory" type="checkbox"
                                   <g:if test="${params.noMandatory}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubSurveyUseForTransfer">${message(code: 'surveyconfig.subSurveyUseForTransfer.label')}</label>
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
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>

        </div>
    </g:form>
</semui:filter>


<semui:form>

    <%
        def tmpParams = params.clone()
        tmpParams.remove("tab")
    %>

    <semui:tabs actionName="${actionName}">
        <semui:tabsItem controller="myInstitution" action="currentSurveys"
                        params="${tmpParams+[id: params.id, tab: 'new']}" text="${message(code: "surveys.tabs.new")}" tab="new"
                        counts="${countSurveys.new}"/>
        <semui:tabsItem controller="myInstitution" action="currentSurveys"
                        params="${tmpParams+[id: params.id, tab: 'processed']}" text="${message(code: "surveys.tabs.processed")}" tab="processed"
                        counts="${countSurveys.processed}"/>
        <semui:tabsItem controller="myInstitution" action="currentSurveys"
                        params="${tmpParams+[id: params.id, tab: 'finish']}" text="${message(code: "surveys.tabs.finish")}" tab="finish"
                        counts="${countSurveys.finish}"/>
        <semui:tabsItem controller="myInstitution" action="currentSurveys" class="ui red" countsClass="red"
                        params="${tmpParams+[id: params.id, tab: 'termination']}" text="${message(code: "surveys.tabs.termination")}" tab="termination"
                        counts="${countSurveys.termination}"/>
        <semui:tabsItem controller="myInstitution" action="currentSurveys" class="ui orange" countsClass="orange"
                        params="${tmpParams+[id: params.id, tab: 'notFinish']}" text="${message(code: "surveys.tabs.notFinish")}" tab="notFinish"
                        counts="${countSurveys.notFinish}"/>

    </semui:tabs>

    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <th rowspan="2" class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>
            <g:sortableColumn params="${params}" property="surInfo.name"
                              title="${message(code: 'surveyInfo.slash.name')}"/>
            <g:sortableColumn params="${params}" property="surInfo.type"
                              title="${message(code: 'surveyInfo.type.label')}"/>
            <g:sortableColumn params="${params}" property="surInfo.endDate"
                              title="${message(code: 'default.endDate.label')}"/>
            <g:sortableColumn params="${params}" property="surInfo.owner"
                              title="${message(code: 'surveyInfo.owner.label')}"/>

            <th><g:message code="surveyInfo.finished"/></th>


            <g:if test="${params.tab == 'finish'}">
                <th><g:message code="surveyInfo.finishedDate"/></th>
            </g:if>

            <th class="la-action-info">${message(code:'default.actions.label')}</th>
        </tr>

        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">

            <g:set var="surveyConfig"
                   value="${SurveyConfig.get(surveyResult.key)}"/>

            <g:set var="surveyInfo"
                   value="${surveyConfig.surveyInfo}"/>

            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>
                <td>
                    <div class="la-flexbox">
                        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyconfig.subSurveyUseForTransfer.label.info2")}">
                                <i class="grey icon pie chart la-list-icon"></i>
                            </span>
                        </g:if>

                        <g:link controller="myInstitution" action="surveyInfos" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]"
                                class="ui">
                            ${surveyConfig.getSurveyName()}
                        </g:link>
                    </div>
                </td>
                <td>
                    <div class="ui label survey-${surveyInfo.type.value}">
                        ${surveyInfo.type.getI10n('value')}
                    </div>

                    <g:if test="${surveyInfo.isMandatory}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: "surveyInfo.isMandatory.label.info2")}">
                            <i class="yellow icon exclamation triangle"></i>
                        </span>
                    </g:if>
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.endDate}"/>
                </td>

                <td class="center aligned">

                    ${surveyInfo.owner}

                </td>


                <td class="center aligned">

                    <semui:surveyFinishIcon participant="${institution}" surveyConfig="${surveyConfig}" surveyOwnerView="${false}"/>

                </td>
                <g:if test="${params.tab == 'finish'}">
                    <td class="center aligned">
                        <semui:surveyFinishDate participant="${institution}" surveyConfig="${surveyConfig}"/>
                    </td>
                </g:if>

                <td class="x">

                    <g:if test="${editable}">
                            <span class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                                <g:link controller="myInstitution" action="surveyInfos" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                                        class="ui icon button blue la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                            </span>
                    </g:if>
                </td>

            </tr>

        </g:each>
    </table>
</semui:form>


%{--<g:if test="${countSurveys."${params.tab}"}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${countSurveys."${params.tab}"}"/>
</g:if>--}%

</body>
</html>
