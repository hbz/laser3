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
    def validateOnixPlDocument() {
        /*
        agenda:
        - first: create XML document hand-coded
        - create then a translation script from License into XML; the MarkupBuilder returns
        - implement validator: move XSD(s) into project or reference them with CDN and apply validator on it (cf. https://stackoverflow.com/questions/55067050/validating-a-xml-doc-in-groovy)
         */
        File schemaFile = grailsApplication.mainContext.getResource('files/ONIX_PublicationsLicense_V1.0.xsd').file
        Validator validator = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile).newValidator()
        Locale locale = LocaleUtils.getCurrentLocale()
        StreamingMarkupBuilder builder = new StreamingMarkupBuilder()
        Org institution = contextService.getOrg()
        SimpleDateFormat onixTimestampFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")
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
                            LicenseAgentRelator('onixPL:LicenseeRepresentative')
                            RelatedAgent('Lizenzierungskonsortium des Analysestadtverbundes')
                        }
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:LicenseeConsortium')
                            RelatedAgent('Universitätsbibliothek Eichlinghausen')
                        }
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:LicenseeConsortium')
                            RelatedAgent('Friedrich-Vieweg-Hochschule')
                        }
                        LicenseRelatedAgent {
                            LicenseAgentRelator('onixPL:LicenseeConsortium')
                            RelatedAgent('Helen Louise Brownston university Shawinigan')
                        }
                        //other user-related properties: Authorized Users, Walk-In User, Simuser
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
                            RelatedTimePoint('2012-02-16') //or dd.mm.yyyy? Schema does not make any restrictions on that point
                        }
                        /*
                        if exists
                        LicenseRelatedTimePoint {
                            LicenseTimePointRelator('onixPL:LicenseEndDate')
                        }
                        */
                        LicenseRelatedPlace {
                            //LicenseProperty Governing jurisdiction
                            LicensePlaceRelator('onixPL:PlaceOfJurisdiction')
                            RelatedPlace('Zeidel')
                        }
                    }
                    Definitions {
                        //LicenseRelatedAgents more expanded ==> again OrgRoles!
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
                            AgentRelatedAgent {
                                AgentAgentRelator('onixPL:IsA')
                                RelatedAgent('Biblitoheksnutzer:in') //free-text value of property Authorized User
                            }
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

                    }
                    //optional 0-1
                    ContinuingTermsAccess {

                    }
                    //optional 0-1
                    PaymentTerms {

                    }
                    //optional 0-1
                    GeneralTerms {

                    }
                    //optional 0-1; possible container for LicenseProperty.paragraph-s
                    //not possible to implement because mandatory data is missing: DocumentLabel (I cannot ensure an underlying document is available); SortNumber (is mostly not given)
                    /*
                    LicenseDocumentText {

                    }
                    */
                }
            }
        }
        validator.validate(new StreamSource(new StringReader(XmlUtil.serialize(xml))))
        xml.toString()
    }
}
