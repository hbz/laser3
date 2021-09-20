<%@ page import="de.laser.reporting.export.myInstitution.ExportGlobalHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<semui:infoModal id="${modalID}">

    %{-- subscription --}%

    <div class="help-section" data-help-section="subscription-x-identifier">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über für Lizenzen vergebene Identifikatoren
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
            Diese Abfrage liefert eine Übersicht über für Lizenzen vergebene Merkmale
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale - also <strong>allgemeine oder eigene Merkmale</strong>, die Lizenzen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Lizenzen mit Merkmal X, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Merkmale X für die betrachteten Lizenzen <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-annualX"></div>
    <div class="help-section" data-help-section="subscription-x-memberAnnualX"></div>
    <div class="help-section" data-help-section="subscription-x-providerX"></div>
    <div class="help-section" data-help-section="subscription-x-memberProviderX"></div>
    <div class="help-section" data-help-section="subscription-x-platformX"></div>
    <div class="help-section" data-help-section="subscription-x-memberSubscriptionX"></div>
    <div class="help-section" data-help-section="subscription-x-memberX"></div>

    %{-- license --}%

    <div class="help-section" data-help-section="license-x-identifier">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über für Verträge vergebene Identifikatoren
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
            Diese Abfrage liefert eine Übersicht über für Verträge vergebene Merkmale
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale - also <strong>allgemeine oder eigene Merkmale</strong>, die Verträgen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Verträge.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Verträge mit Merkmal X, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Merkmale X für die betrachteten Verträge <br />
        </p>
    </div>

    %{-- org --}%

    <div class="help-section" data-help-section="org-x-identifier">
        <p class="ui header">
            Diese Abfrage liefert eine Übersicht über für Organisationen vergebene Identifikatoren
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
            Diese Abfrage liefert eine Übersicht über für Organisationen vergebene Merkmale
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale - also <strong>allgemeine oder eigene Merkmale</strong>, die Organisationen konkret vergeben wurden.
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
