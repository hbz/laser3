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
        Locale locale = LocaleUtils.getCurrentLocale()
        Org institution = contextService.getOrg()
        Date now = new Date()
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
                        //LicenseRenewalType() //n/a
                        /*
                        Notes cannot be mapped into Annotations; probably, I have to include LicenseProperties here ...?
                        Annotation {
                            AnnotationType()
                            controlled list:
                            [onixPL:AcknowledgmentToInclude, onixPL:AcknowledgementWording, onixPL:AmbiguityInLicenseText, onixPL:ErrorInLicenseText, onixPL:FeesPayableNote, onixPL:Interpretation,
                            onixPL:NewTitleAccessNote, onixPL:PaymentNote, onixPL:PostCancellationFileSupplyHoldingsNote, onixPL:PostCancellationFileSupplyNote, onixPL:PostCancellationOnlineAccessHoldingsNote,
                            onixPL:PostCancellationOnlineAccessSupplyNote, onixPL:PriceBasisNote, onixPL:PriceIncreaseCap, onixPL:ReferencedText, onixPL:SpecialConditions, onixPL:TitleSubstitutionNote,
                            onixPL:TransferredTitleNote, onixPL:VariationsFromModel, onixPL:ERMI:ArchiveCopyFormat, onixPL:ERMI:CitationRequirementDetail, onixPL:ERMI:ConcurrentUserNote, onixPL:ERMI:DistanceEducationNote,
                            onixPL:ERMI:DistanceEducationStatus, onixPL:ERMI:LocalText, onixPL:ERMI:MaintenanceWindow, onixPL:ERMI:Note, onixPL:ERMI:OtherUseRestrictionNote, onixPL:ERMI:OtherUserRestrictionNote,
                            onixPL:ERMI:PerpetualAccessHoldings, onixPL:ERMI:TerminationRequirementsNote, onixPL:ERMI:TermsNote, onixPL:ERMI:Text, onixPL:ERMI:UptimeGuarantee, onixPL:ERMI:Value, onixPL:ERMI:WalkInUserTermNote]

                            Authority('hbz')
                            AnnotationText('Dieses Dokument beschreibt die Gründung des Analysestadtverbundes, dessen Rahmenvertrag sich jede neue Analysestadt anzuschließen hat.')
                        }*/
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
                            RelatedAgent('Licensing Consortium') //take the RefdataValue
                        }
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:LicenseeConsortium')
                            RelatedAgent('Licensee Consortial') //take the RefdataValue
                        }
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:LicenseeConsortium')
                            RelatedAgent('Helen Louise Brownston university Shawinigan')
                        }
                        //other user-related properties: Authorized Users, Walk-In User, Simuser; see definition in AgentDefinition
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:AuthorizedUsers')
                            RelatedAgent('Authorized Users')
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
                        LicenseRelatedPlace {
                            //LicenseProperty Governing jurisdiction
                            LicensePlaceRelator('onixPL:PlaceOfJurisdiction')
                            RelatedPlace('Zeidel')
                        }
                    }
                    Definitions {
                        //LicenseRelatedAgents explained ==> again OrgRoles!
                        AgentDefinition {
                            AgentLabel('Bloomsbury')
                            AgentType('onixPL:Organization')
                            AgentIdentifier {
                                AgentIDType('onixPL:Proprietary')
                                IDTypeName('wekbId')
                                IDValue('wekb UUID')
                            }
                        }
                        AgentDefinition {
                            AgentLabel('Licensing Consortium') //take the RefdataValue label
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
                            AgentLabel('Licensee Consortial') //take the RefdataValue label
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
                            AgentRelatedAgent {
                                AgentAgentRelator('onixPL:IsOneOf')
                                //!!! mark here the ___existence___ of the following properties: Walk-In User, (Simuser)
                                RelatedAgent('onixPL:WalkInUser')
                            }
                        }
                        /*
                        not mappable because Walk-In Users in LAS:eR keeps note only about the existence of walk-in users, not how they are called in the license text
                        AgentDefinition {
                            AgentLabel('Walk-In Users')
                            AgentType('onixPL:Person')
                            AgentRelatedAgent {
                                AgentAgentRelator('onixPL:IsA')
                                RelatedAgent('Angehörige der Universität') //free-text value of property Walk-In User
                            }
                        }
                        */
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
                            DocumentLabel('Continuing Access: Title Transfer')
                            DocumentType('onixPL:WebResource')
                            Description('Licese Property Value submitted in LAS:eR ERMS')
                        }
                    }
                    //optional 0-1
                    LicenseGrant {

                    }
                    //mandatory 1
                    UsageTerms {
                        Usage {
                            UsageType()
                            /*onixPL:Access, onixPL:AccessByRobot, onixPL:AccessByStreaming, onixPL:Copy, onixPL:Deposit, onixPL:DepositInPerpetuity
                            onixPL:DisableOrOverrideProtection, onixPL:Include, onixPL:MakeAvailable, onixPL:MakeAvailableByStreaming, onixPL:MakeDerivedWork, onixPL:MakeDigitalCopy
                            onixPL:MakeTemporaryDigitalCopy, onixPL:Modify, onixPL:Perform onixPL:Photocopy, onixPL:PrintCopy, onixPL:ProvideIntegratedAccess, onixPL:ProvideIntegratedIndex, onixPL:Reformat
                            onixPL:RemoveObscureOrModify, onixPL:Sell, onixPL:SupplyCopy, onixPL:SystematicallyCopy, onixPL:Use, onixPL:UseForDataMining
                            */
                            UsageStatus()
                            User()
                            UsedResource()
                        }
                    }
                    //optional 0-1
                    SupplyTerms {
                        SupplyTerm {
                            //license property Accessibility compliance
                            SupplyTermType('onixPL:ComplianceWithAccessibilityStandards')
                            TermStatus('onixPL:Yes') //or no or uncertain
                            LicenseDocumentReference {
                                //the reference
                            }
                        }
                        SupplyTerm {
                            //license property Continuing Access: Title Transfer
                            SupplyTermType('onixPL:ComplianceWithProjectTransferCode')
                            //is the only way to map this/such property value ...
                            OtherDocumentReference {
                                DocumentLabel('Continuing Access: Title Transfer') //licenseProp.name
                                DocumentText('Regelung vorhanden') //licenseProp.value
                            }
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
                    /*
                    LicenseDocumentText {
                        DocumentLabel('LicenseProperty Governing law')
                        TextElement(id: 'lp_governing_law_01') { //"lp_${toSnakeCase(lp.name)}_property count number"
                            SortNumber(1) //property count number; dummy value because LAS:eR data structure does not offer storage, then, it cannot be expected that users fill it properly
                            Text('The parties agree that this Agreement shall be governed by and construed in accordance with the laws of Germany.')
                        }
                    }
                    */
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
