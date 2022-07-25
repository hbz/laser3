// module: assets/javascripts/modules/a11y/elements/icon.js

a11yIcon = {

    go: function () {
        // console.log('a11yIcon.go()')
        a11yIcon.init('body')
    },

    init: function (ctxSel) {
        console.log('a11yIcon.init( ' + ctxSel + ' )')

        $(ctxSel + ' .ui.sortable.table thead .sorted.asc').attr( {
            'aria-sort' : 'ascending'
        });
        $(ctxSel + ' .ui.sortable.table thead .sorted.desc').attr( {
            'aria-sort' : 'descending'
        });
        $(ctxSel + ' i.icon').not('.dropdown.icon').attr( {
            'aria-hidden' : 'true'
        });
    }
}