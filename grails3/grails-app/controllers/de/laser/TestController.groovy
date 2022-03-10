package de.laser

import de.laser.auth.Perm
import de.laser.auth.PermGrant
import de.laser.auth.Role
import grails.plugin.springsecurity.annotation.Secured


/**
 *	Used for tests and debugging
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TestController {

    def contextService
	def accessService

	/**
	 * Currently not used anywhere
	 */
    @Secured(['ROLE_YODA'])
    def index() {

		// AccessService - private boolean checkOrgPerm(Org contextOrg, String[] orgPerms) {

		boolean check = false
		def contextOrg = contextService.getOrg()
		String[] orgPerms = ["ORG_BASIC_MEMBER", "ORG_CONSORTIUM"]
		if (orgPerms) {
			Org ctx = contextOrg
			def oss = OrgSetting.get(ctx, OrgSetting.KEYS.CUSTOMER_TYPE)

			Role fakeRole
			//println(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes().params)
			//println(oss.getValue())
			boolean isOrgBasicMemberView = false
			try {
				isOrgBasicMemberView = org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes().params.orgBasicMemberView
			} catch (IllegalStateException e) {}

			if(isOrgBasicMemberView && (oss.getValue() == Role.findAllByAuthority('ORG_CONSORTIUM'))){
				fakeRole = Role.findByAuthority('ORG_BASIC_MEMBER')
			}

			if (oss != OrgSetting.SETTING_NOT_FOUND) {
				orgPerms.each{ cd ->
					check = check || PermGrant.findByPermAndRole(Perm.findByCode(cd?.toLowerCase()?.trim()), (Role) fakeRole ?: oss.getValue())
				}
			}
		} else {
			check = true
		}

		// }

		def o1 = OrgSetting.get(contextService.getOrg(), OrgSetting.KEYS.CUSTOMER_TYPE)

		def pm1 = Perm.findByCode("ORG_CONSORTIUM".toLowerCase()?.trim())

		def pg1 = PermGrant.findByPermAndRole(pm1, (Role) o1.getValue())
		def pg2 = PermGrant.findByPermAndRole(pm1, o1.roleValue)
		def pg3 = PermGrant.findByPermAndRole(pm1, Role.get(24))

		def pg4 = PermGrant.executeQuery('select pg from PermGrant pg where pg.perm.id = 6 and pg.role.id = 24')

		Map<String, Object> result = [
			user: [
				'contextService.getUser()' :
						contextService.getUser(),
				'contextService.getUser().affiliations' :
							contextService.getUser().affiliations
			],
			org: [
				'contextService.getOrg()' :
						contextService.getOrg(),
				'OrgSetting.get(contextService.getOrg(), OrgSetting.KEYS.CUSTOMER_TYPE)' :
						o1,
				'OrgSetting.get(contextService.getOrg(), OrgSetting.KEYS.CUSTOMER_TYPE)?.getValue()' :
						o1?.getValue()
			],
			role: [
					'OrgSetting.roleValue' :
							o1.roleValue,
					'Role.get(o1.roleValue.id)' :
						Role.get(o1.roleValue.id),
					'pm1' :
						pm1,
					'pg1' :
						pg1,
					'pg2' :
						pg2,
					'pg3' :
						pg3,
					'pg4' :
						pg4
			],
			accessService: [
				'accessService.checkPerm("ORG_BASIC_MEMBER")' :
						accessService.checkPerm("ORG_BASIC_MEMBER"),
				'accessService.checkPerm("ORG_INST")' :
						accessService.checkPerm("ORG_INST"),
				'accessService.checkPerm("ORG_CONSORTIUM")' :
						accessService.checkPerm("ORG_CONSORTIUM"),

				'accessService_checkOrgPerm_fragment' : check
			]
		]

		//response.setContentType(Constants.MIME_APPLICATION_JSON)
		//render result

		render text: result
    }
}
