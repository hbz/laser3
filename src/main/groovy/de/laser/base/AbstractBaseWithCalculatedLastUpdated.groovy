package de.laser.base

import de.laser.traces.DeletedObject
import de.laser.storage.BeanStore
import de.laser.interfaces.CalculatedLastUpdated
import groovy.util.logging.Slf4j

/**
 *  class Test extends AbstractBaseWithCalculatedLastUpdated
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
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
        DeletedObject.withTransaction {
            if(this.hasProperty('isPublicForApi') && this.isPublicForApi)
                DeletedObject.construct(this)
            else if(!this.hasProperty('isPublicForApi'))
                DeletedObject.construct(this)
        }
    }

    abstract def afterInsert() /* { afterInsertHandler() } */

    abstract def afterUpdate() /* { afterUpdateHandler() } */

    abstract def afterDelete() /* { afterDeleteHandler() } */

    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }
}
