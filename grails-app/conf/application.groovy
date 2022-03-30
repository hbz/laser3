
// -- gorm

grails.gorm.default.mapping = {
    // autowire true         // service dependency injection for domain classes
    id generator: 'identity' // postgresql sequences for primary keys
}

// -- spring security plugin

grails.plugin.springsecurity.roleHierarchy = '''
    ROLE_YODA > ROLE_ADMIN
    ROLE_ADMIN > ROLE_GLOBAL_DATA
    ROLE_GLOBAL_DATA > ROLE_USER
'''

// -- mail

grails.mail.poolSize = 20 //default 5 emails at a time, then que based system (prereq = async true)
//grails.mail.port = 30//TODO: Diese Zeile nur für Lokal nutzen!!!

// -- elasticsearch

aggr_es_indices = [
        "DocContext": "laser_doc_contexts",
        "IssueEntitlement": "laser_issue_intitlements",
        "License": "laser_licenses",
        "LicenseProperty": "laser_license_propertys",
        "Org": "laser_orgs",
        "Package": "laser_packages",
        "Platform": "laser_platforms",
        "Subscription": "laser_subscriptions",
        "SubscriptionProperty": "laser_subscription_propertys",
        "SurveyConfig": "laser_survey_configs",
        "SurveyOrg": "laser_survey_orgs",
        "Task": "laser_tasks",
        "TitleInstancePackagePlatform": "laser_tipps",
]