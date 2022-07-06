<h2 class="ui dividing header">Confimation Modal<a class="anchor" id="icons"></a></h2>
<h4 class="ui header">Einfacher Link</h4>
<div class="html ui top attached segment example">
    <div class="ui top attached label"></div>
    <g:link controller="dev"
            action="frontend"
            params=""
            data-content="Hier kommt der Tooltip rein"
            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
            data-confirm-term-how="delete"
            class="ui icon negative button la-modern-button js-open-confirm-modal la-popup-tooltip la-delay"
            role="button">
        <i aria-hidden="true" class="trash alternate outline icon"></i>
    </g:link>
    <ul>
        <li>Damit aus dem Tabellen-Header ein Label wird, wenn Tabelle zusammengeschoben
            <ol>
                <li>Die Tabelle muss die Klasse <strong>'la-js-responsive-table'</strong> bekommen</li>
                <li>Alle Tabellen-Spalten müssen einen <strong>Bezeichner</strong> bekommen, außer Icons, diese benötigen einen <strong>Tooltip</strong>
                </li>
            </ol>
        </li>
        <li><g:link controller="subscription" action="show">zum Beispiel hier verwendet</g:link></li>
    </ul>
    <br/>
</div>
<div class="annotation transition visible" style="display: none;">
    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;td&gt;
            01.01.2022
            &lt;br&gt;
            <strong>&lt;span&gt; class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label')}:">&lt;/span&gt;</strong>
            &lt;/td&gt;
            .
            .
            .
        </pre>
    </div>
</div>

<%-- ERMS-2082 --%>
<h4 class="ui header">Inhalt der Nachricht per Ajax: AjaxController.genericDialogMessage()</h4>
<div class="html ui top attached segment example">
    <div class="ui top attached label"></div>

    <g:link controller="dev"
            action="frontend"
            extaContentFlag="false"
            params=""
            data-content="Hier kommt der Tooltip rein"
            data-confirm-messageUrl="${createLink(controller:'ajax', action:'genericDialogMessage', params:[template:'abc'])}"
            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
            data-confirm-term-how="delete"
            class="ui icon negative button la-modern-button js-open-confirm-modal la-popup-tooltip la-delay"
            role="button">
        <i aria-hidden="true" class="trash alternate outline icon"></i>
    </g:link>
</div>

<%-- ERMS-2082 --%>
<h4 class="ui header">Link (Button), der den AJAX-Contoler aufruft</h4>
<div class="html ui top attached segment example">
    <div class="ui top attached label"></div>
    <laser:remoteLink class="ui icon negative button la-modern-button js-open-confirm-modal la-popup-tooltip la-delay"
                      controller="dev"
                      action="frontend"
                      params=""
                      id=""
                      data-content="Hier kommt der Tooltip rein"
                      data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
                      data-confirm-term-how="delete"
                      role="button">

        <i aria-hidden="true" class="trash alternate outline icon"></i>
    </laser:remoteLink>
</div>

<%-- Confirmation modal in case of selecting special option in  dropdown--%>
<h4 class="ui header">Auswahl einer bestimmten Option im X-Editable Dropdown</h4>
<div class="html ui top attached segment example">
    <div class="ui top attached label"></div>
    <a href="#" id="ezb_server_access" class="xEditableManyToOne js-open-confirm-modal-xeditable editable editable-click" data-onblur="ignore" data-pk="de.laser.OrgSetting:3103" data-confirm-term-how="ok" data-confirm-tokenmsg="Wollen Sie wirklich der Weitergabe der Lizenzdaten Ihrer Einrichtung an die EZB zustimmen?" data-confirm-value="de.laser.RefdataValue:1" data-type="select" data-name="rdValue" data-source="/ajax/select2RefdataSearch/y.n?format=json&amp;oid=de.laser.OrgSetting%3A3103" data-url="/ajax/genericSetData" data-emptytext="Bearbeiten" style="background-color: rgb(241, 235, 229);">Ja</a>
    <laser:script file="${this.getGroovyPageFileName()}">
        $('body #ezb_server_access').editable('destroy').editable({
            tpl: '<select class="ui dropdown"></select>'
        }).on('shown', function() {
        r2d2.initDynamicSemuiStuff('body');

        $('.ui.dropdown')
            .dropdown({
            clearable: true
        })
        ;
        }).on('hidden', function() {
        });
    </laser:script>
    <ul>
        <li>Damit aus dem X-Editable-Element ein Confirmation Modal aufgerufen wird, müssen folgende zusätzlichen Attribute zum x-Editable hinzugefügt werden
            <ol>
                <li>das x-editable braucht eine HTML-ID. Sie wird benutzt, um Javascript mit dem x-Editable zu verbinden, zum Beispiel <strong>id="oamonitor_server_access"</strong></li>
                <li>data_confirm_tokenMsg="HIER KOMMT DIE FRAGE IM CONFIRMATION MODAL REIN"</li>
                <li>data_confirm_term_how="ok"</li>
                <li>cssClass="js-open-confirm-modal-xeditable"</li>
                <li>data_confirm_value="HIER KOMMT DAS VALUE, DASS DAS MODAL AUSLÖSEN SOLL REIN", zum Beispiel <pre aria-hidden="true">&#x0024;{RefdataValue.class.name}:&#x0024;{RDStore.YN_YES.id}</pre> </li>
            </ol>
        </li>
    </ul>
    <br/>
</div>
<div class="annotation transition visible" style="display: none;">
    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            $('body #ezb_server_access').editable('destroy').editable({
            .
            .
            .
            }).on('shown', function() {
            <strong>r2d2.initDynamicSemuiStuff('body');</strong>

                $('.ui.dropdown')
                    .dropdown({
                    clearable: true
                });
            .
            .
            .
        </pre>
    </div>
</div>
