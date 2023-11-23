// module: assets/javascripts/modules/docs.js

docs = {

    init: function (cssSel) {
        console.log('docs.init( ' + cssSel + ' )')

        $(cssSel).find ('a[data-documentKey]').on ('click', function(e) {
            e.preventDefault();
            let docKey = $(this).attr('data-documentKey')
            let previewModalId = '#document-preview-' + docKey.split(':')[0]

            $.ajax({
                url: JSPC.config.ajax.htmlDocumentPreview + '?key=' + docKey
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

