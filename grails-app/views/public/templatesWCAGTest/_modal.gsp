<%@ page import="de.laser.helper.Icons" %>
<p class="la-clear-before">
    <g:link controller="public"
            id="trigger-lock"
            action="wcagTest"
            params=""
            data-content="Hier kommt der Tooltip rein"
            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.function", args: ['Button auf der YODA/FRONTENDSEITE'])}"
            data-confirm-term-how="delete"
            class="ui icon negative button js-open-confirm-modal la-popup-tooltip la-delay"
            role="button">
        <i aria-hidden="true" class="${Icons.CMD_DELETE}"></i>
    </g:link>
</p>
