package de.laser

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.util.concurrent.ExecutorService
import java.util.concurrent.FutureTask

/**
 * This is a controller for test functions
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DevController  {

    ContextService contextService
    ExecutorService executorService
    LicenseService licenseService

    @Secured(['ROLE_ADMIN'])
    def index() {
    }

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def frontend() {
        Map<String, Object> result = [
            user: contextService.getUser(),
            institution: contextService.getOrg()
        ]
        result
    }

    @Secured(['ROLE_ADMIN'])
    def backend() {
        Map<String, Object> result = [
            user: contextService.getUser(),
            institution: contextService.getOrg(),
            view: (params.id ?: 'index')
        ]
        render view: 'backend/' + result.view, model: result
    }

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def klodav() {
        Map<String, Object> result = [
            user: contextService.getUser(),
            institution: contextService.getOrg(),
            view: (params.id ?: 'index')
        ]

        if (result.view == 'threads') {
            List tasks = []

            (1..10).each { c ->
                FutureTask f = executorService.submit ({
                    Thread.currentThread().setName('klodav_1_' + c)
                    if (Math.random() > 0.75) {
                        throw new Exception()
                    } else {
                        Thread.sleep(new Random().nextLong(20))
                    }
                })
                tasks << f
            }
            (1..10).each { c ->
                FutureTask f = executorService.submit ({
                    Thread.currentThread().setName('klodav_2_' + c)
                    if (Math.random() > 0.75) {
                        throw new Exception()
                    } else {
                        Thread.wait(new Random().nextLong(20))
                    }
                })
                tasks << f
            }
            (1..10).each { c ->
                Runnable r = {
                    Thread.currentThread().setName('klodav_3_' + c)
                    if (Math.random() > 0.75) {
                        throw new Exception()
                    } else {
                        Thread.sleep(new Random().nextLong(20))
                    }
                }
                executorService.execute { r }
                tasks << r
            }
            (1..10).each { c ->
                Runnable r = {
                    Thread.currentThread().setName('klodav_4_' + c)
                    if (Math.random() > 0.75) {
                        throw new Exception()
                    } else {
                        Thread.wait(new Random().nextLong(20))
                    }
                }
                executorService.execute { r }
                tasks << r
            }
            result.executorService = executorService
            result.tasks = tasks
        }
        render view: 'klodav/' + result.view, model: result
    }

    /**
     * JavaScript call area
     */
    @Secured(['ROLE_ADMIN'])
    def jse() {
        if (params.xhr_full) {
            render template: 'jse_xhr_full'
        }
        else if (params.xhr) {
            render template: 'jse_xhr'
        }
        else {
            render view: 'jse'
        }
    }

    @Secured(['ROLE_ADMIN'])
    def onixValidationPrecheck() {
        Org institution = contextService.getOrg()
        License license = License.get(params.id)
        Map<String, Object> result = [validationErrors: licenseService.precheckValidation(license, institution)]
        if(result.validationErrors == null) {
            redirect controller: 'license', action: 'show', params: [export: 'onix', id: params.id]
        }
        else
            render result as JSON
    }

    @Secured(['ROLE_YODA'])
    def queryOutputChecker() {
        Set<Subscription> result = Subscription.executeQuery('select vr.subscription from VendorRole vr where exists (select v2 from VendorRole v2 where v2.subscription = vr.subscription and v2.sharedFrom = null) and exists (select v3 from VendorRole v3 where v3.subscription = vr.subscription and v3.sharedFrom != null)')
        flash.message = "subs concerned: ${result.collect { Subscription s -> "${s.id} => ${s.name} => ${s.getSubscriberRespConsortia().collect { Org oo -> "${oo.name} (${oo.sortname})" }.join(',')}" }.join('<br>')}"
        redirect controller: 'yoda', action: 'index'
    }
}
