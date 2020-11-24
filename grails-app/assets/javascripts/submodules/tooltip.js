
// submodules/tooltip.js

tooltip = {
    configs : {
        tooltipTrigger: null
    },
    go : function() {
        console.log('tooltip.go()')
        tooltip.init()
        tooltip.initializePopup(tooltip.configs.tooltipTrigger);
        tooltip.acccessViaKeys();
    },
    init: function() {
        tooltip.configs.tooltipTrigger = $('.la-popup-tooltip')
    },
    initializePopup: function(obj) {
        $('.la-popup-tooltip').each(function() {
                console.log("-------------------------------------------------------");
                $(this).attr('aria-label',$(this).attr('data-content'));
                // add aria-label to container-span
                //
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
        // for click and focus
        $(tooltip.configs.tooltipTrigger).on('click focus', function(){$(this).popup('show'); })

        // for unfocus
        $(tooltip.configs.tooltipTrigger).on('focusout', function(){$(this).popup('hide'); })

        // for ESC
        $(tooltip.configs.tooltipTrigger).on('keydown', function(){
            if(event.keyCode==27){
                $(this).popup('hide');
            }
        })
    },
}