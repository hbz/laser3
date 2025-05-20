// module: assets/javascripts/modules/a11y/elements/icon.js

a11yIcon = {

    go: function () {
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

        // icon handling at sortable table header
        let sortable = $('.ui.sortable.table thead th.sortable a').not('.ui.sortable.table thead th.desc a').not('.ui.sortable.table thead th.asc a');
        let desc = $('.ui.sortable.table thead th.desc a');
        let asc = $('.ui.sortable.table thead th.asc a');


        $('<i class="large la-lighter-grey sort icon"></i>').appendTo($(sortable));
        $('<i class="large caret down icon"></i>').appendTo($(desc));
        $('<i class="large caret up icon"></i>').appendTo($(asc));
    }
}

JSPC.modules.add( 'a11yIcon', a11yIcon );