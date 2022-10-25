// module: assets/javascripts/modules/a11y/modules/modal.js

a11yModal = {

    go: function (config) {
        a11yModal._loopFocus(config)
    },

    /**
     * Source https://medium.com/@im_rahul/focus-trapping-looping-b3ee658e5177
     *
     * A stateless keyboard utility to -
     * - Trap focus,
     * - Focus the correct Element
     * @param config (
     *   el: HTMLElement. The Parent element, within which the focus should be trapped
     *   focusElement: <Optional> HTMLElement. If Not provided, focus is put to the first focusable element
     * )
     * @return {Function} Function. The cleanup function. To undo everything done for handling A11Y
     */

    _loopFocus: function (config) {
        if (!config) {
            throw new Error('Could not initialize focus-trapping - Config Missing');
        }
        const el = config.el,
            escCallback = config.escCallback,
            focusElement = config.focusElement;

        if (!el) {
            console.log('Could not initialize focus-trapping - Element Missing');
            return;
            //throw new Error('Could not initialize focus-trapping - Element Missing');
        }
        if (escCallback && !(escCallback instanceof Function)) {
            throw new Error('Could not initialize focus-trapping - `config.escCallback` is not a function');
        }

        const FOCUSABLE_ELEMENT_SELECTORS = 'a[href], area[href], input:not([type=hidden]):not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, [tabindex="0"], [contenteditable]';

        const KEY_CODE_MAP = {
            TAB: 9
        };

        const focusableElements = el.querySelectorAll(FOCUSABLE_ELEMENT_SELECTORS);
        let keyboardHandler;

        //There can be containers without any focusable element
        if (focusableElements.length > 0) {
            const firstFocusableEl = focusableElements[0],
                lastFocusableEl = focusableElements[focusableElements.length - 1],
                elementToFocus = focusElement ? focusElement : firstFocusableEl;
            elementToFocus.focus();

            keyboardHandler = function keyboardHandler(e) {
                if (e.keyCode === KEY_CODE_MAP.TAB) {
                    //Rotate Focus
                    if (e.shiftKey && document.activeElement === firstFocusableEl) {
                        e.preventDefault();
                        lastFocusableEl.focus();
                    } else if (!e.shiftKey && document.activeElement === lastFocusableEl) {
                        e.preventDefault();
                        firstFocusableEl.focus();
                    }
                }
            };
            el.addEventListener('keydown', keyboardHandler);
        }

        //The cleanup function. Put future cleanup tasks inside this.
        return function cleanUp() {

            if (keyboardHandler) {
                el.removeEventListener('keydown', keyboardHandler);
            }
        };
    }
}

JSPC.modules.add( 'a11yModal', a11yModal );