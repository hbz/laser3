
### API - Version 0.81


#### Objects
    
    Endpoint            Api Level¹                  OrgRoles²                   Subscription³       License⁴     
    ---------------------------------------------------------------------------------------------------------------
    
    /license            API_LEVEL_READ              LICENSING_CONSORTIUM,                           isPublicForApi
                                                    LICENSEE,
                                                    LICENSEE_CONS
    
        => if ¹ given: access to license if ² and ⁴ 
            
    /organisation       API_LEVEL_READ
    
        => if ¹ given: access to organisation if it's your own
         
    /package            API_LEVEL_READ
    
        => if ¹ given: access to package 
    
    /subscription       API_LEVEL_READ              SUBSCRIPTION_CONSORTIA,     isPublicForApi
                                                    SUBSCRIBER,
                                                    SUBSCRIBER_CONS
    
        => if ¹ given: access to subscription if ² and ³


#### Lists
    
    Endpoint            Api Level¹                  OrgRoles²                   Subscription³       License⁴     
    ---------------------------------------------------------------------------------------------------------------
        
    /licenseList        API_LEVEL_READ              LICENSING_CONSORTIUM,                           isPublicForApi
                                                    LICENSEE,
                                                    LICENSEE_CONS
                                                    
         => if ¹ given: access to all licenses where ² and ⁴
    
    /subscriptionList   API_LEVEL_READ              SUBSCRIPTION_CONSORTIA,     isPublicForApi
                                                    SUBSCRIBER,
                                                    SUBSCRIBER_CONS
                                                        
        => if ¹ given: access to all subscriptions if ² and ³ 


#### Datamanager
    
    Endpoint            Api Level¹                  OrgSetting²                 Subscription³       License⁴     
    ---------------------------------------------------------------------------------------------------------------
    
    /statisticList      API_LEVEL_DATAMANAGER       NATSTAT_SERVER_ACCESS
    
        => if ¹ given: access to all packages (as stub) with (org² <-> sub <-> subPkg <-> pkg)
    
    /statistic          API_LEVEL_DATAMANAGER       NATSTAT_SERVER_ACCESS       isPublicForApi      isPublicForApi      
    
        => if ¹ given: access to all packages with (org² <-> sub <-> subPkg <-> pkg)
      
                       and nested subscription (as stub) and/or license (as stub) if ³ and/or ⁴
    
    /oaMonitorList      API_LEVEL_DATAMANAGER       OAMONITOR_SERVER_ACCESS
    
        => if ¹ given: access to all orgs (as stub) with (org²)
    
    /oaMonitor          API_LEVEL_DATAMANAGER       OAMONITOR_SERVER_ACCESS     isPublicForApi      isPublicForApi
    
        => if ¹ given: access to all org with (org²)
          
                       and nested subscriptions (as stub) if ³
 