<%@page import="de.laser.ui.Icon; de.laser.ui.Btn" %>
<ui:modal id="financeEnrichment" message="financials.enrichment.header" msgSave="${message(code: 'financials.enrichment.submit')}">

        <g:if test="${subscription && !surveyInfo}">
            <ui:msg class="info" hideClose="true" showIcon="true" message="financials.enrichment.manual" />
            <g:form class="ui form" method="post" enctype="multipart/form-data" mapping="subfinance" controller="finance" action="index" params="[sub: subscription.id, showBulkCostItems: true]">
                <div class="two fields">
                    <div class="field">
                        <ui:select name="selectedCostItemElement" id="selectedCostItemElement" class="ui dropdown clearable "
                                   from="${assignedCostItemElements}"
                                   optionKey="id"
                                   optionValue="value" />
                    </div>
                    <div class="field">
                        <div class="ui action input">
                            <input type="text" readonly="readonly"
                                   placeholder="${message(code: 'template.addDocument.selectFile')}">
                            <input type="file" id="costInformation" name="costInformation" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                                   style="display: none;">
                            <div class="${Btn.ICON.SIMPLE}">
                                <i class="${Icon.CMD.ATTACHMENT}"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </g:form>
        </g:if>
        <g:elseif test="${surveyInfo}">
            <ui:msg class="info" hideClose="true" showIcon="true" message="${actionName == 'surveyCostItemsPackages' ? 'financials.enrichment.manual.surveyPackages' : 'financials.enrichment.manual.survey'}" />
            <g:form class="ui form" method="post" enctype="multipart/form-data" controller="survey" action="$actionName" params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id]">
                <div class="${actionName == 'surveyCostItemsPackages' ? 'three' : 'two'} fields">
                    <div class="field">
                        <ui:select name="selectedCostItemElement" id="selectedCostItemElement" class="ui dropdown clearable "
                                   from="${assignedCostItemElements}"
                                   optionKey="id"
                                   optionValue="value" />
                    </div>
                    <g:if test="${actionName == 'surveyCostItemsPackages'}">
                        <div class="field">
                            <g:select name="selectedPackageID" id="selectedPackageID" class="ui dropdown clearable "
                            from="${assignedPackages}"
                            optionKey="id"
                            optionValue="name" />
                        </div>
                    </g:if>
                    <div class="field">
                        <div class="ui action input">
                            <input type="text" readonly="readonly"
                                   placeholder="${message(code: 'template.addDocument.selectFile')}">
                            <input type="file" id="costInformation" name="costInformation" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                                   style="display: none;">
                            <div class="${Btn.ICON.SIMPLE}">
                                <i class="${Icon.CMD.ATTACHMENT}"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </g:form>
        </g:elseif>
    <laser:script file="${this.getGroovyPageFileName()}">
        $('.action .icon.button').click(function () {
            $(this).parent('.action').find('input:file').click();
        });

        $('input:file', '.ui.action.input').on('change', function (e) {
            var name = e.target.files[0].name;
            $('input:text', $(e.target).parent()).val(name);
        });
    </laser:script>
</ui:modal>