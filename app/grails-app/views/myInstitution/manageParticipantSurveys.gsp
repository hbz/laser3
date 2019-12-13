<%@ page import="com.k_int.kbplus.SurveyInfo; de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyResult" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'manageParticipantSurveys.header')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="manageParticipantSurveys.header" class="active"/>
</semui:breadcrumbs>
<br>
<h1 class="ui left floated aligned icon header la-clear-before">
    <semui:headerIcon/>${message(code: 'manageParticipantSurveys.header')}
    <semui:totalNumber total="${countSurveys.values().sum { it }}"/>
</h1>

<semui:messages data="${flash}"/>

<g:render template="../templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="manageParticipantSurveys" controller="myInstitution" method="post" id="${params.id}"
            params="[tab: params.tab]" class="ui small form">

        <div class="three fields">
            <div class="field">
                <label for="name">${message(code: 'surveyInfo.name.label')}
                </label>

                <div class="ui input">
                    <input type="text" id="name" name="name"
                           placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
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

        <div class="four fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.type.label')}</label>
                <laser:select class="ui dropdown" name="type"
                              from="${RefdataCategory.getAllRefdataValues('Survey Type')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.type}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label', default: 'Filter')}">
            </div>

        </div>
    </g:form>
</semui:filter>

<g:if test="${participant}">
    <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <table class="ui table la-table la-table-small">
        <tbody>
        <tr>
            <td>
                <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                ${choosenOrg?.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <g:render template="/templates/cpa/person_details"
                                  model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:if>
            </td>
        </tr>
        </tbody>
    </table>
</g:if>


<div>

    <semui:form>

        <semui:tabs actionName="${actionName}">
            <semui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'new']}" text="neu" tab="new"
                            counts="${countSurveys?.new}"/>
            <semui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'processed']}" text="bearbeitet" tab="processed"
                            counts="${countSurveys?.processed}"/>
            <semui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'finish']}" text="abgeschlossen" tab="finish"
                            counts="${countSurveys?.finish}"/>
            <semui:tabsItem controller="myInstitution" action="manageParticipantSurveys" class="ui red"
                            countsClass="red"
                            params="${[id: params.id, tab: 'notFinish']}" text="vorsorgliche Kündigungen"
                            tab="notFinish"
                            counts="${countSurveys?.notFinish}"/>
        </semui:tabs>

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <g:sortableColumn params="${params}" property="surveyInfo.name"
                                  title="${message(code: 'surveyInfo.slash.name')}"/>
                <g:sortableColumn params="${params}" property="surveyInfo.type"
                                  title="${message(code: 'surveyInfo.type.label')}"/>
                <g:sortableColumn params="${params}" property="surveyInfo.endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
                <th>${message(code: 'surveyProperty.plural.label')}</th>
                <th><g:message code="surveyInfo.finished"/></th>
                <g:if test="${params.tab == 'finish'}">
                    <th><g:message code="surveyInfo.finishedDate"/></th>
                </g:if>
                <th class="la-action-info">${message(code: 'default.actions')}</th>
            </tr>

            </thead>
            <g:each in="${surveyResults}" var="surveyResult" status="i">

                <g:set var="surveyConfig"
                       value="${com.k_int.kbplus.SurveyConfig.get(surveyResult.key)}"/>

                <g:set var="surveyInfo"
                       value="${surveyConfig.surveyInfo}"/>

                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <td>
                        <div class="la-flexbox">
                            <g:if test="${surveyConfig?.isSubscriptionSurveyFix}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: "surveyConfig.isSubscriptionSurveyFix.label.info2")}">
                                    <i class="yellow icon envelope large "></i>
                                </span>
                            </g:if>
                            <i class="icon chart pie la-list-icon"></i>
                            <g:link controller="survey" action="show" id="${surveyInfo?.id}" class="ui ">
                                ${surveyConfig?.getSurveyName()}
                            </g:link>
                        </div>
                    </td>
                    <td>
                        ${surveyInfo.type.getI10n('value')} (${surveyInfo.isSubscriptionSurvey ? message(code: 'subscriptionSurvey.label') : message(code: 'generalSurvey.label')})
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo?.endDate}"/>
                    </td>

                    <td class="center aligned">

                        <g:if test="${surveyConfig && !surveyConfig?.pickAndChoose}">
                            <g:if test="${surveyConfig?.type == 'Subscription'}">
                                <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo?.id}"
                                        params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                    <div class="ui circular label">
                                        ${surveyConfig?.surveyProperties?.size() ?: 0}
                                    </div>
                                </g:link>
                            </g:if>
                        </g:if>
                    </td>

                    <td class="center aligned">
                        <semui:surveyFinishIcon participant="${Org.get(params.id)}" surveyConfig="${surveyConfig}" surveyOwnerView="${true}"/>
                    </td>
                    <g:if test="${params.tab == 'finish'}">
                        <td class="center aligned">
                            <semui:surveyFinishDate participant="${Org.get(params.id)}" surveyConfig="${surveyConfig}"/>
                        </td>
                    </g:if>
                    <td>
                        <g:if test="${!surveyConfig?.pickAndChoose}">
                            <span class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                                <g:link action="surveyParticipantConsortiaNew" id="${participant?.id}"
                                        params="[surveyConfig: surveyConfig?.id]"
                                        class="ui icon button">
                                    <i class="write icon"></i>
                                </g:link>
                            </span>
                        </g:if>

                        <g:if test="${surveyConfig?.pickAndChoose}">
                            <g:link controller="survey" action="showEntitlementsRenew"
                                    id="${surveyConfig?.id}" params="[participant: participant?.id]"
                                    class="ui icon button"><i
                                    class="write icon"></i>
                            </g:link>
                        </g:if>

                    </td>

                </tr>

            </g:each>
        </table>
    </semui:form>
</div>
</div>

%{--<g:if test="${countSurveys}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${countSurveys."${params.tab}"}"/>
</g:if>--}%

</body>
</html>
