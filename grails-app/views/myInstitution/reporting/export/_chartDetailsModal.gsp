<%@ page import="de.laser.reporting.export.ExportHelper; de.laser.reporting.export.GenericExportManager" %>
<laser:serviceInjection />
<!-- _chartDetailsModal.gsp -->
<g:set var="export" value="${GenericExportManager.getCurrentExport( query )}" />

<g:if test="${export}">
    <g:set var="formFields" value="${export.getAllFields()}" />
    <g:set var="filterLabels" value="${ExportHelper.getFilterLabels(params)}" />
    <g:set var="queryLabels" value="${ExportHelper.getQueryLabels(params)}" />

    <semui:modal id="${modalID}" text="CSV-Export" hideSubmitButton="true">

        <p><span class="ui label red">DEMO : (Basisvariante) in Entwicklung</span></p>

        <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: filterLabels, tmplSize: 'tiny']}" />

        <g:render template="/myInstitution/reporting/details/generic_queryLabels" model="${[queryLabels: queryLabels, tmplSize: 'tiny']}" />

        <g:form controller="ajaxHtml" action="chartDetailsExport" method="POST" target="_blank">
            <div class="ui form">
                <div class="ui grid">
                    <div class="eight wide column">
                        <div class="ui field">
                            <label>Exportfelder</label>
                            <g:each in="${formFields}" var="field">
                                <div class="ui checkbox">
                                    <g:if test="${field.key == 'globalUID'}">
                                        <input type="checkbox" name="cde:${field.key}" id="cde:${field.key}">
                                    </g:if>
                                    <g:else>
                                        <input type="checkbox" name="cde:${field.key}" id="cde:${field.key}" checked="checked">
                                    </g:else>
                                    <label for="cde:${field.key}">${export.getFieldLabel(field.key as String)}</label>
                                </div>
                                <br />
                            </g:each>
                        </div>
                    </div>

                    <div class="eight wide column">
                        <div class="ui field">
                            <label for="filename">Dateiname</label>
                            <input name="filename" id="filename" value="${ExportHelper.getFileName(queryLabels)}" />
                        </div>
                        <div class="ui field">
                            <label>&nbsp;</label>
                            <button class="ui button" id="export-chart-details-as-csv">Als CSV-Datei exportieren</button>
                        </div>
                    </div>
                </div><!-- .grid -->
            </div><!-- .form -->

            <input type="hidden" name="token" value="${token}" />
            <input type="hidden" name="query" value="${query}" />
            <input type="hidden" name="idList_cs" value="${objectList.collect{it.id}.join(',')}" />
        </g:form>

    </semui:modal>
</g:if>
<!-- _chartDetailsModal.gsp -->

