package de.laser

import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.springframework.context.MessageSource

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import java.text.SimpleDateFormat

/**
 * This service handles license specific matters
 * @see License
 */
@Transactional
class LicenseService {

    ContextService contextService
    GrailsApplication grailsApplication
    MessageSource messageSource

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
        visibleProviderRelations.addAll(ProviderRole.executeQuery('select pr from ProviderRole pr join pr.provider p where pr.license = :license order by p.sortname', [license: license]))
        visibleProviderRelations
    }

    /**
     * Retrieves all visible vendor links for the given license
     * @param license the license to retrieve the relations from
     * @return a sorted set of visible relations
     */
    SortedSet<VendorRole> getVisibleVendors(License license) {
        SortedSet<VendorRole> visibleVendorRelations = new TreeSet<VendorRole>()
        visibleVendorRelations.addAll(VendorRole.executeQuery('select vr from VendorRole vr join vr.vendor v where vr.license = :license order by v.sortname', [license: license]))
        visibleVendorRelations
    }

    /*
    do's: - return type either bool or XML
    - input arg: License
     */
    String validateOnixPlDocument() {
        /*
        agenda:
        - first: create XML document hand-coded
        - create then a translation script from License into XML; the MarkupBuilder returns
        - the translation script gets as input a Map in which inputParameters are being specified; those inputParameters are being processed upon creation of the XML
        - implement validator: move XSD(s) into project or reference them with CDN and apply validator on it (cf. https://stackoverflow.com/questions/55067050/validating-a-xml-doc-in-groovy)

        LAS:eR structure enhancements:
        - new field paragraphNumber for LicenseProperty (optional on LAS:eR side, mandatory for ex/import, throw exception if not set!)

        generic dos:
        - RDConstants.YN and derivates and RDConstants.PERMISSIONS: implement helper; TermStatusCode is base for RefdataCategory RDConstants.PERMISSIONS
        - paragraph reference: create method for paragraph extraction

        structure:
        - elements may contain references to definitions; internal labels are being used as referrers
        - definitions contain explanations (e.g. a LicenseStartDate is being referred to in LicenseDefinition but explained fully in TimePointDefinition)
         */
        File schemaFile = grailsApplication.mainContext.getResource('files/ONIX_PublicationsLicense_V1.0.xsd').file
        Validator validator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile).newValidator()
        String xml = createHardCodedTestFile_v0()
        //def xml = createHardCodedTestFile_v1()
        validator.validate(new StreamSource(new StringReader(xml)))
        xml
    }

    /**
     * experiment sandbox 1
     * @return
     */
    String createHardCodedTestFile_v0() {
        SimpleDateFormat onixTimestampFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        Locale locale = LocaleUtils.getCurrentLocale()
        Org institution = contextService.getOrg()
        Date now = new Date()
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
                            AddresseeIDType()
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
                            AgentLabel('Authorized Users')
                            AgentType('onixPL:Person')
                            //the value of Authorized Users cannot be used because it is free text; include paragraphs here
                            LicenseTextLink(href: '')
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
                            Description('On request') //maps refdata value of license property Archival Copy: Time
                        }
                        DocumentDefinition {

                        }
                    }
                    //optional 0-1
                    LicenseGrant {

                    }
                    //mandatory 1
                    UsageTerms {
                        //covers Authorized Users, Alumni Access Walk-In User
                        //multiple Usage statements per each status
                        Usage {
                            UsageType('onixPL:Use')
                            UsageStatus('onixPL:Permitted')
                            User()
                            /*

                                //!!! mark here the ___existence___ of the following properties: Walk-In User, (Simuser)
                                RelatedAgent('onixPL:WalkInUser')
                             */
                            UsedResource()
                        }
                        //multiple Usage statements per each status
                        Usage {
                            UsageType('onixPL:Use')
                            UsageStatus('onixPL:Prohibited')
                            User()
                            /*

                                //!!! mark here the ___existence___ of the following properties: Walk-In User, (Simuser)
                                RelatedAgent('onixPL:WalkInUser')
                             */
                            UsedResource()
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
                            //license property Continuing Access: Title Transfer
                            SupplyTermType('onixPL:ComplianceWithProjectTransferCode')
                        }
                    }
                    //optional 0-1
                    ContinuingAccessTerms {
                        //license property Post Cancellation Online Access
                        ContinuingAccessTerm {
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
                                AnnotationText('Restrictions hold') //reference paragraph here
                            }
                        }
                        ContinuingAccessTerm {
                            //license property Archival Copy: Permission
                            ContinuingAccessTermType('onixPL:PostCancellationFileSupply')
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
                            ContinuingAccessTermRelatedTimePoint {
                                RelatedTimePoint('ArchivalCopyTimePoint')
                            }
                        }
                    }
                    //optional 0-1
                    PaymentTerms {

                    }
                    //optional 0-1
                    GeneralTerms {
                        GeneralTerm {
                            GeneralTermType('onixPL:AllRightsReserved')
                            TermStatus('onixPL:Yes')
                            LicenseTextLink(href: 'lp_all_rights_reserved_indicator_01')
                        }
                        GeneralTerm {
                            GeneralTermType()
                            GeneralTermQuantity {
                                GeneralTermQuantityType('onixPL:PeriodForCureOfBreach')
                                //extractor script "30 Tage" -> 30 and onixPL:Days; throw exception if mapping failed with indicating correct values
                                QuantityDetail {
                                    Value(30)
                                    QuantityUnit('onixPL:Days')
                                }
                            }
                        }
                    }
                    //optional 0-1; possible container for LicenseProperty.paragraph-s
                    //not possible to implement properly because mandatory data is missing: DocumentLabel (I cannot ensure an underlying document is available); SortNumber (is mostly not given)
                    //DocumentLabel: substituted by LicenseProperty paragraph, SortNumber: substituted by 0; may be removed completely if no productive use is possible, proposal character!
                    LicenseDocumentText {
                        DocumentLabel('license.reference')
                        TextElement(id: 'lp_accessibility_compliance_01') { //"lp_${toSnakeCase(lp.name)}_property count number"
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
                            Text('1.3.2 [...] Soweit im Nutzungsvertrag nicht abweichend geregelt, sind berechtigte Nutzer nur solche Personen, die [...] zu diesem [dem Kunden] in einem Dienst-, Arbeits- oder Ausbildungsverhältnis stehen. Eine unmittelbare oder mittelbare Nutzung durch andere Personen ist nicht zulässig. (siehe AGB, S. 1)')
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
    String createHardCodedTestFile_v1() {
        SimpleDateFormat onixTimestampFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        def xml = builder.bind {

        }
        XmlUtil.serialize(xml)
    }
}
