println('*** INFO: using local config file ***')

// --- ---
SystemBaseURL       = 'http://localhost:8080/laser' // no slash at end
laserSystemId       = 'LAS:eR-Demo'
localauth           = true

// --- datasource ---
dataSource.url      = 'jdbc:mysql://localhost/laserDev?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8'
//dataSource.username
//dataSource.password

// --- elastic search ---
aggr_es_cluster     = 'elasticsearch'
aggr_es_index       = 'esIndex'
aggr_es_hostname    = 'localhost'

// --- documents ---
documentStorageLocation = '/opt/laser/documentStorageLocation'

// --- statistics ---
//statsApiUrl = 'http://statsServer'

// --- dashboard and emails ---
isUpdateDashboardTableInDatabase  = true
isSendEmailsForDueDatesOfAllUsers = false

// --- jira ---

// --- features ---
//feature.eBooks
//feature.issnl
feature_finance = false
feature.notifications = true

// --- others ---
//globalDataSyncJobActiv
//AdminReminderJobActiv
onix_ghost_licence  = 'Jisc Collections Model Journals Licence 2015'
publicationService.baseurl = 'http://knowplus.edina.ac.uk:2012/kbplus/api'
docstore            = 'http://deprecated/deprecated'
//hbzMaster        = true
doDocstoreMigration = false
showDebugInfo       = true

// --- example users ---
systemUsers = [
    [
        name:'aaa',
        pass:'aaa',
        display:'User A',
        email:'aaa@localhost',
        affils:['hbz Konsortium':['INST_ADM','INST_USER'], 'UB xyz':['INST_USER']],
        roles:['ROLE_ADMIN','ROLE_USER']
    ],
    [ 
        name:'bbb',
        pass:'bbb',
        display:'User B',
        email:'bbb@localhost',
        affils:['hbz Konsortium':['INST_ADM','INST_USER']],
        roles:['ROLE_USER']
    ],
    [ 
        name:'ccc',
        pass:'ccc',
        display:'User C',
        email:'ccc@localhost',
        affils:['UB xyz':['INST_USER']],
        roles:['ROLE_USER']
    ],
    [ 
        name:'admin',
        pass:'admin',
        display:'Admin',
        email:'admin@localhost',
        roles:['ROLE_USER']
    ]
]

