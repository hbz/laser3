import de.laser.License
import de.laser.OrgRole

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1645004653876-1") {
        grailsChange {
            change {
                Set<OrgRole> rolesConcerned = OrgRole.executeQuery('select oo from OrgRole oo where oo.lic != null and oo.isShared = true')
                rolesConcerned.each { OrgRole oo ->
                    Set<License> childLicenses = OrgRole.executeQuery('select lic from OrgRole os where os.sharedFrom = :oo', [oo: oo])
                    Set<License> totalLicenses = oo.lic.getDerivedLicenses()
                    totalLicenses.removeAll(childLicenses)
                    //remainder
                    totalLicenses.each { License l ->
                        new OrgRole([lic: l, org: oo.org, roleType: oo.roleType, sharedFrom: oo]).save()
                    }
                }
            }
            rollback {}
        }
    }

}
