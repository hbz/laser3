
// modules/system.js

system = {

    go: function () {
        system.init()
    },

    init: function () {
        system.status()
    },

    status: function () {
        window.addEventListener('beforeunload', function(event) {
            if (eventSource) {
                eventSource.close()
            }
        })

        var eventSource = new EventSource( JSPC.vars.sse.status )

        eventSource.onmessage = function(event) {
            if (event.data) {
                var data = JSON.parse(event.data)
                console.log( data )
                if (data && data.status && data.status === 'ok') {
                    if (data.maintenance) {
                        $('#maintenance').removeClass('hidden')
                    } else {
                        $('#maintenance').addClass('hidden')
                    }
                    if (data.messages) {
                        $('#systemMessages').load( JSPC.vars.ajax.openMessages, function() { $('#systemMessages').removeClass('hidden') })
                    } else {
                        $('#systemMessages').addClass('hidden').empty()
                    }
                }
            }
        }
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