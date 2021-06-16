
## PDF Generation

**wkhtmltopdf** - server and local  
**xvfb-run** - server only

- https://wkhtmltopdf.org/
- https://plugins.grails.org/plugin/rlovtangen/wkhtmltopdf

#### Configuration

**build.gradle**

    dependencies {
        compile 'org.grails.plugins:wkhtmltopdf:1.0.0.RC9'
        // ..
    }

**laser2-config.groovy**

    grails.plugin.wkhtmltopdf.binary     = '/usr/bin/wkhtmltopdf' // server and local
    grails.plugin.wkhtmltopdf.xvfbRunner = '/usr/bin/xvfb-run'    // server only

#### Usage

    def pdf = wkhtmltoxService.makePdf (
        view:  '/path/pdf',
        model: []
    )
    response.setHeader( 'Content-disposition', 'attachment; filename="test.pdf"' )
    response.setContentType( 'application/pdf' )
    response.outputStream.withStream{ it << pdf }

#### NOT SUPPORTED YET

    class SomeController {
        def someAction() {
            render( 
                filename: 'test.pdf',
                view:     '/path/pdf',
                model:    []
            )
        }
    }