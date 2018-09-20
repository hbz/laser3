package com.k_int.kbplus

class InstitutionsService {

    def contextService

    def copyLicense(License base, params) {

        if (! base) {
            return null
        }

        def org = contextService.getOrg()

        def lic_name = params.lic_name ?: "Kopie von ${base.reference}"
        def license_type = RefdataValue.getByValueAndCategory('Actual', 'License Type')
        def license_status = RefdataValue.getByValueAndCategory('Current', 'License Status')

        def licenseInstance = new License(
                reference: lic_name,
                status: license_status,
                type: license_type,
                noticePeriod: base.noticePeriod,
                licenseUrl: base.licenseUrl,
                onixplLicense: base.onixplLicense,
                startDate: base.startDate,
                endDate: base.endDate,

                instanceOf: base,
                isSlaved: params.isSlaved
        )
        if (params.copyStartEnd) {
            licenseInstance.startDate = base.startDate
            licenseInstance.endDate = base.endDate
        }
        for (prop in base.customProperties) {
            def copiedProp = new LicenseCustomProperty(type: prop.type, owner: licenseInstance)
            copiedProp = prop.copyValueAndNote(copiedProp)
            licenseInstance.addToCustomProperties(copiedProp)
        }

        if (! licenseInstance.save(flush: true)) {
            log.error("Problem saving license ${licenseInstance.errors}");
            return licenseInstance
        } else {
            log.debug("Save ok");

            def licensee_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee')
            def lic_cons_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensing Consortium')

            log.debug("adding org link to new license");

            if (params.asOrgType) {
                if (RefdataValue.get(params.asOrgType)?.value == 'Consortium') {
                    org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: lic_cons_role))
                } else {
                    org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: licensee_role))
                }
            }
            else if (base.licensor) {
                // legacy
                def licensor_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensor')
                org.links.add(new OrgRole(lic: licenseInstance, org: base.licensor, roleType: licensor_role));
            }

            if (org.save(flush: true)) {
            }
            else {
                log.error("Problem saving org links to license ${org.errors}");
            }

            // Clone documents
            base.documents?.each { dctx ->
                Doc clonedContents = new Doc(
                        blobContent: dctx.owner.blobContent,
                        status: dctx.owner.status,
                        type: dctx.owner.type,
                        alert: dctx.owner.alert,
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

            return licenseInstance
        }
    }

    @Deprecated
    def copyLicense(params) {
        def baseLicense = params.baselicense ? License.get(params.baselicense) : null;
        def org = contextService.getOrg()

        def license_type = RefdataCategory.lookupOrCreate('License Type', 'Actual')
        def license_status = RefdataCategory.lookupOrCreate('License Status', 'Current')
        def lic_name = params.lic_name ?: "Kopie von ${baseLicense?.reference}"
        def licenseInstance = new License(reference: lic_name,
                status: license_status,
                type: license_type,
                noticePeriod: baseLicense?.noticePeriod,
                licenseUrl: baseLicense?.licenseUrl,
                onixplLicense: baseLicense?.onixplLicense,
                startDate: baseLicense?.startDate,
                endDate: baseLicense?.endDate,

                instanceOf: baseLicense,
                isSlaved: params.isSlaved
        )
        if (params.copyStartEnd) {
            licenseInstance.startDate = baseLicense?.startDate
            licenseInstance.endDate = baseLicense?.endDate
        }
        for (prop in baseLicense?.customProperties) {
            def copiedProp = new LicenseCustomProperty(type: prop.type, owner: licenseInstance)
            copiedProp = prop.copyValueAndNote(copiedProp)
            licenseInstance.addToCustomProperties(copiedProp)
        }
        // the url will set the shortcode of the organisation that this license should be linked with.
        if (!licenseInstance.save(flush: true)) {
            log.error("Problem saving license ${licenseInstance.errors}");
            return licenseInstance
        } else {
            log.debug("Save ok");

            def licensee_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee')
            def lic_cons_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensing Consortium')

            log.debug("adding org link to new license");

            if (params.asOrgType && RefdataValue.get(params.asOrgType)?.value == 'Consortium') {
                org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: lic_cons_role))
            } else {
                org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: licensee_role))
            }

            if (baseLicense?.licensor) {
                def licensor_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensor')
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
                        alert: dctx.owner.alert,
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
