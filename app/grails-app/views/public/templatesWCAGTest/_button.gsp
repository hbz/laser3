<h3 class="ui dividing header">Link funktioniert als Button mit Tooltip</h3>
<a  role="button" class="ui icon mini button la-audit-button la-popup-tooltip la-delay" href='https://www.spiegel.de' data-content="Das ist der Inhalt des Tooltips">
    <i  class="icon thumbtack la-js-editmode-icon"></i>
</a>
<h3 class="ui dividing header">Link funktioniert als Button ohne Tooltip</h3>
<g:link aria-label="Das ist eine Beschreibung für den Accessibility Tree" controller="dev" action="frontend" params="" class="ui icon positive button">
    <i aria-hidden="true" class="checkmark icon"></i>
</g:link>


<h3 class="ui dividing header">Button mit Text und für den Accessibility-Tree verstecktem Icon</h3>
<button class="ui  button blue"> Bezeichnung
        <i class="cocktail icon"></i>
        <span class="ui circular label">111</span>
</button>