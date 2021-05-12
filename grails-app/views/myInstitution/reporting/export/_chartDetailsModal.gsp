<%@ page import="de.laser.reporting.export.AbstractExport; de.laser.reporting.export.ExportHelper; de.laser.reporting.export.GenericExportManager" %>
<laser:serviceInjection />
<!-- _chartDetailsModal.gsp -->
<g:set var="export" value="${GenericExportManager.createExport( token )}" />

<g:if test="${export}">
    <g:set var="formFields" value="${export.getAllFields()}" />
    <g:set var="filterLabels" value="${ExportHelper.getCachedFilterLabels( token )}" />
    <g:set var="queryLabels" value="${ExportHelper.getCachedQueryLabels( token )}" />

    <semui:modal id="${modalID}" text="CSV-${message(code: 'reporting.export.key.' + export.KEY)}" hideSubmitButton="true">

        <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterLabels, tmplSize: 'tiny']}" />

        <g:render template="/myInstitution/reporting/details/generic_queryLabels" model="${[queryLabels: queryLabels, tmplSize: 'tiny']}" />

        <p><span class="ui label red">DEMO : in Entwicklung</span></p>

        <g:form controller="ajaxHtml" action="chartDetailsExport" method="POST" target="_blank">
            <div class="ui form">
                <div class="field">
                    <label>Zu exportierende Felder</label>
                </div>

                <div class="fields">
                    <g:each in="${ExportHelper.reorderFieldsForUI( formFields.findAll { !ExportHelper.isFieldMultiple( it.key ) } )}" var="field" status="fc">
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
                            <div class="fields">
                        </g:if>
                    </g:each>

                </div>
                <div class="fields">

                    <g:each in="${formFields.findAll { ['x-identifier'].contains( it.key ) }}" var="field" status="fc"> %{-- TODO --}%
                        <div class="wide eight field">

                            <g:set var="idnsList" value="${ExportHelper.getIdentifiersForDropdown(export.getCurrentConfig( export.KEY ))}" />

                            <g:select name="cde:${field.key}" class="ui selection dropdown"
                                      from="${idnsList}" multiple="true"
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

                <br />

                <div class="fields">

                    <div class="wide eight field">
                        <label>Konfiguration</label>
                        <p>
                            Feldtrenner: <span class="ui circular label">${AbstractExport.CSV_FIELD_SEPARATOR}</span> <br />
                            Zeichenkettentrenner: <span class="ui circular label">${AbstractExport.CSV_FIELD_QUOTATION}</span> <br />
                            Trenner für mehrfache Werte: <span class="ui circular label">${AbstractExport.CSV_VALUE_SEPARATOR}</span>
                        </p>
                    </div>
                    <div class="wide eight field">
                        <label for="filename">Dateiname</label>
                        <input name="filename" id="filename" value="${ExportHelper.getFileName(queryLabels)}" />
                        <br />
                        <br />
                        <button class="ui button positive right floated" id="export-chart-details-as-csv">Als CSV-Datei exportieren</button>
                    </div>

                </div><!-- .fields -->
            </div><!-- .form -->

            <input type="hidden" name="token" value="${token}" />
        </g:form>

    </semui:modal>
</g:if>
<!-- _chartDetailsModal.gsp -->

