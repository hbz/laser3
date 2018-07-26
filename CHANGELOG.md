
0.7

- switched to gokb index for listing all packages
- improved package linking for subscriptions
- added public gasco overview and details page 
- added license handling for consortia and consortia members
- reworked structure of license linking
- added filter for addressbooks
- added functionality for deleting addresses and contacts 
- added list view and delete function for budget codes 
- reworked org role template, fixed javascript behaviour
- added translations and increased text length of property definitions
- added title field for persons
- added anonymisation for object histories (DSGVO)
- increased session timeout
- removed tmp copy of jquery-plugin
- lots of markup and style changes 
- minor bugfixes and refactorings

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
