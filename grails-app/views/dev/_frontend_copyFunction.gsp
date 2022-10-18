<h2 class="ui dividing header">Kopiertfunktion<a class="anchor" id="copy"></a>
</h2>


<div class="ui js-copyTriggerParent item contact-details" style="display: flex;">
    <div style="display: flex">
        <i class="ui icon envelope outline la-list-icon la-js-copyTriggerIcon"></i>

        <div class="content la-space-right js-copyTrigger la-popup-tooltip la-delay" data-position="top center"
             data-content="Klicke, um zu kopieren">
            <span class="js-copyTopic">mailto:example@example.de</span>
        </div>
    </div>
</div>


<div class="ui list js-copyTriggerParent">
    <div class="item">
        <span class="ui small basic image label js-copyTrigger js-copyTopic la-popup-tooltip la-delay"
              data-position="top center" data-content="Klicke, um zu kopieren">
            <i class="la-copy icon la-js-copyTriggerIcon"></i>

            zdb: <div class="detail">2756942-1</div>
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
            &lt;div class="ui list <strong>js-copyTriggerParent</strong>">
                &lt;div class="item">
                    &lt;span class="ui small basic image label <strong>js-copyTrigger js-copyTopic</strong> la-popup-tooltip la-delay"
                          data-position="top center" data-content="Klicke, um zu kopieren">
                        &lt;i class="la-copy icon <strong>la-js-copyTriggerIcon</strong>" aria-hidden="true">&lt;/i>
                        zdb: &lt;div class="detail">2756942-1&lt;/div>
                     &lt;/span>
                &lt;/div>
            &lt;/div>
        </pre>
    </div>
</div>

