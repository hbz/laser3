
// submodules/bb8.js

bb8 = {

    go: function() {
        bb8.init('body');
    },

    init: function(ctxSel) {
        $(ctxSel + " .la-js-remoteLink").click(function (event) {

            event.preventDefault();
            if (! $(this).hasClass('js-open-confirm-modal')) {

                bb8.ajax(this);
            }
        })

    },

    ajax: function(elem) {

        var url = $(elem).attr('href')
        var before = $(elem).attr('data-before')      // before
        var done = $(elem).attr('data-done')          // onSuccesstrigger
        var fail = $(elem).attr('data-fail')
        var always = $(elem).attr('data-always')      // onComplete
        var update = '#' + $(elem).attr('data-update')

        $.ajax({
            url: url,

            beforeSend: function (xhr) {
                if (before) {
                    //console.log('before')
                    eval(before)
                }
            }
        })
            .done(function (data) {
                //console.log('done')
                $(update).empty()
                $(update).html(data)
                tooltip.go();
                a11yMenu.go();

                if (done) {
                    eval(done)
                }
            })
            .fail(function () {
                //console.log('fail')
                if (fail) {
                    eval(fail)
                }
            })
            .always(function () {
                //console.log('always')
                if (always) {
                    eval(always)
                }
            });
    }
}
