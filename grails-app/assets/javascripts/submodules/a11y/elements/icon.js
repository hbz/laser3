a11yIcon = {
    configs: {
    },
    go: function () {
        console.log('a11yIcon.go()')

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