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
            class="ui icon negative button la-modern-button js-open-confirm-modal la-popup-tooltip la-delay"
            role="button">
        <i aria-hidden="true" class="trash alternate outline icon"></i>
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
            class="ui icon negative button la-modern-button js-open-confirm-modal la-popup-tooltip la-delay"
            role="button">
        <i aria-hidden="true" class="trash alternate outline icon"></i>
    </g:link>
</div>

<%-- ERMS-2082 --%>

<div class="html ui top attached segment example">
    <div class="ui top attached label">Link, der den AJAX-Contoler aufruft und  als Button funktioniert (daß heißt, eine Aktion ausführt)</div>
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