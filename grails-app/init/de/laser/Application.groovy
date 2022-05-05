package de.laser

import de.laser.helper.ConfigMapper
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.util.logging.Slf4j
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource

@Slf4j
class Application extends GrailsAutoConfiguration implements EnvironmentAware {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        File externalConfig = ConfigMapper.getCurrentConfigFile(environment)

        if (externalConfig.exists()) {
            log.info("| ----->")
            log.info("| -----> Found local configuration file: ${externalConfig.absolutePath}")
            log.info("| ----->")
            ConfigObject config = new ConfigSlurper().parse(externalConfig.toURI().toURL())
            environment.propertySources.addFirst(new MapPropertySource('externalGroovyConfig', config))
        }
        else {
            log.warn("| -----> Local configuration file NOT found: ${externalConfig.absolutePath}")
        }
    }
}
