<%@ page import="de.laser.utils.LocaleUtils; de.laser.reporting.report.myInstitution.config.PlatformXCfg; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.export.GlobalExportHelper;" %>
<laser:serviceInjection />
<!-- _helpModal.gsp -->
<%
    String lang = (LocaleUtils.getCurrentLang() == 'en') ? 'en' : 'de'

    String icon_blue   = '<i class="icon circle blue"></i>'
    String icon_green  = '<i class="icon circle green"></i>'
    String icon_yellow = '<i class="icon circle yellow"></i>'
    String icon_pink   = '<i class="icon circle pink"></i>'
    String icon_teal   = '<i class="icon circle teal"></i>'

    Closure hc_identifier = { token1x, token3, token4x, token6 = null ->
        if (lang == 'de') {
            println """
                <p class="ui header"> Identifikatoren von ${token1x} </p>
                <p>
                    Gelistet werden alle relevanten Namensräume - also Namensräume von Identifikatoren, die ${token1x} konkret vergeben wurden.
                    Die Basissuche bestimmt dabei die Menge der betrachteten ${token3}.
                </p>
                <p>
                    Im Detail sind folgende Informationen verfügbar: <br/>
                    ${icon_blue} ${token4x} mit Identifikatoren aus dem jeweiligen Namensraum, <br />
                    ${icon_green} Insgesamt vergebene Identifikatoren aus dem jeweiligen Namensraum <br />
                </p>
            """
            if (token6) {
                println """
                    <p>
                        ${token4x} ohne Identifikatoren werden in der Gruppe ${icon_pink}<strong>* ohne Identifikator</strong> zusammmen gefasst. <br />
                        Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche ${token6} werden unter ${icon_teal}<strong>* kein web:kb Objekt</strong> gelistet. <br />
                    </p>
                """
            } else {
                println """
                    <p> ${token4x} ohne Identifikatoren werden in der Gruppe ${icon_pink}<strong>* ohne Identifikator</strong> zusammmen gefasst. </p>
                """
            }
        }
        else {
            println """
                <p class="ui header"> Identifiers of ${token1x} </p>
                <p>
                    All relevant namespaces are listed - i.e. namespaces of identifiers that ${token1x} have been specifically assigned.
                    The basic search determines the number of ${token3} considered.
                </p>
                <p>
                    The following information is available in detail: <br/>
                    ${icon_blue} ${token4x} with identifiers from the respective namespace, <br />
                    ${icon_green} Altogether assigned identifiers from the respective namespace <br />
                </p>
            """
            if (token6) {
                println """
                    <p>
                        ${token4x} without identifiers are combined in the group ${icon_pink}<strong>* no Identifier</strong>. <br />
                        Without a <strong>we:kb</strong> counterpart, relevant data is missing - such ${token6} are listed in ${icon_teal}<strong>* no web:kb object</strong>. <br />
                    </p>
                """
            } else {
                println """
                    <p> ${token4x} without identifiers are combined in the group ${icon_pink}<strong>* no Identifier</strong>. </p>
                """
            }
        }
    }

    Closure hc_property = { token1, token2x, token4 ->
        if (lang == 'de') {
            println """
                <p class="ui header"> Merkmale von ${token1} </p>
                <p>
                    Gelistet werden alle relevanten (also <strong>private oder öffentliche</strong>) Merkmale, die für ${token2x} konkret vergeben wurden.
                    Die Basissuche bestimmt dabei die Menge der betrachteten ${token2x}.
                </p>
                <p>
                    Im Detail sind folgende Informationen verfügbar: <br/>
                    ${icon_blue} ${token4} mit Merkmal X, <br />
                    ${icon_green} Öffentlich vergebene Merkmale X für die betrachteten ${token2x} <br />
                    ${icon_yellow} Private Merkmale X für die betrachteten ${token2x} <br />
                </p>
            """
        }
        else {
            println """
                <p class="ui header"> Properties of ${token1} </p>
                <p>
                    All relevant (i.e. <strong>private or public</strong>) properties that have been specifically assigned for ${token2x} are listed.
                    The basic search determines the number of ${token2x} considered.
                </p>
                <p>
                    The following information is available in detail: <br/>
                    ${icon_blue} ${token4} with property X, <br />
                    ${icon_green} Public properties X for the ${token2x} under consideration <br />
                    ${icon_yellow} Private properties X for the ${token2x} under consideration <br />
                </p>
            """
        }
    }

    Closure hc_generic_annual = { token1, token2x, token3, token5x ->
        if (lang == 'de') {
            println """
                <p class="ui header"> Laufzeit von ${token1} </p>
                <p>
                    Gruppiert werden die ${token2x} in Jahresringen - abhängig von den jeweiligen Datumsgrenzen.
                    Bedingen vorhandene Daten eine Laufzeit mehrerer Jahre, wird die ${token3} auch mehreren Jahresringen zugeordnet.
                    Die Basissuche bestimmt dabei die Menge der betrachteten ${token2x}.
                </p>
                <p>
                    ${token5x} ohne Enddatum werden <strong>zusätzlich</strong> in der Gruppe ${icon_teal}<strong>* ohne Ablauf</strong> gelistet. <br />
                    ${token5x} ohne Startdatum werden <strong>exklusive</strong> in der Gruppe ${icon_pink}<strong>* ohne Startdatum</strong> gelistet. <br />
                    ${token5x} ohne Angabe von Start- und Enddatum werden <strong>exklusive</strong> in der Gruppe ${icon_pink}<strong>* keine Angabe</strong> gelistet. <br />
                </p>
            """
        }
        else {
            println """
                <p class="ui header"> Duration of ${token1} </p>
                <p>
                    The ${token2x} are grouped in annual rings - depending on the respective date lines.
                    If existing data indicates a duration of several years, the ${token3} is also assigned to several annual rings.
                    The basic search determines the amount of ${token2x} considered.
                </p>
                <p>
                    ${token5x} without an end date are <strong>additionally</strong> listed in the group ${icon_teal}<strong>* no End date</strong>. <br />
                    ${token5x} without a start date will be <strong>exclusive</strong> listed in the group ${icon_pink}<strong>* no Start date</strong>. <br />
                    ${token5x} without a start and end date will be <strong>exclusive</strong> listed in the group ${icon_pink}<strong>* no Information</strong>. <br />
                </p>
            """
        }
    }

    Closure hc_generic_pkg = { token1, token2x, token4, token5 ->
        if (lang == 'de') {
            println """
                <p class="ui header"> ${token1} von Paketen </p>
                <p>
                    Gelistet werden alle relevanten ${token2x} - also ${token2x}, die Paketen konkret zugeordnet werden können.
                    Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
                </p>
                <p> Pakete ohne ausgewiesene ${token4} werden in der Gruppe ${icon_pink}<strong>* ohne ${token5}</strong> zusammmen gefasst. </p>
            """
        }
        else {
            println """
                <p class="ui header"> ${token1} of packages </p>
                <p>
                    All relevant ${token2x} are listed - i.e. ${token2x} that can be specifically assigned to packages.
                    The basic search determines the number of packages considered.
                </p>
                <p> Packages without designated ${token4} are summarized in the group ${icon_pink}<strong>* no ${token5}</strong>. </p>
            """
        }
    }

    Closure hc_generic_pkg_wekb = { token1, token2x, token3 ->
        if (lang == 'de') {
            println """
                <p class="ui header"> ${token1} von Paketen </p>
                <p>
                    Gelistet werden alle relevanten ${token2x} - also ${token3}, die Paketen konkret zugeordnet werden können.
                    Die Basissuche bestimmt dabei die Menge der betrachteten Pakete.
                </p>
                <p> Hierzu werden Paketinformationen in <strong>LAS:eR</strong> mit referenzierten Objekten aus der <strong>we:kb</strong> verglichen. </p>
                <p>
                    Pakete ohne ausgewiesene ${token2x} werden in der Gruppe ${icon_pink}<strong>* keine Angabe</strong> zusammmen gefasst. <br />
                    Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter ${icon_teal}<strong>* kein web:kb Objekt</strong> gelistet. <br />
                </p>
            """
        }
        else {
            println """
                <p class="ui header"> ${token1} of packages </p>
                <p>
                    All relevant ${token2x} are listed - i.e. ${token3} that can be specifically assigned to packages.
                    The basic search determines the number of packages considered.
                </p>
                <p> For this purpose, package information in <strong>LAS:eR</strong> is compared with referenced objects from the <strong>we:kb</strong>. </p>
                <p>
                    Packages without designated ${token2x} are summarized in the group ${icon_pink}<strong>* not specified</strong>. <br />
                    Without a <strong>we:kb</strong> counterpart, relevant data is missing - such packages are listed in ${icon_teal}<strong>* no web:kb object</strong>. <br />
                </p>
            """
        }
    }
%>

<ui:infoModal id="${modalID}">

    %{-- subscription --}%

    <div class="help-section" data-help-section="subscription-x-identifier">
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Lizenzen', 'Lizenzen', 'Lizenzen' )}
        </g:if>
        <g:else>
            ${hc_identifier( 'subscriptions', 'subscriptions', 'Subscriptions' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Lizenzen', 'Lizenzen', 'Lizenzen' )}
        </g:if>
        <g:else>
            ${hc_property( 'subscriptions', 'subscriptions', 'Subscriptions' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberSubscriptionProperty">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Einrichtungslizenzen', 'Einrichtungslizenzen', 'Einrichtungslizenzen' )}
        </g:if>
        <g:else>
            ${hc_property( 'Participant subscriptions', 'participant subscriptions', 'Participant subscriptions' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-annual">
        <g:if test="${lang == 'de'}">
            ${hc_generic_annual( 'Lizenzen', 'Lizenzen', 'Lizenz', 'Lizenzen' )}
        </g:if>
        <g:else>
            ${hc_generic_annual( 'subscriptions', 'subscriptions', 'subscription', 'Subscriptions' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberAnnual">
        <g:if test="${lang == 'de'}">
            ${hc_generic_annual( 'Einrichtungslizenzen', 'Einrichtungslizenzen', 'Einrichtungslizenz', 'Einrichtungslizenzen' )}
        </g:if>
        <g:else>
            ${hc_generic_annual( 'participant subscriptions', 'participant subscriptions', 'participant subscription', 'Participant subscriptions' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-license">
        <g:if test="${lang == 'de'}">
            <p class="ui header"> Verträge von Lizenzen </p>
            <p>
                Gelistet werden alle relevanten Verträge - also Verträge, die Lizenzen konkret zugeordnet werden können.
            </p>
            <p> Lizenzen ohne ausgewiesenen Vertrag werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Vertrag</strong> zusammmen gefasst. </p>
        </g:if>
        <g:else>
            <p class="ui header"> Licenses of subscriptions </p>
            <p>
                All relevant licenses are listed - i.e. licenses that can be specifically assigned to subscriptions.
            </p>
            <p> Subscriptions without a designated license are summarized in the group <i class="icon circle pink"></i><strong>* no License</strong>. </p>
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-licenseCategory">
        <g:if test="${lang == 'de'}">
            <p class="ui header"> Kategorie von Verträgen von Lizenzen </p>
            <p> TODO </p>
        </g:if>
        <g:else>
            <p class="ui header"> Categories of Licenses of subscriptions </p>
            <p> TODO </p>
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-provider">
        <g:if test="${lang == 'de'}">
            <p class="ui header"> Anbieter von Lizenzen </p>
            <p>
                Gelistet werden alle relevanten Anbieter - also Anbieter, die Lizenzen konkret zugeordnet werden können.
                Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen und Anbieter.
            </p>
            <p> Lizenzen ohne ausgewiesenen Anbieter werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Anbieter</strong> zusammmen gefasst. </p>
        </g:if>
        <g:else>
            <p class="ui header"> Providers of subscriptions </p>
            <p>
                All relevant providers are listed - i.e. providers that can be specifically assigned to subscriptions.
                The basic search determines the number of subscriptions and providers considered.
            </p>
            <p> Subscriptions without a designated provider are summarized in the group <i class="icon circle pink"></i><strong>* no Provider</strong>. </p>
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberProvider">
        <g:if test="${lang == 'de'}">
            <p class="ui header"> Anbieter von Einrichtungslizenzen </p>
            <p>
                Gelistet werden alle relevanten Anbieter - also Anbieter, die Einrichtungslizenzen konkret zugeordnet werden können.
                Genauer muss ein solcher Anbieter gleichzeitig <strong>einer Lizenz sowie der zugehörigen Einrichtungslizenz</strong> zugeordnet sein.
                Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen, Einrichtungslizenzen und Anbieter.
            </p>
            <p>
                Einrichtungslizenzen ohne ausgewiesenen Anbieter oder ohne passende Übereinstimmung werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Übereinstimmung</strong> zusammmen gefasst.
            </p>
        </g:if>
        <g:else>
            <p class="ui header"> Providers of participant subscriptions </p>
            <p>
                All relevant providers are listed - i.e. providers that can be specifically assigned to participant subscriptions.
                More precisely, such a provider must be assigned to <strong>a subscription and the associated participant subscription</strong> at the same time.
                The basic search determines the number of subscriptions, participant subscriptions and providers considered.
            </p>
            <p>
                Participant subscriptions without a designated provider or without a suitable match are grouped together in the <i class="icon circle pink"></i><strong>* no Match</strong>.
            </p>
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-platform">
        <g:if test="${lang == 'de'}">
            <p class="ui header"> Plattformen von Lizenzen </p>
            <p>
                Gelistet werden alle relevanten Plattformen - also Plattformen, die Lizenzen konkret zugeordnet werden können.
                Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen und Anbieter.
            </p>
            <p>
                Dabei sind folgende Varianten möglich: <br />
                <i class="icon circle blue"></i> Die Plattform kann direkt über eine Referenz aus dem Lizenz-Bestand ermittelt werden, <br />
                <i class="icon circle green"></i> Der einer Lizenz zugeordnete Anbieter verweist auf eine Plattform <br />
            </p>
            <p> Lizenzen ohne ermittelbare Plattform werden in der Gruppe <i class="icon circle pink"></i><strong>* ohne Plattform</strong> zusammmen gefasst. </p>
        </g:if>
        <g:else>
            <p class="ui header"> Platforms of subscriptions </p>
            <p>
                All relevant platforms are listed - i.e. platforms that can be specifically assigned to subscriptions.
                The basic search determines the number of subscriptions and providers considered.
            </p>
            <p>
                The following variants are possible: <br />
                <i class="icon circle blue"></i> The platform can be determined directly by a reference from the subscription entitlements, <br />
                <i class="icon circle green"></i> The provider assigned to a subscription refers to a platform <br />
            </p>
            <p> Subscriptions without assignable platforms are summarized in the group <i class="icon circle pink"></i><strong>* without Platform</strong>. </p>
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-memberSubscription">
        <g:if test="${lang == 'de'}">
            <p class="ui header"> Einrichtungslizenzen von Lizenzen </p>
            <p>
                Gelistet werden alle relevanten Lizenzen - also Lizenzen, denen entsprechende Einrichtungslizenzen zugeordnet werden können.
                Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen und Einrichtungslizenzen.
            </p>
            <p> Ohne übereinstimmende Zuordnung sind ggf. vorhandene Lizenzen <strong>nicht</strong> im Ergebnis sichtbar. </p>
        </g:if>
        <g:else>
            <p class="ui header"> Participant subscriptions of subscriptions </p>
            <p>
                All relevant subscriptions are listed - i.e. subscriptions to which corresponding participant subscriptions can be assigned.
                The basic search determines the number of subscriptions and participant subscriptions considered.
            </p>
            <p> If there is no suitable assignment, any existing subscriptions are <strong>not</strong> visible in the result. </p>
        </g:else>
    </div>

    <div class="help-section" data-help-section="subscription-x-member">
        <g:if test="${lang == 'de'}">
            <p class="ui header"> Einrichtungen von Lizenzen </p>
            <p>
                Gelistet werden alle relevanten Lizenzen - also Lizenzen, denen entsprechende Einrichtungslizenzen mit konkreten Organisationen als Einrichtungen zugeordnet werden können.
                Die Basissuche bestimmt dabei die Menge der betrachteten Lizenzen, Einrichtungslizenzen und Organisationen.
            </p>
            <p> Ohne übereinstimmende Zuordnung sind ggf. vorhandene Lizenzen <strong>nicht</strong> im Ergebnis sichtbar. </p>
        </g:if>
        <g:else>
            <p class="ui header"> Participants of subscriptions </p>
            <p>
                All relevant subscriptions are listed - i.e. subscriptions to which corresponding participant subscriptions can be assigned with specific organizations as participants.
                The basic search determines the number of subscriptions, participant subscriptions and organizations considered.
            </p>
            <p> If there is no suitable assignment, any existing subscriptions are <strong>not</strong> visible in the result. </p>
        </g:else>
    </div>

    %{-- license --}%

    <div class="help-section" data-help-section="license-x-identifier">
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Verträgen', 'Verträge', 'Verträge')}
        </g:if>
        <g:else>
            ${hc_identifier( 'licenses', 'licenses', 'Licenses' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="license-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Verträgen', 'Verträge', 'Verträge' )}
        </g:if>
        <g:else>
            ${hc_property( 'licenses', 'licenses', 'Licenses' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="license-x-annual">
        <g:if test="${lang == 'de'}">
            ${hc_generic_annual( 'Verträgen', 'Verträge', 'Vertrag', 'Verträge' )}
        </g:if>
        <g:else>
            ${hc_generic_annual( 'licenses', 'licenses', 'license', 'Licenses' )}
        </g:else>
    </div>

    %{-- org --}%

    <div class="help-section" data-help-section="org-x-identifier">
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Organisationen', 'Organisationen', 'Organisationen' )}
        </g:if>
        <g:else>
            ${hc_identifier( 'organisations', 'organisations', 'Organisations')}
        </g:else>
    </div>

    <div class="help-section" data-help-section="org-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Organisationen', 'Organisationen', 'Organisationen' )}
        </g:if>
        <g:else>
            ${hc_property( 'organisations', 'organisations', 'Organisations' )}
        </g:else>
    </div>

    %{-- package --}%

    <div class="help-section" data-help-section="package-x-id">
        <g:if test="${lang == 'de'}">
            ${hc_identifier( 'Paketen', 'Pakete', 'Pakete', 'Pakete' )}
        </g:if>
        <g:else>
            ${hc_identifier( 'packages', 'packages', 'Packages', 'packages' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-provider">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg( 'Anbieter', 'Anbieter', 'Anbieter', 'Anbieter' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg( 'Provider', 'provider', 'provider', 'Provider' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-platform">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg( 'Plattformen', 'Plattformen', 'Plattform', 'Plattform' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg( 'Platforms', 'platforms', 'platforms', 'Platform' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-language">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg( 'Sprachen', 'Sprachen', 'Sprachen', 'Sprachen' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg( 'Languages', 'languages', 'languages', 'Language' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-curatoryGroup">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Kuratorengruppen', 'Kuratorengruppen', 'Gruppen' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'Curatory groups', 'curatory groups', 'groups' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-nationalRange">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Länder', 'Länder', 'Länder' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'National ranges', 'national ranges', 'ranges' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="package-x-regionalRange">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Regionen', 'Regionen', 'Regionen' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'Regional ranges', 'regional ranges', 'ranges' )}
        </g:else>
    </div>
        
    <div class="help-section" data-help-section="package-x-ddc">
        <g:if test="${lang == 'de'}">
            ${hc_generic_pkg_wekb( 'Dewey-Dezimalklassifikation', 'Dewey-Dezimalklassifikation', 'Klassifikationen' )}
        </g:if>
        <g:else>
            ${hc_generic_pkg_wekb( 'Dewey decimal classifications', 'Dewey decimal classifications', 'classifications' )}
        </g:else>
    </div>

    %{-- platform --}%

    <div class="help-section" data-help-section="platform-x-property">
        <g:if test="${lang == 'de'}">
            ${hc_property( 'Plattformen', 'Plattformen', 'Plattformen' )}
        </g:if>
        <g:else>
            ${hc_property( 'platforms', 'platforms', 'Platforms' )}
        </g:else>
    </div>

    <div class="help-section" data-help-section="platform-x-propertyWekb">
        <g:set var="esProperties" value="${PlatformXCfg.CONFIG.base.distribution.default.getAt('platform-x-propertyWekb').esProperties}" />
        <g:set var="esdConfig" value="${BaseConfig.getCurrentConfigElasticsearchData(BaseConfig.KEY_PLATFORM)}" />

        <g:if test="${lang == 'de'}">
            <p class="ui header"> Merkmale von Plattformen </p>
            <p>
                Gelistet werden alle relevanten Merkmale (aus einer fest definierten Liste), die für Plattformen konkret vergeben wurden.
                Die Basissuche bestimmt dabei die Menge der betrachteten Plattformen.
            </p>
            <p>
                <ol class="ui list">
                    <g:each in="${esProperties}" var="prop"> <li value="*"><g:message code="${esdConfig.get(prop).label}" /></li> </g:each>
                </ol>
            </p>
            <p>
                Pakete ohne entsprechende Merkmale werden in der Gruppe <i class="icon circle pink"></i><strong>* keine Angabe</strong> zusammmen gefasst. <br />
                Ohne <strong>we:kb</strong>-Pendant fehlen relevante Daten - solche Pakete werden unter <i class="icon circle teal"></i><strong>* kein web:kb Objekt</strong> gelistet. <br />
            </p>
        </g:if>
        <g:else>
            <p class="ui header"> Properties of platforms </p>
            <p>
                All relevant properties (from a firmly defined list) that have been specifically assigned for platforms are listed.
                The basic search determines the number of platforms considered.
            </p>
            <p>
                <ol class="ui list">
                    <g:each in="${esProperties}" var="prop"> <li value="*"><g:message code="${esdConfig.get(prop).label}" /></li> </g:each>
                </ol>
            </p>
            <p>
                Packages without corresponding properties are summarized in the group <i class="icon circle pink"></i><strong>* no Information</strong>. <br />
                Relevant data is missing without a <strong>we:kb</strong> counterpart - such packages are listed under <i class="icon circle teal"></i><strong>* no web:kb object</strong>. <br />
            </p>
        </g:else>
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
    JSPC.callbacks.modal.onShow['${modalID}'] = function(trigger) {
        $('#${modalID} .help-section').hide();
        $current = $('#${modalID} .help-section[data-help-section=' + JSPC.app.reporting.current.request.query + ']');
        if (! $current.length) {
            $current = $('#${modalID} .help-section[data-help-section=default]')
        }
        $current.show();
    }
</laser:script>
<!-- _helpModal.gsp -->
