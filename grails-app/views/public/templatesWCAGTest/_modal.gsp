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
<style>

#js-modal {
    border: 20px solid green;
    background-color: #EFE;
}

#js-modal.locked {
    border: 20px solid red;
    background-color: #FEE;
}
</style>
<section>
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

<laser:script file="${this.getGroovyPageFileName()}">
    function createLock() {
        var locked = false;
        var lock = document.getElementById('js-modal');


        function setStyle() {
            lock.classList[locked ? 'add' : 'remove']('locked');
        }

        function triggerLock() {
            locked = !locked;
            if (locked) {
                focusLock.on(lock);
            } else {
                focusLock.off(lock);
            }
            setStyle();
        }

        setStyle();
        return triggerLock;
    };

    $('.js-open-confirm-modal').click(createLock())
</laser:script>