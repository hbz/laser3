package de.laser

import grails.gorm.transactions.Transactional
import org.hibernate.Session

/**
 * A container service for methods used widespread in the system
 */
@Transactional
class GlobalService {

    def sessionFactory

    /**
     * Clears the session from residual objects. Necessary for bulk operations which slow down
     * when the GORM container fills up
     */
    void cleanUpGorm() {
        log.debug("Clean up GORM")

        Session session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }
}