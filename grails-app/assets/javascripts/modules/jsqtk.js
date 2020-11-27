
// modules/jsqtk.js

var jsqtk = {
    result: {},
    keys: [],
    idCounter: 0,
    blackList: [],

    info: function (jsqtk_id) {
        console.log('jsqtk.info()')
        let elem = $('*[data-jsqtk-id="' + jsqtk_id + '"]')

        if (elem) {
            console.log(elem)
            let evs = $._data(elem[0], 'events')
            $.each(evs, function (i, elist) {
                $.each(elist, function (ii, oo) {
                    console.log(oo)
                })
            })
        }
    },

    history: function () {
        console.log('jsqtk.history()')
        $.each(jsqtk.keys, function (i, e) {
            console.log(e)
        })
    },

    _check: function (events) {
        let result = []

        for (let i=0; i<events.length; i++) {
            for (let j=i+1; j<events.length; j++) {
                if (events[i].handler.toString() == events[j].handler.toString()) {
                    if ($.inArray(events[i].handler, result) < 0) {
                        result.push(events[i].handler)
                    }
                    if ($.inArray(events[j].handler, result) < 0) {
                        result.push(events[j].handler)
                    }
                }
            }
        }
        return result
    },

    go: function () {
        console.log('jsqtk.go()')
        let evsCounter = 0

        $.each($('*'), function (i, elem) {
            jsqtk.idCounter = jsqtk.idCounter + 1

            let jsqtkEid = 'jsqtk-' + jsqtk.idCounter
            let evs = $._data(elem, 'events')

            if (evs) {
                evsCounter = evsCounter + Object.keys(evs).length

                $.each(evs, function (ii, elist) {
                    if ($.inArray(ii, jsqtk.blackList) < 0 && elist.length > 1) {
                        let checkList = jsqtk._check(elist)

                        if (checkList.length > 0) {
                            if ($(elem).attr('data-jsqtk-id')) {
                                jsqtkEid = $(elem).attr('data-jsqtk-id')
                            } else {
                                $(elem).attr('data-jsqtk-id', jsqtkEid)
                            }

                            if (!jsqtk.result[jsqtkEid]) {
                                jsqtk.result[jsqtkEid] = [elem]
                            }

                            jsqtk.result[jsqtkEid][ii] = checkList
                        }
                    }
                })
            }
        })

        let keys = Object.keys(jsqtk.result).sort(function (a, b) {
            a.split('-')[1] < b.split('-')[1] ? 1 : -1
        })
        jsqtk.keys.push(keys)

        console.group('¯\\_(ツ)_/¯ .. jsqtk')
        console.log('event listeners found overall: ' + evsCounter)
        if (jsqtk.blackList.length > 0) {
            console.log('blacklist: ' + jsqtk.blackList)
        }
        if (keys.length > 0) {
            console.log('used data-jsqtk-ids: ' + keys)
            console.groupCollapsed('elements with doublets: ' + Object.keys(jsqtk.result).length)
            keys.forEach(function (k) {
                console.log(jsqtk.result[k])
            })
            console.groupEnd()
        }
        console.groupEnd()
    }
}