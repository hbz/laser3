println('*** using local config file ***')

localauth=          true
// no slash at end of SystemBaseURL
SystemBaseURL=      'http://localhost:8080/laser'
laserSystemId=     'LAS:eR-Demo'
dataSource.url=     'jdbc:mysql://localhost/laserDev?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8'
aggr_es_cluster=    'elasticsearch'
aggr_es_index=      'laserIndex'
onix_ghost_licence= 'Jisc Collections Model Journals Licence 2015'
publicationService.baseurl='http://knowplus.edina.ac.uk:2012/kbplus/api'
docstore=           'http://deprecated/deprecated'
ZenDeskBaseURL=     'https://projectname.zendesk.com'
ZenDeskDropboxID=   20000000
ZenDeskLoginEmail=  'Zen.Desk@Host.Name'
ZenDeskLoginPass=   'Zen.Desk.Password'
KBPlusMaster=       false
juspThreadPoolSize= 10
doDocstoreMigration=false
JuspApiUrl=         'https://www.jusp.mimas.ac.uk/'

sysusers = [
    [ 
    name:'userb',
    pass:'userb',
    display:'UserB',
    email:'read@localhost',
    roles:['ROLE_USER','INST_USER']
    ],
    [ 
    name:'userc',
    pass:'userc',
    display:'UserC',
    email:'read@localhost',
    roles:['ROLE_USER','INST_USER']
    ],
    [ 
    name:'usera',
    pass:'usera',
    display:'UserA',
    email:'read@localhost',
    roles:['ROLE_USER','INST_USER']  
    ],
    [ 
    name:'admin',
    pass:'admin',
    display:'TestAdmin',
    email:'read@localhost',
    roles:['ROLE_USER','ROLE_ADMIN','INST_ADM']
    ]
]

