// module: assets/javascripts/modules/textareaHeight.js

textareaHeight = {

    go: function () {
        $(function() {
            textareaHeight.init();
        });
    },

    init: function () {
        console.log('textareaHeight.init()')

        $('.la-textarea-resize-vertical').each(function() {
            if (this.scrollHeight > 0) {
                $(this).height(this.scrollHeight)
            }
        });
        $('.la-textarea-resize-vertical').on('keyup keypress', function() {
            $(this).height(0);
            $(this).height(this.scrollHeight);
        });
    }
}

JSPC.modules.add( 'textareaHeight', textareaHeight );