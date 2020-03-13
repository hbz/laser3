<h3 class="ui dividing header">Link funktioniert als Button mit Tooltip</h3>
<a  role="button" class="ui icon mini button la-audit-button la-popup-tooltip la-delay" href='https://www.w3.org/' data-content="Das ist der Inhalt des Tooltips">
    <i  class="icon thumbtack la-js-editmode-icon"></i>
</a>
<h3 class="ui dividing header">Link funktioniert als Button ohne Tooltip</h3>
<g:link aria-label="Das ist eine Beschreibung für den Accessibility Tree" controller="public" action="wcagTest" params="" class="ui icon positive button">
    <i aria-hidden="true" class="checkmark icon"></i>
</g:link>


<h3 class="ui dividing header">Button mit Text und für den Accessibility-Tree verstecktem Icon</h3>
<button class="ui  button blue"> Bezeichnung
        <i class="cocktail icon"></i>
        <span class="ui circular label">111</span>
</button>

<h3 class="ui dividing header">Toggle-Button</h3>
<g:link  controller="public" action="wcagTest" class="ui  button blue meinToggleButton la-popup-tooltip la-delay" data-content="Das ist der Inhalt des Tooltips"> Bezeichnung
    <i class="cocktail icon"></i>
</g:link>

<r:script>
$('.meinToggleButton').click( function () {
    $(this).toggleClass('blue');
})
</r:script>