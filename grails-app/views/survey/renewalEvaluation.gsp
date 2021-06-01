<%@ page import="de.laser.SurveyOrg; de.laser.SurveyConfig; de.laser.interfaces.CalculatedType; de.laser.helper.RDStore; de.laser.properties.PropertyDefinition; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.Org; de.laser.Subscription" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'surveyInfo.evaluation')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="surveyInfo.evaluation" class="active"/>
</semui:breadcrumbs>


<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
            ${surveyConfig.subscription?.name}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyInfo.evaluation')}
</h2>

<g:if test="${!(surveyInfo.status in [RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED])}">
    <div class="ui segment">
        <strong>${message(code: 'renewalEvaluation.notInEvaliation')}</strong>
    </div>
</g:if>
<g:else>

    <semui:messages data="${[message: message(code: 'renewalEvaluation.dynamicSite')]}"/>

    <semui:form>

        <div style="text-align: right">
            <semui:controlButtons>
                <semui:exportDropdown>
                %{--<semui:exportDropdownItem>
                    <g:link class="item" action="renewalEvaluation" id="${surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, exportXLSX: true]">${message(code: 'renewalEvaluation.exportRenewal')}</g:link>
                    </semui:exportDropdownItem>--}%

                    <semui:actionsDropdownItem data-semui="modal" href="#individuallyExportModal"
                                               message="renewalEvaluation.exportRenewal"/>

                </semui:exportDropdown>
                %{--<semui:actionsDropdown>
                    <g:if test="${parentSuccessorSubscription}">
                                              <semui:actionsDropdownItem controller="survey" action="compareMembersOfTwoSubs"
                                                   params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                                   message="surveyInfo.transferOverView"/>
                    </g:if>
                </semui:actionsDropdown>--}%
            </semui:controlButtons>
        </div>

        %{--<h3 class="ui header">
        <g:message code="renewalEvaluation.parentSubscription"/>:
        <g:if test="${parentSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSubscription.id}">${parentSubscription.dropdownNamingConvention()}</g:link>
        </g:if>

        <br/>
        <br/>
        <g:message code="renewalEvaluation.parentSuccessorSubscription"/>:
        <g:if test="${parentSuccessorSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSuccessorSubscription.id}">${parentSuccessorSubscription.dropdownNamingConvention()}</g:link>

            <g:if test="${parentSuccessorSubscription.getAllSubscribers().size() > 0}">
                <g:link controller="subscription" action="copyElementsIntoSubscription" id="${parentSubscription.id}"
                        params="[sourceObjectId: genericOIDService.getOID(parentSubscription), targetObjectId: genericOIDService.getOID(parentSuccessorSubscription), isRenewSub: true, fromSurvey: true]"
                        class="ui button ">
                    <g:message code="renewalEvaluation.newSub.change"/>
                </g:link>
            </g:if>

        </g:if>
        <g:else>
            <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo.id}"
                    params="[surveyConfig: surveyConfig.id, parentSub: parentSubscription.id]"
                    class="ui button ">
                <g:message code="renewalEvaluation.newSub"/>
            </g:link>
        </g:else>
        </br>
        </h3>--}%

        <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>
        <div class="ui horizontal segments">
            <div class="ui segment center aligned">
                <g:link controller="subscription" action="members" id="${subscription.id}">
                    <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

                    <div class="ui circular label">
                        ${countParticipants.subMembers}
                    </div>
                </g:link>
            </div>

            <div class="ui segment center aligned">
                <g:link controller="survey" action="surveyParticipants"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id]">
                    <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

                    <div class="ui circular label">${countParticipants.surveyMembers}</div>
                </g:link>

                <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                    ( ${countParticipants.subMembersWithMultiYear}
                    ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
                </g:if>
            </div>

            <div class="ui segment center aligned">
                <strong>${message(code: 'renewalEvaluation.orgsTotalInRenewalProcess')}:</strong>
                <semui:totalNumber class="${totalOrgs != countParticipants.subMembers ? 'red' : ''}"
                                   total="${totalOrgs}"/>

            </div>
        </div>

    </semui:form>

    <g:if test="${memberProperties}">%{-- check for content --}%
        <g:if test="${parentSubscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL,CalculatedType.TYPE_ADMINISTRATIVE]}">
            <div class="ui card la-dl-no-table">
                <div class="content">
                    <h2 class="ui header">${message(code:'subscription.properties.consortium')}</h2>
                    <div id="member_props_div">
                        <g:render template="/templates/properties/members" model="${[
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: parentSubscription,
                                custom_props_div: "member_props_div"]}"/>
                    </div>
                </div>
            </div>
        </g:if>
    </g:if>

    <semui:form>

        <div class="ui top attached tabular menu">
            <a class="active item" data-tab="orgsContinuetoSubscription">
                ${message(code: 'renewalEvaluation.continuetoSubscription.label')} <semui:totalNumber
                        total="${orgsContinuetoSubscription.size()}"/>
            </a>

            <a class="item" data-tab="newOrgsContinuetoSubscription">
                ${message(code: 'renewalEvaluation.newOrgstoSubscription.label')} <semui:totalNumber
                        total="${newOrgsContinuetoSubscription.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithMultiYearTermSub">
                ${message(code: 'renewalEvaluation.withMultiYearTermSub.label')} <semui:totalNumber
                        total="${orgsWithMultiYearTermSub.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithParticipationInParentSuccessor">
                ${message(code: 'renewalEvaluation.orgsWithParticipationInParentSuccessor.label')} <semui:totalNumber
                        total="${orgsWithParticipationInParentSuccessor.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithTermination">
                ${message(code: 'renewalEvaluation.withTermination.label')} <semui:totalNumber
                        total="${orgsWithTermination.size()}"/>
            </a>

           <a class="item" data-tab="orgsWithoutResult">
                ${message(code: 'renewalEvaluation.orgsWithoutResult.label')} <semui:totalNumber
                        total="${orgsWithoutResult.size()}"/>
            </a>
        </div>

        <div class="ui bottom attached active tab segment" data-tab="orgsContinuetoSubscription">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.continuetoSubscription.label')} <semui:totalNumber
                    total="${orgsContinuetoSubscription.size()}"/></h4>

            <g:render template="renewalResult" model="[participantResults: orgsContinuetoSubscription]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="newOrgsContinuetoSubscription">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.newOrgstoSubscription.label')} <semui:totalNumber
                    total="${newOrgsContinuetoSubscription.size()}"/></h4>

            <g:render template="renewalResult" model="[participantResults: newOrgsContinuetoSubscription]"/>
        </div>

        <div class="ui bottom attached tab segment" data-tab="orgsWithTermination">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.withTermination.label')} <semui:totalNumber
                    total="${orgsWithTermination.size()}"/></h4>

            <g:render template="renewalResult" model="[participantResults: orgsWithTermination]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="orgsWithoutResult">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgsWithoutResult.label')} (${message(code: 'surveys.tabs.termination')})<semui:totalNumber
                    total="${orgsWithoutResult.size()}"/></h4>

            <g:render template="renewalResult" model="[participantResults: orgsWithoutResult]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="orgsWithMultiYearTermSub">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.withMultiYearTermSub.label')} <semui:totalNumber
                    total="${orgsWithMultiYearTermSub.size()}"/></h4>

            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'default.actions.label')}</th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${orgsWithMultiYearTermSub}" var="sub" status="i">
                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
                        <g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
                            <td>
                                ${subscriberOrg.sortname}
                                <br/>

                                <g:link controller="organisation" action="show"
                                        id="${subscriberOrg.id}">(${fieldValue(bean: subscriberOrg, field: "name")})</g:link>
                            </td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                            <td>${sub.status.getI10n('value')}</td>
                            <td>
                                <g:if test="${sub}">
                                    <g:link controller="subscription" action="show" id="${sub.id}"
                                            class="ui button icon"><i class="icon clipboard"></i></g:link>
                                </g:if>
                                <g:if test="${sub._getCalculatedSuccessor()}">
                                    <br/>
                                    <br/>
                                    <g:link controller="subscription" action="show"
                                            id="${sub._getCalculatedSuccessor()?.id}"
                                            class="ui button icon"><i class="icon yellow clipboard"></i></g:link>
                                </g:if>
                            </td>
                        </g:each>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>

        <div class="ui bottom attached tab segment" data-tab="orgsWithParticipationInParentSuccessor">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgsWithParticipationInParentSuccessor.label')} <semui:totalNumber
                    total="${orgsWithParticipationInParentSuccessor.size() }"/></h4>

            <table class="ui celled la-table table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'default.actions.label')}</th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${orgsWithParticipationInParentSuccessor}" var="sub" status="i">
                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
                        <g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
                            <td>
                                ${subscriberOrg.sortname}
                                <br/>
                                <g:link controller="organisation" action="show"
                                        id="${subscriberOrg.id}">(${fieldValue(bean: subscriberOrg, field: "name")})</g:link>
                            </td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                            <td>${sub.status.getI10n('value')}</td>
                            <td>
                                <g:if test="${sub}">
                                    <g:link controller="subscription" action="show" id="${sub.id}"
                                            class="ui button icon"><i class="icon clipboard"></i></g:link>
                                </g:if>
                                <g:if test="${sub._getCalculatedSuccessor()}">
                                    <br/>
                                    <br/>
                                    <g:link controller="subscription" action="show"
                                            id="${sub._getCalculatedSuccessor()?.id}"
                                            class="ui button icon"><i class="icon yellow clipboard"></i></g:link>
                                </g:if>
                            </td>
                        </g:each>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>

    </semui:form>


    <g:form action="renewalSent" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

        <div class="ui right floated compact segment">
            <div class="ui checkbox">
                <input type="checkbox" onchange="this.form.submit()"
                       name="renewalSent" ${surveyInfo.isRenewalSent ? 'checked' : ''}>
                <label><g:message code="surveyInfo.isRenewalSent.label"/></label>
            </div>
        </div>

    </g:form>

    <g:render template="export/individuallyExportModal" model="[modalID: 'individuallyExportModal', token: token]" />

</g:else>

</body>
</html>
