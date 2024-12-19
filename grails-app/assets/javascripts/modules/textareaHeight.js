// module: assets/javascripts/modules/textareaHeight.js

textareaHeight = {

    go: function () {
        $(function() {
            textareaHeight.init('body');
        });
    },

    init: function (ctxSel) {
        console.log('textareaHeight.init( ' + ctxSel + ' )')

        $(ctxSel + ' .la-textarea-resize-vertical').each(function() {
            if (this.scrollHeight > 0) {
                $(this).height(this.scrollHeight)
            }
            $(this).on('keyup keypress', function() {
                $(this).height(0);
                $(this).height(this.scrollHeight);
            });
        });
    }
}

JSPC.modules.add( 'textareaHeight', textareaHeight );