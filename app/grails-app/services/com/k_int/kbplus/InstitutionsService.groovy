package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import de.laser.AccessService
import de.laser.AuditConfig
import de.laser.AuditService
import de.laser.helper.RDConstants
import de.laser.helper.RDStore

class InstitutionsService {

    def contextService
    AccessService accessService

    static final CUSTOM_PROPERTIES_COPY_HARD        = 'CUSTOM_PROPERTIES_COPY_HARD'
    static final CUSTOM_PROPERTIES_ONLY_INHERITED   = 'CUSTOM_PROPERTIES_ONLY_INHERITED'

    License copyLicense(License base, params, Object option) {

        if (! base) {
            return null
        }

        Org org = params.consortium ?: contextService.getOrg()

        String lic_name = params.lic_name ?: "Kopie von ${base.reference}"

        boolean slavedBool = true //params.isSlaved is never settable; may be subject of change

        License licenseInstance = new License(
                reference: lic_name,
                status: base.status,
                noticePeriod: base.noticePeriod,
                licenseUrl: base.licenseUrl,
                instanceOf: base,
                isSlaved: slavedBool
        )

        Set<AuditConfig> inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceId(License.class.name,base.id)

        inheritedAttributes.each { AuditConfig attr ->
            licenseInstance[attr.referenceField] = base[attr.referenceField]
        }

        if (! licenseInstance.save()) {
            log.error("Problem saving license ${licenseInstance.errors}")
            return licenseInstance
        } else {
            log.debug("Save ok")

            if (option == InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED) {

                LicenseCustomProperty.findAllByOwner(base).each { lcp ->
                    AuditConfig ac = AuditConfig.getConfig(lcp)

                    if (ac) {
                        // multi occurrence props; add one additional with backref
                        if (lcp.type.multipleOccurrence) {
                            def additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, licenseInstance, lcp.type)
                            additionalProp = lcp.copyInto(additionalProp)
                            additionalProp.instanceOf = lcp
                            additionalProp.save()
                        }
                        else {
                            // no match found, creating new prop with backref
                            def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, licenseInstance, lcp.type)
                            newProp = lcp.copyInto(newProp)
                            newProp.instanceOf = lcp
                            newProp.save()
                        }
                    }
                }

                // documents
                base.documents?.each { dctx ->

                    if (dctx.isShared) {
                        DocContext ndc = new DocContext(
                                owner: dctx.owner,
                                license: licenseInstance,
                                domain: dctx.domain,
                                status: dctx.status,
                                doctype: dctx.doctype,
                                sharedFrom: dctx
                        )
                        ndc.save()
                    }
                }
            }
            else if (option == InstitutionsService.CUSTOM_PROPERTIES_COPY_HARD) {

                for (prop in base.customProperties) {
                    LicenseCustomProperty copiedProp = new LicenseCustomProperty(type: prop.type, owner: licenseInstance)
                    copiedProp = prop.copyInto(copiedProp)
                    copiedProp.instanceOf = null
                    copiedProp.save()
                    //licenseInstance.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                }

                // clone documents
                base.documents?.each { dctx ->
                    Doc clonedContents = new Doc(
                            blobContent: dctx.owner.blobContent,
                            status: dctx.owner.status,
                            type: dctx.owner.type,
                            content: dctx.owner.content,
                            uuid: dctx.owner.uuid,
                            contentType: dctx.owner.contentType,
                            title: dctx.owner.title,
                            creator: dctx.owner.creator,
                            filename: dctx.owner.filename,
                            mimeType: dctx.owner.mimeType,
                            user: dctx.owner.user,
                            migrated: dctx.owner.migrated
                    ).save()

                    DocContext ndc = new DocContext(
                            owner: clonedContents,
                            license: licenseInstance,
                            domain: dctx.domain,
                            status: dctx.status,
                            doctype: dctx.doctype
                    ).save()
                }
            }

            RefdataValue licensee_role = RDStore.OR_LICENSEE
            RefdataValue lic_cons_role = RDStore.OR_LICENSING_CONSORTIUM

            log.debug("adding org link to new license")

            if (accessService.checkPerm("ORG_CONSORTIUM")) {
                new OrgRole(lic: licenseInstance, org: org, roleType: lic_cons_role).save()
            }
            else {
                new OrgRole(lic: licenseInstance, org: org, roleType: licensee_role).save()
            }
            OrgRole.findAllByLicAndRoleTypeAndIsShared(base,RDStore.OR_LICENSOR,true).each { OrgRole licRole ->
                new OrgRole(lic: licenseInstance, org: licRole.org, roleType: RDStore.OR_LICENSOR, isShared: true).save()
            }

            return licenseInstance
        }
    }

    @Deprecated
    def copyLicense(params) {
        def baseLicense = params.baselicense ? License.get(params.baselicense) : null;
        Org org = contextService.getOrg()

        RefdataValue license_type = RefdataValue.getByValueAndCategory('Actual', RDConstants.LICENSE_TYPE)
        RefdataValue license_status = RefdataValue.getByValueAndCategory('Current', RDConstants.LICENSE_STATUS)
        def lic_name = params.lic_name ?: "Kopie von ${baseLicense?.reference}"

        boolean slavedBool = false // ERMS-1562
        if (params.isSlaved) {
            if (params.isSlaved.toString() in ['1', 'Yes', 'yes', 'Ja', 'ja', 'true']) { // todo tmp fallback; remove later
                slavedBool = true
            }
        }

        License licenseInstance = new License(reference: lic_name,
                status: license_status,
                type: license_type,
                noticePeriod: baseLicense?.noticePeriod,
                licenseUrl: baseLicense?.licenseUrl,
                //onixplLicense: baseLicense?.onixplLicense,
                startDate: baseLicense?.startDate,
                endDate: baseLicense?.endDate,

                instanceOf: baseLicense,
                isSlaved: slavedBool
        )
        if (params.copyStartEnd) {
            licenseInstance.startDate = baseLicense?.startDate
            licenseInstance.endDate = baseLicense?.endDate
        }
        for (prop in baseLicense?.customProperties) {
            def copiedProp = new LicenseCustomProperty(type: prop.type, owner: licenseInstance)
            copiedProp = prop.copyInto(copiedProp)
            copiedProp.instanceOf = null
            copiedProp.save(flush: true)
            //licenseInstance.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
        }
        // the url will set the shortcode of the organisation that this license should be linked with.
        if (!licenseInstance.save(flush: true)) {
            log.error("Problem saving license ${licenseInstance.errors}");
            return licenseInstance
        } else {
            log.debug("Save ok");

            def licensee_role = RDStore.OR_LICENSEE
            def lic_cons_role = RDStore.OR_LICENSING_CONSORTIUM

            log.debug("adding org link to new license");

            def rdvConsortiumOrgRole = RefdataValue.getByValueAndCategory('Consortium', RDConstants.ORG_TYPE)?.id.toString()

            if (params.asOrgType && rdvConsortiumOrgRole in params.asOrgType) {
                org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: lic_cons_role))
            } else {
                org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: licensee_role))
            }

            if (baseLicense?.licensor) {
                def licensor_role = RefdataValue.getByValueAndCategory('Licensor', RDConstants.ORGANISATIONAL_ROLE)
                org.links.add(new OrgRole(lic: licenseInstance, org: baseLicense.licensor, roleType: licensor_role));
            }

            if (org.save(flush: true)) {
            } else {
                log.error("Problem saving org links to license ${org.errors}");
            }

            // Clone documents
            baseLicense?.documents?.each { dctx ->
                Doc clonedContents = new Doc(blobContent: dctx.owner.blobContent,
                        status: dctx.owner.status,
                        type: dctx.owner.type,
                        content: dctx.owner.content,
                        uuid: dctx.owner.uuid,
                        contentType: dctx.owner.contentType,
                        title: dctx.owner.title,
                        creator: dctx.owner.creator,
                        filename: dctx.owner.filename,
                        mimeType: dctx.owner.mimeType,
                        user: dctx.owner.user,
                        migrated: dctx.owner.migrated).save()

                DocContext ndc = new DocContext(owner: clonedContents,
                        license: licenseInstance,
                        domain: dctx.domain,
                        status: dctx.status,
                        doctype: dctx.doctype).save()
            }

            return licenseInstance
        }
    }

    /**
     * Rules [insert, delete, update, noChange]
    **/
    def generateComparisonMap(unionList, mapA, mapB, offset, toIndex, rules){
      def result = new TreeMap()
      def insert = rules[0]
      def delete = rules[1]
      def update = rules[2]
      def noChange = rules[3]

      for (unionTitle in unionList){

        def objA = mapA.get(unionTitle)
        def objB = mapB.get(unionTitle)
        def comparison = null

        if(objA.getClass() == ArrayList || objB.getClass() == ArrayList){
          if(objA && !objB){
            comparison = -1
          }
          else if(objA && objB && objA.size() == objB.size()){
            def tipp_matches = 0

            objA.each { a ->
              objB.each { b ->
                if(a.compare(b) == 0){
                  tipp_matches++;
                }
              }
            }

            if(tipp_matches == objA.size()){
              comparison = 0
            }else{
              comparison = 1
            }
          }
        }else{
          comparison = objA?.compare(objB);
        }
        def value = null;

        if(delete && comparison == -1 ) value = [objA,null, "danger"];
        else if(update && comparison == 1) value = [objA,objB, "warning"];
        else if(insert && comparison == null) value = [null, objB, "success"];
        else if (noChange && comparison == 0) value = [objA,objB, ""];

        if(value != null) result.put(unionTitle, value);

        if (result.size() == toIndex ) {
            break;
        }
      }

      if(result.size() <= offset ){
        result.clear()
      }else if(result.size() > (toIndex - offset) ){
        def keys = result.keySet().toArray();
        result = result.tailMap(keys[offset],true)
      }
      result
    }
}
