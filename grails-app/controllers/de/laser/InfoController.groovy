package de.laser

import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.ctrl.OrganisationControllerService
import de.laser.remote.ApiSource
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.wekb.TitleInstancePackagePlatform
import grails.plugin.springsecurity.annotation.Secured

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

            TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(params.id_tipp)

            ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

            List<Person> ppList = BeanStore.getProviderService().getContactPersonsByFunctionType(
                    tipp.platform.provider, BeanStore.getContextService().getOrg(), false, RDStore.PRS_FUNC_SERVICE_SUPPORT
            )
            if (!ppList) {
                ppList = BeanStore.getProviderService().getContactPersonsByFunctionType(
                        tipp.platform.provider, BeanStore.getContextService().getOrg(), false, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)
            }
            List<Contact> ccList = ppList.contacts.flatten().findAll { it.contentType?.value in ['E-Mail', 'Mail'] } as List<Contact>

            result = [
                    mailto : ccList.collect { it.content }.sort().join(','),
                    mailcc : BeanStore.getContextService().getUser().email
            ]
            result.mailSubject = [
                    de: 'Fehlerhafte Titel-Daten in der We:kb',
                    en: 'Incorrect title information in the We:kb'
            ]
            result.mailText = [
                    de : """
Sehr geehrte Damen und Herren,

bei einem We:kb-Titel sind mir unvollständige/fehlerhafte Informationen aufgefallen:

<bitte ergänzen>

Betroffen ist das folgende Objekt:

${tipp.name}
${apiSource.baseUrl + '/resource/show/' + tipp.gokbId}

Vielen Dank
""",
                    en : """
Dear Sir or Madam,

I noticed incomplete/incorrect information in a We:kb title:

<please complete>

The following object is affected:

${tipp.name}
${apiSource.baseUrl + '/resource/show/' + tipp.gokbId}

Thank you
"""
            ]
        }

        render template: '/info/' + params.template, model: result
    }
}
