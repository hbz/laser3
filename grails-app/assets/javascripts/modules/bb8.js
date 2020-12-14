
// modules/bb8.js

bb8 = {

    go: function() {
        // console.log('bb8.go()')
        bb8.init('body')
    },

    init: function(ctxSel) {
        console.log('bb8.init( ' + ctxSel + ' )')

        $(ctxSel + " .la-js-remoteLink").click(function (event) {
            event.preventDefault();
            if (! $(this).hasClass('js-open-confirm-modal')) {
                bb8.ajax4remoteLink(this)
            }
        })

        $(ctxSel + " .la-js-remoteForm").unbind('submit').submit(function (event) {
            event.preventDefault();
            if (! $(this).hasClass('js-open-confirm-modal')) {
                bb8.ajax4remoteForm(this)
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
                $('#loadingIndicator').show()
                if (before) { eval(before) }
            }
        })
            .done(function (data) {
                $(update).empty()
                $(update).html(data)

                r2d2.initDynamicSemuiStuff(update)
                r2d2.initDynamicXEditableStuff(update)

                bb8.init(update)
                tooltip.init(update)
                a11yIcon.init(update)

                $("html").css("cursor", "auto")

                if (done) { eval(done) }
            })
            .fail(function () {
                if (fail) { eval(fail) }
            })
            .always(function () {
                $('#loadingIndicator').hide()
                if (always) { eval(always) }
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
                $('#loadingIndicator').show()
                if (before) { eval(before) }
            }
        })
            .done(function (data) {
                $(update).empty()
                $(update).html(data)

                r2d2.initDynamicSemuiStuff(update)
                r2d2.initDynamicXEditableStuff(update)

                bb8.init(update)
                tooltip.init(update)
                a11yIcon.init(update)

                $("html").css("cursor", "auto")

                if (done) { eval(done) }
            })
            .fail(function () {
                if (fail) { eval(fail) }
            })
            .always(function () {
                $('#loadingIndicator').hide()
                if (always) { eval(always) }
            });
    },

    ajax4SimpleModalFunction : function (cssId, url, callDynPostFunc) {
        console.log("bb8.ajaxSimpleModalFunction( " + cssId + ", " + url + ", " + callDynPostFunc + " )")

        return function () {
            $.ajax({
                url: url,
                success: function (result) {
                    $("#dynamicModalContainer").empty();
                    $(cssId).remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal({
                        onVisible: function () {
                            r2d2.initDynamicSemuiStuff(cssId);
                            r2d2.initDynamicXEditableStuff(cssId);

                            if (callDynPostFunc) {
                                JSPC.callbacks.dynPostFunc()
                            }
                        }
                    }).modal('show');
                },
                error: function (request, status, error) {
                    alert(request.status + ": " + request.statusText);
                }
            });
        }
    }
}
