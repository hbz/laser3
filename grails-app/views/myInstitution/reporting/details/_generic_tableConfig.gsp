<%@ page import="de.laser.reporting.export.base.BaseDetailsExport; de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.reporting.report.GenericHelper; de.laser.reporting.report.myInstitution.config.PackageXCfg; de.laser.reporting.export.myInstitution.PackageExport; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.export.GlobalExportHelper; grails.plugin.springsecurity.SpringSecurityUtils" %>

<div class="ui segment hidden" id="reporting-dtc-wrapper">

    <span class="ui top attached label" style="border-radius: 0; text-align: center">
        <i class="ui icon th"></i> ${message(code:'reporting.details.dtc.label')}
    </span>

    <div class="ui form" style="margin-top: 3em !important;">
        <div class="fields three">
            <%
                String key = GlobalExportHelper.getCachedExportStrategy(token)

                Map<String, Map> esdConfig = BaseConfig.getCurrentConfigElasticsearchData( key )
                Map<String, Map> dtConfig = BaseConfig.getCurrentConfigDetailsTable( key ).clone()

                if (query != 'platform-x-property') { dtConfig.remove('_?_propertyLocal') }

                String wekbProperty
                if (query == 'platform-x-propertyWekb') {
                    if (params.id != null && params.id != 0) {
                        wekbProperty = GlobalExportHelper.getQueryCache(token).dataDetails.find { it.id == params.long('id') }.esProperty
                        if (wekbProperty && dtConfig[wekbProperty]) {
                            dtConfig[wekbProperty].dtc = true
                        }
                    }
                }
                dtConfig.remove(null) // ?????

                BaseDetails.reorderFieldsInColumnsForUI( dtConfig, 3 ).each { col ->
                    println '<div class="field grouped fields">'
                    col.each { k, b ->
                        String label = BaseDetails.getFieldLabelforColumn( key, k )

                        if (esdConfig.containsKey(k)) {
                            label = label + ' (we:kb)'
                        }
                        else if (k == '_?_propertyLocal') {
                            label = BaseDetailsExport.getExportLabel('x-property') +  ': ' + GlobalExportHelper.getQueryCache( token ).labels.labels[2]
                        }

                        println '<div class="field"><div class="ui checkbox">'
                        println '<input type="checkbox" name="dtc:' + k + '" id="dtc:' + k + '"' + ( b.dtc ? ' checked="checked" ': ' ' ) + '/>'
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
