import de.laser.OrgSetting
import de.laser.RefdataValue
import de.laser.UserSetting
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.storage.RDStore

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1624608360904-1") {
        grailsChange {
            change {
                List<User> nonConsortiaUsers = User.executeQuery("select uo.user from UserOrg uo where uo.org in (select os.org from OrgSetting os where os.key = :customerType and os.roleValue in (:nonConsortia))",[customerType: OrgSetting.KEYS.CUSTOMER_TYPE, nonConsortia: Role.findAllByAuthorityInList(["ORG_BASIC_MEMBER", "ORG_INST"])])
                nonConsortiaUsers.each { User user ->
                    UserSetting setting = user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH, RDStore.YN_YES)
                    if(setting.value != RDStore.YN_YES) {
                        println("updating user ${user.id}")
                        setting.setValue(RDStore.YN_YES)
                    }
                }
            }
            rollback {}
        }
    }

}
