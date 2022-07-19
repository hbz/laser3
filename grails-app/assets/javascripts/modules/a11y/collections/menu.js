
// modules/a11y/collections/menu.js

a11yMenu = {

    go: function () {
        // console.log('a11yMenu.go()')
        a11yMenu.initMenue()
    },

    initMenue: function () {
        console.log('a11yMenu.initMainMenue()')

        // for Main Menu
        $('#mainMenue .dropdown').dropdown({
            action: function (text, value, element) {
                element.click()
            },
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
        $('#mainMenueTest .dropdown').dropdown({
            action: function (text, value, element) {
                element.click()
            },
            selectOnKeydown        : false,
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