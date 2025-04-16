<%@ page import="de.laser.DocContext; de.laser.Doc; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig; de.laser.survey.SurveyOrg; de.laser.interfaces.CalculatedType; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.Org; de.laser.Subscription" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyInfo.evaluation')})" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyInfo.evaluation" class="active"/>
</ui:breadcrumbs>


<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />

<g:if test="${(surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])}">
    <div class="ui segment">
        <strong>${message(code: 'survey.notStarted')}</strong>
    </div>
</g:if>
<g:else>

    <ui:greySegment>
            <g:if test="${editable}">
                <button type="button" class="${Btn.MODERN.SIMPLE} right floated"
                        data-ownerid="${surveyConfig.subscription.id}"
                        data-ownerclass="${surveyConfig.subscription.class.name}"
                        data-doctype="${RDStore.DOC_TYPE_RENEWAL.value}"
                        data-ui="modal"
                        data-href="#modalCreateDocumentRenewal">
                    <i aria-hidden="true" class="${Icon.CMD.ADD} small"></i>
                </button>
            </g:if>

        <table class="ui compact stackable celled sortable table la-table la-js-responsive-table">
            <thead>
            <tr>
                <th colspan="1"class="la-smaller-table-head center aligned">Renewal ${message(code: 'subscriptionsManagement.documents')}</th>
                <th rowspan="2"><ui:optionsIcon /></th>
            </tr>
            <tr>
                <th scope="col" class="wide" rowspan="2">${message(code:'license.docs.table.type')}</th>
            </tr>
            </thead>
            <tbody>
            <%
                Set<DocContext> documentSet2 = DocContext.executeQuery('from DocContext where subscription = :subscription and owner.type = :docType and owner.owner = :owner', [subscription: surveyConfig.subscription, docType: RDStore.DOC_TYPE_RENEWAL, owner: contextService.getOrg()])
                documentSet2 = documentSet2.sort { it.owner?.title }
            %>
            <g:each in="${documentSet2}" var="docctx">
                <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                    <tr>
                        <td>
                            <ui:documentIcon doc="${docctx.owner}" showText="true" showTooltip="true"/>
                            <g:set var="supportedMimeType"
                                   value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}"/>
                            <strong>
                                <g:if test="${supportedMimeType}">
                                    <a href="#documentPreview" data-dctx="${docctx.id}">${docctx.owner.title}</a>
                                </g:if>
                                <g:else>
                                    ${docctx.owner.title}
                                </g:else>
                            </strong>
                            <br />
                            ${docctx.owner.filename}
                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}"
                                    class="${Btn.MODERN.SIMPLE} right floated"
                                    target="_blank">
                                <i class="${Icon.CMD.DOWNLOAD} small"></i>
                            </g:link>
                        </td>
                        <td>
                            <g:if test="${!docctx.sharedFrom && !docctx.isShared && userService.hasFormalAffiliation(docctx.owner.owner, 'INST_EDITOR')}">
                                <g:set var="linkParams" value="${[instanceId:"${surveyInfo.id}", deleteId:"${docctx.id}", redirectController:"${controllerName}", redirectAction:"${actionName}"]}" />
                                <g:link controller="document" action="deleteDocument" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                        data-confirm-term-how="delete"
                                        params="${params.tab ? linkParams << [redirectTab: "${params.tab}"] : linkParams}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </g:if>
                        </td>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
        </table>
    </ui:greySegment>

    <ui:msg class="info" message="renewalEvaluation.dynamicSite" />

    <ui:greySegment>

        <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>
        <div class="ui horizontal segments">
            <div class="ui segment center aligned">
                <g:link controller="subscription" action="members" id="${subscription.id}">
                    <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

                    <ui:bubble count="${countParticipants.subMembers}" />
                </g:link>
            </div>

            <div class="ui segment center aligned">
                <g:link controller="survey" action="surveyParticipants"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id]">
                    <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

                    <ui:bubble count="${countParticipants.surveyMembers}" />
                </g:link>

                <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                    ( ${countParticipants.subMembersWithMultiYear}
                    ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
                </g:if>
            </div>

            <div class="ui segment center aligned">
                <strong>${message(code: 'renewalEvaluation.orgsTotalInRenewalProcess')}:</strong>
                <ui:totalNumber class="${totalOrgs != countParticipants.subMembers ? 'red' : ''}"
                                   total="${totalOrgs}"/>

            </div>
        </div>

    </ui:greySegment>

    <div class="la-inline-lists">
            <div class="ui card">
                <div class="content">
                    <h2 class="ui header">${message(code:'renewalEvaluation.propertiesChanged')}</h2>

                    <g:if test="${propertiesChanged}">
                        <g:link class="${Btn.SIMPLE} right floated" controller="survey" action="showPropertiesChanged"
                                id="${surveyConfig.surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id, tab: params.tab, exportXLSX: true]">
                            Export ${message(code: 'renewalEvaluation.propertiesChanged')}
                        </g:link>
                        <br>
                        <br>
                    </g:if>
                    <div>
                        <table class="ui la-js-responsive-table la-table table">
                            <thead>
                            <tr>
                                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                                <th>${message(code: 'propertyDefinition.label')}</th>
                                <th>${message(code:'renewalEvaluation.propertiesChanged')}</th>
                                <th class="center aligned">
                                    <ui:optionsIcon />
                                </th>
                            </tr>
                            </thead>
                            <tbody>

                            <g:each in="${propertiesChanged}" var="property" status="i">
                                <g:set var="propertyDefinition"
                                       value="${PropertyDefinition.findById(property.key)}"/>
                                <tr>
                                    <td class="center aligned">
                                        ${i + 1}
                                    </td>
                                    <td>
                                        ${propertyDefinition.getI10n('name')}
                                    </td>
                                    <td>${property.value.size()}</td>
                                    <td class="center aligned">
                                        <button class="${Btn.SIMPLE}" onclick="JSPC.app.propertiesChanged(${property.key});">
                                            <g:message code="default.button.show.label"/>
                                        </button>
                                    </td>
                                </tr>

                            </g:each>
                            </tbody>
                        </table>

                    </div>
                </div>
            </div>
    </div>

        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <a class="active item" data-tab="orgsContinuetoSubscription">
                ${message(code: 'renewalEvaluation.continuetoSubscription.label')} <ui:totalNumber
                        total="${orgsContinuetoSubscription.size()}"/>
            </a>

            <a class="item" data-tab="newOrgsContinuetoSubscription">
                ${message(code: 'renewalEvaluation.newOrgstoSubscription.label')} <ui:totalNumber
                        total="${newOrgsContinuetoSubscription.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithMultiYearTermSub">
                ${message(code: 'renewalEvaluation.withMultiYearTermSub.label')} <ui:totalNumber
                        total="${orgsWithMultiYearTermSub.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithParticipationInParentSuccessor">
                ${message(code: 'renewalEvaluation.orgsWithParticipationInParentSuccessor.label')} <ui:totalNumber
                        total="${orgsWithParticipationInParentSuccessor.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithTermination">
                ${message(code: 'renewalEvaluation.withTermination.label')} <ui:totalNumber
                        total="${orgsWithTermination.size()}"/>
            </a>

           <a class="item" data-tab="orgsWithoutResult">
                ${message(code: 'renewalEvaluation.orgsWithoutResult.label')} <ui:totalNumber
                        total="${orgsWithoutResult.size()}"/>
            </a>

            <a class="item" data-tab="orgInsertedItself">
                ${message(code: 'renewalEvaluation.orgInsertedItself.label')} <ui:totalNumber
                        total="${orgInsertedItself.size()}"/>
            </a>
        </div>

        <div class="ui bottom attached active tab segment" data-tab="orgsContinuetoSubscription">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.continuetoSubscription.label')} <ui:totalNumber
                    total="${orgsContinuetoSubscription.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgsContinuetoSubscription]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="newOrgsContinuetoSubscription">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.newOrgstoSubscription.label')} <ui:totalNumber
                    total="${newOrgsContinuetoSubscription.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: newOrgsContinuetoSubscription]"/>
        </div>

        <div class="ui bottom attached tab segment" data-tab="orgsWithTermination">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.withTermination.label')} <ui:totalNumber
                    total="${orgsWithTermination.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgsWithTermination]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="orgsWithoutResult">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgsWithoutResult.label')} (${message(code: 'surveys.tabs.termination')})<ui:totalNumber
                    total="${orgsWithoutResult.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgsWithoutResult]"/>
        </div>

        <div class="ui bottom attached tab segment" data-tab="orgInsertedItself">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgInsertedItself.label')}<ui:totalNumber
                    total="${orgInsertedItself.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgInsertedItself]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="orgsWithMultiYearTermSub">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.withMultiYearTermSub.label')} <ui:totalNumber
                    total="${orgsWithMultiYearTermSub.size()}"/></h4>

            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'default.startDate.label.shy')}</th>
                    <th>${message(code: 'default.endDate.label.shy')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th class="center aligned">
                        <ui:optionsIcon />
                    </th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${orgsWithMultiYearTermSub}" var="sub" status="i">
                    <g:set value="${sub.getSubscriberRespConsortia()}" var="subscriberOrg"/>
                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
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
                                            class="${Btn.MODERN.SIMPLE}"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                                </g:if>
                                <g:if test="${sub._getCalculatedPreviousForSurvey()}">
                                    <br/>
                                    <br/>
                                    <%-- TODO Moe --%>
                                    <g:link controller="subscription" action="show"
                                            id="${sub._getCalculatedPreviousForSurvey()?.id}"
                                            class="${Btn.MODERN.SIMPLE}"><i class="${Icon.SUBSCRIPTION} yellow"></i></g:link>
                                </g:if>
                            </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>

        <div class="ui bottom attached tab segment" data-tab="orgsWithParticipationInParentSuccessor">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgsWithParticipationInParentSuccessor.label')} <ui:totalNumber
                    total="${orgsWithParticipationInParentSuccessor.size() }"/></h4>

            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'default.startDate.label.shy')}</th>
                    <th>${message(code: 'default.endDate.label.shy')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th class="center aligned">
                        <ui:optionsIcon />
                    </th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${orgsWithParticipationInParentSuccessor}" var="sub" status="i">
                    <g:set value="${sub.getSubscriberRespConsortia()}" var="subscriberOrg"/>
                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
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
                                            class="${Btn.MODERN.SIMPLE}"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                                </g:if>
                                <g:if test="${sub._getCalculatedPreviousForSurvey()}">
                                    <br/>
                                    <br/>
                                    <%-- TODO Moe --%>
                                    <g:link controller="subscription" action="show"
                                            id="${sub._getCalculatedPreviousForSurvey()?.id}"
                                            class="${Btn.MODERN.SIMPLE}"><i class="${Icon.SUBSCRIPTION} yellow"></i></g:link>
                                </g:if>
                            </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>


    <g:if test="${editable}">
        <g:form action="setSurveyWorkFlowInfos" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, setSurveyWorkFlowInfo: 'workflowRenewalSent']">

            <div class="ui right floated compact segment">
                <div class="ui checkbox">
                    <input type="checkbox" onchange="this.form.submit()"
                           name="renewalSent" ${surveyInfo.isRenewalSent ? 'checked' : ''}>
                    <label><g:message code="surveyInfo.isRenewalSent.label"/></label>
                </div>
            </div>

        </g:form>
    </g:if>


    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.propertiesChanged = function (propertyDefinitionId) {
            $.ajax({
                url: '<g:createLink controller="survey" action="showPropertiesChanged" params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]"/>&propertyDefinitionId='+propertyDefinitionId,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#modalPropertiesChanged").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal('show');
                }
            });
        }
    </laser:script>

    <g:if test="${editable}">
        <laser:render template="/templates/documents/modal"
                      model="${[newModalId: "modalCreateDocumentRenewal", owntp: 'subscription']}"/>


        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.callbacks.modal.onShow.modalCreateDocumentRenewal = function(trigger) {
                $('#modalCreateDocumentRenewal input[name=ownerid]').attr('value', $(trigger).attr('data-ownerid'))
                $('#modalCreateDocumentRenewal input[name=ownerclass]').attr('value', $(trigger).attr('data-ownerclass'))
                $('#modalCreateDocumentRenewal input[name=ownertp]').attr('value', $(trigger).attr('data-ownertp'))
                $('#modalCreateDocumentRenewal select[name=doctype]').dropdown('set selected', $(trigger).attr('data-doctype'))
            }
        </laser:script>

    </g:if>

</g:else>

<laser:htmlEnd />
