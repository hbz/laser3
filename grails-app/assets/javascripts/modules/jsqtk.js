
// modules/jsqtk.js

jsqtk = {

    id_keys: [],
    id_result: [],

    el_blacklist: [],
    el_keys: [],
    el_result: {},
    el_resultCounter: {},
    el_idCounter: 0,

    go: function () {

        jsqtk._checkIds()

        let elCounter = jsqtk._checkEventListeners()
        let currentKeys = jsqtk.el_keys[jsqtk.el_keys.length - 1]

        let tmp = []
        $.each(jsqtk.el_resultCounter, function (k, v) {
            tmp.push(k + ': ' + v)
        })

        console.groupCollapsed('jsqtk .. el doublets', tmp)
        console.log('- event listener found overall: ' + elCounter)

        if (jsqtk.el_blacklist.length > 0) {
            console.log('- el_blacklist: ' + jsqtk.el_blacklist)
        }
        if (currentKeys.length > 0) {
            let history = []
            jsqtk.el_keys.forEach(function (i){ history.push(i.length) })

            console.log('- history count of used data-jsqtk-ids: [' + history + ']')
            console.log('- currently used data-jsqtk-ids: ' + currentKeys)
            console.log('- currently found elements with event listener doublets: ' + Object.keys(jsqtk.el_result).length)
            currentKeys.forEach(function (k) {
                console.log(jsqtk.el_result[k])
            })
        }
        console.groupEnd()

        console.groupCollapsed('jsqtk .. id doublets', jsqtk.id_result.length)
        jsqtk.id_result.forEach(function (k) {
            let tmp = []
            $.each($('[id="' + k + '"]'), function (i, elem) {
                tmp.push(elem)
            })
            console.log(tmp)
        })
        console.groupEnd()
    },

    help: function() {
        console.log(
            'jsqtk.help()      -> returns this help \n' +
            '  \n' +
            'jsqtk.history()   -> returns existing history \n' +
            'jsqtk.info(id)    -> returns $(*[data-jsqtk-id="jsqtk-<id>"]) \n' +
            'jsqtk.forms()     -> returns $(forms) \n' +
            'jsqtk.headlines() -> returns $(h{1..6}) \n'
        )
    },

    history: function () {
        console.log('jsqtk.history()')
        $.each(jsqtk.el_keys, function (i, e) {
            console.log(e)
        })
    },

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

    forms: function() {
        console.log('jsqtk.forms()')
        $.each($('form'), function (i, elem) {
            console.log(elem)
        })
    },

    headlines: function() {
        console.log('jsqtk.headlines()')
        $.each($('h1,h2,h3,h4,h5,h6'), function (i, elem) {
            console.log(elem)
        })
    },

    // ----->

    _checkEventListeners: function () {
        let elCounter = 0

        jsqtk.el_result = {}
        jsqtk.el_resultCounter = {}

        $.each($('*'), function (i, elem) {
            jsqtk.el_idCounter = jsqtk.el_idCounter + 1

            let jsqtkEid = 'jsqtk-' + jsqtk.el_idCounter
            let evs = $._data(elem, 'events')

            if (evs) {
                elCounter = elCounter + Object.keys(evs).length

                $.each(evs, function (ii, elist) {
                    if ($.inArray(ii, jsqtk.el_blacklist) < 0 && elist.length > 1) {
                        let checkList = jsqtk._checkEventListenersInternal(elist)

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

        return elCounter
    },

    _checkEventListenersInternal: function (events) {
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

    _checkIds: function () {
        jsqtk.id_keys = []
        jsqtk.id_result = []

        $.each($('[id]'), function (i, elem) {
            let id = $(elem).attr('id')
            if ($.inArray(id, jsqtk.id_keys) < 0) {
                jsqtk.id_keys.push(id)
            }
            else if ($.inArray(id, jsqtk.id_result) < 0) {
                jsqtk.id_result.push(id)
            }
        })
    }
}

window.$$$ = jsqtk // tmp. shortcut