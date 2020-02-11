package com.k_int.kbplus


import com.k_int.kbplus.auth.User;

class UserTransforms {
	
	User user
	Transforms transforms

	Date dateCreated
	Date lastUpdated
	
	static mapping = {
		table: 'user_transforms'
		user column:'ut_user_fk', index:'ut_user_id_idxfk_2'
		transforms column:'ut_transforms_fk', index:'ut_transforms_id__idxfk'

		dateCreated column: 'ut_date_created'
		lastUpdated column: 'ut_last_updated'
	}
	
	static constraints = {
		id composite: ['user', 'transforms']
		user(nullable:true, blank:false)
		transforms(nullable:true, blank:false)

		// Nullable is true, because values are already in the database
		lastUpdated (nullable: true, blank: false)
		dateCreated (nullable: true, blank: false)
	}
}
