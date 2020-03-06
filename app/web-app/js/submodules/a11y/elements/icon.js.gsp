a11yIcon = {
    configs: {

    },
    go: function () {
        $('.ui.sortable.table thead .sortable a').attr( {
            'aria-label' : 'Das Klicken auf diesen Link führt zu einem erneuten Laden dieser Seite mit geänderter Sortierreihenfolge für diese Spalte'
            });
        $('.ui.sortable.table thead .sorted.asc').attr( {
            'aria-sort' : 'ascending'
        });
        $('.ui.sortable.table thead .sorted.desc').attr( {
            'aria-sort' : 'descending'
        });
        $('i.icon').attr( {
            'aria-hidden' : 'true'
        });
    }
}