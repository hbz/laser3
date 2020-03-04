a11yMenu = {
    configs: {
        menuSelector  : '#mainMenue'
    },
    go: function () {
        $('.ui.dropdown' , a11yMenu.configs.menuSelector).dropdown({
            action: function (text, value, element) {
                element.click()
            },
            on: 'hover',
            onHide         : function() {
                $(this).attr("aria-expanded", "false"); // a11y
            },
            onShow        : function() {
                $(this).attr("aria-expanded", "true"); // a11y
            }
        });
        $('.ui.menu' , a11yMenu.configs.menuSelector).attr("role", "menubar"); // a11y
        $('.ui.menu a, .ui.menu .item' , a11yMenu.configs.menuSelector).attr("role", "menuitem"); // a11y
    }
}