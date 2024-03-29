// module: assets/javascripts/modules/a11y/collections/menu.js

a11yMenu = {

    go: function () {
        a11yMenu.initMenue()
    },

    initMenue: function () {
        console.log('a11yMenu.initMainMenue()')

        // for Main Menu
        $('#mainMenue .dropdown').dropdown({
            action: function (text, value, element) {
                if(event.type === 'keydown') {
                    $('html').css('cursor', 'wait');
                    (element.clone())[0].click();
                }
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
                if(event.type === 'keydown') {
                    $('html').css('cursor', 'wait');
                    (element.clone())[0].click();
                }
            },
            selectOnKeydown        : false,
            on: 'hover',
            allowTab: false,
            onHide         : function() {
                $(this).attr("aria-expanded", "false"); // a11y
            },
            onShow        : function() {
                $(this).attr("aria-expanded", "true"); // a11y
            }
        });
    }
}

JSPC.modules.add( 'a11yMenu', a11yMenu );