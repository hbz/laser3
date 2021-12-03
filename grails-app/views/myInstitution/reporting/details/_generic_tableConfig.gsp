<%@ page import="de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.reporting.report.GenericHelper; de.laser.reporting.report.myInstitution.config.PackageXCfg; de.laser.reporting.export.myInstitution.PackageExport; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.export.GlobalExportHelper; grails.plugin.springsecurity.SpringSecurityUtils" %>

<div class="ui segment hidden" id="reporting-dtc-wrapper">
    <div class="ui form">
        <div class="fields three">
            <%
                String key = GlobalExportHelper.getCachedExportStrategy(token)

                BaseDetails.reorderFieldsInColumnsForUI( BaseConfig.getCurrentDetailsTableConfig( key ), 3 ).each { col ->
                    println '<div class="field grouped fields">'
                    col.each { k, b ->

                        String label = BaseDetails.getFieldLabelforColumns( key, k )

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
