<%@page import="de.laser.ui.Icon; de.laser.ui.Btn" %>
<ui:modal id="financeEnrichment" message="financials.enrichment.header" msgSave="${message(code: 'financials.enrichment.submit')}">
    <ui:msg class="info" hideClose="true" showIcon="true" message="financials.enrichment.manual" />

        <g:if test="${subscription}">
            <g:form class="ui form" method="post" enctype="multipart/form-data" mapping="subfinance" controller="finance" action="index" params="[sub: subscription.id]">
                <div class="two fields">
                    <div class="field">
                        <ui:select name="selectedCostItemElement" id="selectedCostItemElement" class="ui dropdown"
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
        %{-- TODO Moe use this for survey context
        <g:elseif test="surveyContext">
            <g:form class="ui form" method="post" enctype="multipart/form-data" controller="survey" action="???">
                <div class="two fields">
                    <div class="field">
                        <ui:select name="selectedCostItemElement" id="selectedCostItemElement" class="ui dropdown"
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
        </g:elseif>
        --}%
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