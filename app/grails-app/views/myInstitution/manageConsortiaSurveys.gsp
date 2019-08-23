<%@ page import="com.k_int.kbplus.SurveyInfo; de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyResult" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'manageConsortiaSurveys.header')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="manageConsortiaSurveys.header" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        %{--<semui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="manageConsortiaSurveys"
                    params="${[exportXLS: true, participant: participant?.id]}">${message(code: 'survey.exportSurvey')}</g:link>
        </semui:exportDropdownItem>--}%
    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui left aligned icon header">
    <semui:headerIcon/>${message(code: 'manageConsortiaSurveys.header')}
    <semui:totalNumber total="${countSurvey}"/>
</h1>

<semui:messages data="${flash}"/>

<semui:filter>
    <g:form action="manageConsortiaSurveys" controller="myInstitution" method="get" id="${params.id}" class="form-inline ui small form">

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

            <div class="field fieldcontain">
                <label>${message(code: 'surveyInfo.status.label')}</label>
                <laser:select class="ui dropdown" name="status"
                              from="${RefdataCategory.getAllRefdataValues('Survey Status')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

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
            <semui:tabsItem controller="myInstitution" action="manageConsortiaSurveys"
                            params="${[id: params.id, tab: 'created']}" text="Erstellt" tab="created"
                            counts="${countSurveyConfigs?.created}"/>
            <semui:tabsItem controller="myInstitution" action="manageConsortiaSurveys"
                            params="${[id: params.id, tab: 'active']}" text="Aktiv" tab="active"
                            counts="${countSurveyConfigs?.active}"/>
            <semui:tabsItem controller="myInstitution" action="manageConsortiaSurveys"
                            params="${[id: params.id, tab: 'finish']}" text="Beendet" tab="finish"
                            counts="${countSurveyConfigs?.finish}"/>
            <semui:tabsItem controller="myInstitution" action="manageConsortiaSurveys"
                            params="${[id: params.id, tab: 'inEvaluation']}" text="In Auswertung" tab="inEvaluation"
                            counts="${countSurveyConfigs?.inEvaluation}"/>
            <semui:tabsItem controller="myInstitution" action="manageConsortiaSurveys"
                            params="${[id: params.id, tab: 'completed']}" text="Abgeschlossen" tab="completed"
                            counts="${countSurveyConfigs?.completed}"/>
        </semui:tabs>

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <g:sortableColumn params="${params}" property="si.name"
                                  title="${message(code: 'surveyInfo.slash.name')}"/>
                <g:sortableColumn params="${params}" property="si.type"
                                  title="${message(code: 'surveyInfo.type.label')}"/>
                <g:sortableColumn params="${params}" property="si.startDate"
                                  title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
                <g:sortableColumn params="${params}" property="si.endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
                <g:sortableColumn params="${params}" property="si.status"
                                  title="${message(code: 'surveyInfo.status.label')}"/>
                <th>${message(code: 'surveyInfo.property')}</th>
                <th><g:message code="surveyInfo.finished"/></th>
                <th>${message(code: 'surveyInfo.evaluation')}</th>
            </tr>

            </thead>
            <g:each in="${surveys}" var="survey" status="i">

                <g:set var="surveyInfo"
                       value="${survey[0]}"/>

                <g:set var="surveyConfig"
                       value="${survey[1]}"/>



                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <td>
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <i class="icon clipboard outline la-list-icon"></i>
                            ${surveyConfig?.subscription?.name}
                        </g:if>
                        <g:else>
                            ${surveyConfig?.getConfigNameShort()}
                        </g:else>
                        <div class="la-flexbox">
                        <i class="icon chart bar la-list-icon"></i>
                            ${surveyInfo?.name}
                        </div>
                    </td>
                    <td>
                        ${surveyInfo?.type?.getI10n('value')}
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo?.startDate}"/>

                    </td>
                    <td>

                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo?.endDate}"/>
                    </td>

                    <td>
                        ${surveyInfo?.status?.getI10n('value')}
                    </td>

                    <td class="center aligned">

                        <g:set var="surveyConfigsOfParticipant"
                               value="${SurveyResult.findAllByOwnerAndParticipantAndSurveyConfigInList(contextService.org, Org.get(params.id), surveyInfo?.surveyConfigs)}"/>

                        ${surveyConfigsOfParticipant?.groupBy { it.surveyConfig.id }.size()}
                    </td>

                    <td>
                        <g:set var="surveyResults"
                               value="${SurveyResult.findAllByParticipantAndSurveyConfigInList(Org.get(params.id), surveyInfo?.surveyConfigs)}"/>

                        <g:if test="${surveyResults}">
                            <g:if test="${surveyResults?.finishDate?.contains(null)}">
                            <%--<span class="la-long-tooltip" data-position="top right" data-variation="tiny"
                                  data-tooltip="Nicht abgeschlossen">
                                <i class="circle red icon"></i>
                            </span>--%>
                            </g:if>
                            <g:else>

                                <span class="la-long-tooltip" data-position="top right" data-variation="tiny"
                                      data-tooltip="${message(code: 'surveyResult.finish.info')}">
                                    <i class="check big green icon"></i>
                                </span>
                            </g:else>
                        </g:if>
                    </td>

                    <td>
                        <g:set var="finish"
                               value="${SurveyResult.findAllBySurveyConfigInListAndFinishDateIsNotNull(surveyInfo?.surveyConfigs).size()}"/>
                        <g:set var="total"
                               value="${SurveyResult.findAllBySurveyConfigInList(surveyInfo?.surveyConfigs).size()}"/>
                        <g:link action="surveyParticipantConsortia" id="${surveyInfo.id}" params="[participant: participant?.id]"
                                class="ui icon button">
                            <g:if test="${finish != 0 && total != 0}">
                                <g:formatNumber number="${(finish / total) * 100}" minFractionDigits="2"
                                                maxFractionDigits="2"/>%
                            </g:if>
                            <g:else>
                                0%
                            </g:else>
                        </g:link>
                    </td>
                </tr>

            </g:each>
        </table>
    </semui:form>
</div>
</div>

<g:if test="${surveys}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${countSurveyConfigs}"/>
</g:if>

</body>
</html>
