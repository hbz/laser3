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
        <i aria-hidden="true" class="trash alternate icon"></i>
    </g:link>
</p>

<section id="mySection">
    Focus lock demo:
    <div>
        <a href="#">first link</a>, <a href="#">second link</a>.<br/>
        <button tabindex="1">Button with tabindex</button>
        (first tabbable element)

        <div id="lock">
            Lock content
            <a href="#">first link</a>, <a href="#">second link</a>.<br/>
            <button tabindex="1">Button with tabindex</button>
            (second tabbable element)
            <a href="#">third link</a>, <a href="#">last link</a>.<br/>

            <button id="trigger-lock">TRIGGER LOCK!</button>
        </div>

        <input placeholder="inputOutside">
    </div>
</section>