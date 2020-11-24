
// modules/jsqtk.js

var jsqtk = {
    result: {},
    keys: [],
    idCounter: 0,

    info: function() {
        let evsCounter = 0
        let output = '--- EventListener found (min. 2 of type / element) ---\n\n'

        $.each($('*'), function(i, elem){
            jsqtk.idCounter = jsqtk.idCounter + 1

            let jsqtk_eid = 'jsqtk-' + jsqtk.idCounter
            let evs = $._data(elem, 'events')

            if (evs) {
                evsCounter = evsCounter + Object.keys(evs).length
                let isCandidate = false

                let info = elem.nodeName + ' [' + jsqtk_eid + ']'
                if (elem.id) { info = info + ' id="' + elem.id + '"' }
                if (elem.className) { info = info + ' class="' + elem.className + '"' }

                $.each(evs, function(ii, e){
                    $.each(e, function(ii, oo){
                        info = info + ' @ ' + oo.type + ':' + oo.guid

                        if (e.length > 1){
                            isCandidate = true

                            if ($(elem).attr('data-jsqtk-id')) {
                                jsqtk_eid = $(elem).attr('data-jsqtk-id')
                            } else {
                                $(elem).attr('data-jsqtk-id', jsqtk_eid)
                            }

                            if (! jsqtk.result[jsqtk_eid]) {
                                jsqtk.result[jsqtk_eid] = [elem]
                            }
                            jsqtk.result[jsqtk_eid].push(oo)
                        }
                    })
                })

                if (isCandidate) {
                    output = output + info + '\n'
                }
            }
        })

        let keys = Object.keys(jsqtk.result).sort(function(a, b){
            a.split('-')[1] < b.split('-')[1] ? 1 : -1
        })
        jsqtk.keys.push(keys)

        console.log('[jsqtk] event listener found: ' + evsCounter)
        console.log('[jsqtk] candidates found: ' + Object.keys(jsqtk.result).length)
        console.log('[jsqtk] candidate keys: ' + keys)

        // console.log( output )

        keys.forEach( function(k){
            console.log(jsqtk.result[k])
        })
    }
}

console.log('jsqtk loaded .. run jsqtk.info()')


