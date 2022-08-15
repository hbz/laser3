// module: assets/javascripts/modules/system.js

system = {

    go: function () {
        system.init()
    },

    init: function () {
        // TODO system.status()
    },

    status: function () {
        var socket = new SockJS(JSPC.vars.ws.stompUrl)
        var client = webstomp.over(socket, { debug: true })
        var subscription = function(frame) {
            console.log( frame )
            client.subscribe(JSPC.vars.ws.topicStatusUrl, function(message) {
                console.log( message )
                var body = JSON.parse(message.body)
                console.log( body )
                if (body && body.status && body.status === 'ok') {
                    if (body.maintenance) {
                        $('#maintenance').removeClass('hidden')
                    } else {
                        $('#maintenance').addClass('hidden')
                    }
                    if (body.messages) {
                        $('#systemMessages').load( JSPC.vars.ajax.openMessages, function() { $('#systemMessages').removeClass('hidden') })
                    } else {
                        $('#systemMessages').addClass('hidden').empty()
                    }
                }
            });
        }
        client.connect({}, subscription )
    },

    profiler: function (uri) {
        $.ajax({
            url: JSPC.vars.ajax.openProfiler,
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