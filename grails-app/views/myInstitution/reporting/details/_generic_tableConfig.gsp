<%@ page import="de.laser.reporting.export.base.BaseDetailsExport; de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.reporting.report.GenericHelper; de.laser.reporting.report.myInstitution.config.PackageXCfg; de.laser.reporting.export.myInstitution.PackageExport; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.export.GlobalExportHelper; grails.plugin.springsecurity.SpringSecurityUtils" %>

<div class="ui segment hidden" id="reporting-dtc-wrapper">

    <span class="ui top attached label" style="border-radius: 0; text-align: center">
        <i class="ui icon th"></i> ${message(code:'reporting.details.dtc.label')}
    </span>

    <div class="ui form" style="margin-top: 3em !important;">
        <div class="fields three">
            <%
                String key = GlobalExportHelper.getCachedExportStrategy(token)

                Map<String, Object> esData = BaseConfig.getCurrentEsData( key )
                Map<String, Object> dtCfg = BaseConfig.getCurrentDetailsTableConfig( key ).clone()

                if (query != 'platform-x-property') { dtCfg.remove('_?_propertyLocal') }

                String wekbProperty
                if (query == 'platform-x-propertyWekb') {
                    if (params.id != null && params.id != 0) {
                        wekbProperty = GlobalExportHelper.getQueryCache(token).dataDetails.find { it.id == params.long('id') }.esProperty
                        dtCfg[wekbProperty] = true
                    }
                }
                dtCfg.remove(null) // ?????

                BaseDetails.reorderFieldsInColumnsForUI( dtCfg, 3 ).each { col ->
                    println '<div class="field grouped fields">'
                    col.each { k, b ->
                        String label = BaseDetails.getFieldLabelforColumns( key, k )
                        if (esData.containsKey(k)) {
                            label = label + ' (we:kb)'
                        }
                        else if (k == '_?_propertyLocal') {
                            label = BaseDetailsExport.getMessage('x-property') +  ': ' + GlobalExportHelper.getQueryCache( token ).labels.labels[2]
                        }

                        println '<div class="field"><div class="ui checkbox">'
                        println '<input type="checkbox" name="dtc:' + k + '" id="dtc:' + k + '"' + ( b ? ' checked="checked" ': ' ' ) + '/>'
                        println '<label for="dtc:' + k + '">' + label + '</label>'
                        println '</div></div>'
                    }
                    println '</div>'
                }
            %>
        </div>
    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#reporting-dtc-wrapper input[type=checkbox]').on('change', function() {
        var ref = $(this).attr('id')
        if ($(this).parent('div.checkbox').hasClass('checked')) {
            $('#reporting-detailsTable *[data-column=\'' + ref + '\']').removeClass('hidden')
        } else {
            $('#reporting-detailsTable *[data-column=\'' + ref + '\']').addClass('hidden')
        }
    })

    $('#details-dtc-button').on('click', function(){ $('#reporting-dtc-wrapper').toggleClass('hidden') })
</laser:script>
