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
    <a  role="button" class="ui icon button la-audit-button la-popup-tooltip la-delay" href='' data-content="4 5 6">
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

<a  role="button" class="ui icon button la-audit-button la-popup-tooltip la-delay" href='https://www.spiegel.de' data-content="10 11 12">
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