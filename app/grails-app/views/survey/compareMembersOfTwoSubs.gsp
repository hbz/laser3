<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<g:set var="surveyService" bean="surveyService"/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} :  ${message(code: 'surveyInfo.transferMembers')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
        <semui:crumb controller="survey" action="renewalWithSurvey" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" message="surveyInfo.renewalOverView"/>
    </g:if>
    <semui:crumb message="surveyInfo.transferMembers" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:actionsDropdown>
            <g:if test="${parentSuccessorSubscription}">

                <semui:actionsDropdownItem data-semui="modal" href="#transferParticipantsModal"
                                           message="surveyInfo.transferParticipants"/>
            </g:if>
            <semui:actionsDropdownItem controller="survey" action="renewalWithSurvey" params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.renewalOverView"/>
    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<div class="ui tablet stackable steps">

    <div class="${(actionName == 'compareMembersOfTwoSubs') ? 'active' : ''} step">
        <div class="content">
                <div class="title">
                    <g:link controller="survey" action="compareMembersOfTwoSubs"
                            params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id]">
                        ${message(code: 'surveyInfo.transferMembers')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="exchange icon"></i>${message(code: 'surveyInfo.transferMembers')}
                </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferMembers == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferMembers: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferMembers: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>


    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'surveyProperties') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, tab: 'surveyProperties']">
                    ${message(code: 'copyProperties.surveyProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferSurveyProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>
    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'customProperties') ? 'active' : ''}  step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, tab: 'customProperties']">
                    ${message(code: 'copyProperties.customProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferCustomProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferCustomProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferCustomProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'privateProperties') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, tab: 'privateProperties']">
                    ${message(code: 'copyProperties.privateProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferPrivateProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferPrivateProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferPrivateProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

    <div class="${(actionName == 'copySurveyCostItems') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copySurveyCostItems"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id]">
                    ${message(code: 'copySurveyCostItems.surveyCostItems')}
                </g:link>
            </div>

            <div class="description">
                <i class="money bill alternate outline icon"></i>${message(code: 'copySurveyCostItems.surveyCostItem')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferSurveyCostItems == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyCostItems: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyCostItems: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

</div>

<h2>
    ${message(code: 'surveyInfo.transferMembers')}
</h2>



<semui:form>
    <div class="ui grid">

        <div class="row">
            <div class="eight wide column">
                <h3 class="ui header center aligned">

                    <g:message code="renewalWithSurvey.parentSubscription"/>:<br>
                    <g:if test="${parentSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSubscription?.id}">${parentSubscription?.dropdownNamingConvention()}</g:link>
                        <br>
                        <g:link controller="subscription" action="members"
                                id="${parentSubscription?.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSubscription.getDerivedSubscribers().size() ?: 0}"/>
                    </g:if>
                </h3>
            </div>

            <div class="eight wide column">
                <h3 class="ui header center aligned">
                    <g:message code="renewalWithSurvey.parentSuccessorSubscription"/>:<br>
                    <g:if test="${parentSuccessorSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSuccessorSubscription?.id}">${parentSuccessorSubscription?.dropdownNamingConvention()}</g:link>
                        <br>
                        <g:link controller="subscription" action="members"
                                id="${parentSuccessorSubscription?.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSuccessorSubscription.getDerivedSubscribers().size() ?: 0}"/>

                    </g:if>
                    <g:else>
                        <g:message code="renewalWithSurvey.noParentSuccessorSubscription"/>
                    %{--<br>
                    <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey"
                            id="${surveyInfo?.id}"
                            params="[surveyConfig: surveyConfig?.id, parentSub: parentSubscription?.id]"
                            class="ui button ">
                        <g:message code="renewalWithSurvey.newSub"/>
                    </g:link>--}%
                    </g:else>
                </h3>
            </div>
        </div>
    </div>
</semui:form>
<semui:form>

        <div class="ui grid">

            <div class="row">

                <div class="eight wide column">

                    <table class="ui celled sortable table la-table" id="parentSubscription">
                        <thead>
                        <tr>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${participantsList}" var="participant" status="i">
                            <g:if test="${participant in parentParticipantsList}">
                                <g:set var="termination" value="${!(participant in parentSuccessortParticipantsList)}"/>
                                <g:set var="participantSub" value="${parentSubscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <tr class=" ${termination ? 'negative' : ''}">
                                    <td>${i + 1}</td>
                                    <td class="titleCell">
                                        <g:if test="${participantSub && participantSub.isMultiYear}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                                <i class="map orange icon"></i>
                                            </span>
                                        </g:if>
                                        <g:link controller="myInstitution" action="manageParticipantSurveys"
                                                id="${participant.id}">
                                            ${participant?.sortname}
                                        </g:link>
                                        <br>
                                        <g:link controller="organisation" action="show"
                                                id="${participant.id}">(${fieldValue(bean: participant, field: "name")})</g:link>
                                        <g:if test="${participantSub}">
                                            <div class="la-icon-list">
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.startDate}"/>
                                                -
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.endDate}"/>
                                                <div class="right aligned wide column">
                                                    <b>${participantSub.status.getI10n('value')}</b>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td>
                                        <g:if test="${participantSub}">
                                            <g:link controller="subscription" action="show" id="${participantSub.id}"
                                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:if>
                            <g:else>
                                <tr>
                                    <td>${i + 1}</td>
                                    <td class="titleCell"></td>
                                    <td></td>
                                </tr>
                            </g:else>
                        </g:each>
                        </tbody>
                    </table>

                </div>

                <div class="eight wide column">

                    <table class="ui celled sortable table la-table" id="parentSuccessorSubscription">
                        <thead>
                        <tr>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                            <th></th>
                        </tr>
                        </thead>
                        <g:each in="${participantsList}" var="participant" status="j">
                            <g:if test="${participant in parentSuccessortParticipantsList}">
                                <g:set var="participantSub" value="${parentSuccessorSubscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <tr class=" ${participant in parentParticipantsList ? '' : 'positive'}">
                                    <td>${j+1}</td>
                                    <td class="titleCell">
                                        <g:if test="${participantSub && participantSub.isMultiYear}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                                <i class="map orange icon"></i>
                                            </span>
                                        </g:if>
                                        <g:link controller="myInstitution" action="manageParticipantSurveys"
                                                id="${participant.id}">
                                            ${participant?.sortname}
                                        </g:link>
                                        <br>
                                        <g:link controller="organisation" action="show"
                                                id="${participant.id}">(${fieldValue(bean: participant, field: "name")})</g:link>

                                        <g:if test="${participantSub}">
                                            <div class="la-icon-list">
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.startDate}"/>
                                                -
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.endDate}"/>

                                                <div class="right aligned wide column">
                                                    <b>${participantSub.status.getI10n('value')}</b>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td>
                                        <g:if test="${participantSub}">
                                            <g:link controller="subscription" action="show" id="${participantSub.id}"
                                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:if>
                            <g:else>
                                <tr>
                                    <td>${j+1}</td>
                                    <td class="titleCell"></td>
                                </tr>
                            </g:else>
                        </g:each>

                    </table>
                </div>
            </div>
        </div>

</semui:form>

<div class="sixteen wide field" style="text-align: center;">
    <g:link class="ui button" controller="survey" action="copyProperties"
            params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, tab: 'surveyProperties']">
        ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
    </g:link>
</div>

<r:script>
    $(document).ready(function() {

        $("#parentSubscription .titleCell").each(function(k) {
            var v = $(this).height();
            $("#parentSuccessorSubscription .titleCell").eq(k).height(v);
        });

        $("#parentSuccessorSubscription .titleCell").each(function(k) {
            var v = $(this).height();
            $("#parentSubscription .titleCell").eq(k).height(v);
        });

    });
</r:script>

<g:if test="${parentSuccessorSubscription}">

    <g:set var="auditConfigProvidersAgencies" value="${parentSuccessorSubscription.orgRelations?.findAll {it.isShared}}" />

    <semui:modal id="transferParticipantsModal" message="surveyInfo.transferParticipants"
                 msgSave="${message(code: 'surveyInfo.transferParticipants.button')}">

        <h3><g:message code="surveyInfo.transferParticipants.option"/>:</h3>

        <g:form class="ui form"
                url="[controller: 'survey', action: 'processTransferParticipants', params: [id: params.id, surveyConfigID: surveyConfig?.id]]">
            <div class="field">
                <g:set var="properties" value="${de.laser.AuditConfig.getConfigs(parentSuccessorSubscription)}"></g:set>
                <g:if test="${properties}">

                    <label><g:message code="subscription.details.copyElementsIntoSubscription.auditConfig" />:</label>
                    <div class="ui bulleted list">
                        <g:each in="${properties}" var="prop" >
                            <div class="item">
                                <b><g:message code="subscription.${prop.referenceField}.label" /></b>:
                            <g:if test="${parentSuccessorSubscription.getProperty(prop.referenceField) instanceof com.k_int.kbplus.RefdataValue}">
                                ${parentSuccessorSubscription.getProperty(prop.referenceField).getI10n('value')}
                            </g:if>
                            <g:else>
                                ${parentSuccessorSubscription.getProperty(prop.referenceField)}
                            </g:else>
                            </div>
                        </g:each>
                    </div>
                </g:if>
                <g:else>
                    <g:message code="subscription.details.copyElementsIntoSubscription.noAuditConfig"/>
                </g:else>

                <g:if test="${auditConfigProvidersAgencies}">
                    <label><g:message code="property.share.tooltip.on" />:</label>
                    <div class="ui bulleted list">
                        <g:each in="${auditConfigProvidersAgencies}" var="role" >
                            <div class="item">
                                <b> ${role.roleType.getI10n("value")}</b>:
                            ${role.org.name}
                            </div>
                        </g:each>
                    </div>

                </g:if>

            </div>
            <div class="two fields">
                <g:set var="validPackages" value="${parentSuccessorSubscription.packages?.sort { it.pkg.name }}"/>
                <div class="field">

                    <label><g:message code="myinst.addMembers.linkPackages"/></label>
                    <g:if test="${validPackages}">
                        <div class="ui checkbox">
                            <input type="checkbox" id="linkAllPackages" name="linkAllPackages">
                            <label for="linkAllPackages"><g:message code="myinst.addMembers.linkAllPackages"
                                                                    args="${superOrgType}"/>
                            (<g:each in="${validPackages}" var="pkg">
                                ${pkg.getPackageName()},
                            </g:each>)
                            </label>
                        </div>

                        <div class="ui checkbox">
                            <input type="checkbox" id="linkWithEntitlements" name="linkWithEntitlements">
                            <label for="linkWithEntitlements"><g:message
                                    code="myinst.addMembers.withEntitlements"/></label>
                        </div>

                        <div class="field">
                            <g:select class="ui search multiple dropdown"
                                      optionKey="id" optionValue="${{ it.getPackageName() }}"
                                      from="${validPackages}" name="packageSelection" value=""
                                      noSelection='["": "${message(code: 'subscription.linkPackagesMembers.noSelection')}"]'/>
                        </div>
                    </g:if>
                    <g:else>
                        <g:message code="subscription.linkPackagesMembers.noValidLicenses" args="${superOrgType}"/>
                    </g:else>
                </div>

                <g:if test="${!auditConfigProvidersAgencies}">
                    <div class="field">
                        <g:set var="providers" value="${parentSuccessorSubscription.getProviders()?.sort { it.name }}"/>
                        <g:set var="agencies" value="${parentSuccessorSubscription.getAgencies()?.sort { it.name }}"/>

                        <g:if test="${(providers || agencies)}">
                            <label><g:message code="surveyInfo.transferParticipants.moreOption"/></label>

                            <div class="ui checkbox">
                                <input type="checkbox" id="transferProviderAgency" name="transferProviderAgency" checked>
                                <label for="transferProviderAgency"><g:message
                                        code="surveyInfo.transferParticipants.transferProviderAgency"
                                        args="${superOrgType}"/>
                                <g:set var="providerAgency" value="${providers + agencies}"/>
                                (${providerAgency ? providerAgency.name.join(', ') : ''})
                                </label>
                            </div>

                            <div class="field">

                                <g:if test="${providers}">
                                    <label><g:message code="surveyInfo.transferParticipants.transferProvider"
                                                      args="${superOrgType}"/>:</label>
                                    <g:select class="ui search multiple dropdown"
                                              optionKey="id" optionValue="name"
                                              from="${providers}" name="providersSelection" value=""
                                              noSelection='["": "${message(code: 'surveyInfo.transferParticipants.noSelectionTransferProvider')}"]'/>
                                </g:if>
                            </div>

                            <div class="field">
                                <g:set var="agencies"
                                       value="${parentSuccessorSubscription.getAgencies()?.sort { it.name }}"/>
                                <g:if test="${agencies}">
                                    <label><g:message code="surveyInfo.transferParticipants.transferAgency"
                                                      args="${superOrgType}"/>:</label>
                                    <g:select class="ui search multiple dropdown"
                                              optionKey="id" optionValue="name"
                                              from="${agencies}" name="agenciesSelection" value=""
                                              noSelection='["": "${message(code: 'surveyInfo.transferParticipants.noSelectionTransferAgency')}"]'/>
                                </g:if>
                            </div>
                        </g:if>
                        <g:else>
                            <g:message code="surveyInfo.transferParticipants.noTransferProviderAgency"
                                       args="${superOrgType}"/>
                        </g:else>
                    </div>
                </g:if>
            </div>

            <div class="ui two fields">
                <div class="field">
                    <label><g:message code="myinst.copyLicense"/></label>
                    <g:if test="${parentSuccessorSubscription.owner}">
                        <g:if test="${parentSuccessorSubscription.getCalculatedType() == de.laser.interfaces.TemplateSupport.CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE && institution.id == parentSuccessorSubscription.getCollective().id}">
                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" name="attachToParticipationLic" value="true">
                                <label><g:message code="myinst.attachToParticipationLic"/></label>
                            </div>

                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" name="attachToParticipationLic" value="false"
                                       checked="checked">
                                <label><g:message code="myinst.noAttachmentToParticipationLic"/></label>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="ui radio checkbox">
                                <g:if test="${parentSuccessorSubscription.owner.derivedLicenses}">
                                    <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics"
                                           value="no">
                                </g:if>
                                <g:else>
                                    <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics"
                                           value="no"
                                           checked="checked">
                                </g:else>
                                <label for="generateSlavedLics">${message(code: 'myinst.separate_lics_no')}</label>
                            </div>

                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" id="generateSlavedLics1" name="generateSlavedLics"
                                       value="shared">
                                <label for="generateSlavedLics1">${message(code: 'myinst.separate_lics_shared', args: superOrgType)}</label>
                            </div>

                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" id="generateSlavedLics2" name="generateSlavedLics"
                                       value="explicit">
                                <label for="generateSlavedLics2">${message(code: 'myinst.separate_lics_explicit', args: superOrgType)}</label>
                            </div>

                            <g:if test="${parentSuccessorSubscription.owner.derivedLicenses}">
                                <div class="ui radio checkbox">
                                    <input class="hidden" type="radio" id="generateSlavedLics3"
                                           name="generateSlavedLics"
                                           value="reference" checked="checked">
                                    <label for="generateSlavedLics3">${message(code: 'myinst.separate_lics_reference')}</label>
                                </div>

                                <div class="generateSlavedLicsReference-wrapper hidden">
                                    <br/>
                                    <g:select from="${parentSuccessorSubscription.owner?.derivedLicenses}"
                                              class="ui search dropdown hide"
                                              optionKey="${{ 'com.k_int.kbplus.License:' + it.id }}"
                                              optionValue="${{ it.reference }}"
                                              name="generateSlavedLicsReference"/>
                                </div>
                                <r:script>
                                    $('*[name=generateSlavedLics]').change(function () {
                                        $('*[name=generateSlavedLics][value=reference]').prop('checked') ?
                                                $('.generateSlavedLicsReference-wrapper').removeClass('hidden') :
                                                $('.generateSlavedLicsReference-wrapper').addClass('hidden');
                                    })
                                    $('*[name=generateSlavedLics]').trigger('change')
                                </r:script>
                            </g:if>
                        </g:else>
                    </g:if>
                    <g:else>
                        <semui:msg class="info" text="${message(code: 'myinst.noSubscriptionOwner')}"/>
                    </g:else>
                </div>
            </div>

        </g:form>

    </semui:modal>
</g:if>

</body>
</html>
