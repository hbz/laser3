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

<h3 class="ui dividing header">Toggle-Button in einer Definitionsliste</h3>
<div class="la-inline-lists">
    <div class="ui card">
        <div class="content">
            <dl>
                <dt class="control-label">Status</dt>
                <dd><span><a href="#" id="com.k_int.kbplus.Subscription:11636:status" class="xEditableManyToOne editable editable-click" data-value="com.k_int.kbplus.RefdataValue:103" data-pk="com.k_int.kbplus.Subscription:11636" data-type="select" data-name="status" data-source="/laser/ajax/sel2RefdataSearch/subscription.status?format=json&amp;oid=com.k_int.kbplus.Subscription%3A11636&amp;constraint=removeValue_deleted" data-url="/laser/ajax/genericSetRel" data-emptytext="Bearbeiten">Aktiv</a></span></dd>
                <dd class="la-js-editmode-container"><a role="button"
                                                        data-content="Wert wird nicht vererbt"
                                                        class="ui icon mini button la-audit-button la-popup-tooltip la-delay"
                                                        href="#"
                                                        aria-labelledby="wcag_mlbbjc4mb"><i aria-hidden="true" class="icon la-js-editmode-icon la-thumbtack slash"></i></a>
                </dd>
            </dl>

        </div>
    </div>
</div>

<r:script>
$('.meinToggleButton').click( function () {
    $(this).toggleClass('blue');
})
</r:script>