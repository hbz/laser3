<%@ page import="de.laser.reporting.export.local.ExportLocalHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<semui:modal id="${modalID}" text="?" hideSubmitButton="true">

    <div class="help-section" data-help-section="timeline-member">
        <p>
            Diese Abfrage visualisiert die zeitliche Teilnehmerentwicklung dieser Lizenz.
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit jeweiligen Datumsgrenzen.<br />
            Für jede Lizenz sind die Veränderungen bezogen auf den Vorgänger erkennbar:
        </p>
        <p>
            Teilnehmer hinzugefügt: <span class="ui green circular label">grün</span>, Teilnehmer entfernt: <span class="ui red circular label">rot</span>
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-costX">B</div>
    <div class="help-section" data-help-section="timeline-entitlement">
        <p>
            Diese Abfrage visualisiert die zeitliche Bestandsentwicklung dieser Lizenz.
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit jeweiligen Datumsgrenzen.<br />
            Für jede Lizenz sind die Veränderungen bezogen auf den Vorgänger erkennbar:
        </p>
        <p>
            Titel hinzugefügt: <span class="ui green circular label">grün</span>, Titel entfernt: <span class="ui red circular label">rot</span>
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-annualMember-subscription">
        <p>
            Diese Abfrage visualisiert die zeitliche Entwicklung aller von dieser Lizenz referenzierten Teilnehmerlizenzen.
        </p>
        <p>
            Gelistet werden die Teilnehmerlizenzen in Jahresringen - abhängig von den Datumsgrenzen der jeweiligen Teilnehmerlizenz.
        </p>
        <p>
            <strong>
                Datumsangaben dieser Lizenz werden bei der Abfrage nicht beachtet; <br />
                über ggf. vorhandene Vorgänger oder Nachfolger referenzierte Teilnehmerlizenzen dagegen schon.
            </strong>
        </p>
    </div>

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

