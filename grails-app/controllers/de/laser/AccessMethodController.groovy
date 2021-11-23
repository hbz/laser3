package de.laser


import de.laser.helper.DateUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

import java.text.SimpleDateFormat

/**
 * This controller is responsible for access method manipulations. See the domain definition for the use of a platform access method
 * @see PlatformAccessMethod
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AccessMethodController  {

    ContextService contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], update: ['GET', 'POST'], delete: 'GET']

    /**
     * Creates a new platform access method with the given parameters and redirects to the list after having created it
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def create() {
        params.max = params.max ?: contextService.getUser().getDefaultPageSize()

        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        if (params.validFrom) {
            params.validFrom = sdf.parse(params.validFrom)
        } else {
            //params.validFrom =  sdf.parse(new Date().format( 'dd.MM.yyyy' ));
        }
        if (params.validTo) {
            params.validTo = sdf.parse(params.validTo)
        } else {
            //params.validTo =sdf.parse(new Date().format( 'dd.MM.yyyy' ));
        }
        PlatformAccessMethod accessMethod = new PlatformAccessMethod(params)

        if (params.validTo && params.validFrom && params.validTo.before(params.validFrom)) {
            flash.error = message(code: 'accessMethod.dateValidationError', args: [message(code: 'accessMethod.label'), accessMethod.accessMethod])
        } else {

            accessMethod.platf = Platform.get(params.platfId)

            accessMethod.accessMethod = RefdataValue.get(params.accessMethod)

            accessMethod.save()
            accessMethod.errors.toString()

            flash.message = message(code: 'accessMethod.create.message', args: [accessMethod.accessMethod.getI10n('value')])
        }

        if (params.redirect) {
            redirect(url: request.getHeader('referer'), params: params)
            return
        }
        else {
            redirect controller: 'platform', action: 'AccessMethods', id: params.platfId
            return
        }
        
        //[personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }

    /**
     * Deletes the given platform access method
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def delete() {
        PlatformAccessMethod accessMethod = PlatformAccessMethod.get(params.id)

        Platform platform = accessMethod.platf
        Long platformId = platform.id
        
        try {
            accessMethod.delete()
			flash.message = message(code: 'accessMethod.deleted', args: [accessMethod.accessMethod.getI10n('value')])
            redirect controller: 'platform', action: 'AccessMethods', id: platformId
            return
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label'), params.id])
            redirect action: 'show', id: params.id
            return
        }
    }

    /**
     * Opens the edit view and loads the given platform access method for editing
     */
    @Secured(['ROLE_USER'])
    def edit() {
        PlatformAccessMethod accessMethod= PlatformAccessMethod.get(params.id)
        Platform platf = accessMethod.platf

        [accessMethod: accessMethod, platfId: platf.id]

    }

    /**
     * Updates the given platform access method with the given parameters
     * @return renders the access method list on success, the platform access method edit view upon failure
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def update() {
        PlatformAccessMethod accessMethod= PlatformAccessMethod.get(params.id)
        Platform platf = accessMethod.platf


        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        if (params.validFrom) {
            params.validFrom = sdf.parse(params.validFrom)
        } else {
            //params.validFrom =  sdf.parse(new Date().format( 'dd.MM.yyyy' ));
        }
        if (params.validTo) {
            params.validTo = sdf.parse(params.validTo)
        } else {
            //params.validTo =sdf.parse(new Date().format( 'dd.MM.yyyy' ));
        }

        if (params.validTo == "" || params.validTo && params.validFrom && params.validTo.before(params.validFrom)) {
            flash.error = message(code: 'accessMethod.dateValidationError', args: [message(code: 'accessMethod.label'), accessMethod.accessMethod])
            redirect controller: 'accessMethod', action: 'edit', id: accessMethod.id
            return
        } else {
            accessMethod.validTo = params.validTo
            accessMethod.lastUpdated = new Date()

            accessMethod.save()
            accessMethod.errors.toString()

            flash.message = message(code: 'accessMethod.create.message', args: [accessMethod.accessMethod.getI10n('value')])
            flash.message = message(code: 'accessMethod.updated', args: [accessMethod.accessMethod.getI10n('value')])
            redirect controller: 'platform', action: 'accessMethods', id: platf.id
            return
        }
    }
}
