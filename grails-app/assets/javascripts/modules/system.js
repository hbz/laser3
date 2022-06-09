
// modules/system.js

system = {

    go: function () {
        system.init()
    },

    init: function () {
        system.status()
    },

    status: function () {
        var socket = new SockJS(JSPC.vars.ws.stompUrl)
        var client = webstomp.over(socket, { debug: true })
        var subscription = function() {
            client.subscribe(JSPC.vars.ws.topicStatusUrl, function(message) {
                var body = JSON.parse(message.body)
                console.log( message )
                console.log( body )
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
        }
        client.connect({}, subscription )
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