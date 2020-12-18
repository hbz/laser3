
// modules/a11y/modules/modal.js

a11yModal = {

    go: function () {
        a11yModal.initModal()
    },

    initModal: function () {
        console.log('a11yModal.initModal()')
        $('#js-open-confirm-modal').on("click",loopFocus({
            el: document.getElementById('js-modal'),
            focusElement: '',
            escCallback:''
        }));
    }
}