package com.k_int.kbplus

import grails.gorm.transactions.Transactional

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

/**
 * This service handles multi-thread processing (apart from ForkJoinPool-worker-used procedures)
 */
@Transactional
class ExecutorWrapperService {

	ExecutorService executorService
	GenericOIDService genericOIDService

	ConcurrentHashMap<Object,java.util.concurrent.FutureTask> activeFuture = [:]

	/**
	 * Takes the given closure and initialises an external thread to process it
	 * @param clos the closure which should be externalised
	 * @param owner the domain class to which the procedure is attached
	 */
	def processClosure(clos,owner){
		log.debug('processClosure: ' + owner)
		def newOwner = "${owner?.class?.name}:${owner?.id}"
		//see if we got a process running for owner already
		def existingFuture = activeFuture.get(newOwner)
		if(!existingFuture){
			//start new thread and store the process
		      def future = executorService.submit(clos as java.util.concurrent.Callable)
		      activeFuture.put(newOwner,future)
		}else{
			//if a previous process for this owner is done, remove it and start new one
			if(existingFuture.isDone()){
				activeFuture.remove(newOwner)
				processClosure(clos,genericOIDService.resolveOID(newOwner))
			}
			//if not done, do something else
		}
	}

	/**
	 * Checks if to the given domain class has a parallel process running or not
	 * @param owner the domain class to check
	 * @return true if there is a process and it is running, false otherwise
	 */
	boolean hasRunningProcess(owner){
		owner = "${owner.class.name}:${owner.id}"
		// There is no process running for this owner
		if(activeFuture.get(owner) == null){
			return false
		// there was a process, but now its done.
		}else if(activeFuture.get(owner).isDone()){
			activeFuture.remove(owner)
			return false
		// we have a running process
		}else if(activeFuture.get(owner).isDone() == false){
			return true
		}
	}
}

