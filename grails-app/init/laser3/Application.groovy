package laser3

import de.laser.helper.ConfigUtils
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource

class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static Log static_logger = LogFactory.getLog(Application)

    static void main(String[] args) {
        System.setProperty('spring.devtools.restart.enabled', 'false')
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        File externalConfig = ConfigUtils.getConfigFile(environment)

        if (externalConfig.exists()) {
            static_logger.info("-----> Found local configuration file: ${externalConfig.absolutePath} <-----")
            ConfigObject config = new ConfigSlurper().parse(externalConfig.toURI().toURL())
            environment.propertySources.addFirst(new MapPropertySource('externalGroovyConfig', config))
        }
        else {
            static_logger.warn("-----> Local configuration file NOT found: ${externalConfig.absolutePath} <-----")
        }
    }
}
