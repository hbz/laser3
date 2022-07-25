<%@ page import="de.laser.titles.BookInstance; de.laser.remote.ApiSource; de.laser.storage.RDStore; de.laser.Subscription; de.laser.Platform; de.laser.Org; de.laser.IssueEntitlementGroup;" %>
<laser:htmlStart message="subscription.details.renewEntitlements.label" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
        <ui:crumb class="active" controller="survey" action="surveyEvaluation" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" message="surveyEvaluation.titles.label"/>
    </g:if>

</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" action="renewEntitlements" id="${surveyConfig.id}"
                    params="${[exportKBart: true, participant: participant.id]}">KBART Export</g:link>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:link class="item" action="renewEntitlements" id="${surveyConfig.id}"
                    params="${[exportXLSX: true, participant: participant.id]}">${message(code: 'default.button.exports.xls')}</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>

        <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_SURVEY_STARTED.id, RDStore.SURVEY_SURVEY_COMPLETED.id]}">
            <ui:actionsDropdown>
                <g:if test="${surveyOrg.finishDate}">
                    <ui:actionsDropdownItem controller="survey" action="openSurveyAgainForParticipant"
                                               params="[surveyConfigID: surveyConfig.id, participant: participant.id]"
                                               message="openSurveyAgainForParticipant.button"/>

                </g:if>
                <g:if test="${!surveyOrg.finishDate}">
                    <ui:actionsDropdownItem controller="survey" action="finishSurveyForParticipant"
                                               params="[surveyConfigID: surveyConfig.id, participant: participant.id]"
                                               message="finishSurveyForParticipant.button"/>

                </g:if>
            </ui:actionsDropdown>
        </g:if>


</ui:controlButtons>

<ui:h1HeaderWithIcon message="issueEntitlementsSurvey.label" type="Survey">
    : <g:link controller="subscription" action="index" id="${subscriptionParticipant.id}">${surveyConfig.surveyInfo.name}</g:link>
</ui:h1HeaderWithIcon>

    <ui:messages data="${flash}"/>

<div class="eight wide field" style="text-align: right;">
    <g:link action="evaluationParticipant"
            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
            class="ui icon button">
        <g:message code="surveyInfo.backToSurvey"/>
    </g:link>
</div>

<g:if test="${participant}">
    <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg.getGeneralContactPersons(false)}"/>

    <table class="ui table la-js-responsive-table la-table compact">
        <tbody>
        <tr>
            <td>
                <p><strong>${choosenOrg.name} (${choosenOrg.shortname})</strong></p>

                ${choosenOrg.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <laser:render template="/templates/cpa/person_details"
                                  model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:if>
            </td>
        </tr>
        </tbody>
    </table>
</g:if>

%{--<g:if test="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant).finishDate != null}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message code="renewEntitlementsWithSurvey.finish.info"/>
            </p>
        </div>
    </div>
</g:if>--}%

<ui:form>

    <h2 class="ui header left aligned aligned"><g:message
            code="renewEntitlementsWithSurvey.currentEntitlements"/> (${ies.size()})</h2>

    <div class="ui grid">
        <div class="sixteen wide column">
            <g:set var="counter" value="${1}"/>
            <g:set var="sumlistPrice" value="${0}"/>
            <g:set var="sumlocalPrice" value="${0}"/>

            <g:form action="completeIssueEntitlementsSurveyforParticipant"
                    params="[id: surveyConfig.id, participant: participant.id]" class="ui form">
                <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header">
                    <thead>
                    <tr>
                        <th>
                            <input id="select-all" type="checkbox" name="chkall" onClick="JSPC.app.selectAll()"/>
                        </th>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th><g:message code="title.label"/></th>
                        <th class="two wide"><g:message code="tipp.price"/></th>

                    </tr>
                    </thead>
                    <tbody>

                    <g:each in="${ies}" var="ie">
                        <g:set var="tipp" value="${ie.tipp}"/>
                        <tr>

                        <td>
                        <g:if test="${editable}">
                            <g:checkBox name="selectedIEs" value="${ie.id}" checked="false"
                                        class="bulkcheck center aligned"/>
                        </g:if>


                        </td>
                        <td>${counter++}</td>
                        <td class="titleCell">
                            <ui:ieAcceptStatusIcon status="${ie.acceptStatus}"/>

                            <!-- START TEMPLATE -->
                            <laser:render template="/templates/title_short"
                                      model="${[ie: ie, tipp: tipp,
                                                showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false, overwriteEditable: false]}"/>
                            <!-- END TEMPLATE -->
                        </td>
                        <td>
                            <g:if test="${ie.priceItems}">
                                    <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                        <g:message code="tipp.price.listPrice"/>: <ui:xEditable field="listPrice" owner="${priceItem}" format="" overwriteEditable="${false}"/>
                                        <ui:xEditableRefData field="listCurrency" owner="${priceItem}" config="Currency" overwriteEditable="${false}"/>
                                    <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                                        %{--<g:message code="tipp.price.localPrice"/>: <ui:xEditable field="localPrice"
                                                                                          owner="${priceItem}"/> <ui:xEditableRefData
                                        field="localCurrency" owner="${priceItem}"
                                        config="Currency"/>--}% <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                                        <%--<ui:xEditable field="startDate" type="date"
                                                         owner="${priceItem}"/><ui:dateDevider/><ui:xEditable
                                            field="endDate" type="date"
                                            owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                        <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                        <g:set var="sumlistPrice" value="${sumlistPrice + (priceItem.listPrice ?: 0)}"/>
                                        <g:set var="sumlocalPrice" value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>
                                    </g:each>
                            </g:if>
                        </td>

                    </g:each>
                    </tbody>
                    <tfoot>
                    <tr>
                        <th></th>
                        <th></th>
                        <th></th>
                        <th><g:message code="financials.export.sums"/> <br/>
                            <g:message code="tipp.price.listPrice"/>: <g:formatNumber number="${sumlistPrice}"
                                                                                type="currency"/><br/>
                            %{--<g:message code="tipp.price.localPrice"/>: <g:formatNumber number="${sumlocalPrice}" type="currency"/>--}%
                        </th>
                        <th></th>
                    </tr>
                    </tfoot>
                </table>

                <g:if test="${editable}">
                    <div class="ui two fields">
                        <div class="field">
                            <label for="issueEntitlementGroup">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.add')}:</label>

                            <select name="issueEntitlementGroupID" id="issueEntitlementGroup"
                                    class="ui search dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${subscriptionParticipant.ieGroups.sort { it.name }}" var="titleGroup">
                                    <option value="${titleGroup.id}">
                                        ${titleGroup.name} (${titleGroup.items.size()})
                                    </option>
                                </g:each>
                            </select>
                        </div>

                        <div class="field">
                            <label for="issueEntitlementGroup">${message(code: 'issueEntitlementGroup.entitlementsRenew.selected.new')}:</label>
                            <input type="text" name="issueEntitlementGroupNew"
                                   value="Phase ${IssueEntitlementGroup.findAllBySubAndNameIlike(subscriptionParticipant, 'Phase').size() + 1}">
                        </div>

                    </div>


                    <div class="ui two fields">
                        <div class="field">
                            <button type="submit" name="process" value="preliminary" class="ui green button"><g:message
                                    code="renewEntitlementsWithSurvey.preliminary"/></button>
                        </div>

                        <div class="field" style="text-align: right;">
                            <button type="submit" name="process" value="reject" class="ui red button"><g:message
                                    code="renewEntitlementsWithSurvey.reject"/></button>
                        </div>
                    </div>
                </g:if>
            </g:form>
        </div>

    </div>

</ui:form>



<laser:script file="${this.getGroovyPageFileName()}">

    <g:if test="${editable}">

        JSPC.app.selectAll = function () {
          $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
        }

    </g:if>

</laser:script>

<laser:htmlEnd />
