package laser3

import de.laser.helper.ConfigUtils
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment

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
            static_logger.info("-----> Loading local configuration file: ${externalConfig.absolutePath} <-----")
            ConfigObject config = new ConfigSlurper().parse(externalConfig.toURI().toURL())
            // TODO --
            println 'environment.propertySources.addFirst(new MapPropertySource("externalGroovyConfig", config))'
            println environment
            println config
        }
        else {
            static_logger.warn("Local configuration file not found: ${externalConfig.absolutePath}")
        }
    }
}
