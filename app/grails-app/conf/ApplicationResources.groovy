modules = {

    annotations {
        dependsOn 'semanticUI'

        resource url:'js/summernote.min.js'
        resource url:'css/summernote.css'
        resource url:'css/summernote-bs2.css'
        resource url:'js/annotations.js'
        resource url:'css/annotations.css'
    }

    treeSelects {
        dependsOn 'jquery'

        resource url:'css/jstree-themes/default/style.min.css'
        resource url:'js/jstree.min.js'
        resource url:'js/tree-selects.js'
    }

    onixMatrix {
        dependsOn 'semanticUI'

        resource url:'css/onix.css'
        resource url:'js/onix.js'
    }

    deprecatedCSS {
        resource url:'css/jquery.dataTables.css'
        resource url:'css/dataTables.fixedColumns.min.css'
        resource url:'css/dataTables.colVis.min.css'
        //resource url:'css/bootstrap-editable.css'
        resource url:'css/select2.css'
        resource url:"css/instances/deprecated.css" // legacy

        resource url:'js/tmp_semui.js'   // only tmp
        resource url:'css/tmp_semui.css' // only tmp
    }

    semanticUI {
        dependsOn 'jquery'
        dependsOn 'deprecatedCSS'

        // legacy CRAP ..
        // legacy CRAP ..
        // legacy CRAP ..

        resource url:'semantic_heave/jquery-editable.css'               // updated stuff
        resource url:'semantic_heave/jquery.poshytip.js'                // updated stuff
        resource url:'semantic_heave/jquery-editable-poshytip.min.js'   // updated stuff

        resource url:'js/moment-with-locales.min.js'
        //resource url:'js/inline-content.js'
        resource url:'js/moment.min.js'

        resource url:'js/jquery.dataTables.min.js'
        resource url:'js/dataTables.colVis.min.js'
        resource url:'js/dataTables.fixedColumns.min.js'
        resource url:'js/dataTables.scroller.js'

        resource url:'semantic/semantic.min.js'     // new stuff
        resource url:'semantic/semantic.min.css'    // new stuff

        resource url:'semantic_heave/jquery.readmore.min.js' // new stuff

        resource url:'semantic_heave/select2.css'            // updated stuff // new version 3.5.4
        resource url:'semantic_heave/select2.min.js'         // updated stuff // new version 3.5.4

        resource url:'js/application.js.gsp'          // legacy app js
    }

    swaggerApi {
        dependsOn 'jquery'

        resource url:'vendor/swagger-ui/swagger-ui.css'

        resource url:'vendor/swagger-ui/swagger-ui-bundle.js'
        resource url:'vendor/swagger-ui/swagger-ui-standalone-preset.js'
        resource url:'vendor/cryptoJS-v3.1.2/rollups/hmac-sha256.js'
        //resource url:'js', file: 'jquery-3.2.1.min.js'
    }
}
