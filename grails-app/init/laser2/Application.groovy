package laser2

import de.laser.helper.ConfigUtils
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource

class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        File externalConfig = ConfigUtils.getConfigFile(environment)

        if (externalConfig.exists()) {
            log.info("-----> Loading local configuration file: ${externalConfig.absolutePath} <-----")
            ConfigObject config = new ConfigSlurper().parse(externalConfig.toURI().toURL())
            environment.propertySources.addFirst(new MapPropertySource("externalGroovyConfig", config))
        }
        else {
            log.warn("Local configuration file not found: ${externalConfig.absolutePath}")
        }
    }
}
