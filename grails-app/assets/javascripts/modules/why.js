// module: assets/javascripts/modules/why.js

why = {

    id_keys: [],
    id_result: [],

    el_blacklist: [],
    el_keys: [],
    el_result: {},
    el_resultCounter: {},
    el_idCounter: 0,

    help: function() {
        console.log(
            '_help      : returns this help \n' +
            '  \n' +
            '_tap       : checks for duplicate event listener and id attributes, writes history \n' +
            '_info      : shows current details' +
            '_history   : shows existing history \n' +
            '  \n' +
            '_comments  : shows $(<!-- -->) \n' +
            '_elem(id)  : shows $(*[data-why-id="why-<id>"]) \n' +
            '_forms     : shows $(forms) \n' +
            '_headlines : shows $(h{1..6}) \n' +
            '_modals    : shows $(*[data-ui="modal"]) \n' +
            '_scripts   : shows $(script) \n' +
            '_templates : shows $(<!-- [template: .. ] -->) \n'
        )
    },

    tap: function () {
        console.debug('%cwhy.tap ' + (1 + why.el_keys.length), 'color:grey')
        why._executeTap ()
    },

    info: function (expand = true) {
        console.log('why.info')
        why._executeTap (true, expand)

        if ($.toast) {
            if (why.el_resultCounter.click > 0) {
                $.toast({message: 'Warning: Duplicate event listeners found', displayTime: 6000, class: 'orange', showIcon: 'bug', position: 'bottom left'});
            }
            if (why.id_result.length > 0) {
                $.toast({message: 'Warning: Duplicate ID attributes found: ' + why.id_result, displayTime: 6000, class: 'red', showIcon: 'code', position: 'bottom left'});
            }
        }
    },

    history: function () {
        console.log('why.history: ' + why.el_keys.length + ' entries')
        $.each(why.el_keys, function (i, e) {
            console.log(e)
        })
    },

    elem: function (id) {
        console.log('why.elem: $( [data-why-id] )')
        let elem = $('*[data-why-id="why-' + id + '"]')

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
        console.log('why.comments')
        let comments = $('*').contents().filter(function() { return this.nodeType === 8 })
        $.each(comments, function (i, elem) {
            console.log(elem)
        })
    },

    scripts: function() {
        console.log('why.scripts')
        $.each($('script'), function (i, elem) {
            console.log(elem)
        })
    },

    headlines: function() {
        console.log('why.headlines')
        $.each($('h1,h2,h3,h4,h5,h6'), function (i, elem) {
            console.log(elem)
        })
    },

    forms: function() {
        console.log('why.forms')
        $.each($('form'), function (i, elem) {
            console.log(elem)
        })
    },

    modals: function (id) {
        console.log('why.elem: $( [data-ui="modal"] )')
        let elems = $('*[data-ui="modal"]')

        if (elems) {
            result = []
            $.each(elems, function (i, elem) {
                let href = $(elem).attr('href') ? $(elem).attr('href') : $(elem).attr('data-href')
                let target = $(href)[0 ]? $(href)[0] : 'ERROR'
                result.push([ elem, href, target ])
            })
            if (result) { console.table(result) }
        }
    },

    templates: function() {
        console.log('why.templates')
        let comments = $('*').contents().filter(function () { return this.nodeType === 8 && this.textContent.includes('[template:') && this.textContent.includes('START') })
        $.each(comments, function (i, elem) {
            console.log(elem)
            console.log(elem.nextElementSibling)
        })
    },

    // ----->

    _executeTap: function (output = false, expand = false) {
        why._checkIds()

        let elCounter = why._checkEventListeners()
        let currentKeys = why.el_keys[why.el_keys.length - 1]

        let tmp = []
        let tmpSize = 0
        $.each(why.el_resultCounter, function (k, v) {
            tmp.push(k + ': ' + v)
            tmpSize += v
        })

        if (output) {
            if (expand) {
                console.group('[why] event listener duplicates:', tmpSize, '; id attribute duplicates:', why.id_result.length)
            }
            else {
                console.groupCollapsed('[why] event listener duplicates:', tmpSize, '; id attribute duplicates:', why.id_result.length)
            }
            console.log('+ event listener duplicates', (tmp.length > 0  ? tmp : 0))
            console.log('  event listener found overall: ' + elCounter)

            if (why.el_blacklist.length > 0) {
                console.log('- el_blacklist: ' + why.el_blacklist)
            }
        }
        if (currentKeys.length > 0) {
            let history = []
            why.el_keys.forEach(function (i) { history.push(i.length) })

            if (output) {
                console.log('  - currently found elements with event listener doublets: ' + Object.keys(why.el_result).length)
                console.log('  - currently used data-why-ids: ' + currentKeys)
                console.log('  history count of used data-why-ids: [' + history + ']')
                currentKeys.forEach(function (k) {
                    console.log(why.el_result[k])
                })
            }
        }

        if (output) { console.log('+ id attribute duplicates', why.id_result.length) }

        why.id_result.forEach(function (k) {
            let tmp = []
            $.each($('[id="' + k + '"]'), function (i, elem) {
                tmp.push(elem)
            })
            if (output) { console.log(tmp) }
        })
        if (output) { console.groupEnd() }
    },

    _checkEventListeners: function () {
        let elCounter = 0

        why.el_result = {}
        why.el_resultCounter = {}

        $.each($('*'), function (i, elem) {
            why.el_idCounter = why.el_idCounter + 1

            let whyEid = 'why-' + why.el_idCounter
            let evs = $._data(elem, 'events')

            if (evs) {
                elCounter = elCounter + Object.keys(evs).length

                $.each(evs, function (ii, elist) {
                    if ($.inArray(ii, why.el_blacklist) < 0 && elist.length > 1) {
                        let checkList = why._checkEventListenersInternal(elist)

                        if (checkList.length > 0) {
                            if ($(elem).attr('data-why-id')) {
                                whyEid = $(elem).attr('data-why-id')
                            } else {
                                $(elem).attr('data-why-id', whyEid)
                            }

                            if (!why.el_result[whyEid]) {
                                why.el_result[whyEid] = [elem]
                            }
                            why.el_result[whyEid][ii] = checkList

                            if (!why.el_resultCounter[ii]) {
                                why.el_resultCounter[ii] = 0
                            }
                            why.el_resultCounter[ii]++
                        }
                    }
                })
            }
        })

        let keys = Object.keys(why.el_result).sort(function (a, b) {
            a.split('-')[1] < b.split('-')[1] ? 1 : -1
        })
        why.el_keys.push(keys)

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
        why.id_keys = []
        why.id_result = []

        $.each($('[id]'), function (i, elem) {
            let id = $(elem).attr('id')
            if ($.inArray(id, why.id_keys) < 0) {
                why.id_keys.push(id)
            }
            else if ($.inArray(id, why.id_result) < 0) {
                why.id_result.push(id)
            }
        })
    }
}

JSPC.modules.add( 'why', why );

/* shortcuts */

window._elem = function (id) { why.elem (id) }

Object.defineProperty (window, '_comments',  { get: function () { why.comments() } })
Object.defineProperty (window, '_forms',     { get: function () { why.forms() } })
Object.defineProperty (window, '_headlines', { get: function () { why.headlines() } })
Object.defineProperty (window, '_help',      { get: function () { why.help() } })
Object.defineProperty (window, '_history',   { get: function () { why.history() } })
Object.defineProperty (window, '_info',      { get: function () { why.info() } })
Object.defineProperty (window, '_modals',    { get: function () { why.modals() } })
Object.defineProperty (window, '_scripts',   { get: function () { why.scripts() } })
Object.defineProperty (window, '_tap',       { get: function () { why.tap() } })
Object.defineProperty (window, '_templates', { get: function () { why.templates() } })
