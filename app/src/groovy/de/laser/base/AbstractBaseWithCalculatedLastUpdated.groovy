package de.laser.base

import de.laser.interfaces.CalculatedLastUpdated
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 *  class Test extends AbstractBaseWithCalculatedLastUpdated
 *
 *  static mapping     = { globalUID column:'test_guid' .. }
 *  static constraints = { globalUID(nullable:true, blank:false, unique:true, maxSize:255) .. }
 *
 */

abstract class AbstractBaseWithCalculatedLastUpdated extends AbstractBase
        implements CalculatedLastUpdated {

    //@Autowired
    def cascadingUpdateService // DO NOT OVERRIDE IN SUB CLASSES

    static Log static_logger = LogFactory.getLog(AbstractBaseWithCalculatedLastUpdated)

    protected void afterInsertHandler() {
        static_logger.debug("afterInsertHandler()")

        cascadingUpdateService.update(this, dateCreated)
    }

    protected void afterUpdateHandler() {
        static_logger.debug("afterUpdateHandler()")

        cascadingUpdateService.update(this, lastUpdated)
    }

    protected void afterDeleteHandler() {
        static_logger.debug("afterDeleteHandler()")

        cascadingUpdateService.update(this, new Date())
    }

    abstract def afterInsert() /* { afterInsertHandler() } */

    abstract def afterUpdate() /* { afterUpdateHandler() } */

    abstract def afterDelete() /* { afterDeleteHandler() } */

    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }
}
