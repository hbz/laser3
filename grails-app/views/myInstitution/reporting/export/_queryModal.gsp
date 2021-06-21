<%@ page import=" de.laser.reporting.myInstitution.base.BaseDetails; de.laser.reporting.export.AbstractExport; de.laser.reporting.export.ExportHelper;" %>
<laser:serviceInjection />
<!-- _queryChartModal.gsp -->

    <semui:modal id="${modalID}" text="Ergebnis exportieren" msgSave="Exportieren">

        <g:form controller="ajaxHtml" action="chartQueryExport" method="POST" target="_blank">

            <div class="ui form">

                <div class="ui vertical segment">
                    <div class="fields">

                        <div id="fileformat-csv" class="wide eight field">
                            <label>CSV-Konfiguration</label>
                            <p>
                                Feldtrenner: <span class="ui circular label">${AbstractExport.CSV_FIELD_SEPARATOR}</span> <br />
                                Zeichenkettentrenner: <span class="ui circular label">${AbstractExport.CSV_FIELD_QUOTATION}</span> <br />
                                Trenner für mehrfache Werte: <span class="ui circular label">${AbstractExport.CSV_VALUE_SEPARATOR}</span>
                            </p>
                        </div>

                        <div id="fileformat-pdf" class="wide eight field">
                            <label>PDF-Konfiguration</label>
                            <p>
                                Seitenformat: <span class="ui circular label">auto</span> <br />
                                Suchinformationen: <span class="ui circular label">anzeigen</span> <br />
                            </p>
                        </div>

                        <div class="wide eight field">
                            <div class="field" style="margin-bottom: 1em !important;">
                                <label for="fileformat">Dateiformat</label>
                                <g:select name="fileformat" class="ui selection dropdown la-not-clearable"
                                          optionKey="key" optionValue="value"
                                          from="${[csv:'CSV', pdf: 'PDF']}"
                                />
                            </div>
                            <div class="field">
                                <label for="filename">Dateiname</label>
                                <input name="filename" id="filename" value="${ExportHelper.getFileName(queryLabels)}" />
                            </div>
                        </div>

                    </div><!-- .fields -->
                </div><!-- .segment -->

            </div><!-- .form -->

            <input type="hidden" name="token" value="${token}" />
        </g:form>

    </semui:modal>

    <laser:script file="${this.getGroovyPageFileName()}">

        $('select[name=fileformat]').on( 'change', function() {
            $('*[id^=fileformat-').addClass('hidden')
            $('*[id^=fileformat-' + $('select[name=fileformat]').val()).removeClass('hidden')
        }).trigger('change');
    </laser:script>

<!-- _queryChartModal.gsp -->

