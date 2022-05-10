<h2 class="ui dividing header">Decksaver (An- und Ausschalten von Buttons auf Show-Seiten)<a class="anchor" id="form-elemente"></a></h2>
<div class="dropdown example">

    <div class="html ui top attached segment">
        <ul>
            <li>Cards, die keinen Inhalt haben, müssen ausgeschaltet werden:
                <ol>
                    <li>class <strong>'la-js-dont-hide-this-card'</strong> zu ins Markup einer Tabellen-Zelle</li>
                    <li>class <strong>'la-js-hideable'</strong> zu ins Markup einer Card</li>
                </ol>
            </li>
            <li><g:link controller="subscription" action="show">zum Beispiel hier verwendet</g:link></li>
        </ul>
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
            &lt;th&gt;${message(code:'default.value.label')}&lt;/th&gt;
            &lt;th&gt;${message(code:'property.table.paragraph')}&lt;/th&gt;
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