<%@ page import="de.laser.reporting.export.base.BaseDetailsExport; de.laser.reporting.export.base.BaseExportHelper; de.laser.reporting.report.myInstitution.base.BaseConfig;" %>
<laser:serviceInjection />
<!-- _queryChartModal.gsp -->

    <ui:modal id="${modalID}" text="Export" msgSave="${message(code: 'default.button.export.label')}">

        <g:form controller="ajaxHtml" action="chartQueryExport" method="POST" target="_blank">

            <div class="ui form">

                <div class="ui vertical segment">
                    <div class="fields">

                        <div id="fileformat-query-csv" class="wide eight field">
                            <label>${message(code: 'reporting.modal.export.cfg.csv')}</label>
                            <p>
                                ${message(code: 'reporting.modal.export.cfg.csv.fieldSeparator')}: <span class="ui circular label">${BaseDetailsExport.CSV_FIELD_SEPARATOR}</span> <br />
                                ${message(code: 'reporting.modal.export.cfg.csv.fieldQuotation')}: <span class="ui circular label">${BaseDetailsExport.CSV_FIELD_QUOTATION}</span> <br />
                                ${message(code: 'reporting.modal.export.cfg.csv.valueSeparator')}: <span class="ui circular label">${BaseDetailsExport.CSV_VALUE_SEPARATOR}</span>
                            </p>
                        </div>
                        <div id="fileformat-query-xlsx" class="wide eight field">
                            <label>${message(code: 'reporting.modal.export.cfg.xlsx')}</label>
                            <p>
                                ${message(code: 'reporting.modal.export.cfg.xlsx.default')}
                            </p>
                        </div>

                        %{-- ERMS-3614
                        <div id="fileformat-query-pdf" class="wide eight field">
                            <label>PDF-Konfiguration</label>
                            <p>
                                ${message(code: 'reporting.modal.export.cfg.pdf.pageFormat')}: <span class="ui circular label">auto</span> <br />
                                ${message(code: 'reporting.modal.export.cfg.pdf.queryInfo')}: <span class="ui circular label">anzeigen</span> <br />
                            </p>
                            <p>
                                <g:select name="contentType" class="ui selection dropdown la-not-clearable"
                                          optionKey="key" optionValue="value"
                                          from="${[table:'Tabellarische Darstellung', image: 'Darstellung als Diagramm']}"
                                />
                            </p>
                        </div>
                        --}%

                        <div class="wide eight field">
                            <div class="field" style="margin-bottom: 1em !important;">
                                <label for="fileformat-query">${message(code: 'default.fileFormat.label')}</label>
                                <g:select name="fileformat" id="fileformat-query" class="ui selection dropdown la-not-clearable"
                                          optionKey="key" optionValue="value"
                                          from="${[csv:'CSV', xlsx: 'XLSX']}"
                                />
                                %{-- ERMS-3614
                                <ui:dropdownWithI18nExplanations name="fileformat"
                                        class="ui dropdown la-not-clearable"
                                        from="[csv: ['CSV', 'Comma-Separated Values'], pdf: ['PDF', 'Portable Document Format'], xlsx: ['XLSX', 'Excel - Office Open XML']]" value="csv"
                                        optionKey="key"
                                        optionValue="${{it.value[0]}}"
                                        optionExpl="${{it.value[1]}}" />
                                --}%
                            </div>
                            <div class="field">
                                <label for="filename-query">${message(code: 'default.fileName.label')}</label>
                                <input name="filename" id="filename-query" value="${BaseExportHelper.getFileName([subscription.name])}"/>
                            </div>
                        </div>

                    </div><!-- .fields -->
                </div><!-- .segment -->

            </div><!-- .form -->

            <input type="hidden" name="token" value="${token}" />
            <input type="hidden" name="context" value="${BaseConfig.KEY_SUBSCRIPTION}" />
            %{-- ERMS-3614
            <input type="hidden" name="imageData" value="" />
            <input type="hidden" name="imageSize" value="" />
            --}%
    </g:form>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">

    $('#${modalID} select[name=fileformat]').on( 'change', function() {
        $('#${modalID} *[id^=fileformat-query-]').addClass('hidden')
        $('#${modalID} *[id^=fileformat-query-' + $('#${modalID} select[name=fileformat]').val() + ']').removeClass('hidden')
    }).trigger('change');

    /* -- TODO -- */

    $('#query-export-button').on( 'click', function() {
        $('#${modalID} input[name=imageData]').attr( 'value',
            JSPC.app.reporting.current.chart.echart.getDataURL({
                pixelRatio: 1
            })
        );
        $('#${modalID} input[name=imageSize]').attr( 'value',
            $('#chart-wrapper').width() + ':' + $('#chart-wrapper').height()
        );
    });
</laser:script>

<!-- _queryChartModal.gsp -->

