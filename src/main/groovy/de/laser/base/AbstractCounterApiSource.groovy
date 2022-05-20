package de.laser.base

import de.laser.Org
import de.laser.Platform
import de.laser.exceptions.CreationException
import de.laser.stats.Counter4ApiSource
import de.laser.stats.Counter5ApiSource
import grails.converters.JSON

/**
 * Is actually deprecated and kept by reference; to be removed in later versions
 */
abstract class AbstractCounterApiSource {

    String baseUrl
    Org provider
    Platform platform
    String arguments

    static AbstractCounterApiSource construct(Map<String, Object> configMap) throws CreationException {
        AbstractCounterApiSource result
        Org provider = configMap.provider as Org
        Platform platform = configMap.platform as Platform
        switch(configMap.version) {
            case "counter4": result = Counter4ApiSource.findByProviderAndPlatform(provider, platform) //TODO further differentiators, check documentation!
                if(!result)
                    result = new Counter4ApiSource(provider: provider, platform: platform)
                break
            case "counter5":
                result = Counter5ApiSource.findByProviderAndPlatform(provider, platform)
                if(!result)
                    result = new Counter5ApiSource(provider: provider, platform: platform)
                break
            default: throw new CreationException("no COUNTER version supplied for new source!")
                break
        }
        result.baseUrl = configMap.baseUrl
        if(configMap.arguments) {
            JSON argumentsJSON = new JSON(configMap.arguments)
            result.arguments = argumentsJSON.toString()
        }
        else if(result.arguments && !configMap.arguments) {
            result.arguments = null
        }
        if(result.save(validate: true, failOnError: true))
            result
        else throw new CreationException("Error on saving / updating counter source: ${result.getErrors().getAllErrors().toListString()}")
    }

}
