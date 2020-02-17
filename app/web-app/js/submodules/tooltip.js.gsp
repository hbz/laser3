
// submodules/tooltip.js

tooltip = {
    configs : {
        tooltipTrigger: $('.la-popup-tooltip')
    },
    go : function() {
        tooltip.configs.tooltipTrigger =  $('.la-popup-tooltip');
        tooltip.initializePopup(tooltip.configs.tooltipTrigger);
        tooltip.acccessViaKeys();
    },
    initializePopup: function(obj) {

        $('.ui.toggle.button').next('.ui.popup').remove();
        $(obj).popup({
            hoverable: true,
            inline     : true,
            lastResort: true,
            position: 'left center',
            delay: {
                show: 300,
                hide: 500
            },
            onShow: function() {
                // generate a random ID
                var id =  'wcag_' + Math.random().toString(36).substr(2, 9);
                // add aria-label to container-span
                $(this).prev(tooltip.configs.tooltipTrigger).attr('aria-labelledby',id);
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