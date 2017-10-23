

class FileViewerGrailsPlugin {
    // the plugin version
    def version = "0.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.1 > *"
    // the other plugins this plugin depends on
	def dependsOn = [:]

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Himanshu Seth, Fabien Benichou"
    def authorEmail = "himanshu@intelligrape.com, fabien.benichou@gmail.com"
    def title = "File Viewer Grails plugin"
    def description = '''\\
File Viewer Grails plugin provides a user friendly way for viewing folders and files on the system. The most common use-case is to see the logs on the server.
Documentation available at http://github.com/IntelliGrape/File-Viewer-Grails-Plugin/wiki
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/file-viewer"

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
		def config = application.config.grails.fileViewer
		fileLocations(org.grails.plugins.fileviewer.FileLocations) {
			locations = config.locations ?: [System.getProperty("java.io.tmpdir")]
			linesCount = config.linesCount ?: 300
            areDoubleDotsAllowedInFilePath = config.areDoubleDotsAllowedInFilePath ?: false
		}
	}

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
    }

    def onChange = { event ->
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // The event is the same as for 'onChange'.
    }
}
