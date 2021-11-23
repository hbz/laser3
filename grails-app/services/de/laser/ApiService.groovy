package de.laser

import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.auth.UserRole
import de.laser.finance.CostItemElementConfiguration
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

import java.text.SimpleDateFormat

@Deprecated
@Slf4j
@Transactional
class ApiService {

    def messageSource

    /**
     * Strips whitespaces from the given string
     * @param s the string to sanitise
     * @return trimmed string with multiple whitespaces removed
     */
    private String normString(String str){
        if(! str)
            return ""
            
        str = str.replaceAll("\\s+", " ").trim()
        str
    }

    /**
     * Reduces the child level and flattens the given object with children to a single level
     * @param obj the object to flatten
     * @return list with children of given object or object if no children exist
     */
    private List flattenToken(Object obj){
        List result = []

        obj?.token?.each{ token ->
            result << token
        }
        if(result.size() == 0 && obj){
            result << obj
        }
        
        result
    }

    /**
     * Use for INITIAL import ONLY !!!
     *
     * @param xml
     * @return
     */
    @Deprecated
    GPathResult makeshiftOrgImport(GPathResult xml){

        def count = xml.institution.size()

        // helper

        def resolveStreet = { street ->

            def street1 = '', street2 = '', stParts = normString(street)
            stParts = stParts.replaceAll("(Str\\.|Strasse)", "Straße").replaceAll("stra(ss|ß)e", "str.").trim().split(" ")

            if (stParts.size() > 1) {
                street1 = stParts[0..stParts.size() - 2].join(" ")
                street2 = stParts[stParts.size() - 1]
            }
            else {
                street1 = stParts[0]
            }

            ["street1": street1, "street2": street2]
        }

        def doublets = [:]

        xml.institution.each { inst ->

            // handle identifiers

            def identifiers = []

            inst.identifier.children().each { ident ->

                (ident.children().size() > 0 ? ident.children() : ident).each { innerIdent -> // get nested ones

                    def idValue = normString(innerIdent.text())
                    if (idValue) {
                        identifiers << ["${innerIdent.name()}": idValue]
                        doublets.put("${innerIdent.name()}:${idValue}", (doublets.get("${innerIdent.name()}:${idValue}") ?: 0) + 1)
                    }
                }
            }
//return;
            // find org by identifier or name (Org.lookupOrCreate)

            def org = Org.lookup(inst.name?.text(), identifiers)
            if (! org) {
                org = Org.lookupOrCreate(inst.name?.text(), null, null, identifiers, null)
                org.sector = RefdataValue.getByValueAndCategory('Higher Education', RDConstants.ORG_SECTOR)

                log.debug("#${(count--)} -- creating new org: " + inst.name?.text() + " / " + identifiers)
            }
            else {
                log.debug("#${(count--)} -- updating org: (" + org.id + ") " + org.name)
            }

            // set tenant

            def tenant, isPublic

            if (inst.source == 'edb des hbz') {
               tenant = Org.findByShortnameIlike('hbz')
               isPublic = false
            }
            //else if (inst.source == 'nationallizenzen.de') {
            //    tenant = Org.findByShortcode('NL')
            //}
            if (! tenant) {
                tenant = org
                isPublic = true
            }

            // simple attributes

            Date now = new Date()
            org.importSource    = inst.source?.text() ?: org.importSource
            org.sortname        = inst.sortname?.text() ?: org.sortname
            org.shortname       = inst.shortname?.text() ?: org.shortname
            org.lastImportDate  = now

            org.fteStudents     = inst.fte_students?.text() ? Long.valueOf(inst.fte_students.text()) : org.fteStudents
            org.fteStaff        = inst.fte_staff?.text() ? Long.valueOf(inst.fte_staff.text()) : org.fteStaff
            org.url             = inst.url?.text() ?: org.url

            // refdatas

            org.libraryType     = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.LIBRARY_TYPE, inst.library_type?.text()) ?: org.libraryType
            org.libraryNetwork  = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.LIBRARY_NETWORK, inst.library_network?.text()) ?: org.libraryNetwork
            org.funderType      = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.FUNDER_TYPE, inst.funder_type?.text()) ?: org.funderType

            org.save()

            // adding new identifiers

            identifiers.each{ it ->
                it.each { k, v ->
                    // TODO [ticket=1789]
                    Identifier ident = Identifier.construct([value: v, reference: org, namespace: k])

                    //def ns = IdentifierNamespace.findByNsIlike(k)
                    //if (null == Identifier.findByNsAndValue(ns, v)) {
                    //    new IdentifierOccurrence(org: org, identifier: Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                    //}
                }
            }

            // postal address

            def tmp1 = resolveStreet(inst.street.text())

            Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
                    normString("${inst.name}"),
                    normString("${tmp1.street1}"),
                    normString("${tmp1.street2}"),
                    normString("${inst.zip}"),
                    normString("${inst.city}"),
                    RefdataValue.getByCategoriesDescAndI10nValueDe([RDConstants.REGIONS_DE, RDConstants.REGIONS_AT, RDConstants.REGIONS_CH], inst.county?.text()),
                    RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.COUNTRY, inst.country?.text()),
                    normString("${inst.pob}"),
                    normString("${inst.pobZipcode}"),
                    normString("${inst.pobCity}"),
                    RefdataValue.getByValueAndCategory('Postal address', RDConstants.ADDRESS_TYPE),
                    null,
                    org
            )

            // billing address

            inst.billing_address.each{ billing_address ->

                def tmp2 = resolveStreet(billing_address.street?.text())

                Address billingAddress = Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
                        inst.billing_address.name.text() ? normString("${billing_address.name}") : normString("${inst.name}"),
                        normString("${tmp2.street1}"),
                        normString("${tmp2.street2}"),
                        normString("${billing_address.zip}"),
                        normString("${billing_address.city}"),
                        null,
                        null,
                        billing_address.pob ? normString("${billing_address.pob}") : null,
                        billing_address.pobZipcode ? normString("${billing_address.pobZipcode}") : null,
                        billing_address.pobCity ? normString("${billing_address.pobCity}") : null,
                        RefdataValue.getByValueAndCategory('Billing address', RDConstants.ADDRESS_TYPE),
                        null,
                        org
                )
                if (billing_address.addition_first) {
                    billingAddress.setAdditionFirst( normString("${billing_address.addition_first}"))
                    billingAddress.save()
                }
                if (billing_address.addition_second) {
                    billingAddress.setAdditionSecond(normString("${billing_address.addition_second}"))
                    billingAddress.save()
                }
            }

            // legal address

            inst.legal_address.each{ legal_address ->

                def tmp3 = resolveStreet(legal_address.street?.text())

                Address legalAddress = Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
                        legal_address.name.text() ? normString("${legal_address.name}") : normString("${inst.name}"),
                        normString("${tmp3.street1}"),
                        normString("${tmp3.street2}"),
                        normString("${legal_address.zip}"),
                        normString("${legal_address.city}"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        RefdataValue.getByValueAndCategory('Legal patron address', RDConstants.ADDRESS_TYPE),
                        null,
                        org
                )
                if (legal_address.url.text()) {
                    log.debug("setting urlGov to: " + legal_address.url)
                    org.setUrlGov(legal_address.url?.text())
                    org.save()
                }
            }

            inst.private_property.children().each { pp ->
                String name = pp.name().replace('_', ' ').toLowerCase()

                PropertyDefinition type = PropertyDefinition.findWhere(
                        name:   name,
                        type:   RefdataValue.class.name,
                        descr:  PropertyDefinition.ORG_PROP,
                        tenant: tenant
                )
                if (! type) {
                    type = PropertyDefinition.findWhere(
                            name:   name,
                            type:   String.class.name,
                            descr:  PropertyDefinition.ORG_PROP,
                            tenant: tenant
                    )
                }

                if (type) {
                    OrgProperty op
                    String check = "private property: " + name + " : " + pp.text()

                    if (type.refdataCategory) {
                        def rdv = RefdataValue.getByCategoryDescAndI10nValueDe(type.refdataCategory, pp.text())
                        if (rdv) {
                            op = OrgProperty.findWhere(type: type, owner: org, refValue: rdv)
                            if (! op) {
                                op = new OrgProperty(type: type, owner: org, refValue: rdv, isPublic: false)
                                op.save() ? (check += " > OK") : (check += " > FAIL")
                            } else {
                                check = "ignored existing " + check
                            }
                        }
                    }
                    else {
                        op = OrgProperty.findWhere(type: type, owner: org, stringValue: pp.text())
                        if (! op) {
                            op = new OrgProperty(type: type, owner: org, stringValue: pp.text(), isPublic: false)
                            op.save() ? (check += " > OK") : (check += " > FAIL")
                        } else {
                            check = "ignored existing " + check
                        }
                    }

                    log.debug(check)
                }
                else {
                    log.debug("ignoring private property due mismatch: " + name + " : " + pp.text())
                }
            }

            // persons

            RefdataValue rdvContactPerson = RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION)
            RefdataValue rdvJobRelated    = RefdataValue.getByValueAndCategory('Job-related', RDConstants.CONTACT_TYPE)

            inst.person?.children().each { p ->

                def rdvContactType = RDStore.PERSON_CONTACT_TYPE_PERSONAL

                if( ! p.first_name.text() ) {
                    rdvContactType = RDStore.PERSON_CONTACT_TYPE_FUNCTIONAL
                }
                Person person = Person.lookup( // firstName, lastName, tenant, isPublic, contactType, org, functionType
                        p.first_name.text() ? normString("${p.first_name}") : null,
                        p.last_name.text() ? normString("${p.last_name}") : null,
                        tenant,
                        isPublic,
                        rdvContactType,
                        org,
                        rdvContactPerson,
                )

                // do NOT change existing persons !!!
                if (! person) {
                    log.debug("creating new person: ${p.first_name} ${p.last_name}")

                    person = new Person(
                            first_name: p.first_name.text() ? normString("${p.first_name}") : null,
                            last_name: normString("${p.last_name}"),
                            tenant: tenant,
                            isPublic: isPublic,
                            contactType: rdvContactType,
                            gender: RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.GENDER, p.gender?.text())
                    )

                    if (person.save()) {
                        log.debug("creating new personRole: ${person} ${rdvContactPerson} ${org}")

                        // role

                        def pr = new PersonRole(
                                prs: person,
                                org: org,
                                functionType: rdvContactPerson
                        )
                        pr.save()

                        p.function.children().each { func ->
                            RefdataValue rdv = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.PERSON_FUNCTION, func.text())
                            if (rdv) {
                                PersonRole pf = new PersonRole(
                                        prs: person,
                                        org: org,
                                        functionType: rdv
                                )
                                pf.save()
                            }
                        }

                        // contacts

                        if (p.email) {
                            Contact.lookupOrCreate(
                                    normString("${p.email}"),
                                    RefdataValue.getByValueAndCategory('E-Mail', RDConstants.CONTACT_CONTENT_TYPE),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                        if (p.fax) {
                            Contact.lookupOrCreate(
                                    normString("${p.fax}"),
                                    RefdataValue.getByValueAndCategory('Fax', RDConstants.CONTACT_CONTENT_TYPE),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                        if (p.telephone) {
                            Contact.lookupOrCreate(
                                    normString("${p.telephone}"),
                                    RefdataValue.getByValueAndCategory('Phone', RDConstants.CONTACT_CONTENT_TYPE),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                    }
                }
                else {
                    log.debug("ignoring existing person [${rdvContactPerson}]: " + person)
                }
            }
        }

        log.debug("WARNING: doublets @ " + doublets.findAll{ it.value > 1})

        return xml
    }

    @Deprecated
    GPathResult makeshiftSubscriptionImport(GPathResult xml){

        // TODO: in progress - erms-746
        def count = xml.institution.size()
        log.debug("importing ${count} items")

        xml.subscription.each { sub ->
            String strName = sub.name.text()
            RefdataValue rdvType = RefdataValue.getByValueAndCategory(type.name.text(), RDConstants.SUBSCRIPTION_TYPE)

            log.debug("processing ${strName} / ${rdvType.getI10n('value')}")
        }
    }
    
    /**
     * hic codex data pro organisationibus atque utilisatoribus ex fontem datium leget et in repositorium datium scribit. Fons datium
     * omnia data vel partem datium continere potet; in ultimo caso data incrementum est
     * We should not think in Latin - this code reads off data from an existing dump and writes them into the database. This may be the
     * entire datasource or a part of it; in latter case, this is an increment
     * @param baseFile input file containing the data
     */
    @Deprecated
    void setupBasicData(File baseFile) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss.S')
            def orgBase = new XmlSlurper().parse(baseFile)
            //insert all organisations
            orgBase.organisations.org.each { orgData ->
                //filter out probably deleted orgs and those already existent
                Org org = Org.findByGlobalUID(orgData.globalUID.text())
                if (!orgData.name.text().contains("DEL") && !org) {
                    //insert raw org data
                    org = new Org()
                    //log.debug("globalUID: ${orgData.globalUID.text()}")
                    org.globalUID = orgData.globalUID.text()
                    //log.debug("name: ${orgData.name.text()}")
                    org.name = orgData.name.text()
                    if (orgData.shortname.text()) {
                        //log.debug("shortname: ${orgData.shortname.text()}")
                        org.shortname = orgData.shortname.text()
                    }
                    if (orgData.sortname.text()) {
                        //log.debug("sortname: ${orgData.sortname.text()}")
                        org.sortname = orgData.sortname.text()
                    }
                    if (orgData.url.text()) {
                        //log.debug("url: ${orgData.url.text()}")
                        org.url = orgData.url.text()
                    }
                    if (orgData.urlGov.text()) {
                        //log.debug("urlGov: ${orgData.urlGov.text()}")
                        org.urlGov = orgData.urlGov.text()
                    }
                    if (orgData.importSource.text()) {
                        //log.debug("importSource: ${orgData.importSource.text()}")
                        org.importSource = orgData.importSource.text()
                    }
                    if (orgData.gokbId.text()) {
                        //log.debug("gokbId: ${orgData.gokbId.text()}")
                        org.gokbId = orgData.gokbId.text()
                    }
                    if (orgData.ipRange.text()) {
                        //log.debug("ipRange: ${orgData.ipRange.text()}")
                        org.ipRange = orgData.ipRange.text()
                    }
                    if (orgData.scope.text()) {
                        //log.debug("scope: ${orgData.scope.text()}")
                        org.scope = orgData.scope.text()
                    }
                    if (orgData.categoryId.text()) {
                        //log.debug("categoryId: ${orgData.categoryId.text()}")
                        org.categoryId = orgData.categoryId.text()
                    }
                    if (orgData.sector.rdv.size() && orgData.sector.rdc.size()) {
                        //log.debug("sector: ${RefdataValue.getByValueAndCategory(orgData.sector.rdv.text(),orgData.sector.rdc.text())}")
                        org.sector = RefdataValue.getByValueAndCategory(orgData.sector.rdv.text(), orgData.sector.rdc.text())
                    }
                    if (orgData.status.rdv.size() && orgData.sector.drc.size()) {
                        //log.debug("status: ${RefdataValue.getByValueAndCategory(orgData.status.rdv.text(),orgData.status.rdc.text())}")
                        org.status = RefdataValue.getByValueAndCategory(orgData.status.rdv.text(), orgData.status.rdc.text())
                    }
                    if (orgData.membership.rdv.size() && orgData.membership.rdc.size()) {
                        //log.debug("membership: ${RefdataValue.getByValueAndCategory(orgData.membership.rdv.text(),orgData.membership.rdc.text())}")
                        org.membership = RefdataValue.getByValueAndCategory(orgData.membership.rdv.text(), orgData.membership.rdc.text())
                    }
                    if (orgData.country.rdv.size() && orgData.country.rdc.size()) {
                        //log.debug("country: ${RefdataValue.getByValueAndCategory(orgData.countryElem.rdv.text(),orgData.countryElem.rdc.text())}")
                        org.country = RefdataValue.getByValueAndCategory(orgData.countryElem.rdv.text(), orgData.countryElem.rdc.text())
                    }
                    if (orgData.region.rdv.size() && orgData.region.rdc.size()) {
                        //log.debug("region: ${RefdataValue.getByValueAndCategory(orgData.region.rdv.text(),orgData.region.rdc.text())}")
                        org.region = RefdataValue.getByValueAndCategory(orgData.region.rdv.text(), orgData.region.rdc.text())
                    }
                    if (orgData.libraryType.rdv.size() && orgData.libraryType.rdc.size()) {
                        //log.debug("libraryType: ${RefdataValue.getByValueAndCategory(orgData.libraryType.rdv.text(),orgData.libraryType.rdc.text())}")
                        org.libraryType = RefdataValue.getByValueAndCategory(orgData.libraryType.rdv.text(), orgData.libraryType.rdc.text())
                    }
                    org.dateCreated = sdf.parse(orgData.dateCreated.text())
                    org.lastUpdated = sdf.parse(orgData.lastUpdated.text())
                    Set<RefdataValue> orgTypes = []
                    orgData.orgTypes.orgType.each { orgType ->
                        log.debug("----- processing org types -----")
                        //log.debug("orgType: ${RefdataValue.getByValueAndCategory(orgType.rdv.text(),orgType.rdc.text())}")
                        orgTypes.add(RefdataValue.getByValueAndCategory(orgType.rdv.text(), orgType.rdc.text()))
                    }
                    org.orgType = orgTypes
                    if (org.save()) {
                        //data to be processed after save()
                        orgData.costConfigurations.costConfiguration.each { cc ->
                            log.debug("----- processing cost configurations -----")
                            CostItemElementConfiguration ciec = new CostItemElementConfiguration()
                            //log.debug("costItemElement: ${RefdataValue.getByValueAndCategory(cc.rdv.text(),cc.rdc.text())}")
                            ciec.costItemElement = RefdataValue.getByValueAndCategory(cc.rdv.text(), cc.rdc.text())
                            //log.debug("elementSign: ${RefdataValue.getByValueAndCategory(cc.elementSign.rdv.text(),cc.elementSign.rdc.text())}")
                            ciec.elementSign = RefdataValue.getByValueAndCategory(cc.elementSign.rdc.text(), cc.elementSign.rdv.text())
                            //log.debug("forOrganisation: ${orgData.globalUID.text()}")
                            ciec.forOrganisation = org
                            ciec.save()
                        }
                        orgData.ids.id.each { idData ->
                            log.debug("----- processing identifiers -----")
                            //log.debug("ns: ${IdentifierNamespace.findByNs(idData.@namespace.text())}")
                            //log.debug("value: ${idData.@value.text()}")
                            //new Identifier(ns: IdentifierNamespace.findByNs(idData.@namespace.text()), value: idData.@value.text()).save(flush: true)
                            //Identifier id = Identifier.lookupOrCreateCanonicalIdentifier(idData.@namespace.text(), idData.@value.text())
                            Identifier id = Identifier.construct([value: idData.@value.text(), reference: org, namespace: idData.@namespace.text()])
                            //log.debug("org: ${orgData.globalUID.text()}")
                            //new IdentifierOccurrence(org: org, identifier: id).save()
                        }
                        orgData.settings.setting.each { st ->
                            log.debug("name: ${OrgSetting.KEYS.valueOf(st.name.text())}")
                            //log.debug("value: ${st.value.text()}")
                            if (st.rdValue.size()) {
                                OrgSetting.add(org, OrgSetting.KEYS.valueOf(st.name.text()), RefdataValue.getByValueAndCategory(st.rdValue.rdv.text(), st.rdValue.rdc.text()))
                            }
                            else if (st.roleValue.size()) {
                                OrgSetting.add(org, OrgSetting.KEYS.valueOf(st.name.text()), Role.findByAuthority(st.roleValue.text()))
                            }
                            else {
                                OrgSetting.add(org, OrgSetting.KEYS.valueOf(st.name.text()), st.value.text())
                            }
                        }
                    } else if (org.hasErrors()) {
                        log.error("Error on saving org: ${org.errors}")
                        //System.exit(46)
                    }
                }
                else if(org) {
                    //org exists => update data
                    if(org.name != orgData.name.text()){
                        //log.debug("name: ${orgData.name.text()}")
                        org.name = orgData.name.text()
                    }
                    if (org.shortname != orgData.shortname.text()) {
                        //log.debug("shortname: ${orgData.shortname.text()}")
                        org.shortname = orgData.shortname.text()
                    }
                    if (org.sortname != orgData.sortname.text()) {
                        //log.debug("sortname: ${orgData.sortname.text()}")
                        org.sortname = orgData.sortname.text()
                    }
                    if (org.url != orgData.url.text()) {
                        //log.debug("url: ${orgData.url.text()}")
                        org.url = orgData.url.text()
                    }
                    if (org.urlGov != orgData.urlGov.text()) {
                        //log.debug("urlGov: ${orgData.urlGov.text()}")
                        org.urlGov = orgData.urlGov.text()
                    }
                    if (org.importSource != orgData.importSource.text()) {
                        //log.debug("importSource: ${orgData.importSource.text()}")
                        org.importSource = orgData.importSource.text()
                    }
                    if (org.gokbId != orgData.gokbId.text()) {
                        //log.debug("gokbId: ${orgData.gokbId.text()}")
                        org.gokbId = orgData.gokbId.text()
                    }
                    if (org.ipRange != orgData.ipRange.text()) {
                        //log.debug("ipRange: ${orgData.ipRange.text()}")
                        org.ipRange = orgData.ipRange.text()
                    }
                    if (org.scope != orgData.scope.text()) {
                        //log.debug("scope: ${orgData.scope.text()}")
                        org.scope = orgData.scope.text()
                    }
                    if (org.categoryId != orgData.categoryId.text()) {
                        //log.debug("categoryId: ${orgData.categoryId.text()}")
                        org.categoryId = orgData.categoryId.text()
                    }
                    if ((orgData.sector.rdv.size() && orgData.sector.rdc.size()) && org.sector?.value != orgData.sector.rdv.text()) {
                        //log.debug("sector: ${RefdataValue.getByValueAndCategory(orgData.sector.rdv.text(),orgData.sector.rdc.text())}")
                        org.sector = RefdataValue.getByValueAndCategory(orgData.sector.rdv.text(), orgData.sector.rdc.text())
                    }
                    if ((orgData.status.rdv.size() && orgData.sector.drc.size()) && org.status?.value != orgData.status.rdv.text()) {
                        //log.debug("status: ${RefdataValue.getByValueAndCategory(orgData.status.rdv.text(),orgData.status.rdc.text())}")
                        org.status = RefdataValue.getByValueAndCategory(orgData.status.rdv.text(), orgData.status.rdc.text())
                    }
                    if ((orgData.membership.rdv.size() && orgData.membership.rdc.size()) && org.membership?.value != orgData.membership.rdv.text()) {
                        //log.debug("membership: ${RefdataValue.getByValueAndCategory(orgData.membership.rdv.text(),orgData.membership.rdc.text())}")
                        org.membership = RefdataValue.getByValueAndCategory(orgData.membership.rdv.text(), orgData.membership.rdc.text())
                    }
                    if ((orgData.country.rdv.size() && orgData.country.rdc.size()) && org.country?.value != orgData.country.rdv.text()) {
                        //log.debug("country: ${RefdataValue.getByValueAndCategory(orgData.countryElem.rdv.text(),orgData.countryElem.rdc.text())}")
                        org.country = RefdataValue.getByValueAndCategory(orgData.countryElem.rdv.text(), orgData.countryElem.rdc.text())
                    }
                    if ((orgData.region.rdv.size() && orgData.region.rdc.size()) && org.region?.value != orgData.region.rdv.text()) {
                        //log.debug("region: ${RefdataValue.getByValueAndCategory(orgData.region.rdv.text(),orgData.region.rdc.text())}")
                        org.region = RefdataValue.getByValueAndCategory(orgData.region.rdv.text(), orgData.region.rdc.text())
                    }
                    if ((orgData.libraryType.rdv.size() && orgData.libraryType.rdc.size()) && org.libraryType?.value != orgData.libraryType.rdv.text()) {
                        //log.debug("libraryType: ${RefdataValue.getByValueAndCategory(orgData.libraryType.rdv.text(),orgData.libraryType.rdc.text())}")
                        org.libraryType = RefdataValue.getByValueAndCategory(orgData.libraryType.rdv.text(), orgData.libraryType.rdc.text())
                    }
                    org.lastUpdated = sdf.parse(orgData.lastUpdated.text())
                    Set<RefdataValue> orgTypes = []
                    log.debug("----- processing org types -----")
                    if(org.orgType.size() > orgData.orgTypes.orgType.size()) {
                        //log.debug("orgType: ${RefdataValue.getByValueAndCategory(orgType.rdv.text(),orgType.rdc.text())}")
                        org.orgType.eachWithIndex { ot, int i ->
                            def orgType = orgData.orgTypes.orgType[i]
                            if(orgType){
                                RefdataValue newRDValue = RefdataValue.getByValueAndCategory(orgType.rdv.text(), orgType.rdc.text())
                                if(ot.id != newRDValue.id) {
                                    orgTypes << newRDValue
                                }
                                else orgTypes << ot
                            }
                        }
                    }
                    else if(org.orgType.size() <= orgData.orgTypes.orgType.size()) {
                        orgData.orgTypes.orgType.eachWithIndex { orgType, int i ->
                            RefdataValue ot = orgTypes[i]
                            RefdataValue newRDValue = RefdataValue.getByValueAndCategory(orgType.rdv.text(), orgType.rdc.text())
                            if(ot) {
                                if(ot.id != newRDValue.id)
                                    orgTypes << newRDValue
                                else orgTypes << ot
                            }
                            else if(!ot) {
                                orgTypes << newRDValue
                            }
                        }
                    }
                    org.orgType = orgTypes
                    org.save()
                    log.debug("----- processing cost configurations -----")
                    orgData.costConfigurations.costConfiguration.each { cc ->
                        //log.debug("costItemElement: ${RefdataValue.getByValueAndCategory(cc.rdv.text(),cc.rdc.text())}")
                        RefdataValue costItemElement = RefdataValue.getByValueAndCategory(cc.rdv.text(), cc.rdc.text())
                        //log.debug("elementSign: ${RefdataValue.getByValueAndCategory(cc.elementSign.rdv.text(),cc.elementSign.rdc.text())}")
                        RefdataValue elementSign = RefdataValue.getByValueAndCategory(cc.elementSign.rdc.text(), cc.elementSign.rdv.text())
                        CostItemElementConfiguration ciec = CostItemElementConfiguration.findByForOrganisationAndCostItemElement(org, costItemElement)
                        if(ciec) {
                            if(ciec.elementSign?.id != elementSign?.id)
                                ciec.elementSign = elementSign
                        }
                        else {
                            ciec = new CostItemElementConfiguration()
                            ciec.costItemElement = costItemElement
                            ciec.elementSign = elementSign
                            //log.debug("forOrganisation: ${orgData.globalUID.text()}")
                            ciec.forOrganisation = org
                        }
                        ciec.save()
                    }
                    log.debug("----- processing identifiers -----")
                    orgData.ids.id.each { idData ->
                        //log.debug("ns: ${IdentifierNamespace.findByNs(idData.@namespace.text())}")
                        //log.debug("value: ${idData.@value.text()}")
						// TODO [ticket=1789] check setup basic data
						Identifier id = Identifier.construct([value: idData.@value.text(), reference: org, namespace: idData.@namespace.text()])
						//Identifier id = Identifier.lookupOrCreateCanonicalIdentifier(idData.@namespace.text(), idData.@value.text())
						//log.debug("org: ${orgData.globalUID.text()}")
						//IdentifierOccurrence idOcc = IdentifierOccurrence.findByOrgAndIdentifier(org, id)
						//if(!idOcc) {
						//    idOcc = new IdentifierOccurrence()
						//    idOcc.save()
						//}
                    }
                    orgData.settings.setting.each { st ->
                        log.debug("name: ${OrgSetting.KEYS.valueOf(st.name.text())}")
                        //log.debug("value: ${st.value.text()}")
                        if (st.rdValue.size() && !OrgSetting.findByOrgAndKeyAndRdValue(org, OrgSetting.KEYS.valueOf(st.name.text()), RefdataValue.getByValueAndCategory(st.rdValue.rdv.text(), st.rdValue.rdc.text()))) {
                            OrgSetting.add(org, OrgSetting.KEYS.valueOf(st.name.text()), RefdataValue.getByValueAndCategory(st.rdValue.rdv.text(), st.rdValue.rdc.text()))
                        }
                        else if (st.roleValue.size() && !OrgSetting.findByOrgAndKeyAndRoleValue(org, OrgSetting.KEYS.valueOf(st.name.text()), Role.findByAuthority(st.roleValue.text()))) {
                            if(OrgSetting.KEYS.valueOf(st.name.text()) == OrgSetting.KEYS.CUSTOMER_TYPE) {
                                OrgSetting oss = OrgSetting.findByOrgAndKey(org, OrgSetting.KEYS.CUSTOMER_TYPE)
                                oss.setValue(Role.findByAuthority(st.roleValue.text()))
                            }
                            else
                                OrgSetting.add(org, OrgSetting.KEYS.valueOf(st.name.text()), Role.findByAuthority(st.roleValue.text()))
                        }
                        else if(st.value.size() && !OrgSetting.findByOrgAndKeyAndStrValue(org, OrgSetting.KEYS.valueOf(st.name.text()), st.value.text())){
                            OrgSetting.add(org, OrgSetting.KEYS.valueOf(st.name.text()), st.value.text())
                        }
                    }
                }
            }
            //insert users not existing
            orgBase.users.user.each { userData ->
                User user = User.findByUsername(userData.username.text())
                if (!user) {
                    user = new User()
                    //log.debug("username: ${userData.username.text()}")
                    user.username = userData.username.text()
                    //log.debug("display: ${userData.display.text()}")
                    user.display = userData.display.text()
                    if (userData.email.text()) {
                        //log.debug("email: ${userData.email.text()}")
                        user.email = userData.email.text()
                    }
                    if (userData.shibbScope.text()) {
                        //log.debug("shibbScope: ${userData.shibbScope.text()}")
                        user.shibbScope = userData.shibbScope.text()
                    }
                    //log.debug("enabled: ${userData.enabled.text().toBoolean()}")
                    user.enabled = userData.enabled.text().toBoolean()
                    //log.debug("accountExpired: ${userData.accountExpired.text().toBoolean()}")
                    user.accountExpired = userData.accountExpired.text().toBoolean()
                    //log.debug("accountLocked: ${userData.accountLocked.text().toBoolean()}")
                    user.accountLocked = userData.accountLocked.text().toBoolean()
                    //log.debug("passwordExpired: ${userData.passwordExpired.text().toBoolean()}")
                    user.passwordExpired = userData.passwordExpired.text().toBoolean()
                    if(userData.dateCreated.text())
                        user.dateCreated = sdf.parse(userData.dateCreated.text())
                    if(userData.lastUpdated.text())
                        user.lastUpdated = sdf.parse(userData.lastUpdated.text())
                    //log.debug("password: ${userData.password.text()}")
                    user.password = "bob" //temp, we will override it with the correct hash
                    if (user.save()) {
                        //data to be processed after save()
                        userData.roles.role.each { uRole ->
                            //log.debug("role: ${Role.findByAuthority(uRole.text())}")
                            new UserRole(user: user, role: Role.findByAuthority(uRole.text())).save() //null pointer exception occuring, make further tests
                        }
                        userData.settings.setting.each { st ->
                            log.debug("name: ${UserSetting.KEYS.valueOf(st.name.text())}")
                            //log.debug("value: ${st.value.text()}")
                            if (st.rdValue.size()) {
                                UserSetting.add(user, UserSetting.KEYS.valueOf(st.name.text()), RefdataValue.getByValueAndCategory(st.rdValue.rdv.text(), st.rdValue.rdc.text()))
                            } else if (st.org.size()) {
                                Org org = Org.findByGlobalUID(st.org.text())
                                if (org) {
                                    UserSetting.add(user, UserSetting.KEYS.valueOf(st.name.text()), org)
                                }
                            } else {
                                UserSetting.add(user, UserSetting.KEYS.valueOf(st.name.text()), st.value.text())
                            }
                        }
                        User.executeUpdate('update User u set u.password = :password where u.username = :username', [password: userData.password.text(), username: userData.username.text()])
                    } else if (user.hasErrors()) {
                        log.error("error on saving user: ${user.errors}")
                    }
                }
                else if(user) {
                    if(user.display != userData.display.text()) {
                        //log.debug("display: ${userData.display.text()}")
                        user.display = userData.display.text()
                    }
                    if (user.email != userData.email.text()) {
                        //log.debug("email: ${userData.email.text()}")
                        user.email = userData.email.text()
                    }
                    if (user.shibbScope != userData.shibbScope.text()) {
                        //log.debug("shibbScope: ${userData.shibbScope.text()}")
                        user.shibbScope = userData.shibbScope.text()
                    }
                    if(user.enabled != userData.enabled.text().toBoolean()){
                        //log.debug("enabled: ${userData.enabled.text().toBoolean()}")
                        user.enabled = userData.enabled.text().toBoolean()
                    }
                    if(user.accountExpired != userData.accountExpired.text().toBoolean()) {
                        //log.debug("accountExpired: ${userData.accountExpired.text().toBoolean()}")
                        user.accountExpired = userData.accountExpired.text().toBoolean()
                    }
                    if(user.accountLocked != userData.accountLocked.text().toBoolean()) {
                        //log.debug("accountLocked: ${userData.accountLocked.text().toBoolean()}")
                        user.accountLocked = userData.accountLocked.text().toBoolean()
                    }
                    if(user.passwordExpired != userData.passwordExpired.text().toBoolean()) {
                        //log.debug("passwordExpired: ${userData.passwordExpired.text().toBoolean()}")
                        user.passwordExpired = userData.passwordExpired.text().toBoolean()
                    }
                }
            }
            //insert all user affiliations
            orgBase.affiliations.affiliation.each { affData ->
                log.debug("----- processing affiliation ${affData.user.text()} <-> ${affData.org.text()} -----")
                UserOrg affiliation = new UserOrg(user: User.findByUsername(affData.user.text()), org: Org.findByGlobalUID(affData.org.text()))
                if (affData.formalRole.text()) {
                    affiliation.formalRole = Role.findByAuthority(affData.formalRole.text())
                }
                if (!affiliation.save() && affiliation.hasErrors()) {
                    log.error("error on saving affiliation: ${affiliation.errors}")
                }
            }
            //insert all combos
            orgBase.combos.combo.each { comboData ->
                log.debug("----- processing combo ${comboData.fromOrg.text()} -> ${comboData.toOrg.text()} -----")
                Combo combo = Combo.findByFromOrgAndToOrg(Org.findByGlobalUID(comboData.fromOrg.text()), Org.findByGlobalUID(comboData.toOrg.text()))
                if(!combo) {
                    //log.debug("type: ${RefdataValue.getByValueAndCategory(comboData.type.rdv.text(),comboData.type.rdc.text())}, fromOrg: ${comboData.fromOrg.text()}, toOrg: ${comboData.toOrg.text()}")
                    combo = new Combo(type: RefdataValue.getByValueAndCategory(comboData.type.rdv.text(), comboData.type.rdc.text()), fromOrg: Org.findByGlobalUID(comboData.fromOrg.text()), toOrg: Org.findByGlobalUID(comboData.toOrg.text()))
                    if (!combo.save() && combo.hasErrors()) {
                        log.error("error on saving combo: ${combo.errors}")
                    }
                }
            }
            //insert all persons
            if(orgBase.persons) {
                orgBase.persons.person.each { personData ->
                    log.debug("----- processing person ${personData.lastName.text()} for ${personData.tenant.text()} -----")
                    if (!Person.findByGlobalUID(personData.globalUID.text())) {
                        Person person = new Person(globalUID: personData.globalUID.text())
                        if (personData.title.text()) {
                            //log.debug("title: ${personData.title.text()}")
                            person.title = personData.title.text()
                        }
                        if (personData.firstName.text()) {
                            //log.debug("first_name: ${personData.firstName.text()}")
                            person.first_name = personData.firstName.text()
                        }
                        if (personData.middleName.text()) {
                            //log.debug("middle_mame: ${personData.middleName.text()}")
                            person.middle_name = personData.middleName.text()
                        }
                        if (personData.lastName.text()) {
                            //log.debug("last_name: ${personData.lastName.text()}")
                            person.last_name = personData.lastName.text()
                        }
                        if(personData.tenant.text()) {
                            //log.debug("tenant: ${Org.findByGlobalUID(personData.tenant.text())}")
                            person.tenant = Org.findByGlobalUID(personData.tenant.text())
                        }
                        if(personData.gender.rdv.size() && personData.gender.rdc.size()) {
                            //log.debug("gender: ${RefdataValue.getByValueAndCategory(personData.gender.rdv.text(),personData.gender.rdc.text())}")
                            person.gender = RefdataValue.getByValueAndCategory(personData.gender.rdv.text(), personData.gender.rdc.text())
                        }

                        if(personData.isPublic.text() == 'Yes' ) {
                            //log.debug("isPublic: ${personData.isPublic.text()")
                            person.isPublic = true
                        }
                        if(personData.contactType.rdv.size() && personData.contactType.rdc.size()) {
                            //log.debug("contactType: ${RefdataValue.getByValueAndCategory(personData.contactType.rdv.text(),personData.contactType.rdc.text())}")
                            person.contactType = RefdataValue.getByValueAndCategory(personData.contactType.rdv.text(), personData.contactType.rdc.text())
                        }
                        if (!person.save(flash: true) && person.hasErrors()) {
                            log.error("error on saving person: ${person.errors}")
                        }
                    } else {
                        log.info("person with ${personData.globalUID.text()} already in the database, skipping ...")
                    }
                }
            }
            //insert all person roles
            if(orgBase.personRoles) {
                orgBase.personRoles.personRole.each { prData ->
                    log.debug("----- processing person role ${prData.org.text()} <-> ${prData.prs.text()} ------")
                    Org targetOrg = Org.findByGlobalUID(prData.org.text())
                    Person targetPrs = Person.findByGlobalUID(prData.prs.text())
                    RefdataValue functionType = RefdataValue.getByValueAndCategory(prData.functionType.rdv.text(), prData.functionType.rdc.text())
                    PersonRole role = PersonRole.findByOrgAndPrsAndFunctionType(targetOrg,targetPrs,functionType)
                    if(!role) {
                        role = new PersonRole(org: targetOrg, prs: targetPrs, functionType: functionType)
                        if (!role.save() && role.hasErrors()) {
                            log.error("error on saving person role: ${role.errors}")
                        }
                    }
                }
                //insert all addresses
            }
            if(orgBase.addresses) {
                orgBase.addresses.address.each { addressData ->
                    log.debug("----- processing address ${addressData.street1.text()} ${addressData.street2.text()} at ${addressData.zipcode.text()} ${addressData.city.text()} -----")
                    Address address
                    if(addressData.org.text()) {
                        address = Address.findByOrg(Org.findByGlobalUID(addressData.org.text()))
                        if(!address) {
                            //log.debug("org: ${addressData.org.text()}")
                            Org addressOrg = Org.findByGlobalUID(addressData.org.text())
                            address = new Address(street_1: addressData.street1.text(), street_2: addressData.street2.text(), zipcode: addressData.zipcode.text(), city: addressData.city.text(), org: addressOrg)
                        }
                    }
                    else {
                        //log.debug("street_1:${addressData.street1.text()},street_2:${addressData.street2.text()},zipcode:${addressData.zipcode.text()},city:${addressData.city.text()}")
                        address = new Address(street_1: addressData.street1.text(), street_2: addressData.street2.text(), zipcode: addressData.zipcode.text(), city: addressData.city.text())
                    }
                    if (addressData.prs.text()) {
                        //log.debug("person: ${addressData.prs.text()}")
                        address.prs = Person.findByGlobalUID(addressData.prs.text())
                    }
                    if (addressData.pob.text()) {
                        //log.debug("pob: ${addressData.pob.text()}")
                        address.pob = addressData.pob.text()
                    }
                    if (addressData.pobZipcode.text()) {
                        //log.debug("pobZipcode: ${addressData.pobZipcode.text()}")
                        address.pobZipcode = addressData.pobZipcode.text()
                    }
                    if (addressData.pobCity.text()) {
                        //log.debug("pobCity: ${addressData.pobCity.text()}")
                        address.pobCity = addressData.pobCity.text()
                    }
                    if (addressData.region.rdv.text() && addressData.region.rdc.text()) {
                        //log.debug("state: ${RefdataValue.getByValueAndCategory(addressData.region.rdv.text(),
                        // addressData.region.rdc.text())}")
                        address.region = RefdataValue.getByValueAndCategory(addressData.region.rdv.text(), addressData
                                .region.rdc.text())
                    }
                    if (addressData.countryElem.rdv.text() && addressData.countryElem.rdc.text()) {
                        //log.debug("country: ${RefdataValue.getByValueAndCategory(addressData.countryElem.rdv.text(),addressData.countryElem.rdc.text())}")
                        address.country = RefdataValue.getByValueAndCategory(addressData.countryElem.rdv.text(), addressData.countryElem.rdc.text())
                    }
                    //log.debug("type: ${RefdataValue.getByValueAndCategory(addressData.type.rdv.text(),addressData.type.rdc.text())}")
                    address.type = RefdataValue.getByValueAndCategory(addressData.type.rdv.text(), addressData.type.rdc.text())
                    if (addressData.name.text()) {
                        //log.debug("name: ${addressData.name.text()}")
                        address.name = addressData.name.text()
                    }
                    if (addressData.additionFirst.text()) {
                        //log.debug("additionFirst: ${addressData.additionFirst.text()}")
                        address.additionFirst = addressData.additionFirst.text()
                    }
                    if (addressData.additionSecond.text()) {
                        //log.debug("additionSecond: ${addressData.additionSecond.text()}")
                        address.additionSecond = addressData.additionSecond.text()
                    }
                    if (!address.save() && address.hasErrors()) {
                        log.error("error on saving address: ${address.errors}")
                    }
                }
            }
            //insert all contacts
            if(orgBase.contacts) {
                orgBase.contacts.contact.each { contactData ->
                    log.debug("----- processing contact ${contactData.content.text()} -----")
                    //log.debug("content: ${contactData.content.text()}")
                    Contact contact = new Contact(content: contactData.content.text())
                    if (contactData.org.text()) {
                        //log.debug("org: ${Org.findByGlobalUID(contactData.org.text())}")
                        contact.org = Org.findByGlobalUID(contactData.org.text())
                    }
                    if (contactData.prs.text()) {
                        //log.debug("person: ${Person.findByGlobalUID(contactData.prs.text())}")
                        contact.prs = Person.findByGlobalUID(contactData.prs.text())
                    }
                    //log.debug("contentType: ${RefdataValue.getByValueAndCategory(contactData.contentType.rdv.text(),contactData.contentType.rdc.text())}")
                    contact.contentType = RefdataValue.getByValueAndCategory(contactData.contentType.rdv.text(), contactData.contentType.rdc.text())
                    //log.debug("type: ${RefdataValue.getByValueAndCategory(contactData.type.rdv.text(),contactData.type.rdc.text())}")
                    contact.type = RefdataValue.getByValueAndCategory(contactData.type.rdv.text(), contactData.type.rdc.text())
                    if (!contact.save() && contact.hasErrors()) {
                        log.error("error on saving contact: ${contact.errors}")
                    }
                }
            }

        }
        catch (FileNotFoundException e) {
            log.error("Data not found - PANIC!")
        }
    }

}
