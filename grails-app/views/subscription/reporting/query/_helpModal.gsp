<%@ page import="de.laser.reporting.export.LocalExportHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<semui:infoModal id="${modalID}">

    <div class="help-section" data-help-section="timeline-member">
        <p class="ui header">
            Die Abfrage visualisiert die zeitliche Entwicklung der Teilnehmer für diese Lizenz
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit ihren jeweiligen Datumsgrenzen.
            Für jede Lizenz sind mögliche Veränderungen, bezogen auf den Vorgänger, wie folgt erkennbar:
        </p>
        <p>
            <i class="icon circle green"></i> Teilnehmer hinzugefügt, <br />
            <i class="icon circle red"></i> Teilnehmer entfernt, <br />
            <i class="icon circle blue"></i> Aktuelle Teilnehmer
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-cost">
        <p class="ui header">
            Die Abfrage visualisiert die zeitliche Entwicklung der Teilnehmerkosten für diese Lizenz
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit ihren jeweiligen Datumsgrenzen.
            Über betroffene Teilnehmerlizenzen und existierende Kostenposten werden folgende Informationen berechnet:
        </p>
        <p>
            <i class="icon circle blue"></i> Kumulierter Endpreis in Euro (nach Steuern), <br />
            <i class="icon circle green"></i> Kumulierter Wert in Euro, <br />
            <i class="icon circle orange"></i> Neutrale Kosten in Euro (nach Steuern), <br />
            <i class="icon circle yellow"></i> Neutrale Kosten in Euro
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-entitlement">
        <p class="ui header">
            Die Abfrage visualisiert die zeitliche Entwicklung des Bestands für diese Lizenz
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen (referenzierte Vorgänger, bzw. Nachfolger) mit ihren jeweiligen Datumsgrenzen.
            Für jede Lizenz sind mögliche Veränderungen, bezogen auf den Vorgänger, wie folgt erkennbar:
        </p>
        <p>
            <i class="icon circle green"></i> Titel hinzugefügt, <br />
            <i class="icon circle red"></i> Titel entfernt, <br />
            <i class="icon circle blue"></i> Aktueller Bestand
        </p>
    </div>
    <div class="help-section" data-help-section="timeline-annualMember-subscription">
        <p class="ui header">
            Die Abfrage visualisiert die zeitliche Entwicklung aller für diese Lizenz relevanten Teilnehmerlizenzen
        </p>
        <p>
            Gruppiert werden die Teilnehmerlizenzen in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
        </p>
        <p>
            Start- und Enddatum <strong>in dieser Lizenz</strong> werden bei der Abfrage nicht beachtet; <br />
            über ggf. vorhandene Vorgänger oder Nachfolger referenzierte Teilnehmerlizenzen dagegen schon.
        </p>
    </div>

    <div class="help-section" data-help-section="default">
        ${message(code:'reporting.ui.global.help.missing')}
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
            $current = $('#${modalID} .help-section[data-help-section=default]');
        }
        $current.show();
    }
</laser:script>
<!-- _helpModal.gsp -->

