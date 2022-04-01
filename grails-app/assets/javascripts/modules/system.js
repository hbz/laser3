
// modules/system.js

system = {

    go: function () {
        system.init()
    },

    init: function () {
        system.status()
    },

    status: function () {
        var socket = new SockJS(JSPC.vars.wsStompUrl)
        var client = webstomp.over(socket, { debug: false })

        client.connect({}, function() {
            client.subscribe('/topic/status', function(message) {
                var body = JSON.parse(message.body)
                if (body && body.status && body.status === 'ok') {
                    if (body.maintenance) {
                        $('#maintenance').removeClass('hidden')
                    } else {
                        $('#maintenance').addClass('hidden')
                    }
                    if (body.messages) {
                        $('#systemMessages').load( JSPC.vars.ajaxMessagesUrl, function() { $('#systemMessages').removeClass('hidden') })
                    } else {
                        $('#systemMessages').addClass('hidden').empty()
                    }
                }
            });
        });
    },

    // status_old: function () {
    //     $.ajax({
    //         url: JSPC.vars.ajaxStatusUrl,
    //         success: function (data) {
    //             if (data.status && data.status == 'ok') {
    //                 setTimeout(system.status, data.interval * 1000)
    //
    //                 if (data.maintenance) {
    //                     $('#maintenance').removeClass('hidden')
    //                 } else {
    //                     $('#maintenance').addClass('hidden')
    //                 }
    //                 if (data.messages) {
    //                     $('#systemMessages').load( JSPC.vars.ajaxMessagesUrl, function() { $('#systemMessages').removeClass('hidden') })
    //                 } else {
    //                     $('#systemMessages').addClass('hidden').empty()
    //                 }
    //             }
    //         }
    //     })
    // },

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