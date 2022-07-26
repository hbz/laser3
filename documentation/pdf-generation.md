
## PDF Generation

**wkhtmltopdf** - server and local  
**xvfb-run** - server only

- https://wkhtmltopdf.org/
- https://plugins.grails.org/plugin/rlovtangen/wkhtmltopdf

### Configuration

**build.gradle**

    dependencies {
        implementation 'org.grails.plugins:wkhtmltopdf:1.0.0.RC9'
        // ..
    }

**grails-app/conf/application.yml**

    grails:
        plugin:
            wkhtmltopdf:
                binary: '/usr/bin/wkhtmltopdf'

**laser3-config.groovy**

    grails.plugin.wkhtmltopdf.binary     = '/usr/bin/wkhtmltopdf' // override for server and local
    grails.plugin.wkhtmltopdf.xvfbRunner = '/usr/bin/xvfb-run'    // override for server only

### Usage

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