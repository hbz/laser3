// module: assets/javascripts/modules/verticalNavi.js

verticalNavi = {

    go: function () {
        verticalNavi.init('body')
    },

    init: function (ctxSel) {
        console.log('verticalNavi.init( ' + ctxSel + ' )')

        // bigger then 992px
        if (window.matchMedia('(min-width: 992px)').matches) {
            // clone hide
            $( '.la-js-verticalNavi-clone' ).hide();
            // sidebar function
            $('#mainContent').removeClass('pusher');
            $('.la-menue-button').hide();
            $( '.la-js-verticalNavi' ).not('.la-js-verticalNavi-clone').show();
        }
        // smaller then 992px
        else {
            // clone
            $( '.la-js-verticalNavi' ).clone().prependTo( 'body' ).addClass('la-js-verticalNavi-clone');
            $( '.la-js-verticalNavi' ).hide();
            // sidebar function
            $('.la-js-verticalNavi-clone').addClass('vertical sidebar sidebarMobile');
            $('.la-menue-button').show();
            //dealing with dropdown menu vs vertical menu
            $('.la-js-verticalNavi-clone').find('div.dropdown').removeClass('dropdown').addClass('item');
            $('.la-js-verticalNavi-clone').find('.menu').removeClass('menu').addClass('content');
            $('.la-js-verticalNavi-clone').addClass('accordion');

            //responsible behavior of floating label in secondary pointing stackable menu
            $('.secondary.pointing.stackable.menu').find('.ui.floating.blue.circular.label').removeClass('floating');
        }

        // Resize the Window
        $(window).resize(function () {

            // bigger then 992px
            if (window.matchMedia('(min-width: 992px)').matches) {
                // clone hide
                $( '.la-js-verticalNavi-clone' ).hide();
                // sidebar function
                $('.la-menue-button').hide();
                $( '.la-js-verticalNavi' ).not('.la-js-verticalNavi-clone').show();
            }
            // smaller then 992px
            else {
                $( '.la-js-verticalNavi' ).hide();
                $('.la-menue-button').show();
                //dealing with dropdown menu vs vertical menu
                $('.la-js-verticalNavi-clone').find('div.dropdown').removeClass('dropdown').addClass('item');
                $('.la-js-verticalNavi-clone').find('.menu').removeClass('menu').addClass('content');

                // clone
                if ($( '.la-js-verticalNavi' ).hasClass('la-js-verticalNavi-clone')) {
                    return
                }
                $( '.la-js-verticalNavi' ).clone().prependTo( 'body' ).addClass('la-js-verticalNavi-clone');
                // sidebar function
                $('.la-js-verticalNavi-clone').addClass('vertical sidebar sidebarMobile');
                $('.la-js-verticalNavi-clone').addClass('accordion');


                $('#mainMenue.ui.accordion').accordion();
                $('.la-js-verticalNavi-clone')
                .sidebar({
                    context: $('body')
                })
                .sidebar('attach events', '.la-menue-button')
                .sidebar('setting', 'dimPage', false);

                //responsible behavior of floating label in secondary pointing stackable menu
                $('.secondary.pointing.stackable.menu').find('.ui.floating.blue.circular.label').removeClass('floating');
            }
        });

        $('#mainMenue.ui.accordion').accordion();
        $('.la-js-verticalNavi-clone')
        .sidebar({
            context: $('body')
        })
        .sidebar('attach events', '.la-menue-button')
        .sidebar('setting', 'dimPage', false);
    }
}

JSPC.modules.add( 'verticalNavi', verticalNavi );