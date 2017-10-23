import grails.util.Holders

// OK this works, but isn't ideal
// import grails.util.Environment
// switch (Environment.current) {
//     case Environment.DEVELOPMENT:
//         println("AppRes - Development");
//         break
//     case Environment.PRODUCTION:
//         println("AppRes - Prod");
//         break
// }
// resource url:"css/instances/${ApplicationHolder.application.config.defaultCssSkin?:'standard.css'}"


modules = {
    application {
        dependsOn 'jquery'

        resource url:'js/application.js'
        resource url:'js/plugins.min.js'
    }

    // deprecated
    kbplus {
        dependsOn 'jquery'
        dependsOn 'deprecatedCSS'

        resource url:'js/inline-content.js'
        resource url:'js/bootstrap.min.js'
        resource url:'js/bootstrap-editable.js'
        resource url:'js/bootstrap-datepicker.de.js'
        resource url:'js/moment-with-locales.min.js'
        resource url:'js/moment.min.js'
        resource url:'js/select2.min.js'
        resource url:'js/jquery.dataTables.min.js'
        resource url:'js/dataTables.colVis.min.js'
        resource url:'js/dataTables.fixedColumns.min.js'
        resource url:'js/dataTables.scroller.js'
        resource url:'js/jquery.dotdotdot.min.js'

        resource url:'js/kbplusapp.js.gsp'
    }

    annotations {
        dependsOn 'semanticUI'

        resource url:'js/summernote.min.js'
        resource url:'css/summernote.css'
        resource url:'css/summernote-bs2.css'
        resource url:'js/annotations.js'
        resource url:'css/annotations.css'
    }

    treeSelects {
        dependsOn 'font-awesome'
        dependsOn 'jquery'

        resource url:'css/jstree-themes/default/style.min.css'
        resource url:'js/jstree.min.js'
        resource url:'js/tree-selects.js'
    }

    onixMatrix {
        dependsOn 'kbplus'

        resource url:'css/onix.css'
        resource url:'js/onix.js'
    }

    deprecatedCSS {
        dependsOn 'font-awesome'

        resource url:'css/jquery.dataTables.css'
        resource url:'css/dataTables.fixedColumns.min.css'
        resource url:'css/dataTables.colVis.min.css'
        resource url:'css/bootstrap-editable.css'
        resource url:'css/select2.css'
        resource url:"css/instances/${Holders.config.defaultCssSkin?:'standard.css'}"

        // upgrade to bootstrap 4 (tmp)
        resource url:'css/bootstrap_tmp/bs4_card.css'

        resource url:'css/style.css'
    }

    semanticUI {
        dependsOn 'jquery'
        dependsOn 'deprecatedCSS'

        //resource url:'js/jquery-3.1.1.min.js'

        resource url:'semantic/semantic.js'
        resource url:'semantic/semantic.css'

        //resource url:'semantic_heave/bootstrap.3.3.7.min.js'    // new

        // legacy CRAP ..
        // legacy CRAP ..
        // legacy CRAP ..

        resource url:'js/bootstrap.min.js'
        resource url:'js/bootstrap-editable.js'
        resource url:'js/bootstrap-datepicker.de.js'
        resource url:'js/moment-with-locales.min.js'
        resource url:'js/inline-content.js'
        resource url:'js/moment.min.js'
        //resource url:'js/select2.min.js' // updated
        resource url:'js/jquery.dataTables.min.js'
        resource url:'js/dataTables.colVis.min.js'
        resource url:'js/dataTables.fixedColumns.min.js'
        resource url:'js/dataTables.scroller.js'
        resource url:'js/jquery.dotdotdot.min.js'

        resource url:'js/kbplusapp.js.gsp'

        // updated stuff

        resource url:'semantic_heave/select2.css'       // new version 3.5.4
        resource url:'semantic_heave/select2.min.js'    // new version 3.5.4
    }
}
