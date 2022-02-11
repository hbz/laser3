
// modules/sys.js

sys = {

    go: function () {
        sys.init()
    },

    init: function () {
        sys.status()
    },

    status: function () {
        $.ajax({
            url: JSPC.vars.ajaxStatusUrl,
            success: function (data) {
                if (data.status && data.status == 'ok') {
                    setTimeout(sys.status, data.interval * 1000)

                    if (data.maintenance) {
                        $('#maintenance').removeClass('hidden')
                    } else {
                        $('#maintenance').addClass('hidden')
                    }
                    if (data.messages) {
                        $('#systemMessages').load( JSPC.vars.ajaxMessagesUrl, function() { $('#systemMessages').removeClass('hidden') })
                    } else {
                        $('#systemMessages').addClass('hidden').empty()
                    }
                }
            }
        })
    },

    profiler: function (uri) {
        $.ajax({
            url: JSPC.vars.ajaxProfilerUrl,
            data: {uri: uri},
            success: function (data) {
                var $sp = $('#system-profiler')
                if ($sp) {
                    if (data.delta > 0) {
                        $sp.removeClass('hidden').find('span').empty().append(data.delta + ' ms')
                    }
                }
            }
        })
    }
}