modules = {

    overrides {
        'jquery' {
            resource id:'js', url:'js/jquery-3.2.1.min.js'
        }
    }

    annotations {
        dependsOn 'semanticUI'

        resource url:'js/libs/summernote.min.js'
        resource url:'css/summernote.css'
        resource url:'css/summernote-bs2.css'

        resource url:'js/legacy.annotations.js'
        resource url:'css/legacy.annotations.css'
    }

    treeSelects {
        dependsOn 'jquery'

        resource url:'css/jstree-themes/default/style.min.css'
        resource url:'js/libs/jstree.min.js'

        resource url:'js/legacy.tree-selects.js'
    }

    onixMatrix {
        dependsOn 'semanticUI'

        resource url:'css/legacy.onix.css'
        resource url:'js/legacy.onix.js'
    }

    deprecated {
        resource url:'css/datatables.css'
        //resource url:'css/select2.css'
        resource url:"css/instances/deprecated.css" // legacy

        resource url:'js/tmp_semui.js'   // only tmp
        resource url:'css/tmp_semui.css' // only tmp
    }

    semanticUI {
        dependsOn 'jquery'
        dependsOn 'deprecated'

        // legacy CRAP ..
        // legacy CRAP ..
        // legacy CRAP ..

        resource url:'semantic-restoration/jquery-editable.css'               // updated stuff
        resource url:'semantic-restoration/jquery.poshytip.js'                // updated stuff
        resource url:'semantic-restoration/jquery-editable-poshytip.min.js'   // updated stuff

        resource url:'js/libs/moment-with-locales.min.js'
        resource url:'js/libs/moment.min.js'

        resource url:'js/libs/datatables.min.js'            // updated stuff // new version 1.10.16
        resource url:'js/libs/jquery.floatThead.min.js'     // new stuff

        resource url:'semantic/semantic.min.js'         // new stuff
        resource url:'semantic/semantic.min.css'        // new stuff

        resource url:'semantic-restoration/jquery.readmore.min.js' // new stuff

        //resource url:'css/select2.css'            // updated stuff // new version 3.5.4
        resource url:'css/select2-laser.css'     // overwrite to look more like semantic ui
        resource url:'js/select2.min.js'         // updated stuff // new version 3.5.4


        resource url:'js/application.js.gsp'
    }

    accessibility {
        dependsOn 'semanticUI'
    }

    swaggerApi {
        dependsOn 'jquery'

        resource url:'vendor/swagger-ui/swagger-ui.css'

        resource url:'vendor/swagger-ui/swagger-ui-bundle.js'
        resource url:'vendor/swagger-ui/swagger-ui-standalone-preset.js'
        resource url:'vendor/cryptoJS-v3.1.2/rollups/hmac-sha256.js'
    }
}
