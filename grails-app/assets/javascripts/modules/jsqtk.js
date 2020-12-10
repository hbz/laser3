
// modules/jsqtk.js

jsqtk = {

    id_keys: [],
    id_result: [],

    el_blacklist: [],
    el_keys: [],
    el_result: {},
    el_resultCounter: {},
    el_idCounter: 0,

    info: function (id) {
        console.log('jsqtk.info()')
        let elem = $('*[data-jsqtk-id="jsqtk-' + id + '"]')

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
        $.each(jsqtk.el_keys, function (i, e) {
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

        $.each($('[id]'), function (i, elem) {
            let id = $(elem).attr('id')
            if ($.inArray(id, jsqtk.id_keys) < 0) {
                jsqtk.id_keys.push(id)
            } else {
                jsqtk.id_result.push(id)
            }
        })

        let evsCounter = 0

        jsqtk.el_result = {}
        jsqtk.el_resultCounter = {}

        $.each($('*'), function (i, elem) {
            jsqtk.el_idCounter = jsqtk.el_idCounter + 1

            let jsqtkEid = 'jsqtk-' + jsqtk.el_idCounter
            let evs = $._data(elem, 'events')

            if (evs) {
                evsCounter = evsCounter + Object.keys(evs).length

                $.each(evs, function (ii, elist) {
                    if ($.inArray(ii, jsqtk.el_blacklist) < 0 && elist.length > 1) {
                        let checkList = jsqtk._check(elist)

                        if (checkList.length > 0) {
                            if ($(elem).attr('data-jsqtk-id')) {
                                jsqtkEid = $(elem).attr('data-jsqtk-id')
                            } else {
                                $(elem).attr('data-jsqtk-id', jsqtkEid)
                            }

                            if (!jsqtk.el_result[jsqtkEid]) {
                                jsqtk.el_result[jsqtkEid] = [elem]
                            }
                            jsqtk.el_result[jsqtkEid][ii] = checkList

                            if (!jsqtk.el_resultCounter[ii]) {
                                jsqtk.el_resultCounter[ii] = 0
                            }
                            jsqtk.el_resultCounter[ii]++
                        }
                    }
                })
            }
        })

        let keys = Object.keys(jsqtk.el_result).sort(function (a, b) {
            a.split('-')[1] < b.split('-')[1] ? 1 : -1
        })
        jsqtk.el_keys.push(keys)

        console.groupCollapsed('jsqtk .. el doublets' , jsqtk.el_resultCounter)
        console.log('- event listener found overall: ' + evsCounter)
        if (jsqtk.el_blacklist.length > 0) {
            console.log('- el_blacklist: ' + jsqtk.el_blacklist)
        }
        if (keys.length > 0) {
            let history = []
            jsqtk.el_keys.forEach(function(i){ history.push(i.length) })

            console.log('- data-jsqtk-ids in use: ' + keys)
            console.log('- history of used data-jsqtk-ids: [' + history + ']')
            console.log('- current elements with event listener doublets: ' + Object.keys(jsqtk.el_result).length)
            keys.forEach(function (k) {
                console.log(jsqtk.el_result[k])
            })
        }
        console.groupEnd()

        if (jsqtk.id_result.length > 0) {
            console.groupCollapsed('jsqtk .. id doublets', jsqtk.id_result.length)
            jsqtk.id_result.forEach(function (k) {
                console.log($('[id="' + k + '"]'))
            })
            console.groupEnd()
        }
    }
}