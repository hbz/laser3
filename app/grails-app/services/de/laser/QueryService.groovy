package de.laser

import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SystemAdmin
import com.k_int.kbplus.abstract_domain.AbstractProperty

class QueryService {

    private def getDueSubscriptions(java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        Org institution = contextService.getOrg()
        def role_sub            = RefdataValue.getByValueAndCategory('Subscriber','Organisational Role')
        def role_subCons        = RefdataValue.getByValueAndCategory('Subscriber_Consortial','Organisational Role')
        def role_sub_consortia  = RefdataValue.getByValueAndCategory('Subscription Consortia','Organisational Role')
        def sub_status_deleted  = RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')
        def base_qry
        def qry_params
        boolean isSubscriptionConsortia = ((RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType') in institution.getallOrgRoleType()))
        boolean isSubscriber = ! isSubscriptionConsortia

        if (isSubscriber) {
            base_qry = "from Subscription as s where ( "+
                    "exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :activeInst ) ) "+
                    "AND ( s.status.value != 'Deleted' ) "+
                    "AND ( "+
                    "( not exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) ) "+
                    "or "+
                    "( ( exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) ) AND ( s.instanceOf is not null) ) "+
                    ") "+
                    ")"
            qry_params = ['roleType1':role_sub, 'roleType2':role_subCons, 'activeInst':institution, 'scRoleType':role_sub_consortia]
        }

        if (isSubscriptionConsortia) {
            base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) ) AND ( s.instanceOf is null AND s.status.value != 'Deleted' ) "
            qry_params = ['roleType':role_sub_consortia, 'activeInst':institution]
        }

        if (endDateFrom && endDateTo) {
            base_qry += " and (endDate >= :endFrom and endDate <= :endTo)"
            qry_params.put("endFrom", endDateFrom)
            qry_params.put("endTo", endDateTo)
        }

        if (manualCancellationDateFrom && manualCancellationDateTo){
            base_qry +=" or (manualCancellationDate >= :cancellFrom and manualCancellationDate <= :cancellTo) "
            qry_params.put("cancellFrom", manualCancellationDateFrom)
            qry_params.put("cancellTo", manualCancellationDateTo)
        }

        base_qry += " and status != :status "
        qry_params.put("status", sub_status_deleted)

        Subscription.executeQuery("select s ${base_qry}", qry_params)
    }

}