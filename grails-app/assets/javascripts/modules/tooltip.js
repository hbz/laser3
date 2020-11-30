
// modules/tooltip.js

tooltip = {
    configs : {
        tooltipTrigger: null
    },
    go : function() {
        console.log('tooltip.go()')
        tooltip.initializePopup($('.la-popup-tooltip'));
        tooltip.acccessViaKeys();
    },

    initializePopup: function(obj) {
        $('.la-popup-tooltip').each(function() {
            // add aria-label
            $(this).attr('aria-label',$(this).attr('data-content'));
        });
        $('.ui.toggle.button').next('.ui.popup').remove();
        $(obj).popup({
            hoverable: true,
            inline     : true,
            lastResort: true,
            movePopup: false,
            boundary: 'body',
            delay: {
                show: 300,
                hide: 500
            },

            onShow: function() {
                // generate a random ID
                var id =  'wcag_' + Math.random().toString(36).substr(2, 9);

                //add role=tooltip and the generated ID to the tooltip-div (generated from semantic)
                $(this).children('.content').attr({role:'tooltip',id:id});
            },
        });
    },

    acccessViaKeys: function(){

        $('.la-popup-tooltip')
            .on('click focus', function(){
                $(this).popup('show');
            }) // for click and focus
            .on('focusout', function(){
                $(this).popup('hide');
            }) // for unfocus
            .on('keydown', function(){  // for ESC
                if(event.keyCode==27){
                    $(this).popup('hide');
                }
            })
    },
}