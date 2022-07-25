<%@ page import="de.laser.reporting.report.myInstitution.config.PlatformXCfg; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.export.GlobalExportHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<ui:infoModal id="${modalID}">

    %{-- subscription --}%

    <div class="help-section" data-help-section="subscription-x-identifier">
        <p class="ui header">
            Identifikatoren von Lizenzen
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
            Merkmale von Lizenzen
        </p>
        <p>
            Gelistet werden alle relevanten (also <strong>private oder öffentliche</strong>) Merkmale, die für Lizenzen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Lizenzen mit Merkmal X, <br />
            <i class="icon circle green"></i> Öffentlich vergebene Merkmale X für die betrachteten Lizenzen <br />
            <i class="icon circle yellow"></i> Private Merkmale X für die betrachteten Lizenzen <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberSubscriptionProperty">
        <p class="ui header">
            Merkmale von Teilnehmerlizenzen
        </p>
        <p>
            Gelistet werden alle relevanten (also <strong>private oder öffentliche</strong>) Merkmale, die für Teilnehmerlizenzen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Teilnehmerlizenzen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Teilnehmerlizenzen mit Merkmal X, <br />
            <i class="icon circle green"></i> Öffentlich vergebene Merkmale X für die betrachteten Teilnehmerlizenzen <br />
            <i class="icon circle yellow"></i> Private Merkmale X für die betrachteten Teilnehmerlizenzen <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-annual">
        <p class="ui header">
            Laufzeit von Lizenzen
        </p>
        <p>
            Gruppiert werden die Lizenzen in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
            Bedingen vorhandene Daten eine Laufzeit mehrerer Jahre, wird die Lizenz auch mehreren Jahresringen zugeordnet.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen.
        </p>
        <p>
            Lizenzen ohne Enddatum werden <strong>zusätzlich</strong> in der Gruppe <i class="icon circle teal"></i><strong>* ohne Ablauf</strong> gelistet. <br />
            Lizenzen ohne Startdatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Startdatum</strong> gelistet. <br />
            Lizenzen ohne Angabe von Start- und Enddatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberAnnual">
        <p class="ui header">
            Laufzeit von Teilnehmerlizenzen
        </p>
        <p>
            Gruppiert werden die Teilnehmerlizenzen in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
            Bedingen vorhandene Daten eine Laufzeit mehrerer Jahre, wird die Teilnehmerlizenzen auch mehreren Jahresringen zugeordnet.
            Die Basissuche bestimmt dabei die Menge der betrachteten Teilnehmerlizenzen.
        </p>
        <p>
            Teilnehmerlizenzen ohne Enddatum werden <strong>zusätzlich</strong> in der Gruppe <i class="icon circle teal"></i><strong>* ohne Ablauf</strong> gelistet. <br />
            Teilnehmerlizenzen ohne Startdatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Startdatum</strong> gelistet. <br />
            Teilnehmerlizenzen ohne Angabe von Start- und Enddatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-provider">
        <p class="ui header">
            Anbieter von Lizenzen
        </p>
        <p>
            Gelistet werden alle relevanten Anbieter - also Anbieter, die Lizenzen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen und Anbieter.
        </p>
        <p>
            Lizenzen ohne ausgewiesenen Anbieter werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Anbieter</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberProvider">
        <p class="ui header">
            Anbieter von Teilnehmerlizenzen
        </p>
        <p>
            Gelistet werden alle relevanten Anbieter - also Anbieter, die Teilnehmerlizenzen konkret zugeordnet werden können.
            Genauer muss ein solcher Anbieter gleichzeitig <strong>einer Lizenz sowie der zugehörigen Teilnehmerlizenz</strong> zugeordnet sein.
            Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen, Teilnehmerlizenzen und Anbieter.
        </p>
        <p>
            Teilnehmerlizenzen ohne ausgewiesenen Anbieter oder ohne passende Übereinstimmung werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Übereinstimmung</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="subscription-x-platform">
        <p class="ui header">
            Plattformen von Lizenzen
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
            Teilnehmerlizenzen von Lizenzen
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
            Teilnehmer von Lizenzen
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
            Identifikatoren von Verträgen
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
            Merkmale von Verträgen
        </p>
        <p>
            Gelistet werden alle relevanten (also <strong>private oder öffentliche</strong>) Merkmale, die für Verträge konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Verträge.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Verträge mit Merkmal X, <br />
            <i class="icon circle green"></i> Öffentlich vergebene Merkmale X für die betrachteten Verträge <br />
            <i class="icon circle yellow"></i> Private Merkmale X für die betrachteten Verträge <br />
        </p>
    </div>

    <div class="help-section" data-help-section="license-x-annual">
        <p class="ui header">
            Laufzeit von Verträgen
        </p>
        <p>
            Gruppiert werden die Verträge in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
            Bedingen vorhandene Daten eine Laufzeit mehrerer Jahre, wird der Vertrag auch mehreren Jahresringen zugeordnet.
            Die Basissuche bestimmt dabei die Menge der betrachteten Verträge.
        </p>
        <p>
            Verträge ohne Enddatum werden <strong>zusätzlich</strong> in der Gruppe <i class="icon circle teal"></i><strong>* ohne Ablauf</strong> gelistet. <br />
            Verträge ohne Startdatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* ohne Startdatum</strong> gelistet. <br />
            Verträge ohne Angabe von Start- und Enddatum werden <strong>exklusive</strong> in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> gelistet. <br />
        </p>
    </div>

    %{-- org --}%

    <div class="help-section" data-help-section="org-x-identifier">
        <p class="ui header">
            Identifikatoren von Organisationen
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
            Merkmale von Organisationen
        </p>
        <p>
            Gelistet werden alle relevanten (also <strong>private oder öffentliche</strong>) Merkmale, die für Organisationen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Organisationen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Organisationen mit Merkmal X, <br />
            <i class="icon circle green"></i> Öffentlich vergebene Merkmale X für die betrachteten Organisationen <br />
            <i class="icon circle yellow"></i> Private Merkmale X für die betrachteten Organisationen <br />
        </p>
    </div>

    %{-- package --}%

    <div class="help-section" data-help-section="package-x-id">
        <p class="ui header">
            Identifikatoren von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Namensräume - also Namensräume von Identifikatoren, die Paketen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Hierzu werden Paketinformationen in <strong>LAS:eR</strong> mit referenzierten Objekten aus der <strong>we:kb</strong> verglichen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Pakete mit Identifikatoren aus dem jeweiligen Namensraum, <br />
            <i class="icon circle green"></i> Insgesamt vergebene Identifikatoren aus dem jeweiligen Namensraum <br />
        </p>
        <p>
            Pakete ohne Identifikatoren werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Identifikator</strong> zusammmen gefasst. <br />
            Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="package-x-provider">
        <p class="ui header">
            Anbieter von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Anbieter - also Anbieter, die Paketen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Pakete ohne ausgewiesenen Anbieter werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Anbieter</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="package-x-platform">
        <p class="ui header">
            Plattformen von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Plattformen - also Plattformen, die Paketen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Pakete ohne ausgewiesene Plattform werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Plattform</strong> zusammmen gefasst.
        </p>
    </div>

    <div class="help-section" data-help-section="package-x-language">
        <p class="ui header">
            Sprachen von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Sprachen - also Sprachen, die Paketen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Pakete ohne ausgewiesene Sprachen werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="package-x-curatoryGroup">
        <p class="ui header">
            Kuratorengruppen von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Kuratorengruppen - also Gruppen, die Paketen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Hierzu werden Paketinformationen in <strong>LAS:eR</strong> mit referenzierten Objekten aus der <strong>we:kb</strong> verglichen.
        </p>
        <p>
            Pakete ohne ausgewiesene Kuratorengruppen werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
            Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="package-x-nationalRange">
        <p class="ui header">
            Länder von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Länder - also Länder, die Paketen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Hierzu werden Paketinformationen in <strong>LAS:eR</strong> mit referenzierten Objekten aus der <strong>we:kb</strong> verglichen.
        </p>
        <p>
            Pakete ohne ausgewiesene Länder werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
            Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="package-x-regionalRange">
        <p class="ui header">
            Regionen von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Regionen - also Regionen, die Paketen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Hierzu werden Paketinformationen in <strong>LAS:eR</strong> mit referenzierten Objekten aus der <strong>we:kb</strong> verglichen.
        </p>
        <p>
            Pakete ohne ausgewiesene Regionen werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
            Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
        </p>
    </div>
        
    <div class="help-section" data-help-section="package-x-ddc">
        <p class="ui header">
            Dewey-Dezimalklassifikation von Paketen
        </p>
        <p>
            Gelistet werden alle relevanten Dewey-Dezimalklassifikationen - also Klassifikationen, die Paketen konkret zugeordnet werden können.
            Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
        </p>
        <p>
            Hierzu werden Paketinformationen in <strong>LAS:eR</strong> mit referenzierten Objekten aus der <strong>we:kb</strong> verglichen.
        </p>
        <p>
            Pakete ohne ausgewiesene Dewey-Dezimalklassifikation werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
            Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
        </p>
    </div>

    %{-- platform --}%

    <div class="help-section" data-help-section="platform-x-property">
        <p class="ui header">
            Merkmale von Plattformen
        </p>
        <p>
            Gelistet werden alle relevanten (also <strong>private oder öffentliche</strong>) Merkmale, die für Plattformen konkret vergeben wurden.
            Die Basissuche bestimmt dabei die Menge der betrachteten Plattformen.
        </p>
        <p>
            Im Detail sind folgende Informationen verfügbar: <br/>
            <i class="icon circle blue"></i> Plattform mit Merkmal X, <br />
            <i class="icon circle green"></i> Öffentlich vergebene Merkmale X für die betrachteten Plattformen <br />
            <i class="icon circle yellow"></i> Private Merkmale X für die betrachteten Plattformen <br />
        </p>
    </div>

    <div class="help-section" data-help-section="platform-x-propertyWekb">
        <p class="ui header">
            Merkmale von Plattformen
        </p>
        <p>
            Gelistet werden alle relevanten Merkmale (aus einer fest definierten Liste), die für Plattformen konkret vergeben wurden.
        Die Basissuche bestimmt dabei die Menge der betrachteten Plattformen.
        </p>
        <p>
            <g:set var="esProperties" value="${PlatformXCfg.CONFIG.base.distribution.default.getAt('platform-x-propertyWekb').esProperties}" />
            <g:set var="esdConfig" value="${BaseConfig.getCurrentConfigElasticsearchData(BaseConfig.KEY_PLATFORM)}" />
            <ol class="ui list">
                <g:each in="${esProperties}" var="prop">
                    <li value="*"><g:message code="${esdConfig.get(prop).label}" /></li>
                </g:each>
            </ol>
        </p>
        <p>
            Pakete ohne entsprechende Merkmale werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
            Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
        </p>
    </div>

    <div class="help-section" data-help-section="default">
        ${message(code:'reporting.ui.global.help.missing')}
    </div>
</ui:infoModal>

<style>
    #queryHelpModal .items .item { padding: 1em; }
    #queryHelpModal .help-section p { line-height: 1.5em; }
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
