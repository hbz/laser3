<%@ page import="de.laser.reporting.export.GlobalExportHelper; de.laser.reporting.export.base.BaseDetailsExport; de.laser.reporting.export.base.BaseExportHelper; de.laser.reporting.export.DetailsExportManager; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.myInstitution.base.BaseDetails;" %>
<laser:serviceInjection />
<!-- _detailsModal.gsp -->
<g:set var="export" value="${DetailsExportManager.createExport( token, BaseConfig.KEY_MYINST )}" />

<g:if test="${export}">
    <g:set var="formFields" value="${export.getAllFields()}" />
    <g:set var="filterLabels" value="${GlobalExportHelper.getCachedFilterLabels( token )}" />
    <g:set var="queryLabels" value="${GlobalExportHelper.getCachedQueryLabels( token )}" />

    <ui:modal id="${modalID}" text="${message(code: 'reporting.modal.export.key.' + export.KEY)}" msgSave="${message(code: 'default.button.export.label')}">

        <g:set var="dcSize" value="${GlobalExportHelper.getDetailsCache(token).idList.size()}" />
        <g:if test="${dcSize > 50}">
            <div class="ui info message">
                <i class="info circle icon"></i> ${message(code: 'reporting.modal.export.todoTime')}
            </div>
        </g:if>

        <div class="ui form">
            <div class="field">
                <label>${message(code: 'reporting.modal.export.todoData')}</label>
            </div>
            <div class="ui segments">
                <laser:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterLabels, stacked: true]}" />
                <laser:render template="/myInstitution/reporting/details/generic_queryLabels" model="${[queryLabels: queryLabels, stacked: true]}" />
            </div>
        </div>

        <g:form controller="ajaxHtml" action="chartDetailsExport" method="POST" target="_blank" style="margin:0">

            <div class="ui form">

            <div class="ui vertical segment">
                <div class="field">
                    <label>${message(code: 'reporting.modal.export.todoFields')}</label>
                </div>
                <div class="fields inline" style="margin-top:1em">%{-- css workaround - fomantic upgrade --}%

                    <g:each in="${BaseExportHelper.reorderFieldsForUI( formFields.findAll { !BaseDetailsExport.isFieldMultiple( it.key ) } )}" var="field" status="fc">
                        <div class="wide eight field">

                            <g:if test="${field.key == 'globalUID'}">
                                <div class="ui checkbox">
                                    <input type="checkbox" name="cde:${field.key}" id="cde:${field.key}">
                                    <label for="cde:${field.key}">${export.getFieldLabel(field.key as String)}</label>
                                </div>
                            </g:if>
                            <g:else>
                                <div class="ui checkbox">
                                    <input type="checkbox" name="cde:${field.key}" id="cde:${field.key}" checked="checked">
                                    <label for="cde:${field.key}">${export.getFieldLabel(field.key as String)}</label>
                                </div>
                            </g:else>

                        </div><!-- .field -->

                        <g:if test="${fc%2 == 1}">
                            </div>
                            <div class="fields inline" style="margin-top:1em">%{-- css workaround - fomantic upgrade --}%
                        </g:if>
                    </g:each>

                </div><!-- .fields -->

                <div class="fields">

                    <g:each in="${formFields.findAll{ BaseDetailsExport.isFieldMultiple( it.key ) }}" var="field" status="fc"> %{-- TODO --}%
                        <div class="wide eight field">

                            <g:set var="multiList" value="${BaseDetailsExport.getMultipleFieldListForDropdown(field.key, export.getCurrentConfig( export.KEY ))}" />

                            <g:select name="cde:${field.key}" class="ui selection dropdown"
                                      from="${multiList}" multiple="true"
                                      optionKey="${{it[0]}}" optionValue="${{it[1]}}"
                                      noSelection="${['': export.getFieldLabel(field.key as String)]}"
                            />

                        </div><!-- .field -->

                        <g:if test="${fc%2 == 1}">
                            </div>
                            <div class="fields">
                        </g:if>
                    </g:each>

                </div><!-- .fields -->
            </div><!-- .segment -->

            <div class="ui vertical segment">
                <div class="fields">

                    <div class="wide eight field fileformat-details-csv">
                        <label>${message(code: 'reporting.modal.export.cfg.csv')}</label>
                        <p>
                            ${message(code: 'reporting.modal.export.cfg.csv.fieldSeparator')}: <span class="ui circular label">${BaseDetailsExport.CSV_FIELD_SEPARATOR}</span> <br />
                            ${message(code: 'reporting.modal.export.cfg.csv.fieldQuotation')}: <span class="ui circular label">${BaseDetailsExport.CSV_FIELD_QUOTATION}</span> <br />
                            ${message(code: 'reporting.modal.export.cfg.csv.valueSeparator')}: <span class="ui circular label">${BaseDetailsExport.CSV_VALUE_SEPARATOR}</span> <br />
                        </p>
                        <p>
                            <span class="ui checkbox">
                                <input type="checkbox" name="hideEmptyResults-csv" id="hideEmptyResults-csv" />
                                <label for="hideEmptyResults-csv">${message(code: 'reporting.modal.export.cfg.hideEmptyResults')}</label>
                            </span>
                        </p>
                    </div>
                    <div class="wide eight field fileformat-details-xlsx">
                        <label>${message(code: 'reporting.modal.export.cfg.xlsx')}</label>
                        <p>
                            <br />
                            <span class="ui checkbox">
                                <input type="checkbox" name="insertNewLines-xlsx" id="insertNewLines-xlsx" />
                                <label for="insertNewLines-xlsx">${message(code: 'reporting.modal.export.cfg.xlsx.newLines')}</label>
                            </span>
                            <br />
                            <span class="ui checkbox">
                                <input type="checkbox" name="hideEmptyResults-xlsx" id="hideEmptyResults-xlsx" />
                                <label for="hideEmptyResults-xlsx">${message(code: 'reporting.modal.export.cfg.hideEmptyResults')}</label>
                            </span>
                            <br />
                            <span class="ui checkbox">
                                <input type="checkbox" name="useHyperlinks-xlsx" id="useHyperlinks-xlsx" />
                                <label for="useHyperlinks-xlsx">${message(code: 'reporting.modal.export.cfg.useHyperlinks')}</label>
                            </span>
                        </p>
                    </div>
                    <div class="wide eight field fileformat-details-pdf">
                        <label>${message(code: 'reporting.modal.export.cfg.pdf')}</label>
                        <p>
                            ${message(code: 'reporting.modal.export.cfg.pdf.queryInfo')}: <span class="ui circular label">${message(code: 'reporting.modal.export.cfg.pdf.queryInfo.default')}</span> <br />
                        </p>
                        <p>
                            <span class="ui checkbox">
                                <input type="checkbox" name="useLineNumbers-pdf" id="useLineNumbers-pdf" checked="checked" />
                                <label for="useLineNumbers-pdf">${message(code: 'reporting.modal.export.cfg.pdf.useLineNumbers')}</label>
                            </span>
                            <br />
                            <span class="ui checkbox">
                                <input type="checkbox" name="hideEmptyResults-pdf" id="hideEmptyResults-pdf" />
                                <label for="hideEmptyResults-pdf">${message(code: 'reporting.modal.export.cfg.hideEmptyResults')}</label>
                            </span>
                            <br />
                            <span class="ui checkbox">
                                <input type="checkbox" name="useHyperlinks-pdf" id="useHyperlinks-pdf" />
                                <label for="useHyperlinks-pdf">${message(code: 'reporting.modal.export.cfg.useHyperlinks')}</label>
                            </span>
                            <br />
                            <span class="ui checkbox">
                                <input type="checkbox" name="useSmallFont-pdf" id="useSmallFont-pdf" />
                                <label for="useSmallFont-pdf">${message(code: 'reporting.modal.export.cfg.useSmallFont')}</label>
                            </span>
                        </p>
                    </div>

                    <div class="wide eight field">
                        <div class="field" style="margin-bottom: 1em !important;">
                            <label for="fileformat-details">${message(code: 'default.fileFormat.label')}</label>
                            <g:select name="fileformat" id="fileformat-details" class="ui selection dropdown la-not-clearable"
                                      optionKey="key" optionValue="value"
                                      from="${[csv:'CSV', pdf:'PDF', xlsx: 'XLSX']}"
                            />
                            %{-- <ui:dropdownWithI18nExplanations name="fileformat"
                                    class="ui dropdown la-not-clearable"
                                    from="[csv: ['CSV', 'Comma-Separated Values'], pdf: ['PDF', 'Portable Document Format'], xlsx: ['XLSX', 'Excel - Office Open XML']]" value="csv"
                                    optionKey="key"
                                    optionValue="${{it.value[0]}}"
                                    optionExpl="${{it.value[1]}}" /> --}%
                        </div>
                        <div class="ui field fileformat-details-pdf" style="margin-bottom: 1em !important;">
                            <label>${message(code: 'reporting.modal.export.cfg.pdf.pageFormat')}</label>
                            <g:select name="pageFormat-pdf" id="pageFormat-pdf" class="ui selection dropdown la-not-clearable"
                                      optionKey="key" optionValue="${{message(code: 'reporting.modal.export.cfg.pdf.format.' + it.key)}}"
                                      from="${BaseExportHelper.PDF_OPTIONS}"
                            />
                        </div>
                        <div class="field">
                            <label for="filename-details">${message(code: 'default.fileName.label')}</label>
                            <input name="filename" id="filename-details" value="${BaseExportHelper.getFileName(queryLabels)}" />
                        </div>
                    </div>

                </div><!-- .fields -->
            </div><!-- .segment -->

            </div><!-- .form -->

            <input type="hidden" name="token" value="${token}" />
            <input type="hidden" name="context" value="${BaseConfig.KEY_MYINST}" />
        </g:form>

    </ui:modal>

    <laser:script file="${this.getGroovyPageFileName()}">

        $('#${modalID} select[name=fileformat]').on( 'change', function() {
            $('#${modalID} div[class*=fileformat-details-').addClass('hidden')
            $('#${modalID} div[class*=fileformat-details-' + $('#${modalID} select[name=fileformat]').val()).removeClass('hidden')
        }).trigger('change');
    </laser:script>
</g:if>
<!-- _detailsModal.gsp -->

