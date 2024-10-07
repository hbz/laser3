package de.laser

import grails.gorm.transactions.Transactional

/**
 * This service manages access control checks
 */
@Transactional
class AccessService {

    static final String CHECK_VIEW = 'CHECK_VIEW'
    static final String CHECK_EDIT = 'CHECK_EDIT'
    static final String CHECK_VIEW_AND_EDIT = 'CHECK_VIEW_AND_EDIT'

}
