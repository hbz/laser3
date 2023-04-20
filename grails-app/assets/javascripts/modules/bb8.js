// module: assets/javascripts/modules/bb8.js

bb8 = {

    go: function() {
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

        let url     = $(elem).attr('href')
        let before  = $(elem).attr('data-before')       // before
        let done    = $(elem).attr('data-done')         // onSuccess-Trigger
        let fail    = $(elem).attr('data-fail')
        let always  = $(elem).attr('data-always')       // onComplete
        let update  = '#' + $(elem).attr('data-update')

        $.ajax({
            url: url,
            beforeSend: function (xhr) {
                $('#globalLoadingIndicator').show()
                if (before) { eval(before) }
            }
        })
            .done(function (data) {
                $(update).empty()
                $(update).html(data)

                r2d2.initDynamicUiStuff(update)
                r2d2.initDynamicXEditableStuff(update)

                bb8.init(update)
                tooltip.init(update)
                a11yIcon.init(update)
                responsiveTables.init('body')

                $("html").css("cursor", "auto")

                if (done) { eval(done) }
            })
            .fail(function () {
                if (fail) { eval(fail) }
            })
            .always(function () {
                $('#globalLoadingIndicator').hide()
                if (always) { eval(always) }
            });
    },

    ajax4remoteForm: function(elem) {

        let url     = $(elem).attr('action')
        let before  = $(elem).attr('data-before')       // before
        let done    = $(elem).attr('data-done')         // onSuccess-Trigger
        let fail    = $(elem).attr('data-fail')
        let always  = $(elem).attr('data-always')       // onComplete
        let data    = $(elem).serialize()
        let update  = '#' + $(elem).attr('data-update')

        $.ajax({
            url: url,
            data : data,
            beforeSend: function (xhr) {
                $('#globalLoadingIndicator').show()
                if (before) { eval(before) }
            }
        })
            .done(function (data) {
                $(update).empty()
                $(update).html(data)

                r2d2.initDynamicUiStuff(update)
                r2d2.initDynamicXEditableStuff(update)

                bb8.init(update)
                tooltip.init(update)
                a11yIcon.init(update)
                responsiveTables.init('body')

                $("html").css("cursor", "auto")

                if (done) { eval(done) }
            })
            .fail(function () {
                if (fail) { eval(fail) }
            })
            .always(function () {
                $('#globalLoadingIndicator').hide()
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
                            r2d2.initDynamicUiStuff(cssId);
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

JSPC.modules.add( 'bb8', bb8 );
