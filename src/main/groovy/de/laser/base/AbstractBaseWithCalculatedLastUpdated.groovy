package de.laser.base

import de.laser.storage.BeanStore
import de.laser.interfaces.CalculatedLastUpdated
import groovy.util.logging.Slf4j

/**
 *  Abstract base class, extending the functionality of {@link AbstractBase} with automatic timestamp update
 *  for incremental harvesters using the LAS:eR API
 *
 *  implementation guide for implementing classes:
 *  class Test extends AbstractBaseWithCalculatedLastUpdated
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 */
@Slf4j
abstract class AbstractBaseWithCalculatedLastUpdated extends AbstractBase
        implements CalculatedLastUpdated {

    protected void afterInsertHandler() {
        log.debug("afterInsertHandler()")

        BeanStore.getCascadingUpdateService().update(this, dateCreated)
    }

    protected void afterUpdateHandler() {
        log.debug("afterUpdateHandler()")

        BeanStore.getCascadingUpdateService().update(this, lastUpdated)
    }

    protected void afterDeleteHandler() {
        log.debug("afterDeleteHandler()")

        BeanStore.getCascadingUpdateService().update(this, new Date())
    }

    abstract def afterInsert() /* { afterInsertHandler() } */

    abstract def afterUpdate() /* { afterUpdateHandler() } */

    abstract def afterDelete() /* { afterDeleteHandler() } */

    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }
}
