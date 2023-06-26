package changelogs

import de.laser.Org
import de.laser.UserSetting
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrgRole
import de.laser.system.SystemEvent

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1685433904452-1") {
        addColumn(tableName: "user") {
            column(name: "usr_formal_org_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1685433904452-2") {
        addColumn(tableName: "user") {
            column(name: "usr_formal_role_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1685433904452-3") {
        addForeignKeyConstraint(baseColumnNames: "usr_formal_role_fk", baseTableName: "user", constraintName: "FK2kywkmm9j78p6d82si41vjjum", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "r_id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1685433904452-4") {
        addForeignKeyConstraint(baseColumnNames: "usr_formal_org_fk", baseTableName: "user", constraintName: "FK7u6g0fgaftqni6uaf7ka3ku8j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1685433904452-5") {
        dropColumn(columnName: "usr_shibb_scope", tableName: "user")
    }

    changeSet(author: "klober (modified)", id: "1685433904452-6") {
        grailsChange {
            change {
                Map<String, Object> result = [simple: [], derived: [], orphaned: [], problems: []]

                User.findAll().each { user ->
                    List<UserOrgRole> orgList = UserOrgRole.findAllByUser(user)

                    if (orgList) {
                        UserSetting us = UserSetting.findByUserAndKey(user, UserSetting.KEYS.DASHBOARD)
                        Org org = null
                        Role role = null

                        if (orgList.size() == 1) {
                            org = orgList[0].org
                            role = orgList[0].formalRole
                        }
                        else if (orgList.size() > 1) {
                            if (us) {
                                org = us.orgValue
                                role = UserOrgRole.findByUserAndOrg(user, org)?.formalRole
                            }
                        }

                        if (org && role) {
                            if (orgList.size() == 1) {
                                result.simple << [ user: user.id, formalOrg: org.id, formalRole: role.id, old_dashboard: us?.orgValue?.id ]
                            }
                            else {
                                result.derived << [ user: user.id, formalOrg: org.id, formalRole: role.id, old_dashboard: us?.orgValue?.id, old_userOrgRoles: orgList.collect{[user: it.user.id, org: it.org.id, role: it.formalRole.id]} ]
                            }

                            User.executeUpdate('update User set formalOrg = :formalOrg, formalRole = :formalRole where id = :id', [formalOrg: org, formalRole: role, id: user.id])
                            UserSetting.executeUpdate('delete from UserSetting where user = :user and key = :key', [user: user, key: UserSetting.KEYS.DASHBOARD])
                            UserOrgRole.executeUpdate('delete from UserOrgRole where user = :user', [user: user])
                        }
                        else {
                            result.problems << [ user: user.id, old_dashboard: us?.orgValue?.id, old_userOrgRoles: orgList.collect{[user: it.user.id, org: it.org.id, role: it.formalRole.id]}  ]
                        }
                    }
                    else {
                        result.orphaned << [ user: user.id ]
                    }
                }
                if (result.simple)      { SystemEvent.createEvent('DBM_SCRIPT_INFO', ['DBM: formalOrg/formalRole for users', 'Strategy: Simple copy from UserOrgRole', result.simple]) }
                if (result.derived)     { SystemEvent.createEvent('DBM_SCRIPT_INFO', ['DBM: formalOrg/formalRole for users', 'Strategy: Derived from UserSettings; multiple UserOrgRole found', result.derived]) }
                if (result.orphaned)    { SystemEvent.createEvent('DBM_SCRIPT_INFO', ['DBM: formalOrg/formalRole for users', 'Strategy: Ignored; because no UserOrgRole found', result.orphaned]) }
                if (result.problems)    { SystemEvent.createEvent('DBM_SCRIPT_INFO', ['DBM: formalOrg/formalRole for users', 'Processing problems', result.problems]) }
            }
            rollback {}
        }
    }
}
