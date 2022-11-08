<%@ page import="de.laser.utils.LocaleUtils; de.laser.reporting.report.myInstitution.config.PlatformXCfg; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.export.GlobalExportHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<%
    String lang = (LocaleUtils.getCurrentLang() == 'en') ? 'en' : 'de'

    Closure hc_identifier = { token1, token2, token3, token4, token5, token6 = null ->
        if (lang == 'de') {
            println """
                <p class="ui header"> Identifikatoren von ${token1} </p>
                <p>
                    Gelistet werden alle relevanten Namensräume - also Namensräume von Identifikatoren, die ${token2} konkret vergeben wurden.
                    Die Basissuche bestimmt dabei die Menge der betrachteten ${token3}.
                </p>
                <p>
                    Im Detail sind folgende Informationen verfügbar: <br/>
                    <i class="icon circle blue"></i> ${token4} mit Identifikatoren aus dem jeweiligen Namensraum, <br />
                    <i class="icon circle green"></i> Insgesamt vergebene Identifikatoren aus dem jeweiligen Namensraum <br />
                </p>
            """
            if (token6) {
                println """
                    <p>
                        ${token5} ohne Identifikatoren werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Identifikator</strong> zusammmen gefasst. <br />
                        Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche ${token6} werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
                    </p>
                """
            } else {
                println """
                    <p> ${token5} ohne Identifikatoren werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Identifikator</strong> zusammmen gefasst. </p>
                """
            }
        }
        else {
            println """
                <p class="ui header"> Identifiers of ${token1} </p>
                <p>
                    All relevant namespaces are listed - i.e. namespaces of identifiers that ${token2} have been specifically assigned.
                    The basic search determines the number of ${token3} considered.
                </p>
                <p>
                    The following information is available in detail: <br/>
                    <i class="icon circle blue"></i> ${token4} with identifiers from the respective namespace, <br />
                    <i class="icon circle green"></i> Altogether assigned identifiers from the respective namespace <br />
                </p>
            """
            if (token6) {
                println """
                    <p>
                        ${token5} without identifiers are combined in the group <i class="icon circle pink"></i><strong>* no identifier</strong>. <br />
                        Without a <strong>we:kb</strong> counterpart, relevant data is missing - such ${token6} are listed in <i class="icon circle teal"></i><strong>* no web:kb object</strong>. <br />
                    </p>
                """
            } else {
                println """
                    <p> ${token5} without identifiers are combined in the group <i class="icon circle pink"></i><strong>* no identifier</strong>. </p>
                """
            }
        }
    }

    Closure hc_property = { token1, token2, token3, token4, token5, token6 ->
        if (lang == 'de') {
            println """
                <p class="ui header"> Merkmale von ${token1} </p>
                <p>
                    Gelistet werden alle relevanten (also <strong>private oder öffentliche</strong>) Merkmale, die für ${token2} konkret vergeben wurden.
                    Die Basissuche bestimmt dabei die Menge der betrachteten ${token3}.
                </p>
                <p>
                    Im Detail sind folgende Informationen verfügbar: <br/>
                    <i class="icon circle blue"></i> ${token4} mit Merkmal X, <br />
                    <i class="icon circle green"></i> Öffentlich vergebene Merkmale X für die betrachteten ${token5} <br />
                    <i class="icon circle yellow"></i> Private Merkmale X für die betrachteten ${token6} <br />
                </p>
            """
        }
        else {
            println """
                <p class="ui header"> Properties of ${token1} </p>
                <p>
                    All relevant (i.e. <strong>private or public</strong>) properties that have been specifically assigned for ${token2} are listed.
                    The basic search determines the number of ${token3} considered.
                </p>
                <p>
                    The following information is available in detail:: <br/>
                    <i class="icon circle blue"></i> ${token4} with property X, <br />
                    <i class="icon circle green"></i> Public properties X for the ${token5} under consideration <br />
                    <i class="icon circle yellow"></i> Private properties X for the ${token6} under consideration <br />
                </p>
            """
        }
    }

    Closure hc_generic_pkg = { token1, token2, token3, token4, token5 ->
        if (lang == 'de') {
            println """
                <p class="ui header"> ${token1} von Paketen </p>
                <p>
                    Gelistet werden alle relevanten ${token2} - also ${token3}, die Paketen konkret zugeordnet werden können.
                    Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
                </p>
                <p> Pakete ohne ausgewiesenen ${token4} werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne ${token5}</strong> zusammmen gefasst. </p>
            """
        }
        else {
            println """
                <p class="ui header"> ${token1} of packages </p>
                <p>
                    All relevant ${token2} are listed - i.e. ${token3} that can be specifically assigned to packages.
                    The basic search determines the number of packages considered.
                </p>
                <p> Packages without designated ${token4} are summarized in the group <i class="icon circle pink"></i><strong>* no ${token5}</strong>. </p>
            """
        }
    }

    Closure hc_generic_pkg_wekb = { token1, token2, token3, token4 ->
        if (lang == 'de') {
            println """
                <p class="ui header"> ${token1} von Paketen </p>
                <p>
                    Gelistet werden alle relevanten ${token2} - also ${token3}, die Paketen konkret zugeordnet werden können.
                    Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
                </p>
                <p> Hierzu werden Paketinformationen in <strong>LAS:eR</strong> mit referenzierten Objekten aus der <strong>we:kb</strong> verglichen. </p>
                <p>
                    Pakete ohne ausgewiesene ${token4} werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
                    Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
                </p>
            """
        }
        else {
            println """
                <p class="ui header"> ${token1} of packages </p>
                <p>
                    All relevant ${token2} are listed - i.e. ${token3} that can be specifically assigned to packages.
                    The basic search determines the number of packages considered.
                </p>
                <p> For this purpose, package information in <strong>LAS:eR</strong> is compared with referenced objects from the <strong>we:kb</strong>. </p>
                <p>
                    Packages without designated ${token4} are summarized in the group <i class="icon circle pink"></i><strong>* not specified</strong>. <br />
                    Without a <strong>we:kb</strong> counterpart, relevant data is missing - such packages are listed in <i class="icon circle teal"></i><strong>* no web:kb object</strong>. <br />
                </p>
            """
        }
    }
%>

<ui:infoModal id="${modalID}">

    %{-- subscription --}%

    <div class="help-section" data-help-section="subscription-x-identifier">
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Lizenzen', 'Lizenzen', 'Lizenzen', 'Lizenzen', 'Lizenzen' )}
        </g:if>
        <g:else>
            ${hc_identifier( 'subscriptions', 'subscriptions', 'subscriptions', 'Subscriptions', 'Subscriptions' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Lizenzen', 'Lizenzen', 'Lizenzen', 'Lizenzen', 'Lizenzen', 'Lizenzen' )}
        </g:if>
        <g:else>
            ${hc_property( 'subscriptions', 'subscriptions', 'subscriptions', 'Subscriptions', 'subscriptions', 'subscriptions' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberSubscriptionProperty">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Teilnehmerlizenzen', 'Teilnehmerlizenzen', 'Teilnehmerlizenzen', 'Teilnehmerlizenzen', 'Teilnehmerlizenzen', 'Teilnehmerlizenzen' )}
        </g:if>
        <g:else>
            ${hc_property( 'Participant subscriptions', 'participant subscriptions', 'participant subscriptions', 'Participant subscriptions', 'participant subscriptions', 'participant subscriptions' )}
        </g:else>
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
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Verträgen', 'Verträgen', 'Verträge', 'Verträge', 'Verträge' )}
        </g:if>
        <g:else>
            ${hc_identifier( 'licenses', 'licenses', 'licenses', 'Licenses', 'Licenses' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="license-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Verträgen', 'Verträge', 'Verträge', 'Verträge', 'Verträge', 'Verträge' )}
        </g:if>
        <g:else>
            ${hc_property( 'licenses', 'licenses', 'licenses', 'Licenses', 'licenses', 'licenses' )}
        </g:else>
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
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Organisationen', 'Organisationen', 'Organisationen', 'Organisationen', 'Organisationen' )}
        </g:if>
        <g:else>
            ${hc_identifier( 'organisations', 'organisations', 'organisations', 'Organisations', 'Organisations' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="org-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Organisationen', 'Organisationen', 'Organisationen', 'Organisationen', 'Organisationen', 'Organisationen' )}
        </g:if>
        <g:else>
            ${hc_property( 'organisations', 'organisations', 'organisations', 'Organisations', 'organisations', 'organisations' )}
        </g:else>
    </div>

    %{-- package --}%

    <div class="help-section" data-help-section="package-x-id">
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Paketen', 'Paketen', 'Pakete', 'Pakete', 'Pakete', 'Pakete' )}
        </g:if>
        <g:else>
            ${hc_identifier( 'packages', 'packages', 'packages', 'Packages', 'Packages', 'packages' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-provider">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg( 'Anbieter', 'Anbieter', 'Anbieter', 'Anbieter', 'Anbieter' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg( 'Provider', 'provider', 'provider', 'provider', 'Provider' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-platform">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg( 'Plattformen', 'Plattformen', 'Plattformen', 'Plattform', 'Plattform' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg( 'Platforms', 'platforms', 'platforms', 'platforms', 'Platform' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-language">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg( 'Sprachen', 'Sprachen', 'Sprachen', 'Sprachen', 'Sprachen' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg( 'Languages', 'languages', 'languages', 'languages', 'Language' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-curatoryGroup">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Kuratorengruppen', 'Kuratorengruppen', 'Gruppen', 'Kuratorengruppen' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'Curatory groups', 'curatory groups', 'groups', 'curatory groups' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-nationalRange">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Länder', 'Länder', 'Länder', 'Länder' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'National ranges', 'national ranges', 'ranges', 'national ranges' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-regionalRange">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Regionen', 'Regionen', 'Regionen', 'Regionen' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'Regional ranges', 'regional ranges', 'ranges', 'regional ranges' )}
        </g:else>
    </div>
        
    <div class="help-section" data-help-section="package-x-ddc">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Dewey-Dezimalklassifikation', 'Dewey-Dezimalklassifikation', 'Klassifikationen', 'Dewey-Dezimalklassifikation' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'Dewey decimal classifications', 'Dewey decimal classifications', 'classifications', 'Dewey decimal classifications' )}
        </g:else>
    </div>

    %{-- platform --}%

    <div class="help-section" data-help-section="platform-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Plattformen', 'Plattformen', 'Plattformen', 'Plattformen', 'Plattformen', 'Plattformen' )}
        </g:if>
        <g:else>
            ${hc_property( 'platforms', 'platforms', 'platforms', 'Platforms', 'platforms', 'platforms' )}
        </g:else>
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
