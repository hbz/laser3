modules = {

    overrides {
        'jquery' {
            resource id:'js', url:'js/jquery-3.2.1.min.js'
        }
    }

/*    annotations { // not used in working code of laser
        //dependsOn 'semanticUI'

        resource url:'js/deprecated/summernote.min.js' //used in annotations.js
        resource url:'css/deprecated/summernote.css', attrs: [media: 'screen,print']
        resource url:'css/deprecated/summernote-bs2.css', attrs: [media: 'screen,print']

        resource url:'js/deprecated/legacy.annotations.js' // used in deprecated code
        resource url:'css/deprecated/legacy.annotations.css', attrs: [media: 'screen,print']
    }*/

    treeSelects { // used only for onix witch will be rebuild
        dependsOn 'jquery'

        resource url:'css/jstree-themes/default/style.min.css'
        resource url:'js/libs/jstree.min.js'

        resource url:'js/legacy.tree-selects.js'
    }

    onixMatrix { //  modules="onixMatrix" not used in working code of laser
        dependsOn 'semanticUI'

        resource url:'css/legacy.onix.css', attrs: [media: 'screen,print']
        resource url:'js/legacy.onix.js'
    }

    deprecated {

        //resource url:'css/select2.css'


        resource url:'js/tmp_semui.js'   // only tmp
        resource url:'css/tmp_semui.css', attrs: [media: 'screen,print']   // only tmp
    }
    scaffolding {
        resource url:"css/instances/deprecated.css", attrs: [media: 'screen,print']   // legacy
    }


    semanticUI {
        dependsOn 'base'

        resource url:'semantic/javascript/semantic.js'         // new stuff
        resource url:'semantic/laser/semantic.min.css', attrs: [media: 'screen,print']       // new stuff

    }
    accessibility {
        dependsOn 'base'
        //dependsOn 'semanticUI'

        resource url:'semantic/javascript/semantic.min.js'         // new stuff
        resource url:'semantic/accessibility/semantic.min.css', attrs: [media: 'screen,print']       // new stuff

    }
    datatables {
        resource url:'js/libs/datatables.min.js'
        resource url:'css/datatables.css', attrs: [media: 'screen,print']
    }
    chartist {
        resource url:'chartist/javascript/chartist.min.js'
        resource url:'chartist/css/chartist.css', attrs: [media: 'screen,print']
    }
    base {
        dependsOn 'jquery'
        dependsOn 'deprecated'

        // legacy CRAP ..
        // legacy CRAP ..
        // legacy CRAP ..

        resource url:'semantic-restoration/jquery-editable.css', attrs: [media: 'screen,print']               // updated stuff
        resource url:'semantic-restoration/jquery.poshytip.js'                // updated stuff
        resource url:'semantic-restoration/jquery-editable-poshytip.min.js'   // updated stuff

        resource url:'js/libs/moment-with-locales.min.js'
        resource url:'js/libs/moment.min.js'

        resource url:'semantic-restoration/jquery.readmore.min.js' // new stuff

        //resource url:'css/select2.css'            // updated stuff // new version 3.5.4
        resource url:'css/select2-laser.css', attrs: [media: 'screen,print']       // overwrite to look more like semantic ui
        resource url:'js/select2.min.js'         // updated stuff // new version 3.5.4
        resource url:'js/readmore.min.js'

        resource url:'js/submodules/dict.js.gsp'
        resource url:'js/application.js.gsp'
        resource url:'js/submodules/decksaver.js.gsp'
        resource url:'js/submodules/tooltip.js.gsp'
        resource url:'js/submodules/bb8.js.gsp'
        resource url:'js/submodules/a11y/collections/menu.js.gsp'
        resource url:'js/submodules/a11y/elements/icon.js.gsp'
    }

    swaggerApi {
        dependsOn 'jquery'

        resource url:'vendor/swagger-ui/swagger-ui.css', attrs: [media: 'screen,print']

        resource url:'vendor/swagger-ui/swagger-ui-bundle.js'
        resource url:'vendor/swagger-ui/swagger-ui-standalone-preset.js'
        resource url:'vendor/cryptoJS-v3.1.2/rollups/hmac-sha256.js'
    }
}
