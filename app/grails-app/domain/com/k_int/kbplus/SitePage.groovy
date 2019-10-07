package com.k_int.kbplus

import de.laser.domain.AbstractBaseDomain


class SitePage extends AbstractBaseDomain {
	String alias
	String action
	String controller
	String rectype = "action"

	Date dateCreated
	Date lastUpdated
	
	static constraints = {
        alias(nullable: false, blank: false, unique: true)
        globalUID(nullable: true, blank: false, unique: true, maxSize: 255)

		// Nullable is true, because values are already in the database
		lastUpdated (nullable: true, blank: false)
		dateCreated (nullable: true, blank: false)
    
    }


    def getLink() {
		def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()

		[linktext:alias, url:g.createLink(controller:controller,action:action)]
	}
}
