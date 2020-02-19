<%@ page import="com.k_int.kbplus.UserSettings" %>
<!doctype html>
<r:require module="chartist" />
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser')} : Frontend for Developers</title>
    <style>
    #example .example .column .icon {
        opacity: 1;
        height: 1em;
        color: #333333;
        display: block;
        margin: 0em auto 0.25em;
        font-size: 2em;
        -webkit-transition: color 0.6s ease, transform 0.2s ease;
        -moz-transition: color 0.6s ease, transform 0.2s ease;
        -o-transition: color 0.6s ease, transform 0.2s ease;
        -ms-transition: color 0.6s ease, transform 0.2s ease;
        transition: color 0.6s ease, transform 0.2s ease;
    }

    #example .example.html.segment {
        padding: 3.5em 1em 1em;
    }

    #example .example .grid > .column {
        opacity: 0.8;
        text-align: center;
    }
    #example .example > .html.segment {
        padding: 3.5em 1em 1em;
    }
    code .tag .title {
        color: #858188;
        font-weight: normal;
    }
    code.code .class {
        color: #008C79;
    }
    code.code .class b {
        background-color: rgba(218, 189, 40, 0.15);
        color: #9E6C00;
    }
    code .string, code .tag .value, code .phpdoc, code .dartdoc, code .tex .formula {
        color: #008C79;
    }
    code:hover .tag .title {
        color: #892A6F;
    }
    </style>
</head>

<body>

<section id="example">

    <h2 class="ui dividing header">Icons<a class="anchor" id="icons"></a></h2>
    <h4 class="ui header">Zusätzliche Icons</h4>
    <div class="html ui top attached segment example">

        <div class="ui doubling five column grid">
            <div class="column"><i aria-hidden="true" class="hand pointer outline slash icon"></i>hand pointer outline slash</div>

            <div class="column"><i aria-hidden="true" class="pencil alternate slash icon"></i>pencil alternate slash</div>

            <div class="column"><i aria-hidden="true" class="la-thumbtack slash icon"></i>la-thumbtack slash</div>

            <div class="column"><i aria-hidden="true" class="plus square slash icon"></i>plus square slash</div>

            <div class="column"><i aria-hidden="true" class="la-chain broken icon"></i>la-chain broken</div>

            <div class="column"><i aria-hidden="true" class="la-chain icon"></i>la-chain</div>

            <div class="column"><i aria-hidden="true" class="la-share icon"></i>la-share</div>

            <div class="column"><i aria-hidden="true" class="la-share slash icon"></i>la-share slash</div>

            <div class="column"><i aria-hidden="true" class="la-copySend icon"></i>la-copySend</div>

            <div class="column"><i aria-hidden="true" class="la-notebook icon"></i>la-notebook</div>

            <div class="column"><i aria-hidden="true" class="la-books icon"></i>la-books</div>

            <div class="column"><i aria-hidden="true" class="la-gokb icon"></i>la-gokb</div>

            <div class="column"><i aria-hidden="true" class="la-laser icon"></i>la-laser</div>

        </div>

        <div class="ui top attached label">Zusätzliche Icons in Laser</div>
    </div>

    <h2 class="ui dividing header">Form-Elemente<a class="anchor" id="form-elemente"></a></h2>
    <h4 class="ui header">Dropdowns</h4>
    <div class="dropdown example">

        <div class="html ui top attached segment">
            <ui>
                <li>versehen mit Label, das mit Extra-Inputdfeld verbunden ist </li>
                <li><g:link controller="myInstitution" action="currentSubscriptions">zum Beispiel hier verwendet (Merkmal innerhalb Filter)</g:link></li>
            </ui>
            <br>
            <div class="field">
                <label for="filterPropDef">
                    Merkmal
                    <i aria-hidden="true" class="question circle icon la-popup"></i>
                    <div class="ui  popup ">
                        <i aria-hidden="true" class="shield alternate icon"></i> = Mein Merkmal
                    </div>
                </label>
                <div class="ui search selection dropdown ">
                    <input type="hidden" name="filterPropDef">
                    <i aria-hidden="true" class="dropdown icon"></i>
                    <input class="search" id="filterPropDef">
                    <div class="default text">Bitte auswählen</div>
                    <div class="menu">
                        <div class="item selected" data-value="com.k_int.properties.PropertyDefinition:418">Abbestellgrund</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:414">Abbestellquote</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:269">AGB <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:384">Alternativname <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:448" data-rdc="com.k_int.kbplus.RefdataCategory:1">Archivzugriff</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:225" data-rdc="com.k_int.kbplus.RefdataCategory:1">Bei hbz Aufnahme der Metadaten nachfragen <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:415">Bestellnummer im Erwerbungssystem</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:393" data-rdc="com.k_int.kbplus.RefdataCategory:1">Bundesweit offen</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:447" data-rdc="com.k_int.kbplus.RefdataCategory:1">DBIS-Eintrag</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:566">DBIS-Link</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:256">DBIS-Nummer <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:424">Eingeschränkter Benutzerkreis</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:387" data-rdc="com.k_int.kbplus.RefdataCategory:1">EZB Gelbschaltung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:217" data-rdc="com.k_int.kbplus.RefdataCategory:1">EZB-Gelbschaltungen <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:423">Fachstatistik / Klassifikation</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:450">GASCO-Anzeigename</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:121" data-rdc="com.k_int.kbplus.RefdataCategory:1">GASCO-Eintrag</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:461">GASCO-Informations-Link</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:451">GASCO-Verhandlername</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:419">Hosting-Gebühr</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:214">Institut <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:406" data-rdc="com.k_int.kbplus.RefdataCategory:1">KBART</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:248">Kostensplitting <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:293">Kostensplitting 2 <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:445" data-rdc="com.k_int.kbplus.RefdataCategory:1">Kündigungsfrist</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:409" data-rdc="com.k_int.kbplus.RefdataCategory:1">Mehrjahreslaufzeit</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:395" data-rdc="com.k_int.kbplus.RefdataCategory:1">Mengenrabatt Stichtag</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:389">Metadaten Quelle</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:388" data-rdc="com.k_int.kbplus.RefdataCategory:1">Metadatenlieferung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:398" data-rdc="com.k_int.kbplus.RefdataCategory:1">Neueinsteigerrabatt</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:234" data-rdc="com.k_int.kbplus.RefdataCategory:1">Open Access <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:421">PDA/EBS-Programm</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:420">Pick&amp;Choose-Paket</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:413">Preis abhängig von</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:403" data-rdc="com.k_int.kbplus.RefdataCategory:1">Preis gerundet</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:446">Preissteigerung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:390" data-rdc="com.k_int.kbplus.RefdataCategory:1">Preisvorteil durch weitere Produktteilnahme</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:408" data-rdc="com.k_int.kbplus.RefdataCategory:1">Private Einrichtungen</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:391" data-rdc="com.k_int.kbplus.RefdataCategory:1">Produktabhängigkeit</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:422">Produktsigel beantragt</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:410">Rabatt</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:411" data-rdc="com.k_int.kbplus.RefdataCategory:1">Rabatt Zählung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:392">Rabattstaffel</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:394" data-rdc="com.k_int.kbplus.RefdataCategory:1">Rechnungsstellung durch Anbieter</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:401">Rechnungszeitpunkt</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:407" data-rdc="com.k_int.kbplus.RefdataCategory:1">reverse charge</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:449" data-rdc="com.k_int.kbplus.RefdataCategory:1">SFX-Eintrag</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:399" data-rdc="com.k_int.kbplus.RefdataCategory:1">Simuser</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:400">Simuser Zahl</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:405">Statistik</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:564">Statistik-Link</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:444">Statistikzugang</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:563" data-rdc="com.k_int.kbplus.RefdataCategory:1">Steuerbefreiung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:565">Subskriptionsnummer des Lieferanten</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:417">Subskriptionsnummer des Verlags</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:404" data-rdc="com.k_int.kbplus.RefdataCategory:1">Teilzahlung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:377" data-rdc="com.k_int.kbplus.RefdataCategory:2">Testeigenschaft zum Übertragen original</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:186">Testmerkmal <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:257" data-rdc="com.k_int.kbplus.RefdataCategory:101">Testmerkmal zum Verschieben von Referenzwerten <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:396">Testzeitraum</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:397" data-rdc="com.k_int.kbplus.RefdataCategory:1">Unterjähriger Einstieg</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:402">Zahlungsziel</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:235">Zugangskennungen (pro DB) <i aria-hidden="true" class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:416">Zugangskennungen für Nutzer (pro Zeitschrift)</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:412" data-rdc="com.k_int.kbplus.RefdataCategory:1">Zusätzliche Software erforderlich?</div>
                    </div>
                </div>
            </div>

            <div class="ui top attached label">WCAG-Proof Dropdown mit hidden Input-Feld (Barrierefrei)

            </div>
        </div>

        <div class="annotation transition visible" style="display: none;">
            <div class="ui instructive bottom attached segment">
                <pre aria-hidden="true">
&lt;div class=&quot;field&quot;&gt;
    &lt;label <strong>for=&quot;filterPropDef&quot;</strong>&gt;
        Merkmal
        &lt;i class=&quot;question circle icon la-popup&quot;&gt;&lt;/i&gt;
        &lt;div class=&quot;ui  popup &quot;&gt;
            &lt;i class=&quot;shield alternate icon&quot;&gt;&lt;/i&gt; = Meine Merkmal
        &lt;/div&gt;
    &lt;/label&gt;
    &lt;div class=&quot;ui search selection dropdown la-filterPropDef&quot;&gt;
        &lt;input type=&quot;hidden&quot; name=&quot;filterPropDef&quot;&gt; &lt;i class=&quot;dropdown icon&quot;&gt;&lt;/i&gt;
        &lt;input class=&quot;search&quot; <strong>id=&quot;filterPropDef&quot;</strong> &gt;
        &lt;div class=&quot;default text&quot;&gt;Bitte ausw&auml;hlen&lt;/div&gt;
        &lt;div class=&quot;menu&quot;&gt;
            &lt;div class=&quot;item selected&quot; data-value=&quot;linkurl&quot;&gt;Abbestellgrund&lt;/div&gt;
            &lt;div class=&quot;item&quot; data-value=&quot;linkurl&quot;&gt;Abbestellquote&lt;/div&gt;
            &lt;div class=&quot;item&quot; data-value=&quot;linkurl&quot;&gt;AGB &lt;i class=&quot;shield alternate icon&quot;&gt;&lt;/i&gt;&lt;/div&gt;
        &lt;/div&gt;
    &lt;/div&gt;
&lt;/div&gt;
                </pre>
            </div>
        </div>
    </div>
    <h4 class="ui header">An- und Ausschalten von Buttons auf Show-Seiten</h4>
    <div class="dropdown example">

        <div class="html ui top attached segment">
            <ui>
                <li>Cards, die keinen Inhalt haben, müssen ausgeschaltet werden:
                    <ol>
                    <li>class <strong>'la-js-dont-hide-this-card'</strong> zu ins Markup einer Tabellen-Zelle</li>
                    <li>class <strong>'la-js-hideable'</strong> zu ins Markup einer Card</li>
                    </ol>
                </li>
                <li><g:link controller="subscription" action="show">zum Beispiel hier verwendet</g:link></li>
            </ui>
            <br>

            <div class="ui top attached label">Cards müssen "ausgeschaltet" werden, wenn es keinen Inhalt gibt

            </div>
        </div>

        <div class="annotation transition visible" style="display: none;">
            <div class="ui instructive bottom attached segment">
                <pre aria-hidden="true">
&lt;thead&gt;
&lt;tr&gt;
    &lt;th class="<strong>la-js-dont-hide-this-card</strong>" &gt;${message(code:'property.table.property')}&lt;/th&gt;
    &lt;th&gt;${message(code:'property.table.value')}&lt;/th&gt;
    &lt;g:if test="${ownobj instanceof com.k_int.kbplus.License}"&gt;
        &lt;th&gt;${message(code:'property.table.paragraph')}&lt;/th&gt;
    &lt;/g:if&gt;
    &lt;th&gt;${message(code:'property.table.notes')}&lt;/th&gt;
    &lt;th&gt;${message(code:'default.actions.label')}&lt;/th&gt;
&lt;/tr&gt;
&lt;/thead&gt;
                    .
                    .
                    .

&lt;div class="ui card la-dl-no-table <strong>la-js-hideable</strong>"&gt;
                </pre>
            </div>
        </div>
    </div>
    <!---- --->

    <h2 class="ui dividing header">Toggle Button<a class="anchor" id="icons"></a></h2>

    <h4 class="ui header">Anzeige Icon</h4>
    <div class="html ui top attached segment example">
        <ul>
            <li><code>tabindex="0"</code><strong> – für den Screenreader</strong>
            <li><code>class="la-popup-tooltip la-delay"</code> <strong> – für die Aktivierung des Tooltips mit Jvascript</strong>
            <li><code>class="icon thumbtack blue"</code> <strong> – für Gestaltung</strong>
            <li><code>data-content="1 2 3"</code>
        </ul>
        <div class="ui top attached label">WCAG-Proof Icon</div>
    </div>
    <i  tabindex="0" class="la-popup-tooltip la-delay icon thumbtack blue" data-content="1 2 3" ></i>
    <h4 class="ui header">Funktions-Button, der ausgeschaltet werden kan, Icon bleibt</h4>
    <div class="html ui top attached segment example">
        <ul>
            <li><code>la-js-editmode-container</code> <strong>im umschließenden Element</strong>
            <li><code>role="button"</code>, <strong>wenn es ein Link ist</strong>
            <li><code>class="ui icon mini button </code>
                <ul>
                    <li><code>la-audit-button </code>
                    <li><code>class="la-popup-tooltip la-delay"</code> <strong> – für die Aktivierung des Tooltips mit Jvascript</strong>
                    <li><code>la-js-editmode-remain-icon"</code>
                </ul>
            </li>

        </ul>
        <div class="ui top attached label">WCAG-Proof Button</div>
    </div>
    <dd class="la-js-editmode-container">
        <a  role="button" class="ui icon mini button la-audit-button la-popup-tooltip la-delay" href='https://www.spiegel.de' data-content="4 5 6">
            <i  class="icon thumbtack la-js-editmode-icon"></i>
        </a>
    </dd><br>

    <h4 class="ui header">Funktions-Button, der ausgeschaltet werden kann, Icon verschwindet</h4>
    <div class="html ui top attached segment example">
        <ul>
            <li><code>role="button"</code>, <strong>wenn es ein Link ist</strong>
            <li><code>class="ui icon mini button </code>
            <li><code>class="la-popup-tooltip la-delay"</code> <strong> – für die Aktivierung des Tooltips mit Jvascript</strong>

            </li>

        </ul>
        <div class="ui top attached label">WCAG-Proof Button</div>
    </div>

    <a  role="button" class="ui icon mini button la-audit-button la-popup-tooltip la-delay" href='https://www.spiegel.de' data-content="10 11 12">
        <i  class="icon thumbtack la-js-editmode-icon"></i>
    </a><br><br>
    <h4 class="ui header">Funktions-Button, der NICHT ausgeschaltet werden kann, Icon und Button verschwinden NICHT</h4>
    <div class="html ui top attached segment example">
        <ul>
            <li><code>role="button"</code>, <strong>wenn es ein Link ist</strong>
            <li><code>class="ui icon mini button </code>
            <li><code>class="la-popup-tooltip la-delay"</code> <strong> – für die Aktivierung des Tooltips mit Jvascript</strong>
            <li><code>class="la-js-dont-hide-button"</code><strong> – für die Aktivierung des NICHTAUSSCHALTENS MIT TOGGLE BUTTON mit Javascript</strong>

            </li>

        </ul>
        <div class="ui top attached label">WCAG-Proof Button</div>
    </div>

    <a  role="button" class="ui icon mini button la-audit-button la-popup-tooltip la-delay la-js-dont-hide-button" href='https://www.spiegel.de' data-content="13 14 15">
        <i  class="icon thumbtack"></i>
    </a><br><br>

    <h2 class="ui dividing header">Modale<a class="anchor" id="icons"></a></h2>
    <h4 class="ui header">Mehrere Modale auf einer Seite-Values in Formularfeldern werden mit Data-Attributen übertrage</h4>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Javascript in der View definiert die Data-Attribute</div>
        <pre aria-hidden="true">
Javascript:

dcbStore.modal.show.customerTypeModal = function(trigger) {
    $('#customerTypeModal #orgName_ct').attr('value', $(trigger).<strong>attr('data-orgName')</strong>)
    $('#customerTypeModal input[name=target]').attr('value', $(trigger).<strong>attr('data-ctTarget')</strong>)
                .
                .
                .</pre>
    </div>
            <div class="html ui top attached segment example">
                <div class="ui top attached label"></div>
                <pre aria-hidden="true">

var customerType = $(trigger).attr('data-customerType')
if (customerType) {
        $('#customerTypeModal select[name=customerType]').dropdown(<strong>'set selected'</strong>, customerType)
} else {
    $('#customerTypeModal select[name=customerType]').dropdown('clear')
}</pre>
    </div>

    <%-- Confimation Modal --%>

    <h2 class="ui dividing header">Confimation Modal<a class="anchor" id="icons"></a></h2>
    <h4 class="ui header">Buttons, die Confirmation Modals haben</h4>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Link, der als Button funktioniert (das heißt, dass er eine Aktion ausführt)</div>
        <g:link controller="dev"
                action="frontend"
                params=""
                data-content="Hier kommt der Tooltip rein"
                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
                data-confirm-term-how="delete"
                class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
                role="button">
            <i aria-hidden="true" class="trash alternate icon"></i>
        </g:link>
    </div>

    <%-- ERMS-2082 --%>

    <div class="html ui top attached segment example">
        <div class="ui top attached label">Inhalt der Nachricht per Ajax: AjaxController.genericDialogMessage()</div>

        <g:link controller="dev"
                action="frontend"
                extaContentFlag="false"
                params=""
                data-content="Hier kommt der Tooltip rein"
                data-confirm-messageUrl="${createLink(controller:'ajax', action:'genericDialogMessage', params:[template:'abc'])}"
                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
                data-confirm-term-how="delete"
                class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
                role="button">
            <i aria-hidden="true" class="trash alternate icon"></i>
        </g:link>
    </div>

    <%-- ERMS-2082 --%>

    <div class="html ui top attached segment example">
        <div class="ui top attached label">Link, der den AJAX-Contoler aufruft und  als Button funktioniert (daß heiß, eine Aktion ausführt)</div>
        <laser:remoteLink class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
                          controller="dev"
                          action="frontend"
                          params=""
                          id=""
                          data-content="Hier kommt der Tooltip rein"
                          data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
                          data-confirm-term-how="delete"
                          data-done=""
                          data-always=""
                          data-update=""
                          role="button"
        >
            <i aria-hidden="true" class="trash alternate icon"></i>
        </laser:remoteLink>
    </div>


    <%-- Charts --%>

    <h2 class="ui dividing header">Charts<a class="anchor" id="icons"></a></h2>
    <h4 class="ui header">Einbindung von Chartist als Javascript Library zum Rendern von Daten</h4>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Require-Tag für Chartist eingebauen</div>

        <pre aria-hidden="true">
            &ltr:require module="chartist" /&gt;
        </pre>

    </div>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Chartis-Objekt mit Javascript aufrufen innerhalb r:script</div>
        <pre aria-hidden="true"> &ltr:script /&gt;
        </pre>
        <pre aria-hidden="true">    new Chartist.Line('.ct-chart', data);
        </pre>
        <pre aria-hidden="true">&lt/r:script&gt;
        </pre>
    </div>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Beispiel</div>
        <div class="ct-chart ct-perfect-fourth"></div>
    </div>
</section>

<h1>Offene Fragen in Bezug auf WCAG</h1>
<h2>Icons</h2>
<h3>Dekorative Icon mit einer Dopplung der Semantik durch Text, der daneben steht.</h3>

<div class="ui icon info message">
    <i aria-hidden="true" class="exclamation triangle icon"></i>
    <div class="content">
        <div class="header">
            Achtung
        </div>
        <p>Wenn Sie ein neues Element nicht finden, warten Sie etwa <strong>10 Minuten</strong>, bis sich der Index aktualisiert hat.</p>
    </div>
</div>
<h3>Icon ohne Linkfunktion</h3>
<div class="ui icon info message">
    <i aria-hidden="true" class="close icon"></i>
    <div class="content">
        <div class="header">
            Achtung
        </div>
        <p>Wenn Sie ein neues Element nicht finden, warten Sie etwa <strong>10 Minuten</strong>, bis sich der Index aktualisiert hat.</p>
    </div>
</div>
<h3>Icons in einem Button, der eine Bezeichnung hat</h3>
<button class="ui   button la-inline-labeled la-js-filterButton la-clearfix blue"> Filter <i
        aria-hidden="true" class="filter icon"></i> <span
        class="ui circular label la-js-filter-total hidden">0</span>
</button>
<h3>Icons in einem Link ohne zusätzlichen Text</h3>
<g:link aria-label="Das ist eine Beschreibung für den Accessibility Tree" controller="dev" action="frontend" params="" class="ui icon positive button">
    <i aria-hidden="true" class="checkmark icon"></i>
</g:link>

<r:script>

    var data = {
        // A labels array that can contain any sort of values
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
        // Our series array that contains series objects or in this case series data arrays
        series: [
            [5, 2, 4, 2, 0]
        ]
    };

    // Create a new line chart object where as first parameter we pass in a selector
    // that is resolving to our chart container element. The Second parameter
    // is the actual data object.
    new Chartist.Line('.ct-chart', data);
</r:script>


</body>
</html>
