package com.k_int.kbplus

class Transformer {
		
	String name
	String url

	Date dateCreated
	Date lastUpdated
	
    static mapping = {
		table 'transformer'
		id column:'tfmr_id', generator: 'increment'
		name column:'tfmr_name'
		url column:'tfmr_url'

		dateCreated column: 'tfmr_date_created'
		lastUpdated column: 'tfmr_last_updated'
    }
	
	static constraints = {
		//id(nullable:false, unique: true, blank:false)
		name(nullable:false, blank:false)
		url(nullable:false, unique: true, blank:false)

		// Nullable is true, because values are already in the database
		lastUpdated (nullable: true, blank: false)
		dateCreated (nullable: true, blank: false)
	}
	
	static hasMany = [ transforms : Transforms ]
}
