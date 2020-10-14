
### API - Version 0.85


#### Objects
    
    Endpoint            Api Level¹                  OrgRoles²                   Subscription³       License⁴     
    ---------------------------------------------------------------------------------------------------------------
    
    /costItem           API_LEVEL_READ,
                        API_LEVEL_INVOICE
                        
                        ⇒ if ¹ given: access to own costItem or all if ¹ = API_LEVEL_INVOICE
    
    /document           --- TODO ---
    
    /license            API_LEVEL_READ              LICENSING_CONSORTIUM,                           isPublicForApi
                                                    LICENSEE,
                                                    LICENSEE_CONS
    
                        ⇒ if ¹ given: access to license if ² and ⁴ 
            
    /organisation       API_LEVEL_READ
    
                        ⇒ if ¹ given: access to organisation if it's your own
         
    /package            API_LEVEL_READ
    
                        ⇒ if ¹ given: access to package 
    
    /subscription       API_LEVEL_READ              SUBSCRIPTION_CONSORTIA,     isPublicForApi
                                                    SUBSCRIBER,
                                                    SUBSCRIBER_CONS
    
                        ⇒ if ¹ given: access to subscription if ² and ³


#### Lists
    
    Endpoint            Api Level¹                  OrgRoles²                   Subscription³       License⁴     
    ---------------------------------------------------------------------------------------------------------------
        
    /costItemList       --- TODO ---
    
    /licenseList        API_LEVEL_READ              LICENSING_CONSORTIUM,                           isPublicForApi
                                                    LICENSEE,
                                                    LICENSEE_CONS
                                                    
                        ⇒ if ¹ given: access to all licenses with ² and ⁴
    
    /propertyList       API_LEVEL_READ
    
                        ⇒ if ¹ given: access to all public and own private properties
    
    /refdataList        API_LEVEL_READ
    
                        ⇒ if ¹ given: access to all refdatas
    
    /subscriptionList   API_LEVEL_READ              SUBSCRIPTION_CONSORTIA,     isPublicForApi
                                                    SUBSCRIBER,
                                                    SUBSCRIBER_CONS
                                                        
                        ⇒ if ¹ given: access to all subscriptions with ² and ³ 


#### Datamanager
    
    Endpoint            Api Level¹                  OrgSetting²                 Subscription³       License⁴     
    ---------------------------------------------------------------------------------------------------------------
    
    /statisticList      API_LEVEL_DATAMANAGER       NATSTAT_SERVER_ACCESS
    
                        ⇒ if ¹ given: access to all packages (as stub) with (org ² <-> sub <-> subPkg <-> pkg)
    
    /statistic          API_LEVEL_DATAMANAGER       NATSTAT_SERVER_ACCESS       isPublicForApi      isPublicForApi      
    
                        ⇒ if ¹ given: access to all packages with (org ² <-> sub <-> subPkg <-> pkg)
      
                                      and nested subscription (as stub) and/or license (as stub) if ³ and/or ⁴
    
    /oaMonitorList      API_LEVEL_DATAMANAGER       OAMONITOR_SERVER_ACCESS
    
                        ⇒ if ¹ given: access to all orgs (as stub) with ²
    
    /oaMonitor          API_LEVEL_DATAMANAGER       OAMONITOR_SERVER_ACCESS     isPublicForApi      isPublicForApi
    
                        ⇒ if ¹ given: access to all org with ²
          
                                      and nested subscriptions (as stub) if ³
 