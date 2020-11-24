var jsqtk = {
    result: {},
    counter: 0,

    info: function() {
        $.each($('*'), function(i, elem){
            let evs = $._data(elem, 'events')
            let jsqtk_eid = 'jsqtk-' + i

            if (evs) {
                $(elem).attr('data-jsqtk-id', jsqtk_eid)
                jsqtk.counter = jsqtk.counter + Object.keys(evs).length

                let info = elem.nodeName + ' [' + jsqtk_eid + ']'
                if (elem.id) {
                    info = info + ' id="' + elem.id + '"'
                }
                if (elem.className) {
                    info = info + ' class="' + elem.className + '"'
                }

                $.each(evs, function(ii, e){
                    $.each(e, function(ii, oo){
                        info = info + ' @ ' + oo.type + ' #' + oo.guid

                        if (e.length > 1){
                            if (! jsqtk.result[jsqtk_eid]) {
                                jsqtk.result[jsqtk_eid] = [elem]
                            }
                            jsqtk.result[jsqtk_eid].push(oo)
                        }
                    })

                })
                //console.log(info)
            }

        })

        console.log('jsqtk = jsEh found: ' + jsqtk.counter)
        console.log('jsqtk = candidates found: ' + Object.keys(jsqtk.result).length)
        console.log(jsqtk.result)
    }
}

console.log('jsqtk loaded .. run jsqtk.info()')