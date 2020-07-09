a11yMenu = {
    configs: {
        menuSelector        : '#mainMenue',
        menuSelectorTest    : '#mainMenueTest'

    },
    go: function () {
        // for Main Menu
        $('.ui.dropdown' , a11yMenu.configs.menuSelector ).dropdown({
            action: 'nothing',
            selectOnKeydown        : false,
            on: 'hover',
            onHide         : function() {
                $(this).attr("aria-expanded", "false"); // a11y
            },
            onShow        : function() {
                $(this).attr("aria-expanded", "true"); // a11y
            }
        });

        // for Test Main Menu on dev/index view
        $('.ui.dropdown' , a11yMenu.configs.menuSelectorTest ).dropdown({
            action: 'nothing',
            selectOnKeydown        : true,
            on: 'hover',
            onHide         : function() {
                $(this).attr("aria-expanded", "false"); // a11y
            },
            onShow        : function() {
                $(this).attr("aria-expanded", "true"); // a11y
            }
        });
    }
}