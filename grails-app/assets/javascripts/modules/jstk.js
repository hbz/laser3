// assets/javascripts/modules/jstk.js

jstk = {

    id_keys: [],
    id_result: [],

    el_blacklist: [],
    el_keys: [],
    el_result: {},
    el_resultCounter: {},
    el_idCounter: 0,

    go: function () {
        console.log('jstk -> go')

        jstk._checkIds()

        let elCounter = jstk._checkEventListeners()
        let currentKeys = jstk.el_keys[jstk.el_keys.length - 1]

        let tmp = []
        $.each(jstk.el_resultCounter, function (k, v) {
            tmp.push(k + ': ' + v)
        })

        console.groupCollapsed('jstk: event listener duplicates', tmp)
        console.log('- event listener found overall: ' + elCounter)

        if (jstk.el_blacklist.length > 0) {
            console.log('- el_blacklist: ' + jstk.el_blacklist)
        }
        if (currentKeys.length > 0) {
            let history = []
            jstk.el_keys.forEach(function (i){ history.push(i.length) })

            console.log('- history count of used data-jstk-ids: [' + history + ']')
            console.log('- currently used data-jstk-ids: ' + currentKeys)
            console.log('- currently found elements with event listener doublets: ' + Object.keys(jstk.el_result).length)
            currentKeys.forEach(function (k) {
                console.log(jstk.el_result[k])
            })
        }
        console.groupEnd()

        console.groupCollapsed('jstk: id attribute duplicates', jstk.id_result.length)
        jstk.id_result.forEach(function (k) {
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
            '_help      -> returns this help \n' +
            '  \n' +
            '_go        -> checks for duplicate event listener and id attributes, writes history \n' +
            '_history   -> returns existing history \n' +
            '_info(id)  -> returns $(*[data-jstk-id="jstk-<id>"]) \n' +
            '_forms     -> returns $(forms) \n' +
            '_headlines -> returns $(h{1..6}) \n' +
            '_comments  -> returns $(<!-- -->) \n' +
            '_templates -> returns $(<!-- [template: .. ] -->) \n'
        )
    },

    history: function () {
        console.log('jstk -> history')
        $.each(jstk.el_keys, function (i, e) {
            console.log(e)
        })
    },

    info: function (id) {
        console.log('jstk -> info')
        let elem = $('*[data-jstk-id="jstk-' + id + '"]')

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

    comments: function() {
        console.log('jstk: $( comments )')
        let comments = $('*').contents().filter(function() { return this.nodeType === 8 })
        $.each(comments, function (i, elem) {
            console.log(elem)
        })
    },

    forms: function() {
        console.log('jstk: $( forms )')
        $.each($('form'), function (i, elem) {
            console.log(elem)
        })
    },

    headlines: function() {
        console.log('jstk: $( headlines )')
        $.each($('h1,h2,h3,h4,h5,h6'), function (i, elem) {
            console.log(elem)
        })
    },

    templates: function() {
        console.log('jstk: $( templates )')
        let comments = $('*').contents().filter(function() { return this.nodeType === 8 && this.textContent.includes('[template:') && this.textContent.includes('START') })
        $.each(comments, function (i, elem) {
            console.log(elem)
            console.log(elem.nextElementSibling)
        })
    },

    // ----->

    _checkEventListeners: function () {
        let elCounter = 0

        jstk.el_result = {}
        jstk.el_resultCounter = {}

        $.each($('*'), function (i, elem) {
            jstk.el_idCounter = jstk.el_idCounter + 1

            let jstkEid = 'jstk-' + jstk.el_idCounter
            let evs = $._data(elem, 'events')

            if (evs) {
                elCounter = elCounter + Object.keys(evs).length

                $.each(evs, function (ii, elist) {
                    if ($.inArray(ii, jstk.el_blacklist) < 0 && elist.length > 1) {
                        let checkList = jstk._checkEventListenersInternal(elist)

                        if (checkList.length > 0) {
                            if ($(elem).attr('data-jstk-id')) {
                                jstkEid = $(elem).attr('data-jstk-id')
                            } else {
                                $(elem).attr('data-jstk-id', jstkEid)
                            }

                            if (!jstk.el_result[jstkEid]) {
                                jstk.el_result[jstkEid] = [elem]
                            }
                            jstk.el_result[jstkEid][ii] = checkList

                            if (!jstk.el_resultCounter[ii]) {
                                jstk.el_resultCounter[ii] = 0
                            }
                            jstk.el_resultCounter[ii]++
                        }
                    }
                })
            }
        })

        let keys = Object.keys(jstk.el_result).sort(function (a, b) {
            a.split('-')[1] < b.split('-')[1] ? 1 : -1
        })
        jstk.el_keys.push(keys)

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
        jstk.id_keys = []
        jstk.id_result = []

        $.each($('[id]'), function (i, elem) {
            let id = $(elem).attr('id')
            if ($.inArray(id, jstk.id_keys) < 0) {
                jstk.id_keys.push(id)
            }
            else if ($.inArray(id, jstk.id_result) < 0) {
                jstk.id_result.push(id)
            }
        })
    }
}

/* shortcuts */

Object.defineProperty(window, '_help',      { get: function() { jstk.help() } })
Object.defineProperty(window, '_go',        { get: function() { jstk.go() } })
Object.defineProperty(window, '_history',   { get: function() { jstk.history() } })
window._info = function(id) { jstk.info(id) }
Object.defineProperty(window, '_comments',  { get: function() { jstk.comments() } })
Object.defineProperty(window, '_forms',     { get: function() { jstk.forms() } })
Object.defineProperty(window, '_headlines', { get: function() { jstk.headlines() } })
Object.defineProperty(window, '_templates', { get: function() { jstk.templates() } })
