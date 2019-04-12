<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser', default: 'LAS:eR')} : Frontend for Developers</title>
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
            <div class="column"><i class="hand pointer outline slash icon"></i>hand pointer outline slash</div>

            <div class="column"><i class="pencil alternate slash icon"></i>pencil alternate slash</div>

            <div class="column"><i class="la-thumbtack slash icon"></i>la-thumbtack slash</div>

            <div class="column"><i class="plus square slash icon"></i>plus square slash</div>

            <div class="column"><i class="la-chain broken icon"></i>la-chain broken</div>

            <div class="column"><i class="la-chain icon"></i>la-chain</div>

            <div class="column"><i class="la-share icon"></i>la-share</div>

            <div class="column"><i class="la-share slash icon"></i>la-share slash</div>

            <div class="column"><i class="la-copySend icon"></i>la-copySend</div>

            <div class="column"><i class="la-notebook icon"></i>la-notebook</div>

            <div class="column"><i class="la-books icon"></i>la-books</div>

            <div class="column"><i class="la-noChange icon"></i>la-noChange</div>

            <div class="column"><i class="la-append icon"></i>la-append</div>

            <div class="column"><i class="la-replace icon"></i>la-replace</div>
        </div>

        <div class="ui top attached label">Zusätzliche Icons in Laser</div>
    </div>

    <h2 class="ui dividing header">Form-Elemente<a class="anchor" id="form-elemente"></a></h2>
    <h4 class="ui header">Dropdowns</h4>
    <div class="dropdown example">

        <div class="html ui top attached segment">
            <ui>
                <li>versehen mit Label, das mit Extra-Inputdfeld verbunden ist </li>
                <li><g:link controller="public" action="gascoDetailsIssueEntitlements">zum Beipsiel hier verwendet (Merkmal innerhalb Filter)</g:link></li>
            </ui>
            <br>
            <div class="field">
                <label for="filterPropDef">
                    Merkmal
                    <i class="question circle icon la-popup"></i>
                    <div class="ui  popup ">
                        <i class="shield alternate icon"></i> = Meine Merkmal
                    </div>
                </label>
                <div class="ui search selection dropdown ">
                    <input type="hidden" name="filterPropDef">
                    <i class="dropdown icon"></i>
                    <input class="search" id="filterPropDef">
                    <div class="default text">Bitte auswählen</div>
                    <div class="menu">
                        <div class="item selected" data-value="com.k_int.properties.PropertyDefinition:418">Abbestellgrund</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:414">Abbestellquote</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:269">AGB <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:384">Alternativname <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:448" data-rdc="com.k_int.kbplus.RefdataCategory:1">Archivzugriff</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:225" data-rdc="com.k_int.kbplus.RefdataCategory:1">Bei hbz Aufnahme der Metadaten nachfragen <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:415">Bestellnummer im Erwerbungssystem</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:393" data-rdc="com.k_int.kbplus.RefdataCategory:1">Bundesweit offen</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:447" data-rdc="com.k_int.kbplus.RefdataCategory:1">DBIS-Eintrag</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:566">DBIS-Link</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:256">DBIS-Nummer <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:424">Eingeschränkter Benutzerkreis</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:387" data-rdc="com.k_int.kbplus.RefdataCategory:1">EZB Gelbschaltung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:217" data-rdc="com.k_int.kbplus.RefdataCategory:1">EZB-Gelbschaltungen <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:423">Fachstatistik / Klassifikation</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:450">GASCO-Anzeigename</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:121" data-rdc="com.k_int.kbplus.RefdataCategory:1">GASCO-Eintrag</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:461">GASCO-Informations-Link</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:451">GASCO-Verhandlername</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:419">Hosting-Gebühr</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:214">Institut <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:406" data-rdc="com.k_int.kbplus.RefdataCategory:1">KBART</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:248">Kostensplitting <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:293">Kostensplitting 2 <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:445" data-rdc="com.k_int.kbplus.RefdataCategory:1">Kündigungsfrist</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:409" data-rdc="com.k_int.kbplus.RefdataCategory:1">Mehrjahreslaufzeit</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:395" data-rdc="com.k_int.kbplus.RefdataCategory:1">Mengenrabatt Stichtag</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:389">Metadaten Quelle</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:388" data-rdc="com.k_int.kbplus.RefdataCategory:1">Metadatenlieferung</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:398" data-rdc="com.k_int.kbplus.RefdataCategory:1">Neueinsteigerrabatt</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:234" data-rdc="com.k_int.kbplus.RefdataCategory:1">Open Access <i class="shield alternate icon"></i></div>
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
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:186">Testmerkmal <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:257" data-rdc="com.k_int.kbplus.RefdataCategory:101">Testmerkmal zum Verschieben von Referenzwerten <i class="shield alternate icon"></i></div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:396">Testzeitraum</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:397" data-rdc="com.k_int.kbplus.RefdataCategory:1">Unterjähriger Einstieg</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:402">Zahlungsziel</div>
                        <div class="item" data-value="com.k_int.properties.PropertyDefinition:235">Zugangskennungen (pro DB) <i class="shield alternate icon"></i></div>
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
                <pre>
&lt;div class=&quot;field&quot;&gt;com.k_int.properties.PropertyDefinition:269
    &lt;label for=&quot;filterPropDef&quot;&gt;
        Merkmal
        &lt;i class=&quot;question circle icon la-popup&quot;&gt;&lt;/i&gt;
        &lt;div class=&quot;ui  popup &quot;&gt;
            &lt;i class=&quot;shield alternate icon&quot;&gt;&lt;/i&gt; = Meine Merkmal
        &lt;/div&gt;
    &lt;/label&gt;
    &lt;div class=&quot;ui fluid search selection dropdown la-filterPropDef category active visible&quot;&gt;
        &lt;input type=&quot;hidden&quot; name=&quot;filterPropDef&quot;&gt; &lt;i class=&quot;dropdown icon&quot;&gt;&lt;/i&gt;
        &lt;input class=&quot;search&quot; id=&quot;filterPropDef&quot; tabindex=&quot;0&quot;&gt;
        &lt;div class=&quot;default text&quot;&gt;Bitte ausw&auml;hlen&lt;/div&gt;
        &lt;div class=&quot;menu transition visible&quot; tabindex=&quot;-1&quot; style=&quot;display: block !important;&quot;&gt;
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
</section>
</body>
</html>
