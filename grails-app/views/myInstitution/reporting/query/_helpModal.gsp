<%@ page import="de.laser.reporting.export.myInstitution.ExportGlobalHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<semui:modal id="${modalID}" text="?" hideSubmitButton="true">

    <div class="help-section" data-help-section="subscription-x-identifierX">A</div>
    <div class="help-section" data-help-section="subscription-x-propertyX">B</div>
    <div class="help-section" data-help-section="subscription-x-annualX">C</div>

    <div class="help-section" data-help-section="default">
        <i class="icon huge la-light-grey meh outline"></i>
        ${message(code:'reporting.help.infoMissing')}
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

