// module: assets/javascripts/modules/system.js

setTextareaHeight = {

    go: function () {
        setTextareaHeight.init()
    },

    init: function () {
        $(function() {
            $('.la-textarea-resize-vertical').each(function() {
                $(this).height(this.scrollHeight);
            });
        });
        $('.la-textarea-resize-vertical').on('keyup keypress', function() {
            $(this).height(0);
            $(this).height(this.scrollHeight);
        });
    }
}