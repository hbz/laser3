<%@ page import="de.laser.exporting.AbstractExport;de.laser.exporting.GenericExportManager" %>
<laser:serviceInjection />
<!-- _chartDetails.gsp -->
<g:set var="export" value="${GenericExportManager.getCurrentExport( query )}" />

<g:if test="${export}">
    <g:set var="formFields" value="${export.getAllFields()}" />

    <semui:modal id="${modalID}" text="CSV-Export (Basisvariante)" hideSubmitButton="true">

        <p><span class="ui label red">DEMO : in Entwicklung</span></p>

        <div class="ui info message">
            <p>${title.join(' > ')}</p>
        </div>

        <g:form controller="ajaxHtml" action="chartDetailsExport" method="POST" target="_blank">
            <div class="ui form">
                <div class="ui grid">
                    <div class="eight wide column">
                        <div class="ui field">
                            BASIC
                        </div>
                        <g:each in="${formFields}" var="field">
                            <div class="ui field">
                                <div class="ui checkbox">
                                    <g:if test="${field.key == 'globalUID'}">
                                        <input type="checkbox" name="cde:${field.key}" id="cde:${field.key}">
                                    </g:if>
                                    <g:else>
                                        <input type="checkbox" name="cde:${field.key}" id="cde:${field.key}" checked="checked">
                                    </g:else>
                                    <label for="cde:${field.key}">${export.getFieldLabel(field.key as String)}</label>
                                </div>
                            </div>
                        </g:each>
                    </div>

                    <div class="eight wide column">
                        <div class="ui field">
                            QUERY SPEZ.
                        </div>
                        <div class="ui field">
                            <button class="ui button" id="export-chart-details-as-csv">Als CSV-Datei exportieren</button>
                        </div>
                    </div>
                </div><!-- .grid -->
            </div><!-- .form -->

            <input type="hidden" name="query" value="${query}" />
            <input type="hidden" name="filename" value="${title.join('_').replaceAll(' ', '_')}" />
            <input type="hidden" name="idList_cs" value="${objectList.collect{it.id}.join(',')}" />
        </g:form>

        <laser:script file="${this.getGroovyPageFileName()}">
            $('#export-chart-details-as-csv').on( 'click', function() {
                $('#${modalID} form').submit()

    /*
        var data = $('#${modalID} form').serialize()

        $.ajax({
            url: "<g:createLink controller="ajaxHtml" action="chartDetailsExport" />",
            method: 'post',
            data: data
        }).done( function (data) {
            console.log(data);
        }).fail( function (data) {
            console.log('failed ..');
            console.log(data);
        })
    */
            })
        </laser:script>

    </semui:modal>
</g:if>
<!-- _chartDetails.gsp -->

