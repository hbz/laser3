<%@ page import="de.laser.ui.Icon" %>
<p class="la-clear-before">
    <g:link controller="public"
            id="trigger-lock"
            action="wcagTest"
            params=""
            data-content="Hier kommt der Tooltip rein"
            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
            data-confirm-term-how="delete"
            class="ui icon negative button js-open-confirm-modal la-popup-tooltip"
            role="button">
        <i aria-hidden="true" class="${Icon.CMD.DELETE}"></i>
    </g:link>
</p>
