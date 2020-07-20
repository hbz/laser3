
// submodules/bb8.js

bb8 = {

    go: function() {

        bb8.init('body');
    },

    init: function(ctxSel) {

        $(ctxSel + " .la-js-remoteLink").click(function (event) {
            event.preventDefault();
            if (! $(this).hasClass('js-open-confirm-modal')) {
                bb8.ajax4remoteLink(this);
            }
        })

        $(ctxSel + " .la-js-remoteForm").unbind('submit').submit(function (event) {
            event.preventDefault();
            if (! $(this).hasClass('js-open-confirm-modal')) {
                bb8.ajax4remoteForm(this);
            }
        })
    },

    ajax4remoteLink: function(elem) {

        var url     = $(elem).attr('href')
        var before  = $(elem).attr('data-before')       // before
        var done    = $(elem).attr('data-done')         // onSuccess-Trigger
        var fail    = $(elem).attr('data-fail')
        var always  = $(elem).attr('data-always')       // onComplete
        var update  = '#' + $(elem).attr('data-update')

        $.ajax({
            url: url,
            beforeSend: function (xhr) {
                if (before) {
                    eval(before)
                }
            }
        })
            .done(function (data) {
                $(update).empty()
                $(update).html(data)

                bb8.init(update);
                tooltip.go();
                a11yMenu.go();

                if (done) {
                    eval(done)
                }
            })
            .fail(function () {
                if (fail) {
                    eval(fail)
                }
            })
            .always(function () {
                if (always) {
                    eval(always)
                }
            });
    },

    ajax4remoteForm: function(elem) {

        var url     = $(elem).attr('action')
        var before  = $(elem).attr('data-before')       // before
        var done    = $(elem).attr('data-done')         // onSuccess-Trigger
        var fail    = $(elem).attr('data-fail')
        var always  = $(elem).attr('data-always')       // onComplete
        var data    = $(elem).serialize()
        var update  = '#' + $(elem).attr('data-update')

        $.ajax({
            url: url,
            data : data,
            beforeSend: function (xhr) {
                if (before) {
                    eval(before)
                }
            }
        })
            .done(function (data) {
                $(update).empty()
                $(update).html(data)

                bb8.init(update);
                //tooltip.go();
                //a11yMenu.go();

                if (done) {
                    eval(done)
                }
            })
            .fail(function () {
                if (fail) {
                    eval(fail)
                }
            })
            .always(function () {
                if (always) {
                    eval(always)
                }
            });
    }
}
