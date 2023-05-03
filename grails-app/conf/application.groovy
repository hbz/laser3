
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

// -- asynchronous-mail plugin
asynchronous.mail.default.attempt.interval=300000l      // Five minutes
asynchronous.mail.default.max.attempts.count=1
asynchronous.mail.send.repeat.interval=60000l           // One minute
asynchronous.mail.expired.collector.repeat.interval=607000l
asynchronous.mail.messages.at.once=100
asynchronous.mail.send.immediately=true
asynchronous.mail.clear.after.sent=false
asynchronous.mail.disable=false
asynchronous.mail.useFlushOnSave=true
asynchronous.mail.persistence.provider='hibernate5'     // Possible values are 'hibernate', 'hibernate4', 'hibernate5', 'mongodb'
asynchronous.mail.newSessionOnImmediateSend=false
asynchronous.mail.taskPoolSize=1