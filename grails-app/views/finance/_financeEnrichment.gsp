<%@page import="de.laser.ui.Icon; de.laser.ui.Btn" %>
<ui:modal id="financeEnrichment" message="financials.enrichment.header" msgSave="${message(code: 'financials.enrichment.submit')}">
    <g:if test="${subscription && !surveyInfo}">
        <ui:msg class="info" hideClose="true" showIcon="true" message="financials.enrichment.manual" />
        <g:form class="ui form" method="post" enctype="multipart/form-data" mapping="subfinance" controller="finance" action="index" params="[sub: subscription.id, showBulkCostItems: true]">

            <div class="two fields">
                <div class="field">
                    <ui:select name="selectedCostItemElement" id="selectedCostItemElement" class="ui dropdown clearable"
                               from="${assignedCostItemElements}"
                               optionKey="id"
                               optionValue="value" />
                </div>
            </div>
            <g:render template="/templates/genericFileImportFormatSelector"/>
        </g:form>
    </g:if>
    <g:elseif test="${surveyInfo}">
        <ui:msg class="info" hideClose="true" showIcon="true" message="${actionName == 'surveyCostItemsPackages' ? 'financials.enrichment.manual.surveyPackages' : (actionName == 'surveyCostItemsSubscriptions' ? 'financials.enrichment.manual.surveySubscriptions' : 'financials.enrichment.manual.survey')}" />
        <g:form class="ui form" method="post" enctype="multipart/form-data" controller="survey" action="${actionName}" params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]">
            <div class="${(actionName == 'surveyCostItemsPackages' || actionName == 'surveyCostItemsSubscriptions') ? 'three' : 'two'} fields">
                <div class="field">
                    <ui:select name="selectedCostItemElement" id="selectedCostItemElement" class="ui dropdown clearable"
                               from="${assignedCostItemElements}"
                               optionKey="id"
                               optionValue="value" />
                </div>
                <g:if test="${actionName == 'surveyCostItemsPackages'}">
                    <div class="field">
                        <g:select name="selectedPackageID" id="selectedPackageID" class="ui dropdown clearable"
                                  from="${assignedPackages}"
                                  optionKey="id"
                                  optionValue="name" />
                    </div>
                </g:if>

                <g:if test="${actionName == 'surveyCostItemsSubscriptions'}">
                    <div class="field">
                        <g:select name="selectedSurveyConfigSubscriptionID" id="selectedSurveyConfigSubscriptionID" class="ui dropdown clearable "
                                  from="${assignedSubscriptions}"
                                  optionKey="id"
                                  optionValue="${{it.getLabel()}}" />
                    </div>
                </g:if>
            </div>
            <g:render template="/templates/genericFileImportFormatSelector"/>
        </g:form>
    </g:elseif>
</ui:modal>

<g:render template="/templates/genericFileImportJS"/>