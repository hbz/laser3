package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException

class ClusterController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [clusterInstanceList: Cluster.list(params), clusterInstanceTotal: Cluster.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':
        	[clusterInstance: new Cluster(params)]
			break
		case 'POST':
	        def clusterInstance = new Cluster(params)
	        if (!clusterInstance.save(flush: true)) {
	            render view: 'create', model: [clusterInstance: clusterInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'cluster.label', default: 'Cluster'), clusterInstance.id])
	        redirect action: 'show', id: clusterInstance.id
			break
		}
    }

    def show() {
        def clusterInstance = Cluster.get(params.id)
        if (!clusterInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), params.id])
            redirect action: 'list'
            return
        }

        [clusterInstance: clusterInstance]
    }

    def edit() {
		switch (request.method) {
		case 'GET':
	        def clusterInstance = Cluster.get(params.id)
	        if (!clusterInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [clusterInstance: clusterInstance]
			break
		case 'POST':
	        def clusterInstance = Cluster.get(params.id)
	        if (!clusterInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (clusterInstance.version > version) {
	                clusterInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'cluster.label', default: 'Cluster')] as Object[],
	                          "Another user has updated this Cluster while you were editing")
	                render view: 'edit', model: [clusterInstance: clusterInstance]
	                return
	            }
	        }

	        clusterInstance.properties = params

	        if (!clusterInstance.save(flush: true)) {
	            render view: 'edit', model: [clusterInstance: clusterInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'cluster.label', default: 'Cluster'), clusterInstance.id])
	        redirect action: 'show', id: clusterInstance.id
			break
		}
    }

    def delete() {
        def clusterInstance = Cluster.get(params.id)
        if (!clusterInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'cluster.label', default: 'Cluster'), params.id])
            redirect action: 'list'
            return
        }

        try {
            clusterInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'cluster.label', default: 'Cluster'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'cluster.label', default: 'Cluster'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
