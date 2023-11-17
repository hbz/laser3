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

    initDynamicPopups: function(ctxSel) {
        console.log('tooltip.initDynamicPopups( ' + ctxSel + ' )')

        $(ctxSel + ' .la-popup-tooltip').each(function() {
            // add aria-label
            $(this).attr('aria-label', $(this).attr('data-content'));
            $(this).popup();
        });

        $(ctxSel + ' .ui.toggle.button').each(function() {
            $(this).next('.ui.popup').remove();

            $(this).popup({
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
                    let id =  'wcag_' + Math.random().toString(36).substr(2, 9);
                    //add role=tooltip and the generated ID to the tooltip-div (generated from semantic)
                    $(this).children('.content').attr({role:'tooltip', id:id});
                },
            });
        });
    },

    initDynamicAccessViaKeys: function(ctxSel){
        console.log('tooltip.initDynamicAccessViaKeys( ' + ctxSel + ' )')

        let $elems = $(ctxSel + ' .la-popup-tooltip')

        // for click and focus
        $elems.on('click.popup focus.popup', function(){
            $(this).popup('show');
        })
        // for unfocus
        $elems.on('focusout.popup', function(){
            $(this).popup('hide');
        })
        // for ESC
        $elems.on('keydown.popup', function(){
            if(event.keyCode==27){
                $(this).popup('hide');
            }
        })
    },
}

JSPC.modules.add( 'tooltip', tooltip );