// module: assets/javascripts/modules/docs.js

docs = {

    init: function (cssSel) {
        console.log('docs.init( ' + cssSel + ' )')

        $(cssSel).find ('a[data-dctx]').on ('click', function(e) {
            e.preventDefault();
            let dctx = $(this).attr('data-dctx')
            let previewModalId = '#document-preview-' + dctx

            $.ajax({
                url: JSPC.config.ajax.htmlDocumentPreview + '?dctx=' + dctx
            }).done (function (data) {
                $('#dynamicModalContainer').html (data)
                $(previewModalId).modal ({
                    onVisible: function () {
                        // workaround - erms-4524
                        setTimeout( function() { $('body').trigger ('resize') }, 100 )
                    },
                    onApprove: function () {
                        return false;
                    },
                    onHidden: function () {
                        $(previewModalId).remove ()
                    }
                }).modal ('show')
            });
        });
    }
}

JSPC.modules.add( 'docs', docs );

