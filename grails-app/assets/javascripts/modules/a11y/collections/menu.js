
// modules/a11y/collections/menu.js

a11yMenu = {

    go: function () {
        // console.log('a11yMenu.go()')

        a11yMenu.initMainMenue()
        a11yMenu.init('body')
    },

    init: function (ctxSel) {
        console.log('a11yMenu.init(' + ctxSel + ')')
        a11yMenu.initDynamicDropdowns(ctxSel)
    },

    initDynamicDropdowns: function(ctxSel) {
        console.log('a11yMenu.initDropdowns(' + ctxSel + ')')

        // for Main Menu
        $(ctxSel + ' .ui.dropdown').dropdown({
            action: 'nothing',
            selectOnKeydown: false,
            on: 'hover',
            onHide: function() {
                $(this).attr("aria-expanded", "false"); // a11y
            },
            onShow: function() {
                $(this).attr("aria-expanded", "true"); // a11y
            }
        });
    },

    initMainMenue: function () {
        console.log('a11yMenu.initMainMenue()')

        // for Main Menu
        $('#mainMenue').dropdown({
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
        $('#mainMenueTest').dropdown({
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