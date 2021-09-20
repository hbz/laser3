<%@ page import="de.laser.reporting.export.local.ExportLocalHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<semui:infoModal id="${modalID}">

    <div class="help-section" data-help-section="timeline-member">
        <p class="ui header">
            Die Abfrage visualisiert die zeitliche Entwicklung der Teilnehmer für diese Lizenz
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit jeweiligen Datumsgrenzen.
            Für jede Lizenz sind mögliche Veränderungen, bezogen auf den Vorgänger, wie folgt erkennbar:
        </p>
        <p>
            <i class="icon circle green"></i> Teilnehmer hinzugefügt, <br />
            <i class="icon circle red"></i> Teilnehmer entfernt
        </p>
        <p>Aktuelle Teilnehmer sind unter <i class="icon circle blue"></i> zu finden.</p>
    </div>
    <div class="help-section" data-help-section="timeline-costX">B</div>
    <div class="help-section" data-help-section="timeline-entitlement">
        <p class="ui header">
            Die Abfrage visualisiert die zeitliche Entwicklung des Bestands für diese Lizenz
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit jeweiligen Datumsgrenzen.
            Für jede Lizenz sind mögliche Veränderungen, bezogen auf den Vorgänger, wie folgt erkennbar:
        </p>
        <p>
            <i class="icon circle green"></i> Titel hinzugefügt, <br />
            <i class="icon circle red"></i>Titel entfernt
        </p>
        <p>Der aktuelle Bestand ist unter <i class="icon circle blue"></i> zu finden.</p>
    </div>
    <div class="help-section" data-help-section="timeline-annualMember-subscription">
        <p class="ui header">
            Die Abfrage visualisiert die zeitliche Entwicklung aller für diese Lizenz relevanten Teilnehmerlizenzen
        </p>
        <p>
            Gruppiert werden die Teilnehmerlizenzen in Jahresringen - abhängig von den Datumsgrenzen der jeweiligen Teilnehmerlizenzen.
        </p>
        <p>
            Datumsangaben <strong>dieser Lizenz</strong> werden bei der Abfrage nicht beachtet; <br />
            über ggf. vorhandene Vorgänger oder Nachfolger referenzierte Teilnehmerlizenzen dagegen schon.
        </p>
    </div>

    <div class="help-section" data-help-section="default">
        ${message(code:'reporting.help.infoMissing')}
    </div>
</semui:infoModal>

<style>
#queryHelpModal .items .item {
    padding: 1em;
}
</style>

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

