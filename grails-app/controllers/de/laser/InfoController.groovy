package de.laser

import de.laser.addressbook.PersonRole
import de.laser.auth.User
import de.laser.ctrl.OrganisationControllerService
import de.laser.remote.Wekb
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.wekb.Platform
import de.laser.wekb.Provider
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

        switch(params.template) {
            case 'org': result = organisationControllerService.mailInfos(params)
                break
            case 'reportTitleToProvider': result = _reportTitleToProvider(params)
                break
            case 'contactStats': result = _contactStats(params)
                break
        }

        render template: '/info/' + params.template, model: result
    }


    private static Map<String, String> _getEmailSignature(TitleInstancePackagePlatform tipp) {

        ContextService cs = BeanStore.getContextService()
        Org org = cs.getOrg()
        CustomerIdentifier custId = CustomerIdentifier.findByCustomerAndPlatform(org, tipp.platform)

        return [
                'de' : """
${org.getSortname()} - ${org.getName()}
${custId ? ('Kundennummer: ' + custId.value) : ''}
""",
                'en' : """
${org.getSortname()} - ${org.getName()}
${custId ? ('Customer Identifier: ' + custId.value) : ''}
"""
        ]
    }

    private static Map<String, String> _getEmailSignature(Platform plat) {

        ContextService cs = BeanStore.getContextService()
        Org org = cs.getOrg()
        CustomerIdentifier custId = CustomerIdentifier.findByCustomerAndPlatform(org, plat)

        return [
                'de' : """
${org.getSortname()} - ${org.getName()}
${custId ? ('Kundennummer: ' + custId.value) : ''}
""",
                'en' : """
${org.getSortname()} - ${org.getName()}
${custId ? ('Customer Identifier: ' + custId.value) : ''}
"""
        ]
    }

    private static List<List> _getProviderContacts(Provider provider, List<RefdataValue> order) {
        List<List> result = [] // [Person, RefdataValue, Contact]

        order.each { rdv ->
            List match = PersonRole.executeQuery(
                    "select p, pr.functionType, c from Person p inner join p.roleLinks pr inner join p.contacts c " +
                            "where pr.provider = :provider and pr.functionType = :ft " +
                            "and ((p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true)) " +
                            "and c.contentType.value in ('Mail', 'E-Mail')",
                    [provider: provider, ft: rdv, ctx: BeanStore.getContextService().getOrg()]
            )
            //println match
            if (match) { result.addAll( match.sort{ it[2].language?.getI10n('value') } ) }
        }
        result
    }

    private static Map<String, Object> _reportTitleToProvider(GrailsParameterMap params) {
        Map<String, Object> result = [:]

        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(params.id_tipp)
        Provider provider = tipp.platform.provider

        List<List> prcList = _getProviderContacts(
                provider,
                [RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
        )
        User user = BeanStore.getContextService().getUser()

        result = [
                mailtoList : prcList,
                mailto: 'Bitte ausfüllen',
                mailcc : user.email
        ]
        result.mailSubject = [
                de: "Fehlerhafte Titel-Daten in der We:kb - ${provider.name}",
                en: "Incorrect title information in the We:kb - ${provider.name}"
        ]

        Map sig = _getEmailSignature(tipp)

        result.mailText = [
                de : sig['de'] + """
Sehr geehrte Damen und Herren,

bei einem We:kb-Titel sind mir unvollständige/fehlerhafte Informationen aufgefallen:

<bitte ergänzen>

Betroffen ist das folgende Objekt:

${tipp.name}
${Wekb.getResourceShowURL() + '/' + tipp.gokbId}

Vielen Dank
""",
                en : sig['en'] + """
Dear Sir or Madam,

I noticed incomplete/incorrect information in a We:kb title:

<please complete>

The following object is affected:

${tipp.name}
${Wekb.getResourceShowURL() + '/' + tipp.gokbId}

Thank you
"""
        ]

        result
    }

    private static Map<String, Object> _contactStats(GrailsParameterMap params) {
        Map<String, Object> result
        User user = BeanStore.getContextService().getUser()
        Platform platform = Platform.get(params.id_platform)

        List<List> prcList = _getProviderContacts(
                platform.provider,
                [RDStore.PRS_FUNC_STATS_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
        )

        result = [
                mailtoList : prcList,
                mailto: 'Bitte ausfüllen',
                mailcc : user.email
        ]
        result.mailSubject = [
                de: "Fehler bei den Nutzungsstatistiken - ${platform.provider.name}",
                en: "Error concerning usage statistics - ${platform.provider.name}"
        ]

        Map sig = _getEmailSignature(platform)

        result.mailText = [
                de : sig['de'] + """
Sehr geehrte Damen und Herren,

bei den Nutzungsstatistiken sind mir unvollständige/fehlerhafte Informationen aufgefallen:

<bitte ergänzen>

Betroffen ist die folgende Plattform:

${platform.name}
${Wekb.getResourceShowURL() + '/' + platform.gokbId}

Vielen Dank
""",
                en : sig['en'] + """
Dear Sir or Madam,

I noticed incomplete/incorrect information about usage statistics:

<please complete>

The following platform is affected:

${platform.name}
${Wekb.getResourceShowURL() + '/' + platform.gokbId}

Thank you
"""
        ]
        result
    }
}