<%@ page import="de.laser.reporting.export.myInstitution.GlobalExportHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<semui:infoModal id="${modalID}">

    %{-- subscription --}%

    <div class="help-section" data-help-section="subscription-x-identifier">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über vergebene Lizenz-Identifikatoren
        </p>
        <p>
            Gelistet werden alle relevanten Namensräume - also Namensräume von Identifikatoren, die Lizenzen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Lizenzen mit Identifikatoren aus dem jeweiligen Namensraum, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Identifikatoren aus dem jeweiligen Namensraum <br />
        </p>
        <p>
            Lizenzen ohne Identifikatoren werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Identifikator</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-property">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über vergebene Lizenz-Merkmale
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale - also <strong>eigene oder allgemeine Merkmale</strong>, die für Lizenzen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Lizenzen mit Merkmal X, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Merkmale X für die betrachteten Lizenzen <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberSubscriptionProperty">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über vergebene Teilnehmerlizenz-Merkmale
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale - also <strong>eigene oder allgemeine Merkmale</strong>, die für Teilnehmerlizenzen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Teilnehmerlizenzen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Teilnehmerlizenzen mit Merkmal X, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Merkmale X für die betrachteten Teilnehmerlizenzen <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-annual">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über die Laufzeit von Lizenzen
        </p>
        <p>
            Gruppiert werden die Lizenzen in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
            Bedingen vorhandene Daten eine Laufzeit mehrerer Jahre, wird die Lizenz auch mehreren Jahresringen zugeordnet.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen.
        </p>
        <p>
            Lizenzen ohne Enddatum werden <strong>zusätzlich</strong> in der Gruppe <i class="icon circle teal"></i><strong>* ohne Ablauf</strong> gelistet. <br />
            Lizenzen ohne Startdatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Startdatum</strong> gelistet. <br />
            Lizenzen ohne Angabe von Start- und Enddatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Angabe</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberAnnual">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über die Laufzeit von Teilnehmerlizenzen
        </p>
        <p>
            Gruppiert werden die Teilnehmerlizenzen in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
            Bedingen vorhandene Daten eine Laufzeit mehrerer Jahre, wird die Teilnehmerlizenzen auch mehreren Jahresringen zugeordnet.
            Die Basissuche bestimmt dabei die Menge der betrachteten Teilnehmerlizenzen.
        </p>
        <p>
            Teilnehmerlizenzen ohne Enddatum werden <strong>zusätzlich</strong> in der Gruppe <i class="icon circle teal"></i><strong>* ohne Ablauf</strong> gelistet. <br />
            Teilnehmerlizenzen ohne Startdatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Startdatum</strong> gelistet. <br />
            Teilnehmerlizenzen ohne Angabe von Start- und Enddatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Angabe</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-provider">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über zugeordnete Lizenz-Anbieter
        </p>
        <p>
            Gelistet werden alle relevanten Anbieter - also Anbieter, die Lizenzen konkret zugeordnet wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen und Anbieter.
        </p>
        <p>
            Lizenzen ohne ausgewiesenen Anbieter werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Anbieter</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberProvider">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über zugeordnete Teilnehmerlizenz-Anbieter
        </p>
        <p>
            Gelistet werden alle relevanten Anbieter - also Anbieter, die Teilnehmerlizenzen konkret zugeordnet wurden.
            Genauer muss ein solcher Anbieter gleichzeitig <strong>einer Lizenz sowie der zugehörigen Teilnehmerlizenz</strong> zugeordnet sein.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen, Teilnehmerlizenzen und Anbieter.
        </p>
        <p>
            Teilnehmerlizenzen ohne ausgewiesenen Anbieter oder ohne passende Übereinstimmung werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Übereinstimmung</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-platform">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über für Lizenzen relevante Plattformen
        </p>
        <p>
            Gelistet werden alle relevanten Plattformen - also Plattformen, die Lizenzen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen und Anbieter.
        </p>
        <p>
            Dabei sind folgende Varianten möglich: <br />
            <i class="icon circle blue"></i> Die Plattform kann direkt über eine Referenz aus dem Lizenz-Bestand ermittelt werden, <br />
            <i class="icon circle green"></i> Der einer Lizenz zugeordnete Anbieter verweist auf eine Plattform <br />
        </p>
        <p>
            Lizenzen ohne ermittelbare Plattform werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Plattform</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberSubscription">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über die Teilnehmerlizenzen von Lizenzen
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen - also Lizenzen, denen entsprechende Teilnehmerlizenzen zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen und Teilnehmerlizenzen.
        </p>
        <p>
            Ohne übereinstimmende Zuordnung sind ggf. vorhandene Lizenzen <strong>nicht</strong> im Ergebnis sichtbar.
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-member">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über die Teilnehmer von Lizenzen
        </p>
        <p>
            Gelistet werden alle relevanten Lizenzen - also Lizenzen, denen entsprechende Teilnehmerlizenzen mit konkreten Organisationen als Teilnehmer zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen, Teilnehmerlizenzen und Organisationen.
        </p>
        <p>
            Ohne übereinstimmende Zuordnung sind ggf. vorhandene Lizenzen <strong>nicht</strong> im Ergebnis sichtbar.
        </p>
    </div>

    %{-- license --}%

    <div class="help-section" data-help-section="license-x-identifier">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über vergebene Vertrags-Identifikatoren
        </p>
        <p>
            Gelistet werden alle relevanten Namensräume - also Namensräume von Identifikatoren, die Verträgen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Verträge.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Verträge mit Identifikatoren aus dem jeweiligen Namensraum, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Identifikatoren aus dem jeweiligen Namensraum <br />
        </p>
        <p>
            Verträge ohne Identifikatoren werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Identifikator</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="license-x-property">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über vergebene Vertrags-Merkmale
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale - also <strong>eigene oder allgemeine Merkmale</strong>, die für Verträge konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Verträge.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Verträge mit Merkmal X, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Merkmale X für die betrachteten Verträge <br />
        </p>
    </div>

    <div class="help-section" data-help-section="license-x-annual">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über die Laufzeit von Verträgen
        </p>
        <p>
            Gruppiert werden die Verträge in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
            Bedingen vorhandene Daten eine Laufzeit mehrerer Jahre, wird der Vertrag auch mehreren Jahresringen zugeordnet.
            Die Basissuche bestimmt dabei die Menge der betrachteten Verträge.
        </p>
        <p>
            Verträge ohne Enddatum werden <strong>zusätzlich</strong> in der Gruppe <i class="icon circle teal"></i><strong>* ohne Ablauf</strong> gelistet. <br />
            Verträge ohne Startdatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Startdatum</strong> gelistet. <br />
            Verträge ohne Angabe von Start- und Enddatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Angabe</strong> gelistet. <br />
        </p>
    </div>

    %{-- org --}%

    <div class="help-section" data-help-section="org-x-identifier">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über vergebene Organisations-Identifikatoren
        </p>
        <p>
            Gelistet werden alle relevanten Namensräume - also Namensräume von Identifikatoren, die Organisationen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Organisationen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Organisationen mit Identifikatoren aus dem jeweiligen Namensraum, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Identifikatoren aus dem jeweiligen Namensraum <br />
        </p>
        <p>
            Organisationen ohne Identifikatoren werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Identifikator</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="org-x-property">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über vergebene Organisations-Merkmale
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale - also <strong>eigene oder allgemeine Merkmale</strong>, die für Organisationen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Organisationen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Organisationen mit Merkmal X, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Merkmale X für die betrachteten Organisationen <br />
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
