package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.kbplus.auth.UserRole
import com.k_int.properties.PropertyDefinition
import groovy.util.logging.Log4j
import groovy.util.slurpersupport.GPathResult

import java.text.SimpleDateFormat

@Log4j
class ApiService {

    def grailsApplication

    /**
     * 
     * @param s
     * @return trimmed string with multiple whitespaces removed
     */
    private String normString(String str){
        if(! str)
            return ""
            
        str = str.replaceAll("\\s+", " ").trim()
        str
    }

    /**
     * 
     * @param obj
     * @return list with children of given object or object if no children
     */
    private List flattenToken(Object obj){
        def result = []

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
                org.sector = RefdataValue.getByValueAndCategory('Higher Education','OrgSector')

                log.debug("#${(count--)} -- creating new org: " + inst.name?.text() + " / " + identifiers)
            }
            else {
                log.debug("#${(count--)} -- updating org: (" + org.id + ") " + org.name)
            }

            // set tenant

            def tenant, isPublic

            if (inst.source == 'edb des hbz') {
               tenant = Org.findByShortnameIlike('hbz')
               isPublic = RefdataValue.getByValueAndCategory('No','YN')
            }
            //else if (inst.source == 'nationallizenzen.de') {
            //    tenant = Org.findByShortcode('NL')
            //}
            if (! tenant) {
                tenant = org
                isPublic = RefdataValue.getByValueAndCategory('Yes','YN')
            }

            // simple attributes

            def now = new Date()
            org.importSource    = inst.source?.text() ?: org.importSource
            org.sortname        = inst.sortname?.text() ?: org.sortname
            org.shortname       = inst.shortname?.text() ?: org.shortname
            org.lastImportDate  = now

            org.fteStudents     = inst.fte_students?.text() ? Long.valueOf(inst.fte_students.text()) : org.fteStudents
            org.fteStaff        = inst.fte_staff?.text() ? Long.valueOf(inst.fte_staff.text()) : org.fteStaff
            org.url             = inst.url?.text() ?: org.url

            // refdatas

            org.libraryType     = RefdataValue.getByCategoryDescAndI10nValueDe('Library Type', inst.library_type?.text()) ?: org.libraryType
            org.libraryNetwork  = RefdataValue.getByCategoryDescAndI10nValueDe('Library Network', inst.library_network?.text()) ?: org.libraryNetwork
            org.funderType      = RefdataValue.getByCategoryDescAndI10nValueDe('Funder Type', inst.funder_type?.text()) ?: org.funderType

            org.save(flush: true)

            // adding new identifiers

            identifiers.each{ it ->
                it.each { k, v ->
                    def ns = IdentifierNamespace.findByNsIlike(k)
                    if (null == Identifier.findByNsAndValue(ns, v)) {
                        new IdentifierOccurrence(org: org, identifier: Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                    }
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
                    RefdataValue.getByCategoryDescAndI10nValueDe('Federal State', inst.county?.text()),
                    RefdataValue.getByCategoryDescAndI10nValueDe('Country', inst.country?.text()),
                    normString("${inst.pob}"),
                    normString("${inst.pobZipcode}"),
                    normString("${inst.pobCity}"),
                    RefdataValue.getByValueAndCategory('Postal address', 'AddressType'),
                    null,
                    org
            )

            // billing address

            inst.billing_address.each{ billing_address ->

                def tmp2 = resolveStreet(billing_address.street?.text())

                def billingAddress = Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
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
                        RefdataValue.getByValueAndCategory('Billing address', 'AddressType'),
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

                def legalAddress = Address.lookupOrCreate( // name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation
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
                        RefdataValue.getByValueAndCategory('Legal patron address', 'AddressType'),
                        null,
                        org
                )
                if (legal_address.url.text()) {
                    log.debug("setting urlGov to: " + legal_address.url)
                    org.setUrlGov(legal_address.url?.text())
                    org.save(flush: true)
                }
            }

            inst.private_property.children().each { pp ->
                def name = pp.name().replace('_', ' ').toLowerCase()

                def type = PropertyDefinition.findWhere(
                        name: name,
                        type: 'class ' + RefdataValue.class.name,
                        descr:  PropertyDefinition.ORG_PROP,
                        tenant: tenant
                )
                if (! type) {
                    type = PropertyDefinition.findWhere(
                            name: name,
                            type: 'class ' + String.class.name,
                            descr:  PropertyDefinition.ORG_PROP,
                            tenant: tenant
                    )
                }

                if (type) {
                    def opp
                    def check = "private property: " + name + " : " + pp.text()

                    if (type.refdataCategory) {
                        def rdv = RefdataValue.getByCategoryDescAndI10nValueDe(type.refdataCategory, pp.text())
                        if (rdv) {
                            opp = OrgPrivateProperty.findWhere(type: type, owner: org, refValue: rdv)
                            if (! opp) {
                                opp = new OrgPrivateProperty(type: type, owner: org, refValue: rdv)
                                opp.save() ? (check += " > OK") : (check += " > FAIL")
                            } else {
                                check = "ignored existing " + check
                            }
                        }
                    }
                    else {
                        opp = OrgPrivateProperty.findWhere(type: type, owner: org, stringValue: pp.text())
                        if (! opp) {
                            opp = new OrgPrivateProperty(type: type, owner: org, stringValue: pp.text())
                            opp.save() ? (check += " > OK") : (check += " > FAIL")
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

            def rdvContactPerson = RefdataValue.getByValueAndCategory('General contact person', 'Person Function')
            def rdvJobRelated    = RefdataValue.getByValueAndCategory('Job-related','ContactType')

            inst.person?.children().each { p ->

                def rdvContactType   = RefdataValue.getByValueAndCategory('Personal contact', 'Person Contact Type')

                if( ! p.first_name.text() ) {
                    rdvContactType = RefdataValue.getByValueAndCategory('Functional contact', 'Person Contact Type')
                }
                def person = Person.lookup( // firstName, lastName, tenant, isPublic, contactType, org, functionType
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
                            gender: RefdataValue.getByCategoryDescAndI10nValueDe('Gender', p.gender?.text())
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
                            def rdv = RefdataValue.getByCategoryDescAndI10nValueDe('Person Function', func.text())
                            if (rdv) {
                                def pf = new PersonRole(
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
                                    RefdataValue.getByValueAndCategory('E-Mail','ContactContentType'),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                        if (p.fax) {
                            Contact.lookupOrCreate(
                                    normString("${p.fax}"),
                                    RefdataValue.getByValueAndCategory('Fax','ContactContentType'),
                                    rdvJobRelated,
                                    person,
                                    null
                            )
                        }
                        if (p.telephone) {
                            Contact.lookupOrCreate(
                                    normString("${p.telephone}"),
                                    RefdataValue.getByValueAndCategory('Phone','ContactContentType'),
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

    GPathResult makeshiftSubscriptionImport(GPathResult xml){

        // TODO: in progress - erms-746
        def count = xml.institution.size()
        log.debug("importing ${count} items")

        xml.subscription.each { sub ->
            def strName = sub.name.text()
            def rdvType = RefdataValue.getByValueAndCategory(type.name.text(), 'Subscription Type')

            log.debug("processing ${strName} / ${rdvType.getI10n('value')}")
        }
    }

    /*
        hic codex data pro organisationibus atque utilisatoribus leget et in repositorium datium scribit si hoc repositorium datium nulla data continet. In casis altris codex nihil facet
        We should not think in Latin - this code reads off data from an existing dump and writes them into the database
    */
    void setupBasicData() {
        log.debug("database is probably empty; setting up essential data ..")
        try {
            def orgBase = new XmlSlurper().parse(new File(grailsApplication.config.basicDataPath+grailsApplication.config.basicDataFileName))
            //insert all organisations
            orgBase.organisations.org.each { orgData ->
                //filter out probably deleted orgs
                if (!orgData.name.text().contains("DEL")) {
                    //insert raw org data
                    Org org = new Org()
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
                    if (orgData.impId.text()) {
                        //log.debug("impId: ${orgData.impId.text()}")
                        org.impId = orgData.impId.text()
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
                    if (orgData.federalState.rdv.size() && orgData.federalState.rdc.size()) {
                        //log.debug("federalState: ${RefdataValue.getByValueAndCategory(orgData.federalState.rdv.text(),orgData.federalState.rdc.text())}")
                        org.federalState = RefdataValue.getByValueAndCategory(orgData.federalState.rdv.text(), orgData.federalState.rdc.text())
                    }
                    if (orgData.libraryType.rdv.size() && orgData.libraryType.rdc.size()) {
                        //log.debug("libraryType: ${RefdataValue.getByValueAndCategory(orgData.libraryType.rdv.text(),orgData.libraryType.rdc.text())}")
                        org.libraryType = RefdataValue.getByValueAndCategory(orgData.libraryType.rdv.text(), orgData.libraryType.rdc.text())
                    }
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
                            Identifier id = Identifier.lookupOrCreateCanonicalIdentifier(idData.@namespace.text(), idData.@value.text())
                            //log.debug("org: ${orgData.globalUID.text()}")
                            new IdentifierOccurrence(org: org, identifier: id).save(flush: true)
                        }
                    } else if (org.hasErrors()) {
                        log.error("Error on saving org: ${org.getErrors()}")
                        System.exit(46)
                    }
                }
            }
            //insert all users
            orgBase.users.user.each { userData ->
                if (!User.findByUsername(userData.username.text())) {
                    User user = new User()
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
                    if (userData.apikey.text())
                        user.apikey = userData.apikey.text()
                    if (userData.apisecret.text())
                        user.apisecret = userData.apisecret.text()
                    //log.debug("password: ${userData.password.text()}")
                    user.password = "bob" //temp, we will override it with the correct hash
                    if (user.save(flush: true)) {
                        //data to be processed after save()
                        userData.roles.role.each { uRole ->
                            //log.debug("role: ${Role.findByAuthority(uRole.text())}")
                            new UserRole(user: user, role: Role.findByAuthority(uRole.text())).save(flush: true) //null pointer exception occuring, make further tests
                        }
                        userData.settings.setting.each { st ->
                            log.debug("name: ${UserSettings.KEYS.valueOf(st.name.text())}")
                            //log.debug("value: ${st.value.text()}")
                            if (st.rdValue.size()) {
                                UserSettings.add(user, UserSettings.KEYS.valueOf(st.name.text()), RefdataValue.getByValueAndCategory(st.rdValue.rdv.text(), st.rdValue.rdc.text()))
                            } else if (st.org.size()) {
                                Org org = Org.findByGlobalUID(st.org.text())
                                if (org) {
                                    UserSettings.add(user, UserSettings.KEYS.valueOf(st.name.text()), org)
                                }
                            } else {
                                UserSettings.add(user, UserSettings.KEYS.valueOf(st.name.text()), st.value.text())
                            }
                        }
                        User.executeUpdate('update User u set u.password = :password where u.username = :username', [password: userData.password.text(), username: userData.username.text()])
                    } else if (user.hasErrors()) {
                        log.error("error on saving user: ${user.getErrors()}")
                        System.exit(47)
                    }
                }
            }
            //insert all user affiliations
            orgBase.affiliations.affiliation.each { affData ->
                log.debug("----- processing affiliation ${affData.user.text()} <-> ${affData.org.text()} -----")
                UserOrg affiliation = new UserOrg(user: User.findByUsername(affData.user.text()), org: Org.findByGlobalUID(affData.org.text()), status: affData.status.text())
                if (affData.formalRole.text()) {
                    affiliation.formalRole = Role.findByAuthority(affData.formalRole.text())
                }
                if (affData.dateActioned.text()) {
                    affiliation.dateActioned = Long.parseLong(affData.dateActioned.text())
                }
                if (affData.dateRequested.text()) {
                    affiliation.dateRequested = Long.parseLong(affData.dateRequested.text())
                }
                if (!affiliation.save(flash: true) && affiliation.hasErrors()) {
                    log.error("error on saving affiliation: ${affiliation.getErrors()}")
                    System.exit(47)
                }
            }
            //insert all combos
            orgBase.combos.combo.each { comboData ->
                log.debug("----- processing combo ${comboData.fromOrg.text()} -> ${comboData.toOrg.text()} -----")
                //log.debug("type: ${RefdataValue.getByValueAndCategory(comboData.type.rdv.text(),comboData.type.rdc.text())}, fromOrg: ${comboData.fromOrg.text()}, toOrg: ${comboData.toOrg.text()}")
                Combo combo = new Combo(type: RefdataValue.getByValueAndCategory(comboData.type.rdv.text(), comboData.type.rdc.text()), fromOrg: Org.findByGlobalUID(comboData.fromOrg.text()), toOrg: Org.findByGlobalUID(comboData.toOrg.text()))
                if (!combo.save(flush: true) && combo.hasErrors()) {
                    log.error("error on saving combo: ${combo.getErrors()}")
                    System.exit(47)
                }
            }
            //insert all persons
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
                    //log.debug("tenant: ${Org.findByGlobalUID(personData.tenant.text())}")
                    person.tenant = Org.findByGlobalUID(personData.tenant.text())
                    //log.debug("gender: ${RefdataValue.getByValueAndCategory(personData.gender.rdv.text(),personData.gender.rdc.text())}")
                    person.gender = RefdataValue.getByValueAndCategory(personData.gender.rdv.text(), personData.gender.rdc.text())
                    //log.debug("isPublic: ${RefdataValue.getByValueAndCategory(personData.isPublic.rdv.text(),personData.isPublic.rdc.text())}")
                    person.isPublic = RefdataValue.getByValueAndCategory(personData.isPublic.rdv.text(), personData.isPublic.rdc.text())
                    //log.debug("contactType: ${RefdataValue.getByValueAndCategory(personData.contactType.rdv.text(),personData.contactType.rdc.text())}")
                    person.contactType = RefdataValue.getByValueAndCategory(personData.contactType.rdv.text(), personData.contactType.rdc.text())
                    if (!person.save(flash: true) && person.hasErrors()) {
                        log.error("error on saving person: ${person.getErrors()}")
                        System.exit(47)
                    }
                } else {
                    log.info("person with ${personData.globalUID.text()} already in the database, skipping ...")
                }
            }
            //insert all person roles
            orgBase.personRoles.personRole.each { prData ->
                log.debug("----- processing person role ${prData.org.text()} <-> ${prData.prs.text()} ------")
                PersonRole role = new PersonRole(org: Org.findByGlobalUID(prData.org.text()), prs: Person.findByGlobalUID(prData.prs.text()), functionType: RefdataValue.getByValueAndCategory(prData.functionType.rdv.text(), prData.functionType.rdc.text()))
                if (!role.save(flush: true) && role.hasErrors()) {
                    log.error("error on saving person role: ${role.getErrors()}")
                    System.exit(47)
                }
            }
            //insert all addresses
            orgBase.addresses.address.each { addressData ->
                log.debug("----- processing address ${addressData.street1.text()} ${addressData.street2.text()} at ${addressData.zipcode.text()} ${addressData.city.text()} -----")
                //log.debug("street_1:${addressData.street1.text()},street_2:${addressData.street2.text()},zipcode:${addressData.zipcode.text()},city:${addressData.city.text()}")
                Address address = new Address(street_1: addressData.street1.text(), street_2: addressData.street2.text(), zipcode: addressData.zipcode.text(), city: addressData.city.text())
                if (addressData.org.text()) {
                    //log.debug("org: ${addressData.org.text()}")
                    address.org = Org.findByGlobalUID(addressData.org.text())
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
                if (addressData.state.rdv.text() && addressData.state.rdc.text()) {
                    //log.debug("state: ${RefdataValue.getByValueAndCategory(addressData.state.rdv.text(),addressData.state.rdc.text())}")
                    address.state = RefdataValue.getByValueAndCategory(addressData.state.rdv.text(), addressData.state.rdc.text())
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
                if (!address.save(flash: true) && address.hasErrors()) {
                    log.error("error on saving address: ${address.getErrors()}")
                    System.exit(47)
                }
            }
            //insert all contacts
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
                if (!contact.save(flash: true) && contact.hasErrors()) {
                    log.error("error on saving contact: ${contact.getErrors()}")
                    System.exit(47)
                }
            }
            //System.exit(45)
        }
        catch (FileNotFoundException e) {
            log.error("Data not found - PANIC!")
            System.exit(48)
        }
    }

}
