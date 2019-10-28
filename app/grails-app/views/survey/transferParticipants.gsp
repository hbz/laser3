<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyInfo.renewal')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="surveyInfo.renewal" class="active"/>
</semui:breadcrumbs>


<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewalwithSurvey" id="${surveyInfo.id}"
                    params="[surveyConfigID: surveyConfig.id, exportXLS: true]">${message(code: 'renewalwithSurvey.exportRenewal')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<h2>
    ${message(code: 'surveyInfo.renewal')}
</h2>


<semui:form>

    <g:form action="proccessRenewalwithSurvey" controller="survey" id="${surveyInfo?.id}"
            params="[surveyConfigID: surveyConfig?.id]"
            method="post" class="ui form newLicence">

        <h3>
        <g:message code="renewalwithSurvey.parentSubscription"/>:
        <g:if test="${parentSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSubscription?.id}">${parentSubscription?.dropdownNamingConvention()}</g:link>
        </g:if>

        <br>
        <br>
        <g:message code="renewalwithSurvey.parentSuccessorSubscription"/>:
        <g:if test="${parentSuccessorSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSuccessorSubscription?.id}">${parentSuccessorSubscription?.dropdownNamingConvention()}</g:link>

            <g:link controller="survey" action="copyElementsIntoRenewalSubscription" id="${parentSubscription?.id}"
                    params="[sourceSubscriptionId: parentSubscription?.id, targetSubscriptionId: parentSuccessorSubscription?.id, isRenewSub: true, isCopyAuditOn: true]"
                    class="ui button ">
                <g:message code="renewalwithSurvey.newSub.change"/>
            </g:link>

        </g:if>
        <g:else>
            <g:message code="renewalwithSurvey.noParentSuccessorSubscription"/>
            <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo?.id}"
                    params="[surveyConfig: surveyConfig?.id, parentSub: parentSubscription?.id]"
                    class="ui button ">
                <g:message code="renewalwithSurvey.newSub"/>
            </g:link>
        </g:else>
        </br>
        </h3>

        <br>

        <g:set var="consortiaSubscriptions" value="${com.k_int.kbplus.Subscription.findAllByInstanceOf(parentSubscription)?.size()}"/>
        <g:set var="surveyParticipants" value="${surveyConfig?.orgs?.size()}"/>
        <g:set var="totalOrgs" value="${(orgsContinuetoSubscription?.size()?:0)+(newOrgsContinuetoSubscription?.size()?:0)+(orgsWithMultiYearTermSub?.size()?:0)+(orgsLateCommers?.size()?:0)+(orgsWithTermination?.size()?:0)+(orgsWithoutResult?.size()?:0)+(orgsWithParticipationInParentSuccessor?.size()?:0)}"/>


        <h3 class="ui left aligned icon header">
            <g:link action="evaluationConfigsInfo" id="${surveyInfo?.id}" params="[surveyConfigID: surveyConfig?.id]" >${message(code: 'survey.label')} ${message(code: 'surveyParticipants.label')}</g:link>
            <semui:totalNumber total="${surveyParticipants}"/>
            <br>
            <g:link controller="subscription" action="members" id="${parentSubscription?.id}" >${message(code: 'renewalwithSurvey.orgsInSub')}</g:link>
            <semui:totalNumber class="${surveyParticipants != consortiaSubscriptions ? 'red': ''}" total="${consortiaSubscriptions}"/>
            <br>
            ${message(code: 'renewalwithSurvey.orgsTotalInRenewalProcess')}
            <semui:totalNumber class="${totalOrgs != consortiaSubscriptions ? 'red': ''}" total="${totalOrgs}"/>
        </h3>

        <br>

        <br>
        <div class="ui grid">

                <div class="row">

                    <div class="eight wide column">

                        ${parentSubscription?.name}

                        <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header" id="${side}">
                            <thead>
                            <tr>
                                <th>
                                    <g:if test="${editable}">
                                        <input class="select-all" type="checkbox" name="chkall">
                                    </g:if>
                                </th>
                                <th>${message(code: 'sidewide.number')}</th>
                                <th></th>
                            </tr>
                            </thead>
                        <g:each in="${participantsList}" var="participant" >
                            <g:if test="${participant in parentParticipantsList}">
                            <tr>
                            <td></td>
                            <td></td>
                            <td>
                            ${participant.sortname}
                            </td>
                            </tr>
                            </g:if>
                            <g:else>
                                <tr>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </tr>
                            </g:else>
                        </g:each>

                    </table>

                    </div>
                    <div class="eight wide column">

                        ${parentSuccessorSubscription?.name}

                        <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header" id="${side}">
                            <thead>
                            <tr>
                                <th>
                                    <g:if test="${editable}">
                                        <input class="select-all" type="checkbox" name="chkall">
                                    </g:if>
                                </th>
                                <th>${message(code: 'sidewide.number')}</th>
                                <th></th>
                            </tr>
                            </thead>
                            <g:each in="${participantsList}" var="participant" >
                                <g:if test="${participant in parentSuccessortParticipantsList}">
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td>
                                            ${participant.sortname}
                                        </td>
                                    </tr>
                                </g:if>
                                <g:else>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                </g:else>
                            </g:each>

                        </table>
                    </div>
        </div>
        </div>


    </g:form>
</semui:form>

</body>
</html>
