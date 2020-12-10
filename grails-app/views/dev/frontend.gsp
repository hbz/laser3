<%@ page import="de.laser.License; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.UserSetting" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : Frontend for Developers</title>

    <asset:stylesheet src="chartist.css"/><asset:javascript src="chartist.js"/>%{-- dont move --}%

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
    .example {
        margin-top: 0em;
        padding-top: 2em;
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
    <h4 class="ui header">Icons und Farben von Objekten</h4>
    <p>Die Größe der Icons ist von der Umgebung abhängig (durch Größenangabe in em, die sich nach der Fontgröße des Elternelements richtet)</p>
    <i class="circular la-license icon"></i>
    <i class="circular la-package icon"></i>
    <i class="circular la-organisation icon"></i>
    <i class="circular la-platform icon"></i>
    <i class="circular la-journal icon"></i>
    <i class="circular la-database icon"></i>
    <i class="circular la-ebook icon"></i>
    <i class="circular la-book icon"></i>
    <div class="html ui top attached segment example">

        <div class="ui doubling five column grid">
            <div class="column"><a href="#"><i class="circular la-license icon"></i></a>circular la-license icon</div>

            <div class="column"><i class="circular la-package icon"></i>circular la-package icon</div>

            <div class="column"><i class="circular la-subscription icon"></i>circular la-subscription icon</div>

            <div class="column"><i class="circular la-organisation icon"></i>circular la-organisation icon</div>

            <div class="column"><i class="circular la-platform icon"></i>circular la-platform icon</div>

            <div class="column"><i class="circular la-journal icon"></i>circular la-journal icon</div>

            <div class="column"><i class="circular la-database icon"></i>circular la-database icon</div>

            <div class="column"><i class="circular la-ebook icon"></i>circular la-ebook icon</div>

            <div class="column"><i class="circular la-book icon"></i>circular la-book icon</div>

        </div>

        <div class="ui top attached label">Zusätzliche Icons in Laser</div>
    </div>
    <h2 class="ui dividing header">Form-Elemente<a class="anchor" id="form-elemente"></a></h2>
    <!-- Dropdowns -->
    <g:render template="frontend_dropdowns" />


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
            <br />

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
    &lt;g:if test="${ownobj instanceof License}"&gt;
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
    </dd><br />

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
    </a><br /><br />
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
    </a><br /><br />

    <h2 class="ui dividing header">Modale<a class="anchor" id="icons"></a></h2>
    <h4 class="ui header">Mehrere Modale auf einer Seite-Values in Formularfeldern werden mit Data-Attributen übertrage</h4>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Javascript in der View definiert die Data-Attribute</div>
        <pre aria-hidden="true">
Javascript:

JSPC.callbacks.modal.show.customerTypeModal = function(trigger) {
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

        <pre aria-hidden="true">        &ltasset:stylesheet src="chartist.css"/&gt;&ltasset:javascript src="chartist.js"/&gt;
        </pre>

    </div>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Chartis-Objekt mit Javascript aufrufen innerhalb r:script</div>
        <pre aria-hidden="true">        &ltscript&gt;
                new Chartist.Line('.ct-chart', data);
        &lt/script&gt;
        </pre>
    </div>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Beispiel LINE CHART - Legende oben mitte</div>
        <div class="ct-chart"></div>
    </div>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Beispiel PIE CHART- Legende rechts untereinander</div>
        <div class="ct-chart-pie"></div>
    </div>
    <div class="html ui top attached segment example">
        <div class="ui top attached label">Beispiel BAR CHART - Legende unten mitte</div>
        <div class="ct-chart-bar"></div>
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

<laser:script file="${this.getGroovyPageFileName()}">
    new Chartist.Line('.ct-chart', {
        labels: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'],
        series: [
            { "name": "Money A", "data": [12, 9, 7, 8, 5] },
            { "name": "Money B", "data": [2, 1, 3.5, 7, 3] },
            { "name": "Money C", "data": [1, 3, 4, 5, 6] }
        ]
    }, {
        fullWidth: true,
        chartPadding: {
            right: 40
        },
        plugins: [
            Chartist.plugins.legend({

                classNames: ['ct-dimmed', 'ct-hidden', ''],

            })
        ]
    });
</laser:script>
<laser:script file="${this.getGroovyPageFileName()}">
    new Chartist.Pie('.ct-chart-pie', {
        labels: ['Piece A', 'Piece B', 'Piece C', 'Piece D'],
        series: [20, 10, 30, 40]
    }, {
        showLabel: false,
        plugins: [
            Chartist.plugins.legend({
                className: 'ct-legend-inside'
            })
        ]
    });
</laser:script>
<laser:script file="${this.getGroovyPageFileName()}">
    new Chartist.Bar('.ct-chart-bar', {
        labels: ['First quarter of the year', 'Second quarter of the year', 'Third quarter of the year', 'Fourth quarter of the year'],
        series: [
            { "name": "Money A", "data": [60000, 40000, 80000, 70000] },
            { "name": "Money B", "data": [40000, 30000, 70000, 65000] },
            { "name": "Money C", "data": [8000, 3000, 10000, 6000] }
        ]
    }, {
        plugins: [
            Chartist.plugins.legend({
                position: 'bottom'
            })
        ]
    });
</laser:script>


</body>
</html>
