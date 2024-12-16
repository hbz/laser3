package de.laser

import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.auth.User
import de.laser.ctrl.OrganisationControllerService
import de.laser.remote.Wekb
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.wekb.TitleInstancePackagePlatform
import grails.plugin.springsecurity.annotation.Secured
import grails.web.servlet.mvc.GrailsParameterMap

@Secured(['IS_AUTHENTICATED_FULLY'])
class InfoController {

    ContextService contextService
    OrganisationControllerService organisationControllerService

    @Secured(['ROLE_USER'])
    def flyout() {
        log.debug('InfoController.infoFlyout ' + params)
        Map<String, Object> result = [:]

        if (params.template == 'org') {

            result = organisationControllerService.mailInfos(null, params)
        }
        else if (params.template == 'reportTitleToProvider') {

            result = _reportTitleToProvider(params)
        }

        render template: '/info/' + params.template, model: result
    }


    private static Map<String, String> _getEmailSignature(TitleInstancePackagePlatform tipp) {

        ContextService cs = BeanStore.getContextService()
        User user = cs.getUser()
        Org org   = cs.getOrg()
        CustomerIdentifier custId = CustomerIdentifier.findByCustomerAndPlatform(org, tipp.platform)

        return [
                'de' : """
${user.getDisplayName()}
${org.getSortname()} - ${org.getName()}
${custId ? ('Kundennummer: ' + custId.value) : ''}
""",
                'en' : """
${user.getDisplayName()}
${org.getSortname()} - ${org.getName()}
${custId ? ('Customer Identifier: ' + custId.value) : ''}
"""
        ]
    }

    private static Map<String, Object> _reportTitleToProvider(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(params.id_tipp)

        List<Person> ppList = BeanStore.getProviderService().getContactPersonsByFunctionType(tipp.platform.provider, false, RDStore.PRS_FUNC_SERVICE_SUPPORT)

        if (!ppList) {
            ppList = BeanStore.getProviderService().getContactPersonsByFunctionType(tipp.platform.provider, false, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)
        }
        List<Contact> ccList = ppList.contacts.flatten().findAll { it.contentType?.value in ['E-Mail', 'Mail'] } as List<Contact>

        User user = BeanStore.getContextService().getUser()

        result = [
                mailto : ccList.collect { it.content }.sort().join(','),
                mailcc : user.email
        ]
        result.mailSubject = [
                de: 'Fehlerhafte Titel-Daten in der We:kb',
                en: 'Incorrect title information in the We:kb'
        ]

        Map sig = _getEmailSignature(tipp)

        result.mailText = [
                de : """
Sehr geehrte Damen und Herren,

bei einem We:kb-Titel sind mir unvollständige/fehlerhafte Informationen aufgefallen:

<bitte ergänzen>

Betroffen ist das folgende Objekt:

${tipp.name}
${Wekb.getResourceShowURL() + '/' + tipp.gokbId}

Vielen Dank,
""" + sig['de'],
                en : """
Dear Sir or Madam,

I noticed incomplete/incorrect information in a We:kb title:

<please complete>

The following object is affected:

${tipp.name}
${Wekb.getResourceShowURL() + '/' + tipp.gokbId}

Thank you,
""" + sig['en']
        ]

        result
    }
}
