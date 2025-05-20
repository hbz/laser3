package de.laser

import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.wekb.ProviderRole
import de.laser.wekb.VendorRole
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.springframework.context.MessageSource
import org.xml.sax.SAXParseException

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import java.text.SimpleDateFormat
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * This service handles license specific matters
 * @see License
 */
@Transactional
class LicenseService {

    ContextService contextService
    GenericOIDService genericOIDService
    GrailsApplication grailsApplication
    MessageSource messageSource

    Set<PropertyDefinition> AGENT_DEFINITION_PROPS = [PropertyStore.LIC_AUTHORIZED_USERS, PropertyStore.LIC_LOCAL_AUTHORIZED_USER_DEFINITION, PropertyStore.LIC_USAGE_STATISTICS_ADDRESSEE]
    Set<PropertyDefinition> TIME_POINT_DEFINITION_PROPS = [PropertyStore.LIC_OA_FIRST_DATE, PropertyStore.LIC_OA_LAST_DATE, PropertyStore.LIC_PERPETUAL_COVERAGE_FROM, PropertyStore.LIC_PERPETUAL_COVERAGE_TO]
    Set<PropertyDefinition> GENERAL_USAGE_STATEMENT_PROPS = [PropertyStore.LIC_ALUMNI_ACCESS, PropertyStore.LIC_CONFORMITY_WITH_URHG, PropertyStore.LIC_DISTANCE_EDUCATION,
                                                                    PropertyStore.LIC_FAIR_USE_CLAUSE_INDICATOR, PropertyStore.LIC_PARTNERS_ACCESS, PropertyStore.LIC_REMOTE_ACCESS,
                                                                    PropertyStore.LIC_SINGLE_USER_ACCESS, PropertyStore.LIC_WALK_IN_ACCESS, PropertyStore.LIC_WIFI_ACCESS]
    Set<PropertyDefinition> COURSE_PACK_USAGE_STATEMENT_PROPS = [PropertyStore.LIC_COURSE_PACK_ELECTRONIC, PropertyStore.LIC_COURSE_PACK_PRINT, PropertyStore.LIC_COURSE_RESERVE_ELECTRONIC, PropertyStore.LIC_COURSE_RESERVE_PRINT]
    Set<PropertyDefinition> ILL_USAGE_STATEMENT_PROPS = [PropertyStore.LIC_ILL_ELECTRONIC, PropertyStore.LIC_ILL_PRINT_OR_FAX, PropertyStore.LIC_ILL_RECORD_KEEPING_REQUIRED, PropertyStore.LIC_ILL_SECURE_ELECTRONIC_TRANSMISSION]
    Set<PropertyDefinition> CORE_USAGE_TERMS = [PropertyStore.LIC_DIGITAL_COPY, PropertyStore.LIC_DIGITAL_COPY_TERM_NOTE, /*PropertyStore.LIC_DOCUMENT_DELIVERY_SERVICE,*/ PropertyStore.LIC_ELECTRONIC_LINK,
                                                  PropertyStore.LIC_SCHOLARLY_SHARING, PropertyStore.LIC_SCHOLARLY_SHARING_TERM_NOTE, PropertyStore.LIC_PRINT_COPY, PropertyStore.LIC_PRINT_COPY_TERM_NOTE, PropertyStore.LIC_TDM,
                                                  PropertyStore.LIC_TDM_RESTRICTIONS, PropertyStore.LIC_TDM_CHAR_COUNT]+GENERAL_USAGE_STATEMENT_PROPS
    Set<PropertyDefinition> USAGE_TERMS = CORE_USAGE_TERMS+AGENT_DEFINITION_PROPS+GENERAL_USAGE_STATEMENT_PROPS+COURSE_PACK_USAGE_STATEMENT_PROPS+ILL_USAGE_STATEMENT_PROPS
    Set<PropertyDefinition> SUPPLY_TERMS = [PropertyStore.LIC_ACCESSIBILITY_COMPLIANCE, PropertyStore.LIC_CHANGE_TO_LICENSED_MATERIAL, PropertyStore.LIC_COMPLETENESS_OF_CONTENT_CLAUSE,
                                                   PropertyStore.LIC_CONCURRENCY_WITH_PRINT_VERSION, PropertyStore.LIC_CONTENT_WARRANTY, PropertyStore.LIC_CONT_ACCESS_TITLE_TRANSFER,
                                                   PropertyStore.LIC_MAINTENANCE_WINDOW, PropertyStore.LIC_PERFORMANCE_WARRANTY, PropertyStore.LIC_UPTIME_GUARANTEE, PropertyStore.LIC_METADATA_DELIVERY,
                                                   PropertyStore.LIC_METADATA_RELATED_CONTRACTUAL_TERMS, PropertyStore.LIC_OA_NOTE, PropertyStore.LIC_OPEN_ACCESS, PropertyStore.LIC_OA_FIRST_DATE, PropertyStore.LIC_OA_LAST_DATE,
                                                   PropertyStore.LIC_USAGE_STATISTICS_AVAILABILITY_INDICATOR, PropertyStore.LIC_USAGE_STATISTICS_ADDRESSEE]
    Set<PropertyDefinition> CONTINUING_ACCESS_TERMS = [PropertyStore.LIC_ARCHIVAL_COPY_CONTENT, PropertyStore.LIC_ARCHIVAL_COPY_COST, PropertyStore.LIC_ARCHIVAL_COPY_PERMISSION, PropertyStore.LIC_ARCHIVAL_COPY_TIME,
                                                              PropertyStore.LIC_CONT_ACCESS_PAYMENT_NOTE, PropertyStore.LIC_CONT_ACCESS_RESTRICTIONS, PropertyStore.LIC_PERPETUAL_COVERAGE_FROM, PropertyStore.LIC_PERPETUAL_COVERAGE_NOTE,
                                                              PropertyStore.LIC_PERPETUAL_COVERAGE_TO, PropertyStore.LIC_POST_CANCELLATION_ONLINE_ACCESS, PropertyStore.LIC_REPOSITORY]
    Set<PropertyDefinition> PAYMENT_TERMS = [PropertyStore.LIC_OFFSETTING, PropertyStore.LIC_PUBLISHING_FEE, PropertyStore.LIC_READING_FEE]
    Set<PropertyDefinition> GENERAL_TERMS = [PropertyStore.LIC_ALL_RIGHTS_RESERVED_INDICATOR, PropertyStore.LIC_APPLICABLE_COPYRIGHT_LAW, PropertyStore.LIC_BRANDING, PropertyStore.LIC_CANCELLATION_ALLOWANCE,
                                                    PropertyStore.LIC_CLICKWRAP_MODIFICATION, PropertyStore.LIC_CONFIDENTIALITY_OF_AGREEMENT, PropertyStore.LIC_CURE_PERIOD_FOR_BREACH, PropertyStore.LIC_DATABASE_PROTECTION_OVERRIDE,
                                                    PropertyStore.LIC_GOVERNING_JURISDICTION, PropertyStore.LIC_GOVERNING_LAW, PropertyStore.LIC_INDEMNIFICATION_BY_LICENSEE, PropertyStore.LIC_INDEMNIFICATION_BY_LICENSOR,
                                                    PropertyStore.LIC_INTELLECTUAL_PROPERTY_WARRANTY, PropertyStore.LIC_LICENSEE_OBLIGATIONS,
                                                    PropertyStore.LIC_LICENSEE_TERMINATION_CONDITION, PropertyStore.LIC_LICENSEE_TERMINATION_NOTICE_PERIOD, PropertyStore.LIC_LICENSEE_TERMINATION_RIGHT,
                                                    PropertyStore.LIC_LICENSOR_TERMINATION_CONDITION, PropertyStore.LIC_LICENSOR_TERMINATION_NOTICE_PERIOD, PropertyStore.LIC_LICENSOR_TERMINATION_RIGHT,
                                                    PropertyStore.LIC_MULTI_YEAR_LICENSE_TERMINATION, PropertyStore.LIC_TERMINATION_REQUIREMENT_NOTE, PropertyStore.LIC_USER_INFORMATION_CONFIDENTIALITY]

    /**
     * Gets a (filtered) list of licenses to which the context institution has reading rights
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    List<License> getMyLicenses_readRights(Map params){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
            tmpQ = getLicensesConsortiaQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesConsortialLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))
        }
        result
    }

    /**
     * Gets a (filtered) list of licenses to which the context institution has writing rights
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    List<License> getMyLicenses_writeRights(Map params){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
            tmpQ = getLicensesConsortiaQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesConsortialLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getLicensesConsortialLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))
        }
        result.sort {it.dropdownNamingConvention()}
    }

    /**
     * Retrieves consortial parent licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent licenses matching the given filter
     */
    List getLicensesConsortiaQuery(Map params) {
        Map qry_params = [roleTypeC: RDStore.OR_LICENSING_CONSORTIUM, roleTypeL: RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
        String base_qry = """from License as l where (
                    exists ( select o from l.orgRelations as o where ( 
                    ( o.roleType = :roleTypeC 
                        AND o.org = :lic_org 
                        AND l.instanceOf is null
                        AND NOT exists (
                        select o2 from l.orgRelations as o2 where o2.roleType = :roleTypeL
                    )
                )
            )))"""

        if (params.status) {
            base_qry += " and l.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }

        return [base_qry, qry_params]
    }

    /**
     * Retrieves consortial member licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent licenses matching the given filter
     */
    List getLicensesConsortialLicenseQuery(Map params) {
        Map qry_params = [roleType: RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType  AND o.org = :lic_org ) ) 
            )"""

        if (params.status) {
            base_qry += " and l.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }

        return [ base_qry, qry_params ]
    }

    /**
     * Retrieves local licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    List getLicensesLocalLicenseQuery(Map params) {
        Map qry_params = [roleType: RDStore.OR_LICENSEE, lic_org: contextService.getOrg()]
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""

        if (params.status) {
            base_qry += " and l.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }

        return [ base_qry, qry_params ]
    }

    /**
     * Retrieves all visible organisational relationships for the given license, i.e. licensors, providers, agencies, etc.
     * @param license the license to retrieve the relations from
     * @return a sorted list of visible relations
     */
    List getVisibleOrgRelations(License license) {
        List visibleOrgRelations = []
        license?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name?.toLowerCase() }
    }

    /**
     * Retrieves all visible vendor links for the given license
     * @param license the license to retrieve the relations from
     * @return a sorted set of visible relations
     */
    SortedSet<ProviderRole> getVisibleProviders(License license) {
        SortedSet<ProviderRole> visibleProviderRelations = new TreeSet<ProviderRole>()
        visibleProviderRelations.addAll(ProviderRole.executeQuery('select pr from ProviderRole pr join pr.provider p where pr.license = :license order by p.name', [license: license]))
        visibleProviderRelations
    }

    /**
     * Retrieves all visible vendor links for the given license
     * @param license the license to retrieve the relations from
     * @return a sorted set of visible relations
     */
    SortedSet<VendorRole> getVisibleVendors(License license) {
        SortedSet<VendorRole> visibleVendorRelations = new TreeSet<VendorRole>()
        visibleVendorRelations.addAll(VendorRole.executeQuery('select vr from VendorRole vr join vr.vendor v where vr.license = :license order by v.name', [license: license]))
        visibleVendorRelations
    }

    String generateOnixPLDocument(License lic, Org institution) {
        File schemaFile = grailsApplication.mainContext.getResource('files/ONIX_PublicationsLicense_V1.0.xsd').file
        Validator validator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile).newValidator()
        String xml = buildDocument(lic, institution)
        if(xml) {
            try {
                validator.validate(new StreamSource(new StringReader(xml)))
            }
            catch(SAXParseException e) {
                log.error("validation error upon XML creation. Document shows as follows: ")
                log.error(xml)
                log.error("the error: ${e.getMessage()}")
            }
        }
        xml
    }

    /**
     * experiment sandbox 1
     * @return
     */
    String createHardCodedTestFile_v0() {
        SimpleDateFormat onixTimestampFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")
        SimpleDateFormat onixDateFormat = new SimpleDateFormat("yyyyMMdd")
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        Locale locale = LocaleUtils.getCurrentLocale()
        Org institution = contextService.getOrg()
        Date now = new Date()
        //sorting of XML nodes within structure: by LAS:eR property name!
        def xml = builder.bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace(ople: "http://www.editeur.org/ople")
            ONIXPublicationsLicenseMessage([version: '1.0',xmlns: "http://www.editeur.org/onix-pl"]) {
                Header {
                    Sender {
                        SenderName(messageSource.getMessage('laser', null, locale))
                        SenderContact('LAS:eR-Support')
                        SenderEmail('laser@hbz-nrw.de')
                    }
                    Addressee {
                        AddresseeIdentifier {
                            AddresseeIDType('Institutional identifier')
                            IDTypeName('ISIL')
                            IDValue(institution.ids.find { Identifier id -> id.ns.ns == IdentifierNamespace.ISIL }?.value)
                        }
                    }
                    SentDateTime(onixTimestampFormat.format(now))
                }
                PublicationsLicenseExpression {
                    ExpressionDetail {
                        ExpressionType('onixPL:LicenseExpression')
                        //LicenseIdentifier does not support proprietary namespaces
                        ExpressionIdentifier {
                            ExpressionIDType('onixPL:Proprietary')
                            IDTypeName('globalUID')
                            IDValue('license:xxxxxxxxxxxxxxxxx')
                        }
                        ExpressionStatus('onixPL:Approved') //license.status => LicenseDocument
                    }
                    LicenseDetail {
                        Description('Analysestadtverbund-Grundvertrag')
                        LicenseStatus('onixPL:ActiveLicense') //current => ActiveLicense, expired => NoLongerActive, expected => ProposedLicense?
                        //if doc.doctype can be mapped to one of: onixPL:Addendum, onixPL:License, onixPL:LicenseMainTerms, onixPL:LicenseSchedule, onixPL:LicenseSummary
                        LicenseDocument {
                            LicenseDocumentType('onixPL:License') //doc.doctype with mapping
                            DocumentLabel('Grundvertrag') //doc.title
                        }
                        /*
                        orgRole.roleType == LicensingConsortium maps to onixPL:LicenseeRepresentative (def: A representative of the licensee(s) authorized to sign the license (usually when licensees are members of a consortium).)
                        orgRole.roleType == Licensee_Consortial maps to onixPL:LicenseeConsortium (def: A consortium of which licensees under a license are members.)
                        orgRole.roleType == Licensee maps to onixPL:Licensee
                         */
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:Licensor')
                            RelatedAgent('Bloomsbury') //licensor provider.name
                        }
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:LicenseeRepresentative')
                            RelatedAgent('Licensing Consortium') //take the RefdataValue and use this as further reference in the document
                        }
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:LicenseeConsortium')
                            RelatedAgent('Licensee Consortial') //take the RefdataValue and use this as further reference in the document
                        }
                        LicenseRelatedResource {
                            LicenseResourceRelator('onixPL:LicensedContent')
                            RelatedResource('Gentz: Alle Werke')
                            RelatedResource('subscription:xxxxxxxxxxxxxxxxxxx') //maybe
                        }
                        LicenseRelatedTimePoint {
                            LicenseTimePointRelator('onixPL:LicenseStartDate')
                            RelatedTimePoint('LicenseStartDate')
                        }
                        /*
                        if exists
                        LicenseRelatedTimePoint {
                            LicenseTimePointRelator('onixPL:LicenseEndDate')
                            RelatedTimePoint('LicenseEndDate')
                        }
                        */
                    }
                    Definitions {
                        //define here the people and organisations taking part in the license: OrgRoles in detail, moreover Walk-In User and other related parties
                        AgentDefinition {
                            AgentLabel('Licensor') //map ProviderRole to Licensor
                            AgentType('onixPL:Organization')
                            AgentIdentifier {
                                AgentIDType('onixPL:Proprietary')
                                IDTypeName('wekbId')
                                IDValue('wekb UUID')
                            }
                            AgentName {
                                AgentNameType('onixPL:RegisteredName')
                                Name('Bloomsbury') //provider.name
                            }
                        }
                        AgentDefinition {
                            AgentLabel('Licensing Consortium') //use this as internal document referrer
                            AgentType('onixPL:Organization')
                            AgentIdentifier {
                                AgentIDType('onixPL:Proprietary')
                                IDTypeName('globalUID')
                                IDValue('org:xxxxxxxxxxxxxxxxxx')
                            }
                            //for each identifier, ISIL, WIBID, etc.
                            AgentIdentifier {
                                AgentIDType('onixPL:CompanyRegistrationNumber')
                                IDTypeName('ISIL')
                                IDValue('ZD-001')
                            }
                            AgentName {
                                AgentNameType('onixPL:RegisteredName')
                                Name('Lizenzierungskonsortium des Analysestadtverbundes') //org.name
                            }
                            AgentName {
                                AgentNameType('onixPL:CommonName')
                                Name('Zeidel, ASVB') //org.sortname
                            }
                        }
                        AgentDefinition {
                            AgentLabel('Licensee Consortial') //use this as internal document referrer
                            AgentType('onixPL:Organization')
                            //Authority() n/a
                            AgentIdentifier {
                                AgentIDType('onixPL:Proprietary')
                                IDTypeName('globalUID')
                                IDValue('org:xxxxxxxxxxxxxxxxxx')
                            }
                            //for each identifier, ISIL, WIBID, etc.
                            AgentIdentifier {
                                AgentIDType('onixPL:CompanyRegistrationNumber')
                                IDTypeName('ISIL')
                                IDValue('ZD-018')
                            }
                            AgentName {
                                AgentNameType('onixPL:RegisteredName')
                                Name('Friedrich-Vieweg-Hochschule') //org.name
                            }
                            AgentName {
                                AgentNameType('onixPL:CommonName')
                                Name('Karlstadt, FVH') //org.sortname
                            }
                        }
                        AgentDefinition {
                            //license property Authorized Users
                            AgentLabel('Authorized Users')
                            AgentType('onixPL:Person')
                            Description('s. Vertragstext') //sic!; the property value
                            LicenseTextLink(href: 'lp_authorized_users_01')
                        }
                        AgentDefinition {
                            //license property Local authorized user defintion
                            AgentLabel('Local Authorized Users')
                            AgentType('onixPL:Person')
                            Description('Fakultätsangehörige, eingeschriebene Studenten, aktuelle Mitarbeiter, Vertragspersonal, das direkt in Bildungs- und Forschungsaktivitäten eingebunden ist') //property value
                            LicenseTextLink(href: 'lp_local_authorized_user_defintion_01')
                        }
                        AgentDefinition {
                            //license property Usage Statistics Addressee
                            AgentLabel('Usage Statistics Addressee')
                            AgentType('onixPL:Person')
                            Description('benannte Administratoren des Kunden')
                            LicenseTextLink(href: 'lp_usage_statistics_addressee_01')
                        }
                        ResourceDefinition {
                            ResourceLabel('Subscription')
                            ResourceType('onixPL:LicensedContent') //supplying; ResourceType has no controlled list behind
                            ResourceIdentifier {
                                ResourceIDType('onixPL:Proprietary')
                                IDTypeName('globalUID')
                                IDValue('subscription:xxxxxxxxxxxxxxxxxxxxx')
                            }
                            Description('Gentz: Alle Werke') //subscription.name
                        }
                        //(all other) properties with type date
                        TimePointDefinition {
                            TimePointLabel('ArchivalCopyTimePoint')
                            Description('Trigger Event') //maps refdata value of license property Archival Copy: Time
                        }
                        TimePointDefinition {
                            //mapping license property OA First Date
                            TimePointLabel('OAFirstDate')
                            TimePointIdentifier {
                                TimePointIDType('onixPL:YYYYMMDD')
                                IDValue(20210801) //format YYYYmmdd
                            }
                        }
                        TimePointDefinition {
                            //mapping license property OA Last Date
                            TimePointLabel('OALastDate')
                            TimePointIdentifier {
                                TimePointIDType('onixPL:YYYYMMDD')
                                IDValue(20211231) //format YYYYmmdd
                            }
                        }
                        TimePointDefinition {
                            //mapping license property Perpetual coverage from
                            TimePointLabel('PerpetualCoverageFrom')
                            TimePointIdentifier {
                                TimePointIDType('onixPL:YYYYMMDD')
                                IDValue(20130211) //format YYYYmmdd
                            }
                        }
                        TimePointDefinition {
                            //mapping license property Perpetual coverage to
                            TimePointLabel('PerpetualCoverageTo')
                            TimePointIdentifier {
                                TimePointIDType('onixPL:YYYYMMDD')
                                IDValue(20281231) //format YYYYmmdd
                            }
                        }
                        PlaceDefinition {
                            //mapping license property Repository
                            PlaceLabel('Repository')
                            PlaceName {
                                Name('Own Choice') //the refdata value
                            }
                        }
                    }
                    //optional 0-1
                    LicenseGrant {
                        //license property Citation requirement detail
                        Annotation {
                            AnnotationType('onixPL:ERMI:CitationRequirementDetail')
                            AnnotationText('[…],,Die Daten aus dem VDE-Vorschrittenwerk (DIN VDENormen) sind diesem mit Erlaubnis des DIN Deutsches Institut für Normung e.V. und des VDE Verband der Elektrotechnik Elektronik Informationstechnik e. V. entnommen worden. Sie entsprechen dem Stand des Deutschen NormenwerksNDE-Vorschriftenwerks vom ... gez. (Name des Kunden)". […]')
                        }
                        //license property Other Use Restriction Note
                        Annotation {
                            AnnotationType('onixPL:ERMI:OtherUseRestrictionNote')
                            AnnotationText('"[...] Authorized Users may not: 1) download, reproduce, retain or redistribute the Licensed Products [...] in its entirety [...], including, but not limited to, accessing the Licensed Products using a robot, spider, crawler or similar technological device; (2) electronically distribute, via e-mail or otherwise, any Article or eBook; (3) abridge, modify, translate or create any derivative work based upon the Licensed Products without the prior written consent of IEEE; (4) display or otherwise make available any part [...] to anyone other than Authorized Users; (5) sell, resell, rent, lease, license, sublicense, assign or otherwise transfer any rights granted under this Agreement, [...]; (6) remove, obscure or modify in any way copyright notices, other notices or disclaimers that appear on Articles or eBooks or in the Licensed Products."')
                        }
                    }
                    //mandatory 1
                    UsageTerms {
                        //covers Authorized Users, Alumni Access, Walk-In User and many others
                        //multiple Usage statements per each status; include user if status applicable
                        Usage {
                            UsageType('onixPL:Use')
                            UsageStatus('onixPL:Permitted')
                            Annotation {
                                AnnotationType('onixPL:ERMI:WalkInUserTermNote')
                                AnnotationText('Nur in den Räumen der Bibliothek') //license property Walk-in User Term Note
                            }
                            LicenseTextLink(href: 'lp_concurrent_users_01')
                            LicenseTextLink(href: 'lp_conformity_with_urhg_01')
                            LicenseTextLink(href: 'lp_method_of_authentication_01')
                            LicenseTextLink(href: 'lp_remote_access_01')
                            LicenseTextLink(href: 'lp_singleuseraccess_01')
                            LicenseTextLink(href: 'lp_walkin_access_01')
                            LicenseTextLink(href: 'lp_wifi_access_01')
                            User('Authorized Users')
                            User('Local Authorized Users')
                            User('onixPL:LicenseeInstitutionAndPartners') //license property Partners Access; no example value in current dataset
                            User('onixPL:WalkInUser') //license property Walk-In User
                            UsedResource('Subscription')
                            UsagePurpose('onixPL:PersonalUse')
                            UsageMethod('onixPL:SecureAuthentication') //license property Method of Authentication
                            UsageMethod('onixPL:PublicNetwork') //Wifi Access
                            /*
                            regex check:
                            if('shibboleth' in lp.value.toLowerCase())
                                UsageMethod('onixPL:Shibboleth') //license property Method of Authentication
                            if('ip' in lp.value.toLowerCase())
                                UsageMethod('onixPL:IPAddressAuthentication') //license property Method of Authentication
                            if('password' in lp.value.toLowerCase())
                                UsageMethod('onixPL:PasswordAuthentication') //license property Method of Authentication
                             */
                            UsageCondition('onixPL:ComplianceWithApplicableCopyrightLaw') //license property Conformity with UrhG; move in Prohibited when No
                            UsageCondition('onixPL:SubjectToFairUse') //license property Fair use clause indicator; move in Prohibited when No
                            UsageQuantity {
                                //license property Concurrent Users
                                UsageQuantityType('onixPL:NumberOfConcurrentUsers')
                                QuantityDetail {
                                    Value(2000)
                                    QuantityUnit('onixPL:Users')
                                }
                            }
                            UsageRelatedPlace {
                                UsagePlaceRelator('onixPL:PlaceOfUsage')
                                RelatedPlace('onixPL:RemoteLocation')
                            }
                        }
                        //multiple Usage statements per each status; include user if status applicable
                        Usage {
                            UsageType('onixPL:Use')
                            UsageStatus('onixPL:Prohibited')
                            //base: license #4060, tenant is HeBIS-Konsortium
                            LicenseTextLink(href: 'lp_alumni_access_01')
                            LicenseTextLink(href: 'lp_distance_education_01')
                            User('onixPL:LicenseAlumnus')
                            User('onixPL:LicenseeDistanceLearningStudent') //license property Distance Education
                            /*
                                //!!! mark here the ___existence___ of the following properties: Walk-In User, (Simuser)
                                RelatedAgent('onixPL:WalkInUser')
                             */
                            UsedResource('Subscription')
                        }
                        //multiple Usage statements per each status; include user if status applicable
                        /*
                        Usage {
                            UsageType('onixPL:Use')
                            UsageStatus('onixPL:SilentUninterpreted') //for y_n_o other or y_n_u unclear
                        }
                        */
                        Usage {
                            //license properties Course pack electronic/cached, Course pack print, Course reserve electronic/cached, Course reserve print; group by status
                            UsageType('onixPL:MakeAvailable')
                            UsageStatus('onixPL:Permitted') //its refdata value
                            Annotation {
                                //license property Course pack term note
                                AnnotationType('onixPL:SpecialConditions')
                                AnnotationText('Hierbei stellt der Lizenznehmer sicher, dass nur Personen, die berechtigte Nutzer und gleichzeitig Teilnehmer der jeweiligen Lehrveranstaltung sind, Zugang zu diesen Readern erhalten.') //value xor paragraph
                            }
                            Annotation {
                                //license property Course reserve term note
                                AnnotationType('onixPL:Interpretation') //if value; else if paragraph, then onixPL:SpecialConditions
                                AnnotationText('keine Beschränkung') //value xor paragraph
                            }
                            LicenseTextLink(href: 'lp_course_pack_electronic_01')
                            LicenseTextLink(href: 'lp_course_pack_print_01')
                            LicenseTextLink(href: 'lp_course_reserve_electronic_01')
                            LicenseTextLink(href: 'lp_course_reserve_print_01')
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                            UsageRelatedResource {
                                UsageResourceRelator('onixPL:TargetResource')
                                RelatedResource('onixPL:CoursePackElectronic')
                                RelatedResource('onixPL:CoursePackPrint')
                                RelatedResource('onixPL:CourseReserveElectronic')
                                RelatedResource('onixPL:CourseReservePrint')
                            }
                        }
                        Usage {
                            //license property Document delivery service (commercial)
                            UsageType('onixPL:MakeAvailable')
                            UsageStatus('onixPL:Prohibited') //its refdata value
                            LicenseTextLink(href: 'lp_document_delivery_service_commercial_01')
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                            UsagePurpose('onixPL:CommercialUse')
                        }
                        Usage {
                            //license property Electronic link
                            UsageType('onixPL:MakeAvailable')
                            UsageStatus('onixPL:Permitted') //its refdata value
                            Annotation {
                                //license property Electronic link term note
                                AnnotationType('onixPL:SpecialConditions')
                                AnnotationText('C. TERMS AND CONDITIONS OF USE c. The Licensee and its Authorized Users may create links to Wiley InterScience from their Online Public Access Catalog (OPAC) records, library catalogs, locally hosted databases or library web pages, provided those links do not result in access to licensed content by anyone other than Authorized Users, or for use in any paid or commercial service.')
                            }
                            LicenseTextLink(href: 'lp_electronic_link_01')
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                            UsageRelatedResource {
                                UsageResourceRelator('onixPL:TargetResource')
                                RelatedResource('onixPL:LinkToLicensedContent')
                            }
                        }
                        Usage {
                            //license property Scholarly sharing
                            UsageType('onixPL:MakeAvailable')
                            UsageStatus('onixPL:Permitted') //its refdata value
                            Annotation {
                                //license property Scholarly sharing term note
                                AnnotationType('onixPL:SpecialConditions')
                                AnnotationText('§ 3 Abs. 1f: Incorporate Parts of the Licensed Material in printed or electronic form in assignments and portfolios, theses and in dissertations ( the Academic Works ), including reproductions of the Academic Works for personal use and library deposit. Reproductions in printed or electronic form of Academic Works may be provided to Sponsors of such Academic Works. Each item shall carry appropriate acknowledgement of the source; Publicly display or publicly perform Parts of the Licensed Material as part of a presentation at a seminar, Conference,-workshop, or other such similar activity. Deposit in perpetuity the learning and teaching objects as referred to in § 3.1.b on Servers operated by the Institution or Licensee. The use of such material shall be limited to Authorised Users.')
                            }
                            LicenseTextLink(href: 'lp_scholarly_sharing_01')
                            User('Licensee Consortial')
                            User('onixPL:AuthorizedUser')
                            UsedResource('Subscription')
                            UsageRelatedAgent {
                                UsageAgentRelator('onixPL:ReceivingAgent')
                                RelatedAgent('onixPL:ExternalAcademic')
                            }
                        }
                        Usage {
                            //license property Digital copy
                            UsageType('onixPL:MakeDigitalCopy')
                            UsageStatus('onixPL:Permitted') //its refdata value
                            Annotation {
                                //license property Digital copy term note
                                AnnotationType('onixPL:SpecialConditions')
                                AnnotationText('10.2: Das Nutzungsrecht berechtigt zur Recherche und zum Lesezugriff, zum Herunterladen und einmaligen Abspeichern eines Dokuments auf dem Endgerät des Kunden oder des berechtigten Nutzers sowie zum einmaligen Ausdruck des Dokuments. Eine weitere Vervielfältigung oder das sonstige Verwerten von Dokumenten oder sonstigen Elementen der Datenbank ist nur mit vorangehender schriftlicher Zustimmung des Verlags zulässig [...]') //value xor paragraph
                            }
                            LicenseTextLink(href: 'lp_digitial_copy_01')
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                        }
                        Usage {
                            //license properties ILL electronic, ILL print or fax, ILL record keeping required, ILL secure electronic transmission, ILL term note; group by status
                            UsageType('onixPL:SupplyCopy')
                            UsageStatus('onixPL:Permitted') //one statement per permission status; structure: permission -> property
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Keine Fernleihe an kommerzielle Bibliotheken liefern') //license property ILL term note - value xor paragraph; include everywhere (to be sure)
                            }
                            LicenseTextLink(href: 'lp_ill_electronic_01')
                            LicenseTextLink(href: 'lp_ill_print_or_fax_01')
                            LicenseTextLink(href: 'lp_ill_secure_electronic_transmission_01')
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                            UsageMethod('onixPL:ElectronicTransmission') //license property ILL electronic
                            UsageMethod('onixPL:SecureElectronicTransmission') //license property ILL secure electronic transmission
                            UsageMethod('onixPL:Fax') //license property ILL print or fax
                            UsageCondition('onixPL:RecordKeepingRequired') //license property ILL record keeping required - currently no example available!
                        }
                        Usage {
                            //license properties ILL electronic, ILL print or fax, ILL record keeping required, ILL secure electronic transmission, ILL term note; group by status
                            UsageType('onixPL:SupplyCopy')
                            UsageStatus('onixPL:Prohibited') //one statement per permission status; structure: permission -> property
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Keine Fernleihe an kommerzielle Bibliotheken liefern') //license property ILL term note - value xor paragraph; include everywhere (to be sure)
                            }
                            LicenseTextLink(href: 'lp_ill_electronic_02')
                            LicenseTextLink(href: 'lp_ill_print_or_fax_02')
                            LicenseTextLink(href: 'lp_ill_secure_electronic_transmission_02')
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                            UsageMethod('onixPL:ElectronicTransmission') //license property ILL electronic
                            UsageMethod('onixPL:SecureElectronicTransmission') //license property ILL secure electronic transmission
                            UsageMethod('onixPL:Fax') //license property ILL print or fax
                            UsageCondition('onixPL:RecordKeepingRequired') //license property ILL record keeping required - currently no example available!
                        }
                        Usage {
                            //license properties Print Copy and Print copy term note
                            UsageType('onixPL:PrintCopy')
                            UsageStatus('onixPL:Permitted')
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Erlaubt für den persönlicher Gebrauch.')
                            }
                            LicenseTextLink(href: 'lp_print_copy_01') //license property Print Copy
                            LicenseTextLink(href: 'lp_print_copy_term_note_01') //license property Print copy term note
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                        }
                        Usage {
                            //license properties Text- and Datamining, Text- and Datamining Character Count, Text- and Datamining Restrictions
                            UsageType('onixPL:UseForDataMining')
                            UsageStatus('onixPL:InterpretedAsPermitted') //its refdata value
                            Annotation {
                                AnnotationType('onixPL:Interpretation') //license property Text- and Datamining Restrictions
                                AnnotationText('Zustimmung des Verlags erforderlich, außer es ist gesetzlich erlaubt. In dem Fall vorheringe Information des Verlags notwendig')
                            }
                            LicenseTextLink(href: 'lp_text_and_datamining_01') //license property Text- and Datamining
                            LicenseTextLink(href: 'lp_text_and_datamining_character_count_01')
                            LicenseTextLink(href: 'lp_text_and_datamining_restrictions_01')
                            User('Licensee Consortial')
                            UsedResource('Subscription')
                            UsageCondition('onixPL:SubjectToVolumeLimit') //license property Text- and Datamining Character Count
                        }
                    }
                    //optional 0-1
                    SupplyTerms {
                        SupplyTerm {
                            //license property Accessibility compliance
                            SupplyTermType('onixPL:ComplianceWithAccessibilityStandards')
                            TermStatus('onixPL:Yes') //or no or uncertain
                            //the reference
                            LicenseTextLink(href: 'lp_accessibility_compliance_01')
                        }
                        SupplyTerm {
                            //license property Change to licensed material
                            SupplyTermType('onixPL:ChangesToLicensedContent')
                            LicenseTextLink(href: 'lp_change_to_licensed_material_01')
                        }
                        SupplyTerm {
                            //license property Completeness of content clause
                            SupplyTermType('onixPL:CompletenessOfContent')
                            TermStatus('onixPL:Yes') //for Existent
                            LicenseTextLink(href: 'lp_completeness_of_content_clause_01')
                        }
                        SupplyTerm {
                            //license property Concurrency with print version
                            SupplyTermType('onixPL:ConcurrencyWithPrintVersion')
                            TermStatus('onixPL:No') //for Nonexistent
                            LicenseTextLink(href: 'lp_concurrency_with_print_version_01')
                        }
                        SupplyTerm {
                            //license property Content warranty
                            SupplyTermType('onixPL:ContentWarranty')
                            LicenseTextLink(href: 'lp_content_warranty_01')
                        }
                        SupplyTerm {
                            //license property Continuing Access: Title Transfer
                            SupplyTermType('onixPL:ComplianceWithProjectTransferCode')
                            LicenseTextLink(href: 'lp_continuing_access_title_transfer_01')
                        }
                        SupplyTerm {
                            //license properties Maintenance window, Performance warranty and Uptime guarantee
                            SupplyTermType('onixPL:ServicePerformanceGuarantee')
                            Annotation {
                                AnnotationType('onixPL:ERMI:MaintenanceWindow')
                                AnnotationText('regelmäßig täglich zwischen 06:00 Uhr und 08:00 Uhr') //property value Maintenance window
                            }
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Yes') //property value Performance warranty
                            }
                            Annotation {
                                AnnotationType('onixPL:ERMI:UptimeGuarantee')
                                AnnotationText('mind. 99,1% bzgl. eines Kalenderjahres') //property value Uptime guarantee
                            }
                            LicenseTextLink(href: 'lp_maintenance_window_01')
                            LicenseTextLink(href: 'lp_performance_warranty_01')
                            LicenseTextLink(href: 'lp_uptime_guarantee_01')
                        }
                        SupplyTerm {
                            //license property Metadata delivery
                            SupplyTermType('onixPL:MetadataSupply')
                            LicenseTextLink(href: 'lp_metadata_delivery_01')
                            //LicenseTextLink(href: 'lp_metadata-related_contractual_terms_01') //license property Metadata-related contractual terms; no example values in current dataset
                        }
                        SupplyTerm {
                            //license property OA First Date, OA Last Date, OA Note, Open Access
                            SupplyTermType('onixPL:OpenAccessContent')
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('OA Komponente enthalten') //maps license property value OA Note
                            }
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Green Open Access') //maps license property value Open Access
                            }
                            LicenseTextLink(href: 'lp_oa_first_date_01') //license property OA First Date
                            LicenseTextLink(href: 'lp_oa_last_date_01') //license property OA Last Date
                            LicenseTextLink(href: 'lp_oa_note_01') //license property OA Note
                            LicenseTextLink(href: 'lp_open_access_01') //license property Open Access
                            SupplyTermRelatedTimePoint {
                                SupplyTermTimePointRelator('onixPL:SupplyStartDate') //the only permitted list value
                                RelatedTimePoint('OAFirstDate')
                            }
                            SupplyTermRelatedTimePoint {
                                SupplyTermTimePointRelator('onixPL:SupplyStartDate') //the only permitted list value
                                RelatedTimePoint('OALastDate')
                            }
                        }
                        SupplyTerm {
                            //license properties Usage Statistics Addressee and Usage Statistics Availability Indicator
                            SupplyTermType('onixPL:UsageStatistics')
                            TermStatus('onixPL:Yes')
                            LicenseTextLink(href: 'lp_usage_statistics_availability_indicator_01')
                            SupplyTermRelatedAgent {
                                SupplyTermAgentRelator('onixPL:ReceivingAgent')
                                RelatedAgent('Usage Statistics Addressee')
                            }
                        }
                    }
                    //optional 0-1
                    ContinuingAccessTerms {
                        ContinuingAccessTerm {
                            //any of Archival Copy Content and Archival Copy: X
                            ContinuingAccessTermType('onixPL:PostCancellationFileSupply')
                            //license property Archival Copy: Permission
                            TermStatus('onixPL:Yes')
                            //license property Archival Copy Content
                            Annotation {
                                AnnotationType('onixPL:PostCancellationFileSupplyNote')
                                AnnotationText('With Metadata') //reference paragraph here
                            }
                            //license property Archival Copy: Cost
                            Annotation {
                                AnnotationType('onixPL:PaymentNote')
                                AnnotationText('With Charge')
                            }
                            LicenseTextLink(href: 'lp_archival_copy_content_01')
                            LicenseTextLink(href: 'lp_archival_copy_cost_01')
                            LicenseTextLink(href: 'lp_archival_copy_permission_01')
                            ContinuingAccessTermRelatedTimePoint {
                                ContinuingAccessTermTimePointRelator('Archival Copy Permission')
                                RelatedTimePoint('ArchivalCopyTimePoint')
                            }
                        }
                        ContinuingAccessTerm {
                            //license property Post Cancellation Online Access
                            ContinuingAccessTermType('onixPL:PostCancellationOnlineAccess')
                            TermStatus('onixPL:Yes') //maps reference values; implement helper; TermStatusCode is base for RefdataCategory RDConstants.PERMISSIONS
                            //license property Continuing Access: Payment Note
                            Annotation {
                                AnnotationType('onixPL:PaymentNote')
                                AnnotationText('Hosting Fee')
                            }
                            //license property Continuing Access: Restrictions
                            Annotation {
                                AnnotationType('onixPL:PostCancellationOnlineAccessHoldingsNote')
                                AnnotationText('Continued Access to Content (term 12 is not applicable to IOP ebooks, IOP Archive and Article Packs) 12.1 Upon termination of this Licence, where the Member: 12.1.1 is not in breach of any of the terms and conditions of this Licence; and 12.1.2 has paid all its fees in full, a Member(s) will be entitled to have continued access to the issues of the Publications dated with the calendar year in which this Licence commenced. 12.2 Where this Licence remains in force for subsequent full calendar years, and the conditions in term 12.1 apply, the Member will have continued access to the issues of the Publications under this Licence which were dated with those full calendar years (the “Available Content”). All other access shall terminate. 12.3 The Available Content will be made available via a website on payment of an annual maintenance fee and for so long as IOP provides electronic access to that content via that website. If access via a website is no longer available at any time, the Available Content will be made available on disk or some other form of electronic media. 12.4 If, at any time, IOP ceases to publish or distribute any of the Available Content then it will use its reasonable endeavours to negotiate the right for the Member(s) to continue to access it in accordance with these terms and conditions.')
                            }
                            LicenseTextLink(href: 'lp_post_cancellation_online_access_01')
                            LicenseTextLink(href: 'lp_repository_01')
                            ContinuingAccessTermRelatedPlace {
                                ContinuingAccessTermPlaceRelator('Repository')
                                RelatedPlace('Repository')
                            }
                        }
                        ContinuingAccessTerm {
                            //license properties Perpetual coverage from, Perpetual coverage note, Perpetual coverage to
                            ContinuingAccessTermType('onixPL:PostCancellationOnlineAccess')
                            TermStatus('onixPL:Uncertain') //refdata value from Perpetual coverage note
                            LicenseTextLink(href: 'lp_perpetual_coverage_from_01') //license property Perpetual coverage from
                            LicenseTextLink(href: 'lp_perpetual_coverage_to_01') //license property Perpetual coverage to
                            //license property Perpetual coverage from
                            ContinuingAccessTermRelatedTimePoint {
                                ContinuingAccessTermTimePointRelator('Perpetual coverage from')
                                RelatedTimePoint('PerpetualCoverageFrom')
                            }
                            //license property Perpetual coverage to
                            ContinuingAccessTermRelatedTimePoint {
                                ContinuingAccessTermTimePointRelator('Perpetual coverage to')
                                RelatedTimePoint('PerpetualCoverageTo')
                            }
                        }
                    }
                    //optional 0-1
                    PaymentTerms {
                        PaymentTerm {
                            //license property Offsetting
                            PaymentTermType('onixPL:OpenAccessOffset')
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('siehe Angebotsmatrix')
                            }
                            LicenseTextLink(href: 'lp_offsetting_01')
                        }
                        PaymentTerm {
                            //license properties Publishing Fee and Reading Fee
                            PaymentTermType('onixPL:PaymentConditions')
                            Annotation {
                                //license property Publishing Fee
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('an APC which is set to be the list price minus a discount of 20%.')
                            }
                            Annotation {
                                //license property Reading Fee
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Stufe 6.')
                            }
                            LicenseTextLink(href: 'lp_publishing_fee_01') //license property Publishing Fee
                            LicenseTextLink(href: 'lp_reading_fee_01') //license property Reading Fee
                        }
                    }
                    //optional 0-1
                    GeneralTerms {
                        GeneralTerm {
                            //license property All rights reserved indicator
                            GeneralTermType('onixPL:AllRightsReserved')
                            TermStatus('onixPL:Yes')
                            LicenseTextLink(href: 'lp_all_rights_reserved_indicator_01')
                        }
                        GeneralTerm {
                            //license property Applicable copyright law
                            GeneralTermType('onixPL:ApplicableCopyrightLaw')
                            LicenseTextLink(href: 'lp_applicable_copyright_law_01')
                        }
                        GeneralTerm {
                            //license property Branding
                            GeneralTermType('onixPL:UseOfDigitalWatermarking')
                            LicenseTextLink(href: 'lp_branding_01')
                        }
                        GeneralTerm {
                            //license property Cancellation allowance
                            GeneralTermType('onixPL:TerminationWithoutPrejudiceToRights')
                            LicenseTextLink(href: 'lp_cancellation_allowance_01')
                        }
                        GeneralTerm {
                            //license property Clickwrap modification
                            GeneralTermType('onixPL:ClickThroughOverride')
                            TermStatus('onixPL:No')
                            LicenseTextLink(href: 'lp_clickwrap_modification_01')
                        }
                        GeneralTerm {
                            //license property Confidentiality of agreement
                            GeneralTermType('onixPL:ConfidentialityOfAgreement')
                            LicenseTextLink(href: 'lp_confidentiality_of_agreement_01')
                        }
                        GeneralTerm {
                            //license property Cure period for breach
                            GeneralTermType('onixPL:TerminationByBreach')
                            LicenseTextLink(href: 'lp_cure_period_for_breach_01')
                            GeneralTermQuantity {
                                GeneralTermQuantityType('onixPL:PeriodForCureOfBreach')
                                //extractor script "30 Tage" -> 30 and onixPL:Days; throw exception if mapping failed with indicating correct values
                                //parser must be fuzzy; there are values like "3 Monate zum Ende eines Ein-Jahres-Berechnungszeitraums"; that needs to be parsed as well
                                QuantityDetail {
                                    Value(30)
                                    QuantityUnit('onixPL:Days')
                                }
                            }
                        }
                        GeneralTerm {
                            //license property Data protection override
                            GeneralTermType('onixPL:DatabaseProtectionOverride')
                            TermStatus('onixPL:Yes')
                            LicenseTextLink(href: 'lp_data_protection_override_01')
                        }
                        GeneralTerm {
                            //license property Governing jurisdiction
                            GeneralTermType('onixPL:Jurisdiction')
                            LicenseTextLink(href: 'lp_governing_jurisdiction_01')
                        }
                        GeneralTerm {
                            //license property Governing law
                            GeneralTermType('onixPL:GoverningLaw')
                            LicenseTextLink(href: 'lp_governing_law_01')
                        }
                        GeneralTerm {
                            //license property Indemnification by licensee clause indicator
                            GeneralTermType('onixPL:IndemnityAgainstBreach')
                            TermStatus('onixPL:No') //if exists
                            LicenseTextLink(href: 'lp_indemnification_by_licensee_clause_indicator_01')
                        }
                        GeneralTerm {
                            //license property Indemnification by licensor
                            GeneralTermType('onixPL:LicensorIndemnity')
                            TermStatus('onixPL:Uncertain') //if exists, maps y_n_o Other
                            LicenseTextLink(href: 'lp_indemnification_by_licensor_01')
                        }
                        GeneralTerm {
                            //license property Intellectual property warranty
                            GeneralTermType('onixPL:LicensorIntellectualPropertyWarranty')
                            TermStatus('onixPL:Yes') //its refdata value
                            LicenseTextLink(href: 'lp_intellectual_property_warranty_01')
                        }
                        GeneralTerm {
                            //license property Licensee obligations
                            GeneralTermType('onixPL:NotificationOfLicenseeIPAddresses')
                            LicenseTextLink(href: 'lp_licensee_obligations_01')
                        }
                        GeneralTerm {
                            //license properties Licensee termination condition, Licensee termination notice period, Licensee termination right
                            GeneralTermType('onixPL:LicenseeTerminationRight')
                            TermStatus('onixPL:Uncertain') //refdata value of License termination right
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Sollte die Erhöhung mehr als 5% betragen, steht den K0NS0RTIAL TEILNEHMERN ein außerordentliches Kündigungsrecht zu.') //license property Licensee termination condition
                            }
                            LicenseTextLink(href: 'lp_licensee_termination_notice_period_01') //license property Licensee termination notice period
                            LicenseTextLink(href: 'lp_licensee_termination_right_01') //license property Licensee termination right
                        }
                        GeneralTerm {
                            //license properties Licensor termination condition, Licensor termination notice period, Licensor termination right
                            GeneralTermType('onixPL:LicensorTerminationRight')
                            TermStatus('onixPL:Yes') //refdata value of Licenor termination right
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('Sollte Preselect eine grobe Missnutzungen der Inhalte feststellen, wird die Hochschule innerhalb von 4 Wochen versuchen diese zubeheben. Sollte dies nicht möglich sein, behält sich Preselect ein außerordentliches Kündigungsrecht und/oder das Abschalten der Hochschule vor.') //license property Licensor termination condition
                            }
                            LicenseTextLink(href: 'lp_licensor_termination_notice_period_01') //license property Licensor termination notice period
                            LicenseTextLink(href: 'lp_licensor_termination_right_01') //license property Licensor termination right
                        }
                        GeneralTerm {
                            //license property Multi Year License Termination
                            GeneralTermType('onixPL:MemberLeavingConsortium')
                            Annotation {
                                AnnotationType('onixPL:Interpretation')
                                AnnotationText('monatlich in 2022') //we will map here the license property value
                            }
                            LicenseTextLink(href: 'lp_multi_year_license_termination_01')
                        }
                        GeneralTerm {
                            //license property Termination requirement note
                            GeneralTermType('onixPL:ActionOnTermination')
                            Annotation {
                                AnnotationType('onixPL:ERMI:TerminationRequirementsNote')
                                AnnotationText('jede Kündigung bedarf der Schriftform')
                            }
                            LicenseTextLink(href: 'lp_termination_requirement_note_01')
                        }
                        GeneralTerm {
                            //license property User information confidentiality
                            GeneralTermType('onixPL:ConfidentialityOfUserData')
                            TermStatus('onixPL:Uncertain')
                            LicenseTextLink(href: 'lp_user_information_confidentiality_01')
                        }
                    }
                    //optional 0-1; possible container for LicenseProperty.paragraph-s
                    //not possible to implement properly because mandatory data is missing: DocumentLabel (I cannot ensure an underlying document is available); SortNumber (is mostly not given)
                    //DocumentLabel: substituted by LicenseProperty paragraph, SortNumber: substituted by 0; may be removed completely if no productive use is possible, proposal character!
                    //general: create iff license.paragraph != null!
                    LicenseDocumentText {
                        DocumentLabel('license.reference')
                        TextElement(id: 'lp_accessibility_compliance_01') { //"lp_${lp.name.replaceAll('/:/','').replaceAll('/ /','_').toLowerCase()}_property count number"
                            //property count number; dummy value 0 if not existent
                            SortNumber(0)
                            Text('4. Kurs-Dossiers und elektronische Bereitstellung 4.2 Zugriffsberechtigte Einrichtungen dürfen lizenzierte Dokumente für Personen, denen die Nutzung des ursprünglichen PDF-Formates nicht möglich ist (z.B. Sehbehinderte), in andere Formate (audio, Braille u.ä.) konvertieren und bereitstellen. Sie werden in diesem Fall gebeten, die konvertierten Dateien dem Distributor zum Verfügung zu stellen, damit sie diesem Personenkreis weltweit zugänglich gemacht werden können.')
                        }
                        TextElement(id: 'lp_all_rights_reserved_indicator_01') {
                            SortNumber(0)
                            Text('EBSCO hereby grants to the Licensee a nontransferable and non-exclusive right to use the Databases and Services made available by EBSCO according to the terms and conditions of this Agreement.')
                        }
                        TextElement(id: 'lp_alumni_access_01') {
                            SortNumber(0)
                            Text('FRAME WORK AGREEMENT\n' +
                                    'VI. General, No. E. (Seite 8)\n' +
                                    '\n' +
                                    'For purposes of this Agreement, "EBSCO" is EBSCO Publishing, Inc.; the "Licensee" is the entity or institution that makes available databases and services offered by EBSCO; the "Sites" are the Internet websites offered or operated by Licensee from which Authorized Users can obtain access to EBSCO\'s Databases and Services; and the "Authorized User(s)" are employees, students, registered patrons, walk-in patrons.\n' +
                                    '\n' +
                                    '"Authorized User(s)" do not include alumni of the Licensee.')
                        }
                        TextElement(id: 'lp_applicable_copyright_law_01') {
                            SortNumber(0)
                            Text('§ 3 Abs. 2: This Licence shall be deemed to complement and extend the rights of Licensee, the lnstitutions and Authorised Users under the German Copyright Law and other applicable legislation in Germany')
                        }
                        TextElement(id: 'lp_archival_copy_content_01') {
                            SortNumber(0)
                            Text('11. Metadata shall be licensed and delivered at no extra costs for non-commercial use by: - local catalogues - union catalogues - any other library and information system (including but not limited to search engines of commercial corporations provided that the metadata is not sold, lent, re-licensed, or distributed in any manner that violates the terms and conditions of the licence) including provisions that they do not create products or perform services which complete or interfere with those of ProQuest or its licensors.')
                        }
                        TextElement(id: 'lp_archival_copy_cost_01') {
                            SortNumber(0)
                            Text("13. Archival Rights ... Archived Licensed Materials are produced by the Publisher as a PDF collection. The files, distributed on CD-ROMs , can be purchased and mounted on the Licensee's local server. The use of the subscribed archived Licensed Materials remains subject to the terms and conditions of this License.")
                        }
                        TextElement(id: 'lp_archival_copy_permission_01') {
                            SortNumber(0)
                            Text("§ 2 Abs. 3: The Licensee is further permitted to make copies or re-format the Licensed Material contained in the archival copies supplied by the Licensor in any way that ensures their future preservation and accessibility in accordance with this Licence.")
                        }
                        TextElement(id: 'lp_archival_copy_time_01') {
                            SortNumber(0)
                            Text("§ 5 Responsibilities of the publisher 1. The Publisher agrees to: [...] e. Deliver the Licensed Material to Licensee as specified below - in case of post-cancellation rights six months after cancellation/publication; - in case of withdrawal of Licensed Material or any part of it before removal from Publisher's server; - in case of termination of this agreement immediately after termination;")
                        }
                        TextElement(id: 'lp_authorized_users_01') {
                            SortNumber(0)
                            Text("1.2 Authorized Users/Sites. Authorized Users are the employees of the Subscriber and individuals who are independent contractors or are employed by independent contractors of the Subscriber affiliated with the Subscriber’s locations listed on Schedule 2 (the “Sites”) and individuals using computer terminals within the library facilities at the Sites permitted by the Subscriber to access the Subscribed Products for purposes of personal research,")
                        }
                        TextElement(id: 'lp_branding_01') {
                            SortNumber(0)
                            Text('§ 3.6 Im Falle elektronischer Lieferung setzt subito e.V. bei allen über seine Websites abgewickelten Lieferungen das in Anlage 1 genannte Wasserzeichen zum Schutz ein. Anlage 1. WASSERZEICHEN UND URHEBERRECHTSHINWEIS FÜR ELEKTRONISCHE LIEFERUNGEN 1. Wasserzeichen Wenn eine Kopie elektronisch geliefert wird, muss auf allen Kopien ein Wasserzeichen mit folgendem Urheberrechtsvermerk angebracht werden: „Kopie für Lizenznutzer von subito e.V., geliefert und ausgedruckt für {Name des Nutzers}, {Nutzernummer}; subito e.V. licensed user copy supplied and printed for {name of Client}, {Client ID}“')
                        }
                        TextElement(id: 'lp_cancellation_allowance_01') {
                            SortNumber(0)
                            Text('Als wichtiger Grund zur fristlosen Kündigung wird unter anderen jede Verletzung von wesentlichen Vertragspflichten, die trotz Aufforderung nicht innerhalb angemessener Frist abgestellt werden, angesehen.')
                        }
                        TextElement(id: 'lp_change_to_licensed_material_01') {
                            SortNumber(0)
                            Text('§ 3.7 Die Urheberrechte und sonstigen Rechte aus geistigem Eigentum oder Schutzrechte an den Zeitschriften und Büchern sowie den Preislisten verbleiben beim Verlag. Weder subito e.V. noch die Lieferbibliotheken oder die Nutzer dürfen die Zeitschriften und Bücher ändern, anpassen, umwandeln, übersetzen oder Bearbeitungen davon erstellen oder diese anderweitig auf eine Weise nutzen, die das Urheberrecht oder sonstige Schutzrechte daran verletzten würde. In den Zeitschriften und Büchern enthaltene Urheberrechtsvermerke, sonstige Vermerke oder Haftungsausschlüsse dürfen in keiner Weise entfernt, unkenntlich gemacht oder verändert werden.')
                        }
                        TextElement(id: 'lp_clickwrap_modification_01') {
                            SortNumber(0)
                            Text('') //no example available in current dataset
                        }
                        TextElement(id: 'lp_completeness_of_content_clause_01') {
                            SortNumber(0)
                            Text('If the total number of ebooks in the ebook collection licensed under these product terms is less than 97% of the estimated numer of titels for all licensed ebook collections, Licensor shall, at licensees request, offer licensee a credit')
                        }
                        TextElement(id: 'lp_concurrency_with_print_version_01') {
                            SortNumber(0)
                            Text('Licensed Online Reference Works (ORW) are the electronic editions of Wiley’s major reference works [...]. They may include tables of content, abstracts, full text and illustrations, data tables and additional content not included in the print versions of the major reference works.')
                        }
                        TextElement(id: 'lp_concurrent_users_01') {
                            SortNumber(0)
                            Text('Es sind 2000 Nutzer generell, nicht nur auf Gleichzeitigkeit bezogen')
                        }
                        TextElement(id: 'lp_confidentiality_of_agreement_01') {
                            SortNumber(0)
                            Text('17. Confidentiality 17.1 Both parties shall keep the terms of this Agreement strictly confidential, with the exception of Schedule D (as required in Clause 5.3) and Schedule F, and shall not disclose same except to the extent any disclosure is required by law, or court or administrative or regulatory body of competent jurisdiction. 17.2 Publisher retains server logs which contain detailed Customer and Authorised User access information including without limitation date and time of access, details of the Secure Authentication employed, and specific file name and type downloaded from Publisher Content. This access information may be used by Publisher and its agents only for Publisher’s internal purposes including management information reporting, monitoring and enforcement of Customer’s access, and Customer support purposes. Publisher shall use its best endeavours to keep confidential from third parties this access information and these usage statistics. Publisher and Customer shall each comply with the requirements of any data protection legislation currently in force and applicable to them.')
                        }
                        TextElement(id: 'lp_conformity_with_urhg_01') {
                            SortNumber(0)
                            Text('§ 2.4 subito stimmt zu, dass der Rahmenvertrag für Artikel auch innerhalb Deutschlands gilt, soweit ansonsten die gesetzliche Lizenz des § 60e Abs. 5 UrhG Anwendung finden würde. Die an den Verlag zu zahlende Lizenzgebühr richtet sich in diesem Fall nach den zwischen der VG WORT und dem subito e.V. vereinbarten Tarifen (vgl. Ziffer 6.1 (a) unten). Während jede Partei ihre Rechtsposition zur Anwendbarkeit von § 53 UrhG beibehält, stimmt subito weiterhin zu, dass der Rahmenvertrag auch für alle rein inländischen Lieferungen an kommerzielle Kunden in Deutschland gilt. subito erwartet, dass der Verlag im Gegenzug einen angemessenen Preis für die Kundengruppen 1 und 3 nach Ziffer 6.2(a) und 6.2(c) festsetzt. Für Teile von Büchern gilt der Rahmenvertrag nicht innerhalb Deutschlands, soweit die gesetzliche Lizenz des § 60e Abs. 5 UrhG Anwendung findet.')
                        }
                        TextElement(id: 'lp_content_warranty_01') {
                            SortNumber(0)
                            Text('LN ermöglicht dem hbz und den teilnehmenden Einrichtungen im Rahmen ihrer technischen und betrieblichen Möglichkeiten mittels Datenfernübertragung Zugang zu den Online-Diensten im vereinbarten Umfang. Das hbz und die teilnehmenden Einrichtungen erhalten dabei die unter Ziffer III. bezeichneten Nutzungsrechte. 2. Umfang und Inhalt des Online Services sind nicht statisch festgelegt und können von Zeit zu Zeit wechseln. Als vereinbart gilt ein Umfang und Inhalt, den LN nach billigem Ermessen festlegen und in zumutbarem Umfang nachträglich anpassen kann (§ 315 BGB). Der Wegfall oder eine Änderung wesentlicher Bestandteile ist dann gegeben, wenn ohne die wesentlichen Bestandteile in dem Online Service ein Festhalten an dem Vertrag für das hbz nicht mehr zumutbar ist. Dies liegt insbesondere dann vor, wenn die vom hbz bzw. den teilnehmenden Einrichtungen meist genutzten Inhalte nicht mehr in dem Online Service verfügbar sind. 3. Der Zugriff kann in zumutbarem Umfang zeitweise oder nur eingeschränkt möglich sein, wenn z. B. Wartungsarbeiten an den Systemen von LN durchgeführt werden. LN wird das hbz über Ausfälle, welche die Dauer üblicher, kurzzeitiger Wartungsarbeiten übersteigen, informieren. 4. Alle Leistungen werden mit verlagsüblicher Sorgfalt erbracht. LN bemüht sich um größtmögliche Aktualität betreffend den Stand des Online Services, kann aber nicht gewährleisten, dass diese tages- oder wochenaktuell sind. 5. LN ist berechtigt, technische Vorkehrungen zu treffen, die geeignet sind, eine vertragswidrige Nutzung durch den Kunden zu verhindern.')
                        }
                        TextElement(id: 'lp_continuing_access_title_transfer_01') {
                            SortNumber(0)
                            Text('Anhang 4 Abs. 7: Transfer of Titles: The Publisher shall comply with the Code of Practice of Project Transfer relating to the transfer of titles between publishers. In addition, the Publisher will use all reasonable efforts to retain a non-exclusive copy of the volumes published and make them available free of charge through the Publisher\'s server. In the event that the Publisher ceases to publish a part or parts of the Licensed Material, a digital archive will be maintained of such Licensed Material and will be made available free of charge through the Publisher\'s server or via a third party server and by supplying such material free of charge to the Institution.')
                        }
                        TextElement(id: 'lp_course_pack_electronic_01') {
                            SortNumber(0)
                            Text('3. Terms and Conditions of Use 3.1.d. Authorized Users who are members of the Customer’s faculty or staff may download and print out multiple copies of material from Licensed Electronic Products for the purpose of making a multi-source collection of information for classroom use (course-pack) or a virtual learning environment, to be distributed to students at the Customer\'s institution free of charge or at a cost-based fee. Material from Licensed Electronic Products may also be stored in electronic format in secure electronic data files for access by Authorized Users who are students at the Customer’s institution, as part of their course work, so long as reasonable access control methods are employed such as username and password.')
                        }
                        TextElement(id: 'lp_course_pack_print_01') {
                            SortNumber(0)
                            Text('4. Permitted Use and Prohibitions Electronic reserves and coursepacks: Articles for course or research use that are supplied to the end user at no cost may be made without explicit permission or fee. Articless that are provided to the end user for a copying fee m ay not be made without payment of permission fees to Duke Univ. Press. E-reserves should be posted on a secure site accessible to class memebers only, and articles purged form the e-reserve system at the end of each semester.')
                        }
                        TextElement(id: 'lp_course_reserve_electronic_01') {
                            SortNumber(0)
                            Text('The Licensee may incorporate parts of the Licensed Materials in printed Course Packs, Electronic Reserve collections and in Virtual Learning Environments for use by Authorised Users only. Each such item incorporated shall carry appropriate acknowledgement of the source, title, author of the extract and Emerald\'s name. Course packs in non-electronic non-print perceptible form, such as audio or Braille, may also be offered by the Licensee to Authorised Users on the same terms as set out in this Clause 5.')
                        }
                        TextElement(id: 'lp_course_reserve_print_01') {
                            SortNumber(0)
                            Text('In addition to the rights already granted to all users, Authorised Users can incorporate OECD Subscribed Material in teaching materials, course packs and reserves (both digital and print, as well as other formats adapted to the needs of the visually impaired), for distance learning, Massive Open Online Courses (MOOCs) and scholarly communication. Each time You cite an OECD publication, You must include suitable acknowledgment of the OECD as source and copyright owner.')
                        }
                        TextElement(id: 'lp_cure_period_for_breach_01') {
                            SortNumber(0)
                            Text('"...If Licensee does not cure the material breach within thirty (30) days after notice of such breach, IEEE shall be entitled to terminate this Agreement immediately."')
                        }
                        TextElement(id: 'lp_data_protection_override_01') {
                            SortNumber(0)
                            Text('Pkt. 9')
                        }
                        TextElement(id: 'lp_digitial_copy_01') {
                            SortNumber(0)
                            Text('5. Urheberrecht und Ausübung der Nutzungsrechte (5) Berechtigten Nutzern ist es gestattet, die in der elibrary im Rahmen der ihnen gewährten Zugriffsmög­lichkeit verfügbaren Inhalte online zu lesen, auszudrucken und/oder die Inhalte insgesamt bzw. einzelne Kapitel der Inhalte als PDF herunterzuladen und zu speichern, sofern und soweit dies technisch möglich ist. Sowohl der Zugriff als auch der Ausdruck, der Download oder die Speicherung erfolgt ausschließlich für den persönlichen Gebrauch bzw. zu nicht kommerziellen Zwecken von Studium, Forschung und Lehre. Die gleichzeitige Nutzung derselben Inhalte durch mehrere berechtigte Nutzer einer Institution ist zulässig.')
                        }
                        TextElement(id: 'lp_distance_education_01') {
                            SortNumber(0)
                            Text('') //no example available in current dataset
                        }
                        TextElement(id: 'lp_document_delivery_service_commercial_01') {
                            SortNumber(0)
                            Text('7. Permitted Use [...] C. Document delivery. In response to individual orders, Members may, for non-commercial purposes, i. transmit reproductions of up to 10 per cent of a published work ii. transmit reproductions of single articles from an electronic journal to the requesting party. OPG is entitled to a remuneration for such reproductions, which shall be paid by Members via the VG Wort collection society according to the applicable standard rate at the time of transmission')
                        }
                        TextElement(id: 'lp_electronic_link_01') {
                            SortNumber(0)
                            Text('Custom Terms and Conditions 6. Linking. Subject to Publisher Restrictions, you may link to search results or materials contained in the Products licensed to you. The security embedded in these links is your responsibility and only on-site users and/or Authorized Users are permitted access to the Products or the materials contained therein consistent with Sections 1(b) and 1(f) of this agreement. With respect to any original materials and third party materials that may be presented in conjunction with links into the Products, you represent that you have all rights necessary to use these third party materials.')
                        }
                        TextElement(id: 'lp_governing_jurisdiction_01') {
                            SortNumber(0)
                            Text('11.2 Ist der Kunde Kaufmann, juristische Person des öffentlichen Rechts, öffentlich-rechtliches Sondervermögen oder im Inland ohne Gerichtsstand, ist ausschließlicher Gerichtsstand für alle Streitigkeiten aus oder im Zusammenhang mit diesen Lizenzbedingungen Berlin. (s. Rahmenlizenzvertrag, S. 8)')
                        }
                        TextElement(id: 'lp_governing_law_01') {
                            SortNumber(0)
                            Text('13.: The Agreement is govemed by and construed in accordance with English Law and the parties agree to submit to the [non-] exclusive Jurisdiction of the English courts.')
                        }
                        TextElement(id: 'lp_ill_electronic_01') {
                            SortNumber(0)
                            Text('The Libraries are allowed to deliver parts of the Content via the interlibrary loan within the framework of the "Gesetz zur Angleichung des Urheberrechts an die aktuellen Erfordernisse der Wissengesellschaft UrhWissG" [...] and the "Gesetz über Urheberrecht und verwandte Schutzrechte UrhG" [...]. The transmission to the ordering Library as well as delivery to the ordering Library user can be done electronically.')
                        }
                        TextElement(id: 'lp_ill_electronic_02') {
                            SortNumber(0)
                            Text('§ 3 Nutzungsrechte .... c) Ausdruck und Fernleihe Kopien der elektronischen Version einzelner Beiträge können ausgedruckt werden; die Ausdrucke dürfen von den teilnehmenden Einrichtungen und ihren berechtigten Benutzern gemäß der anwendbaren Copyright-Vorschriften verwendet werden, wobei sich diese Genehmigung auch auf die Fernleihe bezieht. Fassungen der Publikationen bzw. von Teilen der Publikationen in elektronischer Form dürfen nur an berechtigte Benutzer weitergeleitet werden.')
                        }
                        TextElement(id: 'lp_ill_print_or_fax_01') {
                            SortNumber(0)
                            Text('Grant and Scope of License 2.1.3 transmit to a library pursuant to section 2 of the Leihverkehrsordnung (LVO) single articles, bookchapters or portions thereof only for personal educational, scientific, or research purposes ("InterlibraryLoans”). Such transmission shall be reviewed and fulfilled by Licensee\'s staff, and shall be made by hand, post, fax or through any secure document transmission software, so long as, in the case of any electronic transmission, the electronic file retains the relevant copyright notice. The right set out in this clause does not extend to centralized ordering facilities, such as document delivery systems, nor the distribution of copies in such quantities as to substitute for a subscription or purchase of the distributed Content.')
                        }
                        TextElement(id: 'lp_ill_print_or_fax_02') {
                            SortNumber(0)
                            Text('Licensee may not under any circumst~nces download, -print, make copies of and/ or distribute TLG materials.')
                        }
                        TextElement(id: 'lp_ill_secure_electronic_transmission_01') {
                            SortNumber(0)
                            Text('C. TERMS AND CONDITIONS OF USE e. [...] The electronic copy must be supplied by secure electronic transmission (like Ariel) and must be deleted by the recipient library immediately after printing a paper copy of the document for its user.')
                        }
                        TextElement(id: 'lp_ill_secure_electronic_transmission_02') {
                            SortNumber(0)
                            Text('4.2 [...] Except as provided for in the present GTS, Customer (i) may not redistribute to third parties copies, in electronic form or in paper form, of documents from the Tool(s) without the prior written consent of Questel, except as required by applicable laws or regulations [...]')
                        }
                        TextElement(id: 'lp_indemnification_by_licensee_clause_indicator_01') {
                            SortNumber(0)
                            Text('AGB § 11')
                        }
                        TextElement(id: 'lp_indemnification_by_licensor_01') {
                            SortNumber(0)
                            Text('RV § 6.2-RV § &.2.4')
                        }
                        TextElement(id: 'lp_intellectual_property_warranty_01') {
                            SortNumber(0)
                            Text('§ 4 TIB-Server 4. Die von den Kunden bestellten Volltexte werden von der TIB gegen Rechnung an diese in elektronischer Form oder ggf. in Papierform ausgeliefert. Vor Auslieferung der Dokumente an den Endkunden werden diese vom TIB-Server mit einer kundenindividuellen Signatur im Kopfbereich und auf jeder Seite nach folgendem Schema versehen: „Licenced by VDE VERLAG GMBH. Delivered by TIB for {Kundenname}"')
                        }
                        TextElement(id: 'lp_licensee_obligations_01') {
                            SortNumber(0)
                            Text('SCHEDULE D Terms and Conditions Use of Information from The Royal Society of Chemistry (“RSC”) Academic Subscribers ...')
                        }
                        TextElement(id: 'lp_licensee_termination_notice_period_01') {
                            SortNumber(0)
                            Text('Die Kündigungsfrist beträgt 3 Monate zum Ende eines Ein-Jahres-Berechnungszeitraums [Vertragsbeginn 01.06.2016]. (s. Lizenzvertrag, S. 1)')
                        }
                        TextElement(id: 'lp_licensee_termination_right_01') {
                            SortNumber(0)
                            Text('Vertrag 9. Sonstiges: Der Lizenznehmer kann die Vereinbarung kündigen, wenn aufgrund der Kürzung der finanzierenden Haushalte (müssen genau genannt werden) des Lizenznehmers um mind. 5% keine ausreichenden Mittel zur Fortführung der Lizenz im Folgejahr zur Verfügung gestellt werden können. Die Kündigung muss spätestens 60 Tage vor dem Ende eines Lizenzabschnitts in schriftlicher Form an die jeweilige Gegenseite übermittelt werden.')
                        }
                        TextElement(id: 'lp_licensor_termination_notice_period_01') {
                            SortNumber(0)
                            Text('12. Term and termination of the Agreement 12.1 This Agreement takes effect upon signing and shall remain effective 12 months from date of signing. Thereafter, the Agreement shall be automatically extended for another year, unless the Agreement is terminated subject to a notice period of five months to the end of the respective calendar year.')
                        }
                        TextElement(id: 'lp_licensor_termination_right_01') {
                            SortNumber(0)
                            Text('Das Recht zur fristlosen Kündigung aus wichtigem Grund bleibt unberührt. Ein wichtiger Grund für den Anbieter auch ohne vorherige Abmahnung liegt insbesondere vor, wenn der Lizenznehmer in grober Weise gegen seine Zahlungs- oder sonstigen Verpflichtungen aus diesem Vertrag, insbesondere derjenigen zur Beachtung der Urheberrechte des Anbieters und/oder der über prometheus zugänglichen Bildgeber gemäß Ziff. 1 und 2 verstößt.')
                        }
                        TextElement(id: 'lp_local_authorized_user_defintion_01') {
                            SortNumber(0)
                            Text('faculty members (including temporary or exchange faculty members for the duration of teir assignment); enrolled post-graduate and undergraduate students; current staff members; contract personnell directly involved in educational and research activities; walk-in user')
                        }
                        TextElement(id: 'lp_maintenance_window_01') {
                            SortNumber(0)
                            Text('7.4b) AGB')
                        }
                        TextElement(id: 'lp_metadata_delivery_01') {
                            SortNumber(0)
                            Text('Additional Terms, 11. The license includes meta data necessary for appropriate use of the products.')
                        }
                        //iff paragraph exists!
                        TextElement(id: 'lp_method_of_authentication_01') {
                            SortNumber(0)
                            Text('SCHEDULE 4: Standards, Services and Statistics [...] 2. Secure Authentication methods shall include Shibboleth, Internet Protocol (IP) ranges as well as authentication with username and password or other methods that are to be agreed upon in writing between the Licensor, the Licensee and the Institutions. The use of proxy servers is permitted as long as any proxy server IP addresses provided limit remote or off-campus access to Authorised Users.')
                        }
                        TextElement(id: 'lp_multi_year_license_termination_01') {
                            SortNumber(0)
                            Text('Opt-out: The Customer Goethe University Frankfurt am Main is allowed to terminate the Agreement at the end of any month within the year 2022 by notice which has to be received by ProQuest before the end of the relevant month if the subject matter of this Agreement is not being funded or only being funded in part by the DFG. In this case, (Goethe University Frankfurt am Main) only has to pay a proportional amount ofthe license fees up to the point at which the termination takes effect. Access for Bayerische Staatsbibliothek and License Fee for Bayerische Staatsbibliothekare unaffected by an Opt-out ofthe Customer Goethe University Frankfurt am Main.')
                        }
                        TextElement(id: 'lp_oa_first_date_01') {
                            SortNumber(0)
                            Text('As of 1 August 2021, the terms set out in Schedule 3 of this Amendment will apply for the Open Access Pilot.')
                        }
                        TextElement(id: 'lp_oa_last_date_01') {
                            SortNumber(0)
                            Text('In accordance with Paragraph 4- Term of Agreement: if the agreement is to be extended, for a further period of one (1) year by Consortium Leader and Publisher agreeing the Publisher Content and Fee for the new Term, an addendum shall be signed by both parties. ... This amendment shall enter into force on January 2021')
                        }
                        TextElement(id: 'lp_oa_note_01') {
                            SortNumber(0)
                            Text('Eine Einspielung in kommerzielle Repositorien (z.B. ResearchGate, Academia.edu etc.) ist nicht gestattet. Open-Access-Rechte werden nach dem Vertrag dauerhaft gewährt. Die aufgeführten OA-Rechte (grüner Weg) können nur für Artikel in Anspruch genommen werden, wenn die Einrichtung im Erscheinungsjahr regulärer Teilnehmer des Konsortiums bzw. der Allianz-Lizenz ist/war. Ggf. werden weitere Nutzungsmöglichkeiten über die aktuelle Verlagspolicy zum Open Access geregelt.')
                        }
                        TextElement(id: 'lp_offsetting_01') {
                            SortNumber(0)
                            Text('IGI Global bietet Offsetting bis zu 100% der Lizenzgebühr für alle Journals an. Die aktuelle APC-Gebühr beträgt 1.500,00 USD. Bsp.: Eine Einrichtung zahlt für das Subskriptionsjahr 2019 eine Li-zenzgebühr i.H.v. 3.852 USD. Bei 2 Veröffentlichungen durch For-scher und Forscherinnen der Einrichtung in IGI Global Journals und Zahlung der APC-Gebühr erhält die Einrichtung eine Rückerstattung i.H.v. 3.000 USD. Die Rückerstattung beträgt max. 3.852 USD, wenn 3 oder mehr Veröffentlichungen durch Forscher und Forscherinnen der Einrichtung in IGI Global Journals bei gleichzeitiger Zahlung der APC-Gebühr erfolgen.')
                        }
                        TextElement(id: 'lp_open_access_01') {
                            SortNumber(0)
                            Text('Appendix C Green Open Access Otherwise known as "Self-Archiving" or "Posting Rights", all ACM published authors retain the right to post the pre-submitted (also known as "pre-prints"), submitted, accepted, and peer-reviewed versions of their work in any and all of the following sites: • Author\'s Homepage • Author\'s Institutional Repository • Any Repository legally mandated by the agency or funder funding the research on which the work is based • Any Non-Commercial Repository or Aggregation that does not duplicate ACM tables of contents. Non-Commercial Repositories are defined as Repositories owned by non-profit organizations that do not charge a fee to access deposited articles and that do not sell advertising or otherwise profit from serving scholarly articles For the avoidance of doubt, an example of a site ACM authors may post all versions of their work to, with the exception of the final published "Version of Record", is ArXiv. ACM does request authors, who post to ArXiv or other permitted sites, to also post the published version\'s Digital Object Identifier (DOI) alongside the pre-published version on these sites, so that easy access may be facilitated to the published "Version of Record" upon publication in the ACM Digital Library. Examples of sites ACM authors may not post their work to are ResearchGate, Academia.edu, Mendeley, or Sci-Hub, as these sites are all either commercial or in some instances utilize predatory practices that violate copyright, which negatively impacts both ACM and ACM authors.')
                        }
                        TextElement(id: 'lp_performance_warranty_01') {
                            SortNumber(0)
                            Text('RV § 1.3 RV § 1.6 RV § 12 AGB § 7.4 AGB § 11')
                        }
                        TextElement(id: 'lp_perpetual_coverage_from_01') {
                            SortNumber(0)
                            Text('fortlaufendes, zeitlich unbefristetes Nutzungsrecht S. 1')
                        }
                        TextElement(id: 'lp_perpetual_coverage_to_01') {
                            SortNumber(0)
                            Text('6. The price quoted will include access through December 31, 2028 (the "Term") to all materials available from time to time on CIAO, beginning with CIAO\'s first posting in 1997 and will include updates as they are provided by CUP from time to time, including information from the Economist Intelligence Unit to the extent such information is included within the CIAO database as well as standard service maintenance fees. [...] Achedule A [...] 2. License for Institutional Access Subject to the terms and conditions of this Agreement and upon verification of the information on your Registration Form and payment, CUP grants to Licensee and its member institutions a twenty (20) year, nontransferable license for access to all materials included in Columbia International Affairs Online, commencing on January 1, 2009 and continuing through December 31, 2028. An authorized signature on this Agreement indicates that Licensee has accepted the terms of this license.')
                        }
                        TextElement(id: 'lp_post_cancellation_online_access_01') {
                            SortNumber(0)
                            Text('Bei Continuing-Access-Lizenzen hat der Lizenznehmer auch nach Ablauf der Laufzeit weiterhin Zugriff.')
                        }
                        TextElement(id: 'lp_print_copy_01') {
                            SortNumber(0)
                            Text(' C: Pursuant to these terms and conditions, the Licensee and Authorized Users may download or print limited copies of citations, abstracts, full text or portions thereof, provided the information is used solely in accordance with copyright law. ')
                        }
                        TextElement(id: 'lp_print_copy_term_note_01') {
                            SortNumber(0)
                            Text('3. USAGE RIGHTS 3.2 Authorized Users may, in accordance with the copyright law of The Netherlands and subject to clause 6 below: 3.2.2 Print a copy or download and save Documents for personal use.')
                        }
                        TextElement(id: 'lp_publishing_fee_01') {
                            SortNumber(0)
                            Text('3.1 Corresponding authors respectively their institutions shall pay Publisher an APC which is set to be the list price minus a discount of 20%. Schedule A depicts all journals and their respective APC\'s. In case that a Liberty-model is applied, the authors themselves decide about the APC. 3.2 Payment is made individually. The eligible authors or institutions are identified by their affiliation to a German academic institution as defined above.')
                        }
                        TextElement(id: 'lp_reading_fee_01') {
                            SortNumber(0)
                            Text('Die R-Fee entspricht den jeweiligen institutionsspezifischen Kosten der teilnehmenden Einrichtungen.')
                        }
                        TextElement(id: 'lp_remote_access_01') {
                            SortNumber(0)
                            Text('1. Der Lizenzgeber räumt der TIB/UB das nicht-ausschließliche Recht ein, die Publikationen [...] und ihren autorisierten Nutzern [...]als auch im Fernzugriff kostenlos [...] zugänglich zu machen [...]')
                        }
                        TextElement(id: 'lp_repository_01') {
                            SortNumber(0)
                            Text('Members of authorized institutions are allowed to give open access to all articles, they have published in the licensed product as author ("corresponding author") or co-author ("contributing author") by integrating them in a repository of their own choice. The articles have to be delivered by licensor in the published version / post print (PDF).')
                        }
                        TextElement(id: 'lp_scholarly_sharing_01') {
                            SortNumber(0)
                            Text('3. USAGE RIGHTS 3.2 Authorized Users may, in accordance with the copyright laws of The Netherlands and subject to clause 6 below: 3 .2. 5 Distribute a copy of individual chapters, articles or other single items of the Licensed Materials in print or electronic form to other Authorized Users or to other individual scholars collaborating with Authorized Users but only for the purposes of research and private study; for the avoidance of doubt, this sub-clause shall include the distribution of a copy for teaching purposes to each individual student Authorized User in a class at the Licensee\'s institution. 3.2.6 Download a copy of individual chapters, articles or other single items of the Licensed Materials and share the same with Authorized Users or other individual scholars collaborating in a specific research project with such Authorized Users provided that it is held and accessed within a closed network that is not accessible to any person not directly involved in such collaboration and provided that it is deleted from such network immediately upon completion of the collaboration.')
                        }
                        TextElement(id: 'lp_singleuseraccess_01') {
                            SortNumber(0)
                            Text('1. KEY DEFINITIONS 1.1 Authorised Users [...]Individuals who are not an Authorised User at an eligible Member Institution may access the Licensed Material under the terms herein only through the Consortium or a National, Regional, or State Library qualified to be Member, or through a co-operative system operated by a library service provider, provided such individuals are permanent residents of Germany and provided the Commercial Use Individual is registered with the Consortium and/or the Member or with the Consortium and/or Members co-operatively and subject to registration conditions that have been approved by the Publisher. Such approval shall not be unduly withheld by Publisher.')
                        }
                        TextElement(id: 'lp_termination_requirement_note_01') {
                            SortNumber(0)
                            Text('§ 6 Vertragsdauer, Kündigung 6.2 Jede Kündigung bedarf der Schriftform')
                        }
                        TextElement(id: 'lp_text_and_datamining_01') {
                            SortNumber(0)
                            Text('3. Scope of License b. Downloading, printing, or saving of text for personal, noncommercial use is permissible. These terms and conditions do not permit the downloading of entire issues of PNAS Online, and systematic downloading is prohibited.')
                        }
                        TextElement(id: 'lp_text_and_datamining_character_count_01') {
                            SortNumber(0)
                            Text('1.3 Authorized Uses. [...] The Subscriber may: [...] maximum length of 200 characters surrounding and excluding the text entity matched ("Snippets") or bibliographic metadata.')
                        }
                        TextElement(id: 'lp_text_and_datamining_restrictions_01') {
                            SortNumber(0)
                            Text('For the avoidance of doubt, the Licensee and Auhtorized Users may not [...] carry out any Text and Data Minging without the Licensor\'s prior consent in writing except as permitted by law, but must always notify the Licensor prior to commencing any Data Mining activity.')
                        }
                        TextElement(id: 'lp_uptime_guarantee_01') {
                            SortNumber(0)
                            Text('§ 5 2.-5. § 6')
                        }
                        TextElement(id: 'lp_usage_statistics_addressee_01') {
                            SortNumber(0)
                            Text('2.3 Nutzungs- und Lizenzvereinbarungen')
                        }
                        TextElement(id: 'lp_usage_statistics_availability_indicator_01') {
                            SortNumber(0)
                            Text('20.2 Usage Statistics. 20.2.1 Publisher will provide Customer with COUNTER-compliant usage statistics relating to Publisher Content as identified in Schedule B accessed by Customer Sites by an External reute. Such usage information shall be compiled in a manner consistent with any applicable privacy and data protection laws, and the anonymity of individual users and the confidentiality of their searches shall be fully protected. 20.2.2 Customer will provide Publisher with COUNTER-compliant usage statistics relating to Publisher Content as identified in Schedule B accessed by Customer Sites by an Internal reute. Such usage information shall be compiled in a manner consistent with any applicable privacy and data protection laws, and the anonymity of individual users and the confidentiality of their searches shall be fully protected.')
                        }
                        TextElement(id: 'lp_user_information_confidentiality_01') {
                            SortNumber(0)
                            Text('9. Confidentiality Confidential information received from each other (other than information that is or becomes public or known to us on a non-confidential basis) will not be disclosed to anyone else except to the extent required by law or as necessary to perform the agreement for as long as the information remains confidential. Each of us will use industry standard administrative, physical and technical safeguards to protect the other\'s confidential information. If a court or government agency orders either of us to disclose the confidential information of the other, the other will be promptly notified so that an appropriate protective order or other remedy can be obtained unless the court or government agency prohibits prior notification.')
                        }
                        TextElement(id: 'lp_walkin_access_01') {
                            SortNumber(0)
                            Text("§1: [...] authorised persons physically present in the Licensee's library facilities.")
                        }
                        TextElement(id: 'lp_wifi_access_01') {
                            SortNumber(0)
                            Text('§ 10 Sonstige Bestimmungen Der WLAN-Zugriff auf dem Campus oder in den Räumen einer Bibliothek ist gestattet.')
                        }
                    }
                }
            }
        }
        XmlUtil.serialize(xml)
    }

    /**
     * experiment sandbox 2
     * @return
     */
    String buildDocument(License lic, Org institution) {
        SimpleDateFormat onixTimestampFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")
        SimpleDateFormat onixDateFormat = new SimpleDateFormat("yyyyMMdd")
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        Locale locale = LocaleUtils.getCurrentLocale()
        Date now = new Date()
        //for LicenseDocument.LicenseDocumentTypes; for that doc.doctype can be mapped to one of: onixPL:Addendum, onixPL:License, onixPL:LicenseMainTerms, onixPL:LicenseSchedule, onixPL:LicenseSummary
        Set<RefdataValue> relevantDocTypes = RefdataValue.findAllByValueInListAndOwner(['Addendum', 'License', 'ONIX-PL License'], RefdataCategory.findByDesc(RDConstants.DOCUMENT_TYPE))
        Map<Long, LicenseProperty> licPropertyMap = LicenseProperty.executeQuery('select lp.type.id, lp from LicenseProperty lp where lp.owner = :lic and (lp.isPublic = true or lp.tenant = :ctx)', [lic: lic, ctx: institution]).collectEntries { row -> [row[0], row[1]] }
        Set<PropertyDefinition> paragraphableProps = USAGE_TERMS+SUPPLY_TERMS+CONTINUING_ACCESS_TERMS+PAYMENT_TERMS+GENERAL_TERMS
        paragraphableProps << PropertyStore.LIC_ILL_TERM_NOTE
        Set<String> possibleUsageStatus = ['onixPL:InterpretedAsPermitted', 'onixPL:InterpretedAsProhibited', 'onixPL:Permitted', 'onixPL:Prohibited', 'onixPL:SilentUninterpreted', 'onixPL:NotApplicable'],
        usedGeneralUsageStatus = [], usedCoursePackUsageStatus = [], usedILLUsageStatus = []
        possibleUsageStatus.each { String usageStatus ->
            GENERAL_USAGE_STATEMENT_PROPS.each { PropertyDefinition propDef ->
                if(checkValueAndPermissionStatus(licPropertyMap, propDef, usageStatus))
                    usedGeneralUsageStatus << usageStatus
            }
            COURSE_PACK_USAGE_STATEMENT_PROPS.each { PropertyDefinition propDef ->
                if(checkValueAndPermissionStatus(licPropertyMap, propDef, usageStatus))
                    usedCoursePackUsageStatus << usageStatus
            }
            ILL_USAGE_STATEMENT_PROPS.each { PropertyDefinition propDef ->
                if(checkValueAndPermissionStatus(licPropertyMap, propDef, usageStatus))
                    usedILLUsageStatus << usageStatus
            }
        }
        Set<Subscription> subscriptions = lic.getSubscriptions(institution)
        def xml = builder.bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace(ople: "http://www.editeur.org/ople")
            ONIXPublicationsLicenseMessage([version: '1.0',xmlns: "http://www.editeur.org/onix-pl"]) {
                Header {
                    Sender {
                        SenderName(messageSource.getMessage('laser', null, locale))
                        SenderContact('LAS:eR-Support')
                        SenderEmail('laser@hbz-nrw.de')
                    }
                    Addressee {
                        AddresseeIdentifier {
                            AddresseeIDType('Institutional identifier')
                            IDTypeName('ISIL')
                            IDValue(Identifier.findByOrgAndNs(institution, IdentifierNamespace.findByNs(IdentifierNamespace.ISIL))?.value)
                        }
                    }
                    SentDateTime(onixTimestampFormat.format(now))
                }
                PublicationsLicenseExpression {
                    ExpressionDetail {
                        ExpressionType('onixPL:LicenseExpression')
                        //LicenseIdentifier does not support proprietary namespaces
                        ExpressionIdentifier {
                            ExpressionIDType('onixPL:Proprietary')
                            IDTypeName('globalUID')
                            IDValue(lic.globalUID)
                        }
                        ExpressionStatus('onixPL:Approved')
                    }
                    LicenseDetail {
                        Description(lic.reference)
                        LicenseStatus(refdataToOnixControlledList(lic.status, License.ONIXPL_CONTROLLED_LIST.LICENSE_STATUS_CODE)) //current => ActiveLicense, expired => NoLongerActive, expected => ProposedLicense?
                        Set<DocContext> relevantDocuments = lic.documents.findAll { DocContext dc -> dc.owner.type in relevantDocTypes && dc.status != RDStore.DOC_CTX_STATUS_DELETED }
                        relevantDocuments.each { DocContext dc ->
                            LicenseDocument {
                                LicenseDocumentType(refdataToOnixControlledList(dc.owner.type, License.ONIXPL_CONTROLLED_LIST.LICENSE_DOCUMENT_TYPE_CODE))
                                DocumentLabel(dc.owner.title)
                            }
                        }
                        /*
                        orgRole.roleType == LicensingConsortium maps to onixPL:LicenseeRepresentative (def: A representative of the licensee(s) authorized to sign the license (usually when licensees are members of a consortium).)
                        orgRole.roleType == Licensee_Consortial maps to onixPL:LicenseeConsortium (def: A consortium of which licensees under a license are members.)
                        orgRole.roleType == Licensee maps to onixPL:Licensee
                         */
                        //mandatory are at least two LicenseRelatedAgent statements - error message if no provider is connected!
                        if(lic.getLicensingConsortium()) {
                            LicenseRelatedAgent {
                                LicenseAgentRelator('onixPL:LicenseeRepresentative')
                                RelatedAgent(RDStore.OR_LICENSING_CONSORTIUM.value)
                            }
                        }
                        if(lic.getLicensee()) {
                            if(lic._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) {
                                LicenseRelatedAgent {
                                    LicenseAgentRelator('onixPL:LicenseeConsortium')
                                    RelatedAgent(RDStore.OR_LICENSEE_CONS.value)
                                }
                            }
                            else if(lic._getCalculatedType() == CalculatedType.TYPE_LOCAL) {
                                LicenseRelatedAgent {
                                    LicenseAgentRelator('onixPL:Licensee')
                                    RelatedAgent(RDStore.OR_LICENSEE.value)
                                }
                            }
                        }
                        if(lic.providerRelations) {
                            LicenseRelatedAgent {
                                LicenseAgentRelator('onixPL:Licensor')
                                RelatedAgent('Licensor')
                            }
                        }
                        if(lic.vendorRelations) {
                            LicenseRelatedAgent {
                                LicenseAgentRelator('onixPL:LicensingAgent')
                                RelatedAgent('Vendor')
                            }
                        }
                        LicenseRelatedResource {
                            LicenseResourceRelator('onixPL:LicensedContent')
                            RelatedResource('Subscription')
                        }
                        if(lic.startDate) {
                            LicenseRelatedTimePoint {
                                LicenseTimePointRelator('onixPL:LicenseStartDate')
                                RelatedTimePoint('LicenseStartDate')
                            }
                        }
                        if(lic.endDate) {
                            LicenseRelatedTimePoint {
                                LicenseTimePointRelator('onixPL:LicenseEndDate')
                                RelatedTimePoint('LicenseEndDate')
                            }
                        }
                    }
                    Definitions {
                        //define here the people and organisations taking part in the license: OrgRoles in detail, moreover Walk-In User and other related parties
                        lic.orgRelations.each { OrgRole oo ->
                            AgentDefinition {
                                AgentLabel(oo.roleType.value) //use this as internal document referrer
                                AgentType('onixPL:Organization')
                                AgentIdentifier {
                                    AgentIDType('onixPL:Proprietary')
                                    IDTypeName('globalUID')
                                    IDValue(oo.org.globalUID)
                                }
                                //for each identifier, ISIL, WIBID, etc.
                                AgentIdentifier {
                                    AgentIDType('onixPL:CompanyRegistrationNumber')
                                    IDTypeName('ISIL')
                                    IDValue(Identifier.findByNsAndOrg(IdentifierNamespace.findByNs(IdentifierNamespace.ISIL), oo.org).value)
                                }
                                AgentIdentifier {
                                    AgentIDType('onixPL:CompanyRegistrationNumber')
                                    IDTypeName('WIBID')
                                    IDValue(Identifier.findByNsAndOrg(IdentifierNamespace.findByNs(IdentifierNamespace.WIBID), oo.org).value)
                                }
                                AgentIdentifier {
                                    AgentIDType('onixPL:Proprietary')
                                    IDTypeName('globalUID')
                                    IDValue(oo.org.globalUID)
                                }
                                AgentName {
                                    AgentNameType('onixPL:RegisteredName')
                                    Name(oo.org.name)
                                }
                                AgentName {
                                    AgentNameType('onixPL:CommonName')
                                    Name(oo.org.sortname)
                                }
                            }
                        }
                        if(lic.providerRelations) {
                            lic.providerRelations.each { ProviderRole pvr ->
                                AgentDefinition {
                                    AgentLabel('Licensor') //map ProviderRole to Licensor
                                    AgentType('onixPL:Organization')
                                    AgentIdentifier {
                                        AgentIDType('onixPL:Proprietary')
                                        IDTypeName('wekbId')
                                        IDValue(pvr.provider.gokbId)
                                    }
                                    AgentName {
                                        AgentNameType('onixPL:CommonName')
                                        Name(pvr.provider.sortname)
                                    }
                                    AgentName {
                                        AgentNameType('onixPL:RegisteredName')
                                        Name(pvr.provider.name)
                                    }
                                }
                            }
                        }
                        if(lic.vendorRelations) {
                            lic.vendorRelations.each { VendorRole vr ->
                                AgentDefinition {
                                    AgentLabel('Vendor') //map VendorRole to LicensingAgent
                                    AgentType('onixPL:Organization')
                                    AgentIdentifier {
                                        AgentIDType('onixPL:Proprietary')
                                        IDTypeName('wekbId')
                                        IDValue(vr.vendor.gokbId)
                                    }
                                    AgentName {
                                        AgentNameType('onixPL:CommonName')
                                        Name(vr.vendor.sortname)
                                    }
                                    AgentName {
                                        AgentNameType('onixPL:RegisteredName')
                                        Name(vr.vendor.name)
                                    }
                                }
                            }
                        }
                        //license properties Authorized Users, Local authorized user defintion
                        AGENT_DEFINITION_PROPS.each { PropertyDefinition propDef ->
                            if(isValueSet(licPropertyMap, propDef) || isParagraphSet(licPropertyMap, propDef)) {
                                LicenseProperty licProp = licPropertyMap.get(propDef.id)
                                AgentDefinition {
                                    AgentLabel(propDef.name)
                                    AgentType('onixPL:Person')
                                    if(licProp.getValue())
                                        Description(licProp.getValue())
                                    if(licProp.paragraph)
                                        LicenseTextLink(href: "lp_${toSnakeCase(propDef.name)}_01")
                                }
                            }
                        }
                        //the subscriptions
                        if(subscriptions) {
                            subscriptions.each { Subscription s ->
                                ResourceDefinition {
                                    ResourceLabel('Subscription')
                                    ResourceIdentifier {
                                        ResourceIDType('onixPL:Proprietary')
                                        IDTypeName('globalUID')
                                        IDValue(s.globalUID)
                                    }
                                    Description(s.name)
                                }
                            }
                        }
                        else {
                            ResourceDefinition {
                                ResourceLabel('Subscription')
                            }
                        }
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_TIME)) {
                            TimePointDefinition {
                                TimePointLabel('ArchivalCopyTimePoint')
                                Description(licPropertyMap.get(PropertyStore.LIC_ARCHIVAL_COPY_TIME.id).getValue()) //maps refdata value of license property Archival Copy: Time
                            }
                        }
                        if(lic.startDate) {
                            TimePointDefinition {
                                TimePointLabel('LicenseStartDate')
                                TimePointIdentifier {
                                    TimePointIDType('onixPL:YYYYMMDD')
                                    IDValue(onixDateFormat.format(lic.startDate)) //format YYYYmmdd
                                }
                            }
                        }
                        if(lic.endDate) {
                            TimePointDefinition {
                                TimePointLabel('LicenseEndDate')
                                TimePointIdentifier {
                                    TimePointIDType('onixPL:YYYYMMDD')
                                    IDValue(onixDateFormat.format(lic.endDate)) //format YYYYmmdd
                                }
                            }
                        }
                        //mapping license properties OA First Date, OA Last Date, Perpetual coverage from, Perpetual coverage to
                        TIME_POINT_DEFINITION_PROPS.each { PropertyDefinition propDef ->
                            if(isValueSet(licPropertyMap, propDef)) {
                                TimePointDefinition {
                                    TimePointLabel(propDef.name)
                                    TimePointIdentifier {
                                        TimePointIDType('onixPL:YYYYMMDD')
                                        IDValue(onixDateFormat.format(licPropertyMap.get(propDef.id).getDateValue())) //format YYYYmmdd
                                    }
                                }
                            }
                        }
                        //mapping license property Repository
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_REPOSITORY)) {
                            PlaceDefinition {
                                PlaceLabel('Repository')
                                PlaceName {
                                    Name(licPropertyMap.get(PropertyStore.LIC_REPOSITORY.id).getValue()) //the refdata value
                                }
                            }
                        }
                    }
                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CITATION_REQUIREMENT_DETAIL) || isParagraphSet(licPropertyMap, PropertyStore.LIC_OTHER_USE_RESTRICTION_NOTE)) {
                        LicenseGrant {
                            //license property Citation requirement detail
                            if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CITATION_REQUIREMENT_DETAIL)) {
                                Annotation {
                                    AnnotationType('onixPL:ERMI:CitationRequirementDetail')
                                    AnnotationText(licPropertyMap.get(PropertyStore.LIC_CITATION_REQUIREMENT_DETAIL.id).paragraph)
                                }
                            }
                            //license property Other Use Restriction Note
                            if(isParagraphSet(licPropertyMap, PropertyStore.LIC_OTHER_USE_RESTRICTION_NOTE)) {
                                Annotation {
                                    AnnotationType('onixPL:ERMI:OtherUseRestrictionNote')
                                    AnnotationText(licPropertyMap.get(PropertyStore.LIC_OTHER_USE_RESTRICTION_NOTE.id).paragraph)
                                }
                            }
                        }
                    }
                    UsageTerms {
                        usedGeneralUsageStatus.each { String usageStatus ->
                            Usage {
                                UsageType('onixPL:Use')
                                UsageStatus(usageStatus)
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_WALK_IN_ACCESS, usageStatus) && isValueSet(licPropertyMap, PropertyStore.LIC_WALK_IN_USER_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:ERMI:WalkInUserTermNote')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_WALK_IN_USER_TERM_NOTE.id).getValue())
                                    }
                                }
                                GENERAL_USAGE_STATEMENT_PROPS.each { PropertyDefinition propDef ->
                                    if(checkValueAndPermissionStatus(licPropertyMap, propDef, usageStatus) && isParagraphSet(licPropertyMap, propDef)) {
                                        LicenseTextLink(href: "lp_${toSnakeCase(propDef.name)}_01")
                                    }
                                }
                                AGENT_DEFINITION_PROPS.each { PropertyDefinition pd ->
                                    if(isValueSet(licPropertyMap, pd))
                                        User(pd.name)
                                }
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_PARTNERS_ACCESS, usageStatus))
                                    User('onixPL:LicenseeInstitutionAndPartners')
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_WALK_IN_ACCESS, usageStatus))
                                    User('onixPL:WalkInUser')
                                User('onixPL:Licensee') //fallback
                                UsedResource('Subscription')
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_SINGLE_USER_ACCESS, usageStatus))
                                    UsagePurpose('onixPL:PersonalUse')
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_METHOD_OF_AUTHENTICATION)) {
                                    LicenseProperty lp = licPropertyMap.get(PropertyStore.LIC_METHOD_OF_AUTHENTICATION.id)
                                    if ('shibboleth' in lp.value.toLowerCase())
                                        UsageMethod('onixPL:Shibboleth')
                                    else if ('ip' in lp.value.toLowerCase())
                                        UsageMethod('onixPL:IPAddressAuthentication')
                                    else if ('password' in lp.value.toLowerCase())
                                        UsageMethod('onixPL:PasswordAuthentication')
                                    else
                                        UsageMethod('onixPL:SecureAuthentication')
                                }
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_WIFI_ACCESS, usageStatus))
                                    UsageMethod('onixPL:PublicNetwork')
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_CONFORMITY_WITH_URHG, usageStatus))
                                    UsageCondition('onixPL:ComplianceWithApplicableCopyrightLaw')
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_FAIR_USE_CLAUSE_INDICATOR, usageStatus))
                                    UsageCondition('onixPL:SubjectToFairUse')
                                if('Permitted' in usageStatus && isValueSet(licPropertyMap, PropertyStore.LIC_CONCURRENT_USERS)) {
                                    UsageQuantity {
                                        UsageQuantityType('onixPL:NumberOfConcurrentUsers')
                                        QuantityDetail {
                                            Value(licPropertyMap.get(PropertyStore.LIC_CONCURRENT_USERS.id).getValue())
                                            QuantityUnit('onixPL:Users')
                                        }
                                    }
                                }
                                if(checkValueAndPermissionStatus(licPropertyMap, PropertyStore.LIC_REMOTE_ACCESS, usageStatus)) {
                                    UsageRelatedPlace {
                                        UsagePlaceRelator('onixPL:PlaceOfUsage')
                                        RelatedPlace('onixPL:RemoteLocation')
                                    }
                                }
                            }
                        }
                        usedCoursePackUsageStatus.each { String usageStatus ->
                            Usage {
                                UsageType('onixPL:MakeAvailable')
                                UsageStatus(usageStatus)
                                //value xor paragraph
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_COURSE_PACK_TERM_NOTE.id).getValue())
                                    }
                                }
                                else if(isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:SpecialConditions')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_COURSE_PACK_TERM_NOTE.id).getParagraph())
                                    }
                                }
                                //value xor paragraph
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_COURSE_RESERVE_TERM_NOTE.id).getValue())
                                    }
                                }
                                else if(isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:SpecialConditions')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_COURSE_RESERVE_TERM_NOTE.id).getParagraph())
                                    }
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_ELECTRONIC)) {
                                    LicenseTextLink(href: 'lp_course_pack_electronic_01')
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_PRINT)) {
                                    LicenseTextLink(href: 'lp_course_pack_print_01')
                                }
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_TERM_NOTE) && isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_TERM_NOTE)) {
                                    LicenseTextLink(href: 'lp_course_pack_term_note_01')
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_ELECTRONIC)) {
                                    LicenseTextLink(href: 'lp_course_reserve_electronic_cached_01')
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_PRINT)) {
                                    LicenseTextLink(href: 'lp_course_reserve_print_01')
                                }
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_TERM_NOTE) && isParagraphSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_TERM_NOTE)) {
                                    LicenseTextLink(href: 'lp_course_reserve_term_note_01')
                                }
                                User('Licensee Consortial')
                                UsedResource('Subscription')
                                UsageRelatedResource {
                                    UsageResourceRelator('onixPL:TargetResource')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_ELECTRONIC))
                                        RelatedResource('onixPL:CoursePackElectronic')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_PACK_PRINT))
                                        RelatedResource('onixPL:CoursePackPrint')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_ELECTRONIC))
                                        RelatedResource('onixPL:CourseReserveElectronic')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_COURSE_RESERVE_PRINT))
                                        RelatedResource('onixPL:CourseReservePrint')
                                }
                            }
                        }
                        //license property Digital copy
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_DIGITAL_COPY)) {
                            Usage {
                                UsageType('onixPL:MakeDigitalCopy')
                                UsageStatus('onixPL:Permitted')
                                //license property Digital copy term note
                                //value xor paragraph
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_DIGITAL_COPY_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_DIGITAL_COPY_TERM_NOTE.id).getValue())
                                    }
                                }
                                else if(isParagraphSet(licPropertyMap, PropertyStore.LIC_DIGITAL_COPY_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:SpecialConditions')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_DIGITAL_COPY_TERM_NOTE.id).getParagraph())
                                    }
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_DIGITAL_COPY))
                                    LicenseTextLink(href: 'lp_digitial_copy_01')
                                User('Licensee Consortial')
                                UsedResource('Subscription')
                            }
                        }
                        //license property Document delivery service (commercial)
                        /*
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_DOCUMENT_DELIVERY_SERVICE)) {
                            Usage {
                                UsageType('onixPL:MakeAvailable')
                                UsageStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_DOCUMENT_DELIVERY_SERVICE.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.USAGE_STATUS_CODE))
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_DOCUMENT_DELIVERY_SERVICE))
                                    LicenseTextLink(href: 'lp_document_delivery_service_commercial_01')
                                User('Licensee Consortial')
                                UsedResource('Subscription')
                                UsagePurpose('onixPL:CommercialUse')
                            }
                        }
                        */
                        //license property Electronic link
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_ELECTRONIC_LINK)) {
                            Usage {
                                UsageType('onixPL:MakeAvailable')
                                UsageStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_ELECTRONIC_LINK.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.USAGE_STATUS_CODE))
                                //license property Electronic link term note
                                //value xor paragraph
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_ELECTRONIC_LINK_TERM_NOTE)){
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_ELECTRONIC_LINK_TERM_NOTE.id).getValue())
                                    }
                                }
                                else if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ELECTRONIC_LINK_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:SpecialConditions')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_ELECTRONIC_LINK_TERM_NOTE.id).getParagraph())
                                    }
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ELECTRONIC_LINK))
                                    LicenseTextLink(href: 'lp_electronic_link_01')
                                User('Licensee Consortial')
                                UsedResource('Subscription')
                                UsageRelatedResource {
                                    UsageResourceRelator('onixPL:TargetResource')
                                    RelatedResource('onixPL:LinkToLicensedContent')
                                }
                            }
                        }
                        //license properties ILL electronic, ILL print or fax, ILL record keeping required, ILL secure electronic transmission, ILL term note; group by status
                        usedILLUsageStatus.each { String usageStatus ->
                            Usage {
                                UsageType('onixPL:SupplyCopy')
                                UsageStatus(usageStatus)
                                //license property ILL term note - value xor paragraph; include everywhere (to be sure)
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_ILL_TERM_NOTE)){
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_ILL_TERM_NOTE.id).getValue())
                                    }
                                }
                                else if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ILL_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:SpecialConditions')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_ILL_TERM_NOTE.id).getParagraph())
                                    }
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ILL_ELECTRONIC))
                                    LicenseTextLink(href: 'lp_ill_electronic_01')
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ILL_PRINT_OR_FAX))
                                    LicenseTextLink(href: 'lp_ill_print_or_fax_01')
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ILL_SECURE_ELECTRONIC_TRANSMISSION))
                                    LicenseTextLink(href: 'lp_ill_secure_electronic_transmission_01')
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ILL_RECORD_KEEPING_REQUIRED))
                                    LicenseTextLink(href: 'lp_ill_record_keeping_required_01')
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_ILL_TERM_NOTE) && isParagraphSet(licPropertyMap, PropertyStore.LIC_ILL_TERM_NOTE))
                                    LicenseTextLink(href: 'lp_ill_term_note_01')
                                User('Licensee Consortial')
                                UsedResource('Subscription')
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_ILL_ELECTRONIC))
                                    UsageMethod('onixPL:ElectronicTransmission') //license property ILL electronic
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_ILL_SECURE_ELECTRONIC_TRANSMISSION))
                                    UsageMethod('onixPL:SecureElectronicTransmission') //license property ILL secure electronic transmission
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_ILL_PRINT_OR_FAX))
                                    UsageMethod('onixPL:Fax') //license property ILL print or fax
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_ILL_RECORD_KEEPING_REQUIRED))
                                    UsageCondition('onixPL:RecordKeepingRequired') //license property ILL record keeping required - currently no example available!
                            }
                        }
                        //license property Scholarly sharing
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_SCHOLARLY_SHARING)) {
                            Usage {
                                UsageType('onixPL:MakeAvailable')
                                UsageStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_SCHOLARLY_SHARING.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.USAGE_STATUS_CODE))
                                //license property Scholarly sharing term note
                                //value xor paragraph
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_SCHOLARLY_SHARING_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_SCHOLARLY_SHARING_TERM_NOTE.id).getValue())
                                    }
                                }
                                else if(isParagraphSet(licPropertyMap, PropertyStore.LIC_SCHOLARLY_SHARING_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:SpecialConditions')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_SCHOLARLY_SHARING_TERM_NOTE.id).getParagraph())
                                    }
                                }
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_SCHOLARLY_SHARING))
                                    LicenseTextLink(href: 'lp_scholarly_sharing_01')
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_SCHOLARLY_SHARING_TERM_NOTE) && isParagraphSet(licPropertyMap, PropertyStore.LIC_SCHOLARLY_SHARING_TERM_NOTE))
                                    LicenseTextLink(href: 'lp_scholarly_sharing_term_note_01')
                                User('Licensee Consortial')
                                User('onixPL:AuthorizedUser')
                                UsedResource('Subscription')
                                UsageRelatedAgent {
                                    UsageAgentRelator('onixPL:ReceivingAgent')
                                    RelatedAgent('onixPL:ExternalAcademic')
                                }
                            }
                        }
                        //license properties Print Copy and Print copy term note
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_PRINT_COPY)) {
                            Usage {
                                UsageType('onixPL:PrintCopy')
                                UsageStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_PRINT_COPY.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.USAGE_STATUS_CODE))
                                //value xor paragraph
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_PRINT_COPY_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_PRINT_COPY_TERM_NOTE.id).getValue())
                                    }
                                }
                                else if(isParagraphSet(licPropertyMap, PropertyStore.LIC_PRINT_COPY_TERM_NOTE)) {
                                    Annotation {
                                        AnnotationType('onixPL:SpecialConditions')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_PRINT_COPY_TERM_NOTE.id).getParagraph())
                                    }
                                }
                                LicenseTextLink(href: 'lp_print_copy_01') //license property Print Copy
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_PRINT_COPY_TERM_NOTE) && isParagraphSet(licPropertyMap, PropertyStore.LIC_PRINT_COPY_TERM_NOTE))
                                    LicenseTextLink(href: 'lp_print_copy_term_note_01')
                                User('Licensee Consortial')
                                UsedResource('Subscription')
                            }
                        }
                        //license properties Text- and Datamining, Text- and Datamining Character Count, Text- and Datamining Restrictions
                        if(isValueSet(licPropertyMap, PropertyStore.LIC_TDM)) {
                            Usage {
                                UsageType('onixPL:UseForDataMining')
                                UsageStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_TDM.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.USAGE_STATUS_CODE))
                                //license property Text- and Datamining Restrictions
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_TDM_RESTRICTIONS)) {
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_TDM_RESTRICTIONS.id).getValue())
                                    }
                                }
                                //license property Text- and Datamining
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_TDM))
                                    LicenseTextLink(href: 'lp_text_and_datamining_01')
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_TDM_CHAR_COUNT))
                                    LicenseTextLink(href: 'lp_text_and_datamining_character_count_01')
                                if(isParagraphSet(licPropertyMap, PropertyStore.LIC_TDM_RESTRICTIONS))
                                    LicenseTextLink(href: 'lp_text_and_datamining_restrictions_01')
                                User('Licensee Consortial')
                                UsedResource('Subscription')
                                //license property Text- and Datamining Character Count
                                if(isValueSet(licPropertyMap, PropertyStore.LIC_TDM_CHAR_COUNT))
                                    UsageCondition('onixPL:SubjectToVolumeLimit')
                            }
                        }
                    }
                    if(existsAnyOf(licPropertyMap, SUPPLY_TERMS)) {
                        SupplyTerms {
                            //license property Accessibility compliance
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_ACCESSIBILITY_COMPLIANCE)) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:ComplianceWithAccessibilityStandards')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_ACCESSIBILITY_COMPLIANCE.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ACCESSIBILITY_COMPLIANCE))
                                        LicenseTextLink(href: 'lp_accessibility_compliance_01')
                                }
                            }
                            //license property Change to licensed material
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CHANGE_TO_LICENSED_MATERIAL)) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:ChangesToLicensedContent')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_CHANGE_TO_LICENSED_MATERIAL.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CHANGE_TO_LICENSED_MATERIAL))
                                        LicenseTextLink(href: 'lp_change_to_licensed_material_01')
                                }
                            }
                            //license property Completeness of content clause
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_COMPLETENESS_OF_CONTENT_CLAUSE)) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:CompletenessOfContent')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_COMPLETENESS_OF_CONTENT_CLAUSE.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_COMPLETENESS_OF_CONTENT_CLAUSE))
                                        LicenseTextLink(href: 'lp_completeness_of_content_clause_01')
                                }
                            }
                            //license property Concurrency with print version LIC_CONCURRENCY_WITH_PRINT_VERSION
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CONCURRENCY_WITH_PRINT_VERSION)) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:ConcurrencyWithPrintVersion')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_CONCURRENCY_WITH_PRINT_VERSION.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CONCURRENCY_WITH_PRINT_VERSION))
                                        LicenseTextLink(href: 'lp_concurrency_with_print_version_01')
                                }
                            }
                            //license property Content warranty
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CONTENT_WARRANTY)) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:ContentWarranty')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_CONTENT_WARRANTY.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CONTENT_WARRANTY))
                                        LicenseTextLink(href: 'lp_content_warranty_01')
                                }
                            }
                            //license property Continuing Access: Title Transfer
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CONT_ACCESS_TITLE_TRANSFER)) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:ComplianceWithProjectTransferCode')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_CONT_ACCESS_TITLE_TRANSFER.id).getRefValue().value)
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CONT_ACCESS_TITLE_TRANSFER))
                                        LicenseTextLink(href: 'lp_continuing_access_title_transfer_01')
                                }
                            }
                            //license properties Maintenance window, Performance warranty and Uptime guarantee
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_MAINTENANCE_WINDOW, PropertyStore.LIC_PERFORMANCE_WARRANTY, PropertyStore.LIC_UPTIME_GUARANTEE])) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:ServicePerformanceGuarantee')
                                    //property value Maintenance window
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_MAINTENANCE_WINDOW)) {
                                        Annotation {
                                            AnnotationType('onixPL:ERMI:MaintenanceWindow')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_MAINTENANCE_WINDOW.id).getValue())
                                        }
                                    }
                                    //property value Performance warranty
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_PERFORMANCE_WARRANTY)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_PERFORMANCE_WARRANTY.id).getValue())
                                        }
                                    }
                                    //property value Uptime guarantee
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_UPTIME_GUARANTEE)) {
                                        Annotation {
                                            AnnotationType('onixPL:ERMI:UptimeGuarantee')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_UPTIME_GUARANTEE.id).getValue())
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_MAINTENANCE_WINDOW))
                                        LicenseTextLink(href: 'lp_maintenance_window_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_PERFORMANCE_WARRANTY))
                                        LicenseTextLink(href: 'lp_performance_warranty_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_UPTIME_GUARANTEE))
                                        LicenseTextLink(href: 'lp_uptime_guarantee_01')
                                }
                            }
                            //license property Metadata delivery and Metadata-related contractual terms
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_METADATA_DELIVERY, PropertyStore.LIC_METADATA_RELATED_CONTRACTUAL_TERMS])) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:MetadataSupply')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_METADATA_DELIVERY)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_METADATA_DELIVERY.id).getValue())
                                        }
                                    }
                                    //license property Metadata-related contractual terms
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_METADATA_RELATED_CONTRACTUAL_TERMS)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_METADATA_RELATED_CONTRACTUAL_TERMS.id).getValue())
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_METADATA_DELIVERY))
                                        LicenseTextLink(href: 'lp_metadata_delivery_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_METADATA_RELATED_CONTRACTUAL_TERMS))
                                        LicenseTextLink(href: 'lp_metadata-related_contractual_terms_01')
                                }
                            }
                            //license property OA First Date, OA Last Date, OA Note, Open Access
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_OA_FIRST_DATE, PropertyStore.LIC_OA_LAST_DATE, PropertyStore.LIC_OA_NOTE, PropertyStore.LIC_OPEN_ACCESS])) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:OpenAccessContent')
                                    //license property value OA Note
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_OA_NOTE)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_OA_NOTE.id).getValue())
                                        }
                                    }
                                    //license property value Open Access
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_OPEN_ACCESS)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_OPEN_ACCESS.id).getRefValue().value)
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_OA_FIRST_DATE))
                                        LicenseTextLink(href: 'lp_oa_first_date_01') //license property OA First Date
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_OA_LAST_DATE))
                                        LicenseTextLink(href: 'lp_oa_last_date_01') //license property OA Last Date
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_OA_NOTE))
                                        LicenseTextLink(href: 'lp_oa_note_01') //license property OA Note
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_OPEN_ACCESS))
                                        LicenseTextLink(href: 'lp_open_access_01') //license property Open Access
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_OA_FIRST_DATE)) {
                                        SupplyTermRelatedTimePoint {
                                            SupplyTermTimePointRelator('onixPL:SupplyStartDate') //the only permitted list value
                                            RelatedTimePoint('OAFirstDate')
                                        }
                                    }
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_OA_LAST_DATE)) {
                                        SupplyTermRelatedTimePoint {
                                            SupplyTermTimePointRelator('onixPL:SupplyStartDate') //the only permitted list value
                                            RelatedTimePoint('OALastDate')
                                        }
                                    }
                                }
                            }
                            //license properties Usage Statistics Addressee and Usage Statistics Availability Indicator
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_USAGE_STATISTICS_ADDRESSEE, PropertyStore.LIC_USAGE_STATISTICS_AVAILABILITY_INDICATOR])) {
                                SupplyTerm {
                                    SupplyTermType('onixPL:UsageStatistics')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_USAGE_STATISTICS_AVAILABILITY_INDICATOR.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_USAGE_STATISTICS_ADDRESSEE))
                                        LicenseTextLink(href: 'lp_usage_statistics_addressee_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_USAGE_STATISTICS_AVAILABILITY_INDICATOR))
                                        LicenseTextLink(href: 'lp_usage_statistics_availability_indicator_01')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_USAGE_STATISTICS_ADDRESSEE)) {
                                        SupplyTermRelatedAgent {
                                            SupplyTermAgentRelator('onixPL:ReceivingAgent')
                                            RelatedAgent('Usage Statistics Addressee')
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(existsAnyOf(licPropertyMap, CONTINUING_ACCESS_TERMS)) {
                        ContinuingAccessTerms {
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_ARCHIVAL_COPY_CONTENT, PropertyStore.LIC_ARCHIVAL_COPY_COST, PropertyStore.LIC_ARCHIVAL_COPY_PERMISSION, PropertyStore.LIC_ARCHIVAL_COPY_TIME])) {
                                //any of Archival Copy Content and Archival Copy: X
                                ContinuingAccessTerm {
                                    ContinuingAccessTermType('onixPL:PostCancellationFileSupply')
                                    //license property Archival Copy: Permission
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_PERMISSION))
                                        TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_ARCHIVAL_COPY_PERMISSION.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    //license property Archival Copy Content
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_PERMISSION)) {
                                        Annotation {
                                            AnnotationType('onixPL:PostCancellationFileSupplyNote')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_ARCHIVAL_COPY_CONTENT.id).getValue())
                                        }
                                    }
                                    //license property Archival Copy: Cost
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_COST)) {
                                        Annotation {
                                            AnnotationType('onixPL:PaymentNote')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_ARCHIVAL_COPY_COST.id).getValue())
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_CONTENT))
                                        LicenseTextLink(href: 'lp_archival_copy_content_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_COST))
                                        LicenseTextLink(href: 'lp_archival_copy_cost_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_PERMISSION))
                                        LicenseTextLink(href: 'lp_archival_copy_permission_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_TIME))
                                        LicenseTextLink(href: 'lp_archival_copy_time_01')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_ARCHIVAL_COPY_TIME)) {
                                        ContinuingAccessTermRelatedTimePoint {
                                            ContinuingAccessTermTimePointRelator('Archival Copy Permission')
                                            RelatedTimePoint('ArchivalCopyTimePoint')
                                        }
                                    }
                                }
                            }
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_CONT_ACCESS_PAYMENT_NOTE, PropertyStore.LIC_CONT_ACCESS_RESTRICTIONS, PropertyStore.LIC_POST_CANCELLATION_ONLINE_ACCESS, PropertyStore.LIC_REPOSITORY])) {
                                ContinuingAccessTerm {
                                    //license property Post Cancellation Online Access
                                    ContinuingAccessTermType('onixPL:PostCancellationOnlineAccess')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_POST_CANCELLATION_ONLINE_ACCESS))
                                        TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_POST_CANCELLATION_ONLINE_ACCESS.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    //license property Continuing Access: Payment Note
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_CONT_ACCESS_PAYMENT_NOTE)) {
                                        Annotation {
                                            AnnotationType('onixPL:PaymentNote')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_CONT_ACCESS_PAYMENT_NOTE.id).getValue())
                                        }
                                    }
                                    //license property Continuing Access: Restrictions
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_CONT_ACCESS_RESTRICTIONS)) {
                                        Annotation {
                                            AnnotationType('onixPL:PostCancellationOnlineAccessHoldingsNote')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_CONT_ACCESS_RESTRICTIONS.id).getValue())
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CONT_ACCESS_PAYMENT_NOTE))
                                        LicenseTextLink(href: 'lp_continuing_access_payment_note_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CONT_ACCESS_RESTRICTIONS))
                                        LicenseTextLink(href: 'lp_continuing_access_restrictions_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_POST_CANCELLATION_ONLINE_ACCESS))
                                        LicenseTextLink(href: 'lp_post_cancellation_online_access_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_REPOSITORY))
                                        LicenseTextLink(href: 'lp_repository_01')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_REPOSITORY)) {
                                        ContinuingAccessTermRelatedPlace {
                                            ContinuingAccessTermPlaceRelator('Repository')
                                            RelatedPlace('Repository')
                                        }
                                    }
                                }
                            }
                            //license properties Perpetual coverage from, Perpetual coverage note, Perpetual coverage to
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_PERPETUAL_COVERAGE_FROM, PropertyStore.LIC_PERPETUAL_COVERAGE_NOTE, PropertyStore.LIC_PERPETUAL_COVERAGE_TO])) {
                                ContinuingAccessTerm {
                                    ContinuingAccessTermType('onixPL:PostCancellationOnlineAccess')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_PERPETUAL_COVERAGE_NOTE))
                                        TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_PERPETUAL_COVERAGE_NOTE.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE)) //refdata value from Perpetual coverage note
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_PERPETUAL_COVERAGE_FROM))
                                        LicenseTextLink(href: 'lp_perpetual_coverage_from_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_PERPETUAL_COVERAGE_NOTE))
                                        LicenseTextLink(href: 'lp_perpetual_coverage_note_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_PERPETUAL_COVERAGE_TO))
                                        LicenseTextLink(href: 'lp_perpetual_coverage_to_01')
                                    //license property Perpetual coverage from
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_PERPETUAL_COVERAGE_FROM)) {
                                        ContinuingAccessTermRelatedTimePoint {
                                            ContinuingAccessTermTimePointRelator('Perpetual coverage from')
                                            RelatedTimePoint('PerpetualCoverageFrom')
                                        }
                                    }
                                    //license property Perpetual coverage to
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_PERPETUAL_COVERAGE_TO)) {
                                        ContinuingAccessTermRelatedTimePoint {
                                            ContinuingAccessTermTimePointRelator('Perpetual coverage to')
                                            RelatedTimePoint('PerpetualCoverageTo')
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(existsAnyOf(licPropertyMap, PAYMENT_TERMS)) {
                        PaymentTerms {
                            //license property Offsetting
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_OFFSETTING) || isParagraphSet(licPropertyMap, PropertyStore.LIC_OFFSETTING)) {
                                PaymentTerm {
                                    PaymentTermType('onixPL:OpenAccessOffset')
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_OFFSETTING)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_OFFSETTING.id).getValue())
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_OFFSETTING))
                                        LicenseTextLink(href: 'lp_offsetting_01')
                                }
                            }
                            //license properties Publishing Fee and Reading Fee
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_PUBLISHING_FEE, PropertyStore.LIC_READING_FEE]) || isParagraphOfAnySet(licPropertyMap, [PropertyStore.LIC_PUBLISHING_FEE, PropertyStore.LIC_READING_FEE])) {
                                PaymentTerm {
                                    PaymentTermType('onixPL:PaymentConditions')
                                    //license property Publishing Fee
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_PUBLISHING_FEE)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_PUBLISHING_FEE.id).getValue())
                                        }
                                    }
                                    //license property Reading Fee
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_READING_FEE)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_READING_FEE.id).getValue())
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_PUBLISHING_FEE))
                                        LicenseTextLink(href: 'lp_publishing_fee_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_READING_FEE))
                                        LicenseTextLink(href: 'lp_reading_fee_01')
                                }
                            }
                        }
                    }
                    if(existsAnyOf(licPropertyMap, GENERAL_TERMS)) {
                        GeneralTerms {
                            //license property All rights reserved indicator
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_ALL_RIGHTS_RESERVED_INDICATOR)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:AllRightsReserved')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_ALL_RIGHTS_RESERVED_INDICATOR).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_ALL_RIGHTS_RESERVED_INDICATOR))
                                        LicenseTextLink(href: 'lp_all_rights_reserved_indicator_01')
                                }
                            }
                            //license property Applicable copyright law
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_APPLICABLE_COPYRIGHT_LAW)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:ApplicableCopyrightLaw')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_APPLICABLE_COPYRIGHT_LAW.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_APPLICABLE_COPYRIGHT_LAW))
                                        LicenseTextLink(href: 'lp_applicable_copyright_law_01')
                                }
                            }
                            //license property Branding
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_BRANDING)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:UseOfDigitalWatermarking')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_BRANDING.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_BRANDING))
                                        LicenseTextLink(href: 'lp_branding_01')
                                }
                            }
                            //license property Cancellation allowance
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CANCELLATION_ALLOWANCE)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:TerminationWithoutPrejudiceToRights')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_CANCELLATION_ALLOWANCE.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CANCELLATION_ALLOWANCE))
                                        LicenseTextLink(href: 'lp_cancellation_allowance_01')
                                }
                            }
                            //license property Clickwrap modification
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CLICKWRAP_MODIFICATION)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:ClickThroughOverride')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_CLICKWRAP_MODIFICATION.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CLICKWRAP_MODIFICATION))
                                        LicenseTextLink(href: 'lp_clickwrap_modification_01')
                                }
                            }
                            //license property Confidentiality of agreement
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CONFIDENTIALITY_OF_AGREEMENT)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:ConfidentialityOfAgreement')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_CONFIDENTIALITY_OF_AGREEMENT.id).getRefValue().value)
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CONFIDENTIALITY_OF_AGREEMENT))
                                        LicenseTextLink(href: 'lp_confidentiality_of_agreement_01')
                                }
                            }
                            //license property Cure period for breach
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_CURE_PERIOD_FOR_BREACH)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:TerminationByBreach')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_CURE_PERIOD_FOR_BREACH.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_CURE_PERIOD_FOR_BREACH))
                                        LicenseTextLink(href: 'lp_cure_period_for_breach_01')
                                    /*
                                    that should actually be the structure, impossible because of the current values
                                    GeneralTermQuantity {
                                        GeneralTermQuantityType('onixPL:PeriodForCureOfBreach')
                                        QuantityDetail {
                                            Value(30)
                                            QuantityUnit('onixPL:Days')
                                        }
                                    }
                                    */
                                }
                            }
                            //license property Data protection override
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_DATABASE_PROTECTION_OVERRIDE)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:DatabaseProtectionOverride')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_DATABASE_PROTECTION_OVERRIDE.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_DATABASE_PROTECTION_OVERRIDE))
                                        LicenseTextLink(href: 'lp_data_protection_override_01')
                                }
                            }
                            //license property Governing jurisdiction
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_GOVERNING_JURISDICTION)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:Jurisdiction')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_GOVERNING_JURISDICTION.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_GOVERNING_JURISDICTION))
                                        LicenseTextLink(href: 'lp_governing_jurisdiction_01')
                                }
                            }
                            //license property Governing law
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_GOVERNING_LAW)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:GoverningLaw')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_GOVERNING_LAW.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_GOVERNING_LAW))
                                        LicenseTextLink(href: 'lp_governing_law_01')
                                }
                            }
                            //license property Indemnification by licensee clause indicator
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_INDEMNIFICATION_BY_LICENSEE)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:IndemnityAgainstBreach')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_INDEMNIFICATION_BY_LICENSEE.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_INDEMNIFICATION_BY_LICENSEE))
                                        LicenseTextLink(href: 'lp_indemnification_by_licensee_clause_indicator_01')
                                }
                            }
                            //license property Indemnification by licensor
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_INDEMNIFICATION_BY_LICENSOR)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:LicensorIndemnity')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_INDEMNIFICATION_BY_LICENSOR.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_INDEMNIFICATION_BY_LICENSOR))
                                        LicenseTextLink(href: 'lp_indemnification_by_licensor_01')
                                }
                            }
                            //license property Intellectual property warranty
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_INTELLECTUAL_PROPERTY_WARRANTY)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:LicensorIntellectualPropertyWarranty')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_INTELLECTUAL_PROPERTY_WARRANTY.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_INTELLECTUAL_PROPERTY_WARRANTY))
                                        LicenseTextLink(href: 'lp_intellectual_property_warranty_01')
                                }
                            }
                            //license property Licensee obligations
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_LICENSEE_OBLIGATIONS)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:NotificationOfLicenseeIPAddresses')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_LICENSEE_OBLIGATIONS.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_LICENSEE_OBLIGATIONS))
                                        LicenseTextLink(href: 'lp_licensee_obligations_01')
                                }
                            }
                            //license properties Licensee termination condition, Licensee termination notice period, Licensee termination right
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_LICENSEE_TERMINATION_CONDITION, PropertyStore.LIC_LICENSEE_TERMINATION_NOTICE_PERIOD, PropertyStore.LIC_LICENSEE_TERMINATION_RIGHT])) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:LicenseeTerminationRight')
                                    //license property License termination right
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_LICENSEE_TERMINATION_RIGHT))
                                        TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_LICENSEE_TERMINATION_RIGHT.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    //license property Licensee termination condition
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_LICENSEE_TERMINATION_CONDITION)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_LICENSEE_TERMINATION_CONDITION.id).getRefValue().value)
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_LICENSEE_TERMINATION_CONDITION))
                                        LicenseTextLink(href: 'lp_licensee_termination_condition_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_LICENSEE_TERMINATION_NOTICE_PERIOD))
                                        LicenseTextLink(href: 'lp_licensee_termination_notice_period_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_LICENSEE_TERMINATION_RIGHT))
                                        LicenseTextLink(href: 'lp_licensee_termination_right_01')
                                }
                            }
                            //license properties Licensor termination condition, Licensor termination notice period, Licensor termination right
                            if(isValueOfAnySet(licPropertyMap, [PropertyStore.LIC_LICENSOR_TERMINATION_CONDITION, PropertyStore.LIC_LICENSOR_TERMINATION_NOTICE_PERIOD, PropertyStore.LIC_LICENSOR_TERMINATION_RIGHT])) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:LicensorTerminationRight')
                                    //license property of Licensor termination right
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_LICENSOR_TERMINATION_RIGHT))
                                        TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_LICENSOR_TERMINATION_RIGHT.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    // license property Licensor termination condition
                                    if(isValueSet(licPropertyMap, PropertyStore.LIC_LICENSOR_TERMINATION_CONDITION)) {
                                        Annotation {
                                            AnnotationType('onixPL:Interpretation')
                                            AnnotationText(licPropertyMap.get(PropertyStore.LIC_LICENSOR_TERMINATION_CONDITION.id).getRefValue().value)
                                        }
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_LICENSOR_TERMINATION_CONDITION))
                                        LicenseTextLink(href: 'lp_licensor_termination_condition_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_LICENSOR_TERMINATION_NOTICE_PERIOD))
                                        LicenseTextLink(href: 'lp_licensor_termination_notice_period_01')
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_LICENSOR_TERMINATION_RIGHT))
                                        LicenseTextLink(href: 'lp_licensor_termination_right_01')
                                }
                            }
                            //license property Multi Year License Termination
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_MULTI_YEAR_LICENSE_TERMINATION)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:MemberLeavingConsortium')
                                    Annotation {
                                        AnnotationType('onixPL:Interpretation')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_MULTI_YEAR_LICENSE_TERMINATION.id).getRefValue().value)
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_MULTI_YEAR_LICENSE_TERMINATION))
                                        LicenseTextLink(href: 'lp_multi_year_license_termination_01')
                                }
                            }
                            //license property Termination requirement note
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_TERMINATION_REQUIREMENT_NOTE)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:ActionOnTermination')
                                    Annotation {
                                        AnnotationType('onixPL:ERMI:TerminationRequirementsNote')
                                        AnnotationText(licPropertyMap.get(PropertyStore.LIC_TERMINATION_REQUIREMENT_NOTE.id).getValue())
                                    }
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_TERMINATION_REQUIREMENT_NOTE))
                                        LicenseTextLink(href: 'lp_termination_requirement_note_01')
                                }
                            }
                            //license property User information confidentiality
                            if(isValueSet(licPropertyMap, PropertyStore.LIC_USER_INFORMATION_CONFIDENTIALITY)) {
                                GeneralTerm {
                                    GeneralTermType('onixPL:ConfidentialityOfUserData')
                                    TermStatus(refdataToOnixControlledList(licPropertyMap.get(PropertyStore.LIC_USER_INFORMATION_CONFIDENTIALITY.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE))
                                    if(isParagraphSet(licPropertyMap, PropertyStore.LIC_USER_INFORMATION_CONFIDENTIALITY))
                                        LicenseTextLink(href: 'lp_user_information_confidentiality_01')
                                }
                            }
                        }
                    }
                    //DocumentLabel: substituted by LicenseProperty paragraph, SortNumber: substituted by 0; may be removed completely if no productive use is possible, proposal character!
                    //general: create iff license.paragraph != null!
                    if(isParagraphOfAnySet(licPropertyMap, paragraphableProps)) {
                        LicenseDocumentText {
                            DocumentLabel(lic.reference)
                            paragraphableProps.each { PropertyDefinition propDef ->
                                if (isParagraphSet(licPropertyMap, propDef)) {
                                    TextElement(id: "lp_${toSnakeCase(propDef.name)}_01") {
                                        SortNumber(licPropertyMap.get(propDef.id).getParagraphNumber())
                                        Text(licPropertyMap.get(propDef.id).getParagraph())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        XmlUtil.serialize(xml)
    }

    String precheckValidation(License lic, Org institution) {
        Set<String> errors = []
        String error = null
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<Long, LicenseProperty> licPropertyMap = LicenseProperty.executeQuery('select lp.type.id, lp from LicenseProperty lp where lp.owner = :lic and (lp.isPublic = true or lp.tenant = :ctx)', [lic: lic, ctx: institution]).collectEntries { row -> [row[0], row[1]] }
        Set<RefdataValue> matchingStatus = [RDStore.LICENSE_CURRENT, RDStore.LICENSE_EXPIRED, RDStore.LICENSE_INTENDED]
        if(!(lic.status.id in matchingStatus.collect { RefdataValue rdv -> rdv.id })) {
            List<Object> errArgs = [matchingStatus.collect { RefdataValue rdv -> rdv.getI10n('value') }.join(', ')]
            errors << messageSource.getMessage("onix.validation.error.noMatchingStatus", errArgs.toArray(), locale)
        }
        if(!lic.providerRelations) {
            errors << messageSource.getMessage("onix.validation.error.noLicensorError", null, locale)
        }
        if(!isValueOfAnySet(licPropertyMap, CORE_USAGE_TERMS)) {
            List<Object> errArgs = [CORE_USAGE_TERMS.collect { PropertyDefinition pd -> pd.getI10n('name') }.join(', ')]
            errors << messageSource.getMessage("onix.validation.error.noUsageTerms", errArgs.toArray(), locale)
        }
        if(errors) {
            error = messageSource.getMessage('onix.validation.error', null, locale)
            error += '\n'
            errors.each { String errMess ->
                error += errMess+'\n'
            }
        }
        error
    }

    /**
     * Helper method to map LAS:eR reference values to ONIX-PL controlled lists.
     * ONIX-PL controlled list values commented out have currently no LAS:eR equivalent (yet)
     * @param value the LAS:eR {@link RefdataValue} to map
     * @param onixCodeList the ONIX-PL code list ({@link License.ONIXPL_CONTROLLED_LIST}) where the mapping should be looked up
     * @return the ONIX-PL controlled list value
     */
    String refdataToOnixControlledList(RefdataValue value, License.ONIXPL_CONTROLLED_LIST onixCodeList) {
        switch(onixCodeList) {
            case License.ONIXPL_CONTROLLED_LIST.LICENSE_DOCUMENT_TYPE_CODE:
                switch(value) {
                    case RDStore.DOC_TYPE_ADDENDUM: return 'onixPL:Addendum'
                    case [RDStore.DOC_TYPE_LICENSE, RDStore.DOC_TYPE_ONIXPL]: return 'onixPL:License'
                    //onixPL:LicenseMainTerms
                    //onixPL:LicenseSchedule
                    //onixPL:LicenseSummary
                    default:
                        if(value) log.error("unmapped refdata: ${value}:${value.owner.desc}")
                        else log.error("PANIC! refdata missing but passed existence check?!")
                        return null
                }
            case License.ONIXPL_CONTROLLED_LIST.LICENSE_STATUS_CODE:
                switch(value) {
                    case RDStore.LICENSE_CURRENT: return 'onixPL:ActiveLicense'
                        //onixPL:Model
                    case RDStore.LICENSE_EXPIRED: return 'onixPL:NoLongerActive'
                        //onixPL:PolicyTemplate
                    case RDStore.LICENSE_INTENDED: return 'onixPL:ProposedLicense'
                        //onixPL:Template
                    default:
                        if(value) log.error("unmapped refdata: ${value}:${value.owner.desc}")
                        else log.error("PANIC! refdata missing but passed existence check?!")
                        return null
                }
            case License.ONIXPL_CONTROLLED_LIST.TERM_STATUS_CODE:
                switch(value) {
                    case RDStore.PERM_PROH_EXPL: return 'onixPL:ExplicitNo'
                    case RDStore.PERM_PERM_EXPL: return 'onixPL:ExplicitYes'
                    case RDStore.PERM_PROH_INTERP: return 'onixPL:InterpretedNo'
                    case RDStore.PERM_PERM_INTERP: return 'onixPL:InterpretedYes'
                    case [RDStore.YN_NO, RDStore.YNO_NO, RDStore.YNU_NO, RDStore.NON_EXISTENT]: return 'onixPL:No'
                    case RDStore.PERM_NOT_APPLICABLE: return 'onixPL:NotApplicable'
                    case RDStore.PERM_SILENT: return 'onixPL:Silent'
                    case [RDStore.PERM_UNKNOWN, RDStore.YNO_OTHER, RDStore.YNU_UNKNOWN]: return 'onixPL:Uncertain'
                    case [RDStore.YN_YES, RDStore.YNO_YES, RDStore.YNU_YES, RDStore.EXISTENT]: return 'onixPL:Yes'
                    default:
                        if(value) log.error("unmapped refdata: ${value}:${value.owner.desc}")
                        else log.error("PANIC! refdata missing but passed existence check?!")
                        return null
                }
            case License.ONIXPL_CONTROLLED_LIST.USAGE_STATUS_CODE:
                switch(value) {
                    case [RDStore.PERM_PERM_INTERP, RDStore.CONCURRENT_ACCESS_SPECIFIED]: return 'onixPL:InterpretedAsPermitted'
                    case RDStore.PERM_PROH_INTERP: return 'onixPL:InterpretedAsProhibited'
                    case [RDStore.PERM_PERM_EXPL, RDStore.YN_YES, RDStore.YNO_YES, RDStore.YNU_YES, RDStore.CONCURRENT_ACCESS_NO_LIMIT]: return 'onixPL:Permitted'
                    case [RDStore.PERM_PROH_EXPL, RDStore.YN_NO, RDStore.YNO_NO, RDStore.YNU_NO]: return 'onixPL:Prohibited'
                    case [RDStore.PERM_SILENT, RDStore.PERM_UNKNOWN, RDStore.YNO_OTHER, RDStore.YNU_UNKNOWN, RDStore.CONCURRENT_ACCESS_NOT_SPECIFIED, RDStore.CONCURRENT_ACCESS_OTHER]: return 'onixPL:SilentUninterpreted'
                    case RDStore.PERM_NOT_APPLICABLE: return 'onixPL:NotApplicable'
                    default:
                        if(value) log.error("unmapped refdata: ${value}:${value.owner.desc}")
                        else log.error("PANIC! refdata missing but passed existence check?!")
                        return null
                }
            default: log.error("unmapped refdata: ${value}:${value.owner.desc}")
                return null
        }
    }

    boolean isValueSet(Map<Long, LicenseProperty> licPropertyMap, PropertyDefinition pd) {
        boolean isValueSet = false
        if(licPropertyMap.containsKey(pd.id)) {
            isValueSet = licPropertyMap.get(pd.id).getValue() != null
        }
        isValueSet
    }

    boolean isValueOfAnySet(Map<Long, LicenseProperty> licPropertyMap, Collection<PropertyDefinition> propDefs) {
        boolean isValueSet = false
        int i = 0
        while(!isValueSet && i < propDefs.size()) {
            PropertyDefinition pd = propDefs[i]
            if(licPropertyMap.containsKey(pd.id)) {
                isValueSet = licPropertyMap.get(pd.id).getValue() != null
            }
            i++
        }
        isValueSet
    }

    boolean existsAnyOf(Map<Long, LicenseProperty> licPropertyMap, Set<PropertyDefinition> checkList) {
        isValueOfAnySet(licPropertyMap, checkList)
    }

    boolean isParagraphSet(Map<Long, LicenseProperty> licPropertyMap, PropertyDefinition pd) {
        boolean isValueSet = false
        if(licPropertyMap.containsKey(pd.id)) {
            isValueSet = licPropertyMap.get(pd.id).paragraph != null
        }
        isValueSet
    }

    boolean isParagraphOfAnySet(Map<Long, LicenseProperty> licPropertyMap, Collection<PropertyDefinition> propDefs) {
        boolean isParagraphSet = false
        int i = 0
        while(!isParagraphSet && i < propDefs.size()) {
            PropertyDefinition pd = propDefs[i]
            if(licPropertyMap.containsKey(pd.id)) {
                isParagraphSet = licPropertyMap.get(pd.id).paragraph != null
            }
            i++
        }
        isParagraphSet
    }

    boolean checkValueAndPermissionStatus(Map<Long, LicenseProperty> licPropertyMap, PropertyDefinition pd, String onixPLPermissionStatus) {
        boolean hasStatus = false
        if(isValueSet(licPropertyMap, pd)) {
            hasStatus = refdataToOnixControlledList(licPropertyMap.get(pd.id).getRefValue(), License.ONIXPL_CONTROLLED_LIST.USAGE_STATUS_CODE) == onixPLPermissionStatus
        }
        hasStatus
    }

    String toSnakeCase(String input) {
        input.replaceAll(/[:()-]/, "").replaceAll(/[ \/]/,"_").toLowerCase()
    }
    Map<String, Object> getCopyResultGenerics(GrailsParameterMap params) {
        Map<String, Object> result             = [:]
        result.user            = contextService.getUser()
        result.institution     = contextService.getOrg()
        result.contextOrg      = result.institution

        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId ?: params.id
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        if(params.containsKey('copyMyElements'))
            result.editable = contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_INST_PRO)
        else
            result.editable = result.sourceObject.isEditableBy(result.user)

        result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL) ?: false
        result.transferIntoMember = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_LOCAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)

        result.allObjects_readRights = getMyLicenses_readRights([status: RDStore.LICENSE_CURRENT.id])
        result.allObjects_writeRights = getMyLicenses_writeRights([status: RDStore.LICENSE_CURRENT.id])

        List<String> licTypSubscriberVisible = [CalculatedType.TYPE_CONSORTIAL,
                                                CalculatedType.TYPE_ADMINISTRATIVE]
        result.isSubscriberVisible = result.sourceObject && result.targetObject &&
                licTypSubscriberVisible.contains(result.sourceObject._getCalculatedType()) &&
                licTypSubscriberVisible.contains(result.targetObject._getCalculatedType())
        result
    }

}
