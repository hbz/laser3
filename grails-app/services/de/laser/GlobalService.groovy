package de.laser

import grails.gorm.transactions.Transactional
import org.hibernate.Session

@Transactional
class GlobalService {

    def sessionFactory

    void cleanUpGorm() {
        log.debug("Clean up GORM")

        Session session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }
}