<%@ page import="de.laser.reporting.export.local.ExportLocalHelper; de.laser.reporting.myInstitution.base.BaseConfig; de.laser.reporting.export.base.BaseExport; de.laser.reporting.export.myInstitution.ExportGlobalHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<semui:modal id="${modalID}" text="?" hideSubmitButton="true">

    <div class="help-section" data-help-section="timeline-memberX">A</div>
    <div class="help-section" data-help-section="timeline-costX">B</div>
    <div class="help-section" data-help-section="timeline-entitlementX">C</div>
    <div class="help-section" data-help-section="timeline-annualMember-subscriptionX">D</div>

    <div class="help-section" data-help-section="default">
        <i class="icon huge la-light-grey meh outline"></i>
        Leider ist der Hilfetext zu diesem Vorgang noch in Arbeit.
    </div>
</semui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.callbacks.modal.show['${modalID}'] = function() {
        $('#${modalID} .help-section').hide();
        $current = $('#${modalID} .help-section[data-help-section=' + JSPC.app.reporting.current.request.query + ']');
        if (! $current.length) {
            $current = $('#${modalID} .help-section[data-help-section=default]')
        }
        $current.show();
    }
</laser:script>
<!-- _helpModal.gsp -->

