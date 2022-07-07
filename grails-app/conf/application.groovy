
// -- gorm

grails.gorm.default.mapping = {
    // autowire true         // service dependency injection for domain classes
    id generator: 'identity' // postgresql sequences for primary keys
}

// -- spring security plugin

grails.plugin.springsecurity.roleHierarchy = '''
    ROLE_YODA > ROLE_ADMIN
    ROLE_ADMIN > ROLE_USER
'''
