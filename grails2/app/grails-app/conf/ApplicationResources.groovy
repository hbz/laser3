modules = {

    overrides {
        'jquery' {
            resource id:'js', url:'js/libs/jquery-3.2.1.min.js'
        }
    }

//    annotations { // not used in working code of laser
//        //dependsOn 'semanticUI'
//
//        resource url:'js/deprecated/summernote.min.js' //used in annotations.js
//        resource url:'css/deprecated/summernote.css', attrs: [media: 'screen,print']
//        resource url:'css/deprecated/summernote-bs2.css', attrs: [media: 'screen,print']
//
//        resource url:'js/deprecated/legacy.annotations.js' // used in deprecated code
//        resource url:'css/deprecated/legacy.annotations.css', attrs: [media: 'screen,print']
//    }

    semanticUI {
        dependsOn 'base'

        resource url:'semantic/javascript/semantic.min.js'         // new stuff
        resource url:'semantic/laser/semantic.min.css', attrs: [media: 'screen,print']       // new stuff

    }
    accessibility {
        dependsOn 'base'
        //dependsOn 'semanticUI'

        resource url:'semantic/javascript/semantic.min.js'         // new stuff
        resource url:'semantic/accessibility/semantic.min.css', attrs: [media: 'screen,print']       // new stuff

    }
    datatables {
        resource url:'vendor/datatables/datatables.min.js'
        resource url:'vendor/datatables/datatables.css', attrs: [media: 'screen,print']
    }
    chartist {
        resource url:'vendor/chartist/javascript/chartist.min.js'
        resource url:'vendor/chartist/css/chartist.css', attrs: [media: 'screen,print']
        resource url:'vendor/chartist-plugin-legend/chartist-plugin-legend.min.js'
    }
    base {
        dependsOn 'jquery'

        // deprecated

        resource url:'js/tmp_semui.js'   // only tmp
        resource url:'css/tmp_semui.css', attrs: [media: 'screen,print']   // only tmp

        // legacy CRAP ..

        resource url:'css/jquery-editable.css', attrs: [media: 'screen,print']               // updated stuff
        resource url:'js/libs/jquery.poshytip.js'                // updated stuff
        resource url:'js/libs/jquery-editable-poshytip.min.js'   // updated stuff

        resource url:'js/libs/moment-with-locales.min.js'
        resource url:'js/libs/moment.min.js'

        resource url:'js/libs/jquery.readmore.min.js' // new stuff

        resource url:'vendor/select2/css/select2-laser.css', attrs: [media: 'screen,print']       // overwrite to look more like semantic ui
        resource url:'vendor/select2/js/select2.min.js'       // updated stuff // new version 3.5.4
        resource url:'js/libs/readmore.min.js'

        resource url:'js/application.js'
        resource url:'js/submodules/decksaver.js'
        resource url:'js/submodules/tooltip.js'
        resource url:'js/submodules/bb8.js'
        resource url:'js/submodules/a11y/collections/menu.js'
        resource url:'js/submodules/a11y/elements/icon.js'
    }

    swaggerApi {
        dependsOn 'jquery'

        resource url:'vendor/swagger-ui/swagger-ui.css', attrs: [media: 'screen,print']

        resource url:'vendor/swagger-ui/swagger-ui-bundle.js'
        resource url:'vendor/swagger-ui/swagger-ui-standalone-preset.js'
        resource url:'vendor/cryptoJS-v3.1.2/rollups/hmac-sha256.js'
    }
}
