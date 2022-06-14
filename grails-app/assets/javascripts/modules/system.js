
// modules/system.js

system = {

    go: function () {
        system.init()
    },

    init: function () {
        system.status()
    },

    status: function () {
        let eventSource

        window.addEventListener('beforeunload', function(event) {
            if (eventSource) {
                eventSource.close()
            }
        })
        eventSource = new EventSource( JSPC.vars.sse.status )

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
        eventSource.onopen = function(event) {
            console.log( 'EventSource open: ' )
            console.log( event )
        }
        eventSource.onclose = function(event) {
            console.log( 'EventSource close: ' )
            console.log( event )
        }
        eventSource.onerror = function(event) {
            console.log( 'EventSource error: ' )
            console.log( event )
            eventSource.close()
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