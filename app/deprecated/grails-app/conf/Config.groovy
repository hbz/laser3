
onix = [
        "codelist" : "ONIX_PublicationsLicense_CodeLists.xsd",
        "comparisonPoints" : [
                'template' : '$value$',
                'values' : [
                        '_:PublicationsLicenseExpression' : [
                                'text' : 'All',
                                'children' : [
                                        'template' : '_:$value$',
                                        'values' : [
                                                'Definitions' : [
                                                        'processor': ({ List<Map> data ->

                                                            def new_data = []
                                                            data.each { Map item ->
                                                                switch (item."_name") {
                                                                    case "AgentRelatedAgent" :
                                                                        // Add a new row for each related agent.
                                                                        (0..(item."RelatedAgent"?.size() - 1)).each { int idx ->
                                                                            def entry = [:]

                                                                            // Copy the whole of the data.
                                                                            entry << item

                                                                            // Replace the related agent with a list of just 1.
                                                                            entry."RelatedAgent" = [item["RelatedAgent"][idx]]

                                                                            new_data += entry
                                                                        }
                                                                        break

                                                                    default :
                                                                        // Just add the item.
                                                                        new_data += item
                                                                        break
                                                                }
                                                            }
                                                            if (new_data.size() > 0) {
                                                                // Because we want to edit the referenced data we can not create a new list,
                                                                // we must instead empty the old and repopulate with the new.
                                                                data.clear()
                                                                data.addAll(new_data)
                                                            }

                                                            // Return the data.
                                                            data
                                                        }),
                                                        'text' : 'Authorised Users',
                                                        'children': [
                                                                'template' : "_:AgentDefinition[normalize-space(_:AgentLabel/text())='\$value\$']/_:AgentRelatedAgent",
                                                                'values' : [
                                                                        'AuthorizedUser' : ['text': 'Authorized User'],
                                                                ]
                                                        ]
                                                ],
                                                'LicenseGrant' : [
                                                        'text' : 'License Grants'
                                                ],
                                                'UsageTerms' : [
                                                        'processor': ({ List<Map> data ->
                                                            def new_data = []
                                                            def users = data.getAt(0)['User']
                                                            def deepcopy = { orig ->
                                                                def bos;
                                                                def oos;
                                                                def bin;
                                                                def ois;
                                                                try{
                                                                    bos = new ByteArrayOutputStream()
                                                                    oos = new ObjectOutputStream(bos)
                                                                    oos.writeObject(orig); oos.flush()
                                                                    bin = new ByteArrayInputStream(bos.toByteArray())
                                                                    ois = new ObjectInputStream(bin)
                                                                    return ois.readObject()
                                                                }finally{
                                                                    bos?.close()
                                                                    oos?.close()
                                                                    bin?.close()
                                                                    ois?.close()
                                                                }
                                                            }
                                                            def refresh_data = {
                                                                if (new_data.size() > 0) {
                                                                    // Because we want to edit the referenced data we can not create a new list,
                                                                    // we must instead empty the old and repopulate with the new.
                                                                    data.clear()
                                                                    data.addAll(new_data)
                                                                    new_data.clear()
                                                                }
                                                            }
                                                            if(users?.size() > 1){
                                                                users.each{ item ->
                                                                    def copy = [:]
                                                                    //Copy the data
                                                                    copy << data.getAt(0)
                                                                    //Grab the single user and add him to an array
                                                                    def temp = [item]
                                                                    //Then replace the data User(s) with the single User
                                                                    copy['User'] = temp

                                                                    new_data += copy
                                                                }
                                                            }

                                                            refresh_data();
                                                            //Create several rows for comparison points that need to be split
                                                            def replicate_row = {usage,type ->
                                                                usage?."${type}"?.each{ method ->
                                                                    def copy = [:]
                                                                    copy << usage
                                                                    def temp = [method]
                                                                    copy[type] = temp
                                                                    new_data += copy
                                                                }
                                                            }

                                                            def replicate_nested_row = {usage,parent,child ->
                                                                usage."${parent}"?."${child}"?.getAt(0)?.each{ place ->
                                                                    def copy = [:]
                                                                    def entry = [:]
                                                                    copy = deepcopy(usage)
                                                                    entry = place.clone()
                                                                    copy."${parent}"[0]."${child}"= [entry]
                                                                    new_data.addAll(copy)
                                                                }
                                                            }
                                                            //Need to loop we might have multiple data here, genetrated from above
                                                            data.each{ usage ->
                                                                def usageType = usage."UsageType"?.getAt(0)?."_content"
                                                                switch (usageType){
                                                                    case "onixPL:Access":
                                                                        replicate_row(usage,'UsageMethod');
                                                                        break;
                                                                    case "onixPL:Copy":
                                                                        replicate_row(usage,'UsagePurpose');
                                                                        break;
                                                                    case "onixPL:DepositInPerpetuity":
                                                                        replicate_nested_row(usage,'UsageRelatedPlace','RelatedPlace');
                                                                        break;
                                                                    case "onixPL:Include":
                                                                        if(usage.'UsageRelatedResource'?.'UsageResourceRelator'?.'_content'?.contains(['onixPL:TargetResource'])){
                                                                            replicate_nested_row(usage,'UsageRelatedResource','RelatedResource');
                                                                        }
                                                                        break;
                                                                    case "onixPL:MakeAvailable":
                                                                        if(usage.'UsageRelatedAgent'?.'UsageAgentRelator'?.'_content'?.contains(['onixPL:ReceivingAgent'])){
                                                                            replicate_nested_row(usage,'UsageRelatedAgent','RelatedAgent');
                                                                        }
                                                                        break;
                                                                    case "onixPL:SupplyCopy":
                                                                        if(usage.'UsageRelatedAgent'?.'UsageAgentRelator'?.'_content'?.contains(['onixPL:ReceivingAgent'])){
                                                                            replicate_nested_row(usage,'UsageRelatedAgent','RelatedAgent');
                                                                        }
                                                                        break;
                                                                    case "onixPL:Use":
                                                                        replicate_row(usage,'UsagePurpose');
                                                                        break;
                                                                    case "onixPL:UseForDataMining":
                                                                        replicate_row(usage,'UsagePurpose');
                                                                        break;
                                                                    default:
                                                                        new_data += usage
                                                                        break;
                                                                }

                                                            }
                                                            refresh_data();

                                                            //Return the data.
                                                            data
                                                        }),
                                                        'text' : 'Usage Terms',
                                                        'children' : [
                                                                'template' : "_:Usage[normalize-space(_:UsageType/text())='\$value\$']",
                                                                'values' : [
                                                                        'onixPL:Access' : ['text' :  'Access'],
                                                                        'onixPL:Copy' : ['text' : 'Copy'],
                                                                        'onixPL:DepositInPerpetuity' : ['text' :  'Deposit In Perpetuity'],
                                                                        'onixPL:Include': ['text': 'Include'],
                                                                        'onixPL:MakeAvailable': ['text': 'Make Available'],
                                                                        'onixPL:MakeDigitalCopy' : ['text' :  'Make Digital Copy'],
                                                                        'onixPL:Modify' : ['text' :  'Modify'],
                                                                        'onixPL:PrintCopy' : ['text': 'PrintCopy'],
                                                                        'onixPL:ProvideIntegratedAccess' : ['text' :  'Provide Integrated Access'],
                                                                        'onixPL:ProvideIntegratedIndex' : ['text' :  'Provide Integrated Index'],
                                                                        'onixPL:RemoveObscureOrModify' : ['text' :  'Remove Obscure Or Modify'],
                                                                        'onixPL:Sell' : ['text' :  'Sell'],
                                                                        'onixPL:SupplyCopy' : ['text' : 'Supply Copy'],
                                                                        'onixPL:Use' : ['text': 'Use'],
                                                                        'onixPL:UseForDataMining' : ['text':'Use For Data Mining'],


                                                                ]
                                                        ]
                                                ],
                                                'SupplyTerms' : [
                                                        'text' : 'Supply Terms',
                                                        'children' : [
                                                                'template' : "_:SupplyTerm[normalize-space(_:SupplyTermType/text())='\$value\$']",
                                                                'values' : [
                                                                        'onixPL:ChangeOfOwnershipOfLicensedResource' : ['text': 'Change Of Ownership Of Licensed Resource'],
                                                                        'onixPL:ChangesToLicensedContent' : ['text': 'Changes To Licensed Content'],
                                                                        'onixPL:CompletenessOfContent' : ['text': 'Completeness Of Content'],
                                                                        'onixPL:ComplianceWithAccessibilityStandards' : ['text': 'Compliance With Accessibility Standards'],
                                                                        'onixPL:ComplianceWithONIX' : ['text': 'Compliance With ONIX'],
                                                                        'onixPL:ComplianceWithOpenURLStandard' : ['text': 'Compliance With OpenURL Standard'],
                                                                        'onixPL:ComplianceWithProjectTransferCode' : ['text': 'Compliance With Project Transfer Code'],
                                                                        'onixPL:ComplianceWithStandardsAndBestPractices' : ['text': 'Compliance With Standards And Best Practices'],
                                                                        'onixPL:ConcurrencyWithPrintVersion' : ['text': 'Concurrency With Print Version'],
                                                                        'onixPL:ContentDelivery' : ['text': 'Content Delivery'],
                                                                        'onixPL:ContentWarranty' : ['text': 'Content Warranty'],
                                                                        'onixPL:LicenseeOpenAccessContent' : ['text': 'Licensee OpenAccess Content'],
                                                                        'onixPL:MediaWarranty' : ['text': 'Licensee OpenAccess Content'],
                                                                        'onixPL:MetadataSupply' : ['text': 'Metadata Supply'],
                                                                        'onixPL:NetworkAccess' : ['text': 'Network Access'],
                                                                        'onixPL:OpenAccessContent' : ['text': 'OpenAccess Content'],
                                                                        'onixPL:ProductDocumentation' : ['text': 'Product Documentation'],
                                                                        'onixPL:PublicationSchedule' : ['text': 'Publication Schedule'],
                                                                        'onixPL:ServicePerformance' : ['text': 'Service Performance'],
                                                                        'onixPL:ServicePerformanceGuarantee' : ['text': 'Service Performance Guarantee'],
                                                                        'onixPL:StartOfService' : ['text': 'Start Of Service'],
                                                                        'onixPL:UsageStatistics' : ['text': 'Usage Statistics'],
                                                                        'onixPL:UserRegistration' : ['text': 'User Registration'],
                                                                        'onixPL:UserSupport' : ['text': 'UserSupport']
                                                                ]
                                                        ]
                                                ],
                                                'ContinuingAccessTerms' : [
                                                        'processor': ({ List<Map> data ->

                                                            def new_data = []
                                                            def deepcopy = { orig ->
                                                                def bos;
                                                                def oos;
                                                                def bin;
                                                                def ois;
                                                                try{
                                                                    bos = new ByteArrayOutputStream()
                                                                    oos = new ObjectOutputStream(bos)
                                                                    oos.writeObject(orig); oos.flush()
                                                                    bin = new ByteArrayInputStream(bos.toByteArray())
                                                                    ois = new ObjectInputStream(bin)
                                                                    return ois.readObject()
                                                                }finally{
                                                                    bos?.close()
                                                                    oos?.close()
                                                                    bin?.close()
                                                                    ois?.close()
                                                                }
                                                            }
                                                            data.each{access ->
                                                                access."ContinuingAccessTermRelatedAgent"?."RelatedAgent"?.getAt(0)?.each{ agent ->
                                                                    def copy = [:]
                                                                    def entry = [:]
                                                                    copy = deepcopy(access)
                                                                    entry = agent.clone()
                                                                    copy."ContinuingAccessTermRelatedAgent"."RelatedAgent"[0].clear()
                                                                    copy."ContinuingAccessTermRelatedAgent"."RelatedAgent"[0].addAll(entry)
                                                                    new_data.addAll(copy)
                                                                }
                                                            }
                                                            if (new_data.size() > 0) {
                                                                // Because we want to edit the referenced data we can not create a new list,
                                                                // we must instead empty the old and repopulate with the new.
                                                                data.clear()
                                                                data.addAll(new_data)
                                                            }

                                                            data
                                                        }),
                                                        'text' : 'Continuing Access Terms',
                                                        'children' : [
                                                                'template' : "_:ContinuingAccessTerm[normalize-space(_:ContinuingAccessTermType/text())='\$value\$']",
                                                                'values' : [
                                                                        'onixPL:ContinuingAccess' : ['text' :  'Continuing Access' ],
                                                                        'onixPL:ArchiveCopy' : ['text' :  'Archive Copy' ],
                                                                        'onixPL:PostCancellationFileSupply': ['text': 'Post Cancellation File Supply'],
                                                                        'onixPL:PostCancellationOnlineAccess': ['text': 'Post Cancellation Online Access'],
                                                                        'onixPL:NotificationOfDarkArchive': ['text': 'Notification Of Dark Archive'],
                                                                        'onixPL:PreservationInDarkArchive': ['text': 'Preservation In Dark Archive']
                                                                ]
                                                        ]
                                                ],
                                                'PaymentTerms/_:PaymentTerm' : [
                                                        'text' : 'Payment Terms'
                                                ],
                                                'GeneralTerms/_:GeneralTerm' : [
                                                        'text' : 'General Terms'
                                                ]
                                        ]
                                ]
                        ]
                ]
        ]
]

defaultOaiConfig = [
        serverName: 'K-Int generic Grails OAI Module :: KBPlus.ac.uk',
        lastModified:'lastUpdated',
        serverEmail:'laser@laser.laser',
        schemas:[
                'oai_dc':[
                        type:'method',
                        methodName:'toOaiDcXml',
                        schema:'http://www.openarchives.org/OAI/2.0/oai_dc.xsd',
                        metadataNamespaces: [
                                '_default_' : 'http://www.openarchives.org/OAI/2.0/oai_dc/',
                                'dc'        : "http://purl.org/dc/elements/1.1/"
                        ]],
                'kbplus':[
                        type:'method',
                        methodName:'toKBPlus',
                        schema:'http://www.kbplus.ac.uk/schemas/oai_metadata.xsd',
                        metadataNamespaces: [
                                '_default_': 'http://www.kbplus.ac.uk/oai_metadata/'
                        ]],
        ]
]