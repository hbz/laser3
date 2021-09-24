<%@ page import="de.laser.reporting.myInstitution.base.BaseConfig; de.laser.reporting.export.base.BaseExport; de.laser.reporting.export.myInstitution.ExportGlobalHelper;" %>
<laser:serviceInjection />
<!-- _queryModal.gsp -->

    <semui:modal id="${modalID}" message="reporting.modal.export" msgSave="${message(code: 'default.button.export.label')}">

        <g:form controller="ajaxHtml" action="chartQueryExport" method="POST" target="_blank">

            <div class="ui form">

                <div class="ui vertical segment">
                    <div class="fields">

                        <div id="fileformat-csv" class="wide eight field">
                            <label>${message(code: 'reporting.modal.export.cfg.csv')}</label>
                            <p>
                                ${message(code: 'reporting.modal.export.cfg.csv.fieldSeparator')}: <span class="ui circular label">${BaseExport.CSV_FIELD_SEPARATOR}</span> <br />
                                ${message(code: 'reporting.modal.export.cfg.csv.fieldQuotation')}: <span class="ui circular label">${BaseExport.CSV_FIELD_QUOTATION}</span> <br />
                                ${message(code: 'reporting.modal.export.cfg.csv.valueSeparator')}: <span class="ui circular label">${BaseExport.CSV_VALUE_SEPARATOR}</span>
                            </p>
                        </div>

                        %{-- ERMS-3614
                        <div id="fileformat-pdf" class="wide eight field">
                            <label>PDF-Konfiguration</label>
                            <p>
                                ${message(code: 'reporting.modal.export.cfg.pdf.pageFormat')}: <span class="ui circular label">${message(code: 'reporting.modal.export.cfg.pdf.pageFormat.default')}</span> <br />
                                ${message(code: 'reporting.modal.export.cfg.pdf.queryInfo')}: <span class="ui circular label">${message(code: 'reporting.modal.export.cfg.pdf.queryInfo.default')}</span> <br />
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
                                <label for="fileformat">${message(code: 'default.fileFormat.label')}</label>
                                <g:select name="fileformat" class="ui selection dropdown la-not-clearable"
                                          optionKey="key" optionValue="value"
                                          from="${[csv:'CSV']}"
                                />
                                %{-- ERMS-3614
                                <g:select name="fileformat" class="ui selection dropdown la-not-clearable"
                                          optionKey="key" optionValue="value"
                                          from="${[csv:'CSV', pdf:'PDF']}"
                                />
                                --}%
                            </div>
                            <div class="field">
                                <label for="filename">${message(code: 'default.fileName.label')}</label>
                                <input name="filename" id="filename" value="${message(code: 'reporting.modal.export.fileName.auto')}" disabled />
                            </div>
                        </div>

                    </div><!-- .fields -->
                </div><!-- .segment -->

            </div><!-- .form -->

            <input type="hidden" name="token" value="${token}" />
            <input type="hidden" name="context" value="${BaseConfig.KEY_MYINST}" />
            %{-- ERMS-3614
            <input type="hidden" name="imageData" value="" />
            <input type="hidden" name="imageSize" value="" />
            --}%
    </g:form>

</semui:modal>

<laser:script file="${this.getGroovyPageFileName()}">

    $('#${modalID} select[name=fileformat]').on( 'change', function() {
        $('#${modalID} *[id^=fileformat-').addClass('hidden')
        $('#${modalID} *[id^=fileformat-' + $('#${modalID} select[name=fileformat]').val()).removeClass('hidden')
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

<!-- _queryModal.gsp -->

