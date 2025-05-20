<%@ page import="de.laser.ui.Icon" %>
<h2 class="ui dividing header">Kopierfunktion<a class="anchor" id="copy"></a>
</h2>


<div class="ui list">
    <div class="item js-copyTriggerParent">
        <span class="js-copyTrigger js-copyTopic la-popup-tooltip"
              data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
            <i class="${Icon.SYM.EMAIL} la-list-icon la-js-copyTriggerIcon"></i>
            mailto:example@example.de
        </span>
    </div>
</div>

<div class="ui list">
    <div class="item js-copyTriggerParent">
        <span class="ui small basic image label js-copyTrigger la-popup-tooltip"
              data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
            <i class="la-copy grey icon la-js-copyTriggerIcon"></i>

            zdb: <div class="detail js-copyTopic">2756942-1</div>
        </span>
    </div>
    <div class="item js-copyTriggerParent">
        <span class="ui small basic image label js-copyTrigger  la-popup-tooltip"
              data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
            <i class="la-copy grey icon la-js-copyTriggerIcon"></i>

            zdb: <div class="detail js-copyTopic">123456</div>
        </span>
    </div>
</div>


<div class="html ui top attached segment example">
    <ul>
        <li>Klasse <strong>"js-copyTriggerParent"</strong>: kommt an das umgebene Div</li>
        <li>Klasse <strong>"js-copyTopic"</strong>: kommt an das Element, dessen Inhalt kopiert werden soll </li>
        <li>Klasse <strong>"js-copyTrigger"</strong>: kommt an das Element, das gedrückt wird, um zu kopieren </li>
    </ul>
    <br/>

    <div class="ui top attached label">Markup für Kopiertfunktion von E-Mails-Adressen, Telefonnummern und Identifieren</div>
</div>

<div class="annotation transition visible">
    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;div class="ui list">
                &lt;div class="item <strong>js-copyTriggerParent</strong>">
                    &lt;span class="ui small basic image label <strong>js-copyTrigger</strong> la-popup-tooltip"
                          data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
                        &lt;i class="la-copy grey icon <strong>la-js-copyTriggerIcon</strong>" aria-hidden="true">&lt;/i>
                        zdb: &lt;div class="detail <strong>js-copyTopic</strong>">2756942-1&lt;/div>
                     &lt;/span>
                &lt;/div>
            &lt;/div>
        </pre>
    </div>
</div>

