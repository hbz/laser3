package de.laser


import de.laser.interfaces.ShareSupport
import de.laser.storage.RDStore
import de.laser.wekb.Package
import de.laser.wekb.ProviderRole
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.VendorRole
import grails.gorm.transactions.Transactional

/**
 * This service handles the sharing of objects from a parent to member objects.
 * This is insofar different from inheriting as no object copies are generated, only the access is being multiplied
 * @see DocContext
 * @see ShareSupport
 */
@Transactional
class ShareService {

    /**
     * Adds a share flag to the given document link so that the document may be accessed from member objects as well
     * @param share the context to be shared
     * @param target the share flag
     * @return true if the setting was successful, false otherwise
     */
    boolean addDocShareForTarget(DocContext share, ShareSupport target) {

        if (share.sharedFrom) {
            return false
        }
        // todo check existence

        DocContext clonedShare = new DocContext(
                owner:          share.owner ,
                license:        (target instanceof License) ? target : share.license,
                subscription:   (target instanceof Subscription) ? target : share.subscription,
                link:           (target instanceof Links) ? target : share.link,
                sharedFrom:     share,
                isShared:       false
        )
        if (clonedShare.license && clonedShare.license.getAllLicensee().size() == 1)
            clonedShare.targetOrg = clonedShare.license.getLicensee()
        else if(clonedShare.subscription)
            clonedShare.targetOrg = clonedShare.subscription.getSubscriber()
        if (clonedShare.save()) {
            if(target instanceof Subscription) {
                //damn that three-tier inheritance level ... check if there are departments for a consortial subscription!!!! Show David!!!
                List<Subscription> descendants = Subscription.findAllByInstanceOf(target)
                descendants.each { Subscription d ->
                    DocContext clonedDescendantShare = new DocContext(
                            owner:          share.owner ,
                            subscription:   d,
                            sharedFrom:     share,
                            targetOrg:      d.getSubscriber(),
                            isShared:       false
                    )
                    clonedDescendantShare.save()
                }
            }
            if (! share.isShared) {
                share.isShared = true
                if (share.save()) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Removes the given share flag
     * @param share unused
     * @param target the share flag to unset
     * @return true if the flag could be removed successfully, false otherwise
     */
    boolean deleteDocShareForTarget(ShareSupport target) {

        String tp =
                (target instanceof License) ? 'license' :
                        (target instanceof Subscription) ? 'subscription' :
                                (target instanceof Links) ? 'link' :
                                        null

        if (tp) {
            DocContext.executeUpdate('delete from DocContext dc where dc.sharedFrom = :sf and ' + tp + ' = :target', [sf: this, target: target])
            return true
        }
        return false
    }

    /**
     * Adds a share flag to the given organisational relation so that the organisation may be accessed from member objects as well
     * @param share the role to be shared
     * @param target the share flag
     * @return true if the setting was successful, false otherwise
     */
    boolean addOrgRoleShareForTarget(OrgRole share, ShareSupport target) {

        if (share.sharedFrom) {
            return false
        }
        // todo check existence

        OrgRole clonedShare = new OrgRole(
                org:            share.org ,
                lic:            (target instanceof License) ? target : share.lic,
                sub:            (target instanceof Subscription) ? target : share.sub,
                pkg:            (target instanceof Package) ? target : share.pkg,
                tipp:           (target instanceof TitleInstancePackagePlatform) ? target : share.tipp,
                roleType:       share.roleType,
                startDate:      share.startDate,
                endDate:        share.endDate,
                sharedFrom:     share,
                isShared:       false
        )

        if (clonedShare.save()) {
            if (! share.isShared) {
                share.isShared = true
                if (share.save()) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Adds a share flag to the given vendor relation so that the vendor may be accessed from member objects as well
     * @param share the role to be shared
     * @param target the share flag
     * @return true if the setting was successful, false otherwise
     */
    boolean addVendorRoleShareForTarget(VendorRole share, ShareSupport target) {

        if (share.sharedFrom) {
            return false
        }
        // todo check existence

        VendorRole clonedShare = new VendorRole(
                vendor:         share.vendor ,
                license:        (target instanceof License) ? target : share.license,
                subscription:   (target instanceof Subscription) ? target : share.subscription,
                sharedFrom:     share,
                isShared:       false
        )

        if (clonedShare.save()) {
            if (! share.isShared) {
                share.isShared = true
                if (share.save()) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Adds a share flag to the given vendor relation so that the vendor may be accessed from member objects as well
     * @param share the role to be shared
     * @param target the share flag
     * @return true if the setting was successful, false otherwise
     */
    boolean addProviderRoleShareForTarget(ProviderRole share, ShareSupport target) {

        if (share.sharedFrom) {
            return false
        }
        // todo check existence

        ProviderRole clonedShare = new ProviderRole(
                provider:       share.provider ,
                license:        (target instanceof License) ? target : share.license,
                subscription:   (target instanceof Subscription) ? target : share.subscription,
                sharedFrom:     share,
                isShared:       false
        )

        if (clonedShare.save()) {
            if (! share.isShared) {
                share.isShared = true
                if (share.save()) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Removes the given share flag
     * @param share unused
     * @param target the share flag to unset
     * @return true if the flag could be removed successfully, false otherwise
     */
    boolean deleteOrgRoleShareForTarget(ShareSupport target) {

        String tp =
                (target instanceof License) ? 'lic' :
                        (target instanceof Subscription) ? 'sub' :
                                (target instanceof Package) ? 'pkg' :
                                        (target instanceof TitleInstancePackagePlatform) ? 'tipp' : // todo: remove
                                                null

        if (tp) {
            OrgRole.executeUpdate('delete from OrgRole oorr where oorr.sharedFrom = :sf and ' + tp + ' = :target', [sf: this, target: target])
            return true
        }
        return false
    }

    /**
     * Removes the given share flag
     * @param target the share flag to unset
     * @return true if the flag could be removed successfully, false otherwise
     */
    boolean deleteProviderRoleShareForTarget(ShareSupport target) {

        String tp =
                (target instanceof License) ? 'lic' :
                        (target instanceof Subscription) ? 'sub' :
                                null

        if (tp) {
            ProviderRole.executeUpdate('delete from ProviderRole pvr where pvr.sharedFrom = :sf and ' + tp + ' = :target', [sf: this, target: target])
            return true
        }
        return false
    }

    /**
     * Removes the given share flag
     * @param target the share flag to unset
     * @return true if the flag could be removed successfully, false otherwise
     */
    boolean deleteVendorRoleShareForTarget(ShareSupport target) {

        String tp =
                (target instanceof License) ? 'lic' :
                        (target instanceof Subscription) ? 'sub' :
                                null

        if (tp) {
            VendorRole.executeUpdate('delete from VendorRole vr where vr.sharedFrom = :sf and ' + tp + ' = :target', [sf: this, target: target])
            return true
        }
        return false
    }
}