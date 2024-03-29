// module: assets/javascripts/modules/system.js

system = {

    go: function () {
        system.init()
    },

    init: function () {
        // TODO system.status()
    },

    status: function () {
        let socket = new SockJS(JSPC.config.ws.stompUrl)
        let client = webstomp.over(socket, { debug: true })
        let subscription = function(frame) {
            console.log( frame )
            client.subscribe(JSPC.config.ws.topicStatusUrl, function(message) {
                console.log( message )
                let body = JSON.parse(message.body)
                console.log( body )
                if (body && body.status && body.status === 'ok') {
                    if (body.maintenance) {
                        $('#systemMaintenanceMode').removeClass('hidden')
                    } else {
                        $('#systemMaintenanceMode').addClass('hidden')
                    }
                    if (body.messages) {
                        $('#systemMessages').load( JSPC.config.ajax.openMessages, function() { $('#systemMessages').removeClass('hidden') })
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
            url: JSPC.config.ajax.openProfiler,
            data: {uri: uri},
            success: function (data) {
                let $sp = $('#system-profiler')
                if ($sp) {
                    if (data.delta > 0) {
                        $sp.removeClass('hidden').find('span').empty().append(data.delta + ' ms')
                    }
                }
            }
        })
    }
}

JSPC.modules.add( 'system', system );