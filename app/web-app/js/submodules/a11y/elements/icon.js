a11yIcon = {
    configs: {

    },
    go: function () {
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