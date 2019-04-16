0.15.4

- bugfix: false creation of mandatory properties
- reworked heredity of subscription name

0.15.3

- bugfix: missing service for subscription worksheet generation
- bugfix: corrected identifier namespaces
- fixed access for org users view

0.15.2

- changed some properties (bootstrap)
- bugfix: documents null pointer
- bugfix: subscription link formular validation
- bugfix: profile reminder settings

0.15.1

- added admin function for deleting property definitions
- bugfix: missing service
- bugfix: faulty ebook icons
- bugfix: null pointer

0.15

- added improved document management for institutions
- reworked and improved user management for institutions
- reworked views for managing consortia members
- added new finance enhancements
- reworked consortia heredity
- reworked sharing of documents
- added/reworked api endpoints
- switched to org based api access
- added user profile language setting
- bugfix: faulty property display
- refactoring legacy code
- reworked various templates and views
- lots of ui improvements
- various minor enhancements and bugfixes

0.14.8

- fixed document type null pointer

0.14.7

- bugfix: global data sync

0.14.6

- fixed query for usage data
- bugfix: global data sync

0.14.5

- bugfix: processing pending changes
- bugfix: handle nameless subscriptions

0.14.4

- bugfix: processing dashboard data
- bugfix: global data sync
- fixed false INST_ADM checks
- system event improvements

0.14.3

- fixed inheritance bug
- fixed tipp handling / gokb sync
- prevent deleting pending changes at package sync

0.14.2

- fixed regression bugs

0.14.1

- fixed frontend issues

0.14

- added sharing of documents and notes
- supporting new gokb identifiers
- importing e-books fields from gokb 
- reworked finance views
- supported preselection of issue entitlements with KBart
- added consortia view for subscriber depending licenses
- added consortia view for linking subscriber licenses
- improved management of refdatas and properties
- reworked modals for adding providers, agencies and licensors
- improved pagination
- added person positions
- added internal benchmark tools
- reworked several filters
- various ui improvements
- various minor enhancements and bugfixes


0.13.2

- fixed current subscriptions filter

0.13.1

- reworked hardcoded refdata values and categories
- reworked hardcoded property definitions
- fixed adding providers/agencies modal for subscriptions
- fixed missing member navigation at subcription finance view
- fixed start page typo
- fixed gasco page typo

0.13

- reworked and improved finance handling
- added cost item configurations for improved calculations
- added user based reminder configuration
- reworked and improved laser-api
- reworked public gasco view
- improved excel export
- improved property management
- improved namespace and identifier management
- added yoda dashboard
- improved profiler
- added new event logging
- added data consistency check
- added various paginations
- reworked dashboard tabs
- improved ui dialogs and buttons
- clearable filter dropdowns
- fixed current titles date filter
- various ui improvements
- various bugfixes
- upgrade to semantic-ui 2.4.2

0.12.3

- fixed usage data processing
- fixed presentation of property type url
- minor ui improvement
- added debug information

0.12.2

- reactivated usage and statistics
- fixed xeditable double values
- fixed xls export for consortia members
- fixed and improved finance xls export

0.12.1

- temporary deactivated usage and statistics
- modified api 1 property export

0.12

- reintroduction and improvement of finance overview page
- added cronjob for status change
- added cronjob and email reminder for dashboard due dates
- added cronjob overview 
- addes new user profile settings
- added new property type: url
- added explanations to properties 
- retrieving package list from gokb api
- show api access information at user profile
- cleaned up duplicate docstore uuids
- refactoring: use filesystem instead of database blobs
- migration to postgresql
- various markup and style changes
- various bugfixes

0.11.4

- improved performance at finance view
- forced deletion of cost items before deleting subscriptions
- fixed: excel export for subscriptions

0.11.3

- reworked consortia subscription message
- fixed: cost after tax rounding

0.11.2

- added system profiler
- api v1 changes

0.11.1

- added pagination for manage consortia view
- fixed subscription list: view and filter
- fixed providers list: missing editable permission
- fixed custom properties bootstrapping

0.11

- added functionality for grouping and management of custom properties
- added new subscription properties
- merged license: open access and license: archive into license properties 
- reworked some list views, added count of total items
- reworked filter layouts
- fixed property filter for date type
- fixed deletion of tasks
- various markup and style changes
- minor bugfixes

0.10.10

- changed v1 api

0.10.9

- bugfix: visible private contacts

0.10.8

- hotfix: gorm failed to lazily initialize a collection

0.10.7

- bugfix: faulty elastic search index during linking packages 

0.10.6

- increased session timeout to 120 minutes
- fixed: empty license custom property when starting inheritance
- fixed: preselected tab on dashboard
- fixed: null pointer at issue entitlement view 
- fixed: property filter query
- fixed: invalid data format function

0.10.5

- reworked error page
- added table indices for performance
- added user setting for default dashbord tab
- fixed: editing org role types as role_org_editor  
- fixed: table sorting and filtering via org templates
- bugfix: contacts compareTo caused null pointer
- bugfix: accessing addresses caused null pointer
- bugfix: gorm lazy loading caused null pointer

0.10.4

- added maintenance mode 
- hotfix: shown deleted licenses

0.10.3

- hotfix: gorm lazy loading causes null pointer

0.10.2

- fixed:  adding subscription members
- hotfix: faulty url at finance view

0.10.1

- bugfix: subscription renewal
- bugfix: copying licenses
- bugfix: copying subscriptions 
- fixed: sluggish query at my providers 
- fixed: my providers filter
- fixed: null pointer at manage consortia view

0.10

- added dashboard tab for upcoming dates
- added inheritance for license and subscription custom properties
- added modal dialog for object depending inheritance configuration
- improved auto acception for pending changes
- added translations for pending changes    
- added optional cost items access for consortial subscribers
- added export function for organisations, provider and consortia
- added export for mail addresses
- added functionality for custom property replacement
- added edit mode switch
- reworked various table sortings
- reworked user depending settings
- reworked page header
- reworked visible contacts 
- improved intern refdata management
- fixed: editable addresses for editors
- fixed: empty federal state dropdown
- fixed: correct person functions in addressbooks
- changed meaningless subscription names  
- increased text length for tasks
- added internal cache configuration and wrapper
- removed legacy code
- various markup and style changes
- minor bugfixes

0.9.2

- added JSON date marshaller
- added date format fallback for datepicker
- fixed: private property date type

0.9.1

- fixed: datepicker format
- fixed: list sorting for provider and organisations

0.9

- reworked dashbord, e.g. tasks
- reworked provider list view
- added list view for platforms
- improved filter and views for managing consortia members
- added bulk creation for cost items (consortia only)
- added copy function for cost items
- added copy function for subscriptions
- restricted subscription assignment for licenses (consortia only)
- added sorting for contacts
- fixed: cost items calculation
- fixed: sorting tasks table
- some markup, style and translations changes
- minor improvements and bugfixes

0.8.4

- fixed: copying properties with zero values
- fixed: faulty query for adding license members

0.8.3

- added: prev/next navigation for subscription
- fixed: pagination on package view
- fixed: hiding tipps with status 'deleted'
- fixed: javascript dropdowns
- fixed: org names and line breaks on gasco view
- modified: filter on gasco view

0.8.2

- fixed: empty user dropdown when creating tasks
- fixed: download files without filenames
- fixed: confirm dialog javascript

0.8.1

- news on landing page

0.8

- improved public gasco views
- improved subscription renewals
- reworked subscription and package linkage
- reworked cost items and cost calculation
- reworked finance functionality and views
- added functionality for copying licenses
- added new role for statistics management
- added default role for new users
- improved address and contact management
- added confirmation dialog before deleting persons and contacts
- improved inline editing, e.g. textareas
- responsible task user is now visible
- added pagination on some views
- added sortable columns for some tables
- added cache management
- fixed deletion of mandatory private properties
- security: restricted permissions check to context org
- fixed some wording issues
- lots of markup and style changes 
- minor bugfixes

0.7.5

- changed access for org editors 

0.7.4

- bugfix: matching organisations by name

0.7.3

- denied access to subscription list for licensees
- bugfix: remove deleted subscriptions from dropdown
- bugfix: faulty breadcrumbs due multiple licensees
- bugfix: faulty menu entry on license views

0.7.2

- force explicit closing of modals
- ignore cancellation date when adding child subscriptions
- bugfix: double encoding of html entities
- bugfix: deleting persons in addressbook
- bugfix: on delete cascade addresses and contacts

0.7.1

- minors changes on dashboard
- bufgix: avoid queries for person custom properties
- bugfix: missing dropdown for person function
- bugfix: ordering tasks by enddate

0.7

- added public gasco overview and details page 
- added anonymisation for object histories (DSGVO)
- added license handling for consortia and consortia members
- improved package linking for subscriptions
- switched to gokb ES index for package listing
- improved error reporting ticket system
- added list view and budget code handling 
- reworked contact, address and person views
- added filter for addressbooks
- added functionality for deleting addresses and contacts 
- added menu actions for adding tasks, documents and notes
- reworked dashboard
- reworked org role template, fixed javascript behaviour
- added translations and increased text length for property definitions
- added title field for persons
- reworked structure of license linking
- fixed javascript injection vulnerability
- increased session timeout
- removed legacy jusp and zendesk stuff
- upgraded some plugins and dependencies
- added java monitoring
- added debug information views
- lots of markup and style changes 
- lots of bugfixes

0.6.1

- bugfix: javascript for creating person modal
- disabled faulty function to copy licenses

0.6

- added usage statistics for refdata values and property definitions
- added functionality to replace refdata values
- added property filter for subscription and licenses
- added cost items filter for finance
- added page for error reporting with jira binding
- added modal dialog for editing notes
- reworked view for creating licenses
- added new org role agency with depending functionality
- reworked org role templates
- added datepicker support for inline editing (xeditable)
- bugfix: xsl export current subscriptions
- bugfix: incorrect type for identifier namespaces via frontend
- and more bugfixes ..
- variety of minor markup, stylesheet and wording changes

0.5.1

- added public overview for refdata values and properties
- minor style and markup changes
- bugfix: setting default org role target for new persons
- bugfix: now exporting entire set of subscriptions
- bugfix: creation and editing of tasks
- bugfix: removed closed tasks from dashboard
- bugfix: multiple modals for adding and editing cost items
- bugfix: finished deletion of cost items
- bugfix: editing of notes
- bugfix: improved org selection at profile

0.5

- splitted titles into derived objects: books, databases and journals
- added new roles for consortia context: subscriber_consortial and licensee_consortial
- added views and functionality for managing consortia members
- added predefined constraints for adding orgRoles to objects
- complete rework of finance views and functionality
- integrated connection to statistic server
- reworked views and templates for managing persons, contacts and addresses
- reworked tasks (views and functionality)
- added list views for providers
- improved various search forms and filter
- reworked various modals
- reworked a great number of views and ui elements
- removed legacy stylesheets
- more translations
- fixed a great number of bugs
- reworked refdata vocabulary
- upgrade to semantic-ui 2.3.1

0.4.6

- added imprint and dsvgo links

0.4.5

- reworked xml import for organisations

0.4.4

- changed GlobalDataSyncJob config 

0.4.3

- added rudimentary consortia member management
- added view for current subscription providers
- bugfix: modal dialog datepickers were broken 
- bugfix: adding subscriber to subscription was broken
- bugfix: current subscription list shows no subscriber info

0.4.2

- added prev/next subscription navigation
- improved spotlight search
- added affiliation management for inst admins
- added security access service
- secured myInstitution controller
- reworked landing page and logo

0.4.1

- reworked finance views
- added help/faq page
- bugfix: session timout causes null pointer on security closure
- bugfix: elastic search usage without org context
- bugfix: alphabetically order for query results

0.4

- removed url parameter: shortcode
- stored context organisation in session
- added cost per use statistics
- improved user management
- improved passwort management in profile
- added admin reminder service
- introduced yoda
- reworked system and user roles
- ractivated spotlight search
- reworked renewals
- reworked cost items bulk import
- reworked markup and stylesheets
- reworked templates
- more translations
- upgrade to spring security 2.0
- upgrade to elasticsearch 2.4
- upgrade to semantic-ui 2.3
- removed file viewer plugin
- bugfix: reseting default dashboard by revoking affiliations

0.3.4

- bugfix: corrupted orgPermShare access

0.3.3

- bugfix: subscription get consortia
- bugfix: redirect organisation edit
- added admin action for creating users

0.3.2

- bugfix: current subscriptions query for subscribers
- hotfix: legacy bootstrap for tooltip and popover

0.3.1

- reworked inplace edit date fields
- bugfixes and improvements for global data sync
- bugfix: unaccessible subscription form
- hotfix: title list query

0.3

- switched frontend to semantic ui 
- upgraded to jQuery 3.x
- upgraded x-editable library
- removed legacy bootstrap
- reworked complete markup
- reworked javascript
- reworked navigation and menus
- unified modal dialogs
- introduced filter panels
- reworked orgs, subscriptions, licenses and costitems
- reworked persons, contacts and addresses
- added task functionality
- added globalUID support
- added more consortial functionality
- added new custom tags
- more localization
- updated database structure
- modified elastic search config
- bugfix: added missing elasticsearch mapping

0.2.3

- bugfix: date format in finance controller

0.2.2

- bugfix: rest api

0.2.1

- bugfix: javascript

0.2

- new rest api endpoints (get only) for onix-pl and issue entitlements
- improved and refactored property definitions
- improved refdata values and categories handling
- improved consortia support
- improved subscription functionality
- exception handling for pending changes
- new field templates for org and platform attributes
- new custom tags
- datepicker localization
- more localization
- bugfix: global data sync
- bugfix: rest api file download
- upgrade to Grails 2.5.6 / Groovy 2.4.10
- use of database migration plugin
- use of local plugin repositories

0.1.1

- bugfix: locale detection for i10n
- bugfix: https://github.com/hbz/laser/issues/3

0.1  

- first release: 2017-09-21
