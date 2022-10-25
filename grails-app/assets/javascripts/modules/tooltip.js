// module: assets/javascripts/modules/tooltip.js

tooltip = {

    go : function() {
        tooltip.init('body')
    },

    init : function(ctxSel) {
        console.log('tooltip.init( ' + ctxSel + ' )')

        tooltip.initDynamicPopups(ctxSel)
        tooltip.initDynamicAccessViaKeys(ctxSel)
    },

    initializePopup_deprecated: function(obj) {
        $('.la-popup-tooltip').each(function() {
            // add aria-label
            $(this).attr('aria-label',$(this).attr('data-content'));
        });
        $('.ui.toggle.button').next('.ui.popup').remove();
        $(obj).popup({
            hoverable: true,
            inline     : false,
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

    initDynamicPopups: function(ctxSel) {
        console.log('tooltip.initDynamicPopups( ' + ctxSel + ' )')

        $(ctxSel + ' .la-popup-tooltip').each(function() {
            // add aria-label
            $(this).attr('aria-label', $(this).attr('data-content'));
            $(this).popup()
        });

        $(ctxSel + ' .ui.toggle.button').next('.ui.popup').remove();

        $(ctxSel + ' .ui.toggle.button').popup({
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

    initDynamicAccessViaKeys: function(ctxSel){
        console.log('tooltip.initDynamicAccessViaKeys( ' + ctxSel + ' )')

        var $elem = $(ctxSel + ' .la-popup-tooltip')

        // for click and focus
        $elem.on('click focus', function(){
            $(this).popup('show');
        })
        // for unfocus
        $elem.on('focusout', function(){
            $(this).popup('hide');
        })
        // for ESC
        $elem.on('keydown', function(){
            if(event.keyCode==27){
                $(this).popup('hide');
            }
        })
    },
}

JSPC.modules.add( tooltip, 'tooltip' );