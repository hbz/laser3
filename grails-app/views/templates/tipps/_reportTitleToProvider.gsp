<%@ page import="de.laser.storage.BeanStore; de.laser.remote.ApiSource; de.laser.Person; de.laser.Contact; grails.plugin.springsecurity.SpringSecurityUtils;" %>

<g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">

    <g:if test="${tipp?.platform?.provider}">
<%
    ApiSource apiSource  = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
    List<Person> ppList  = BeanStore.getProviderService().getContactPersonsByFunctionType( tipp.platform.provider, BeanStore.getContextService().getOrg(), true, null )
    List<Contact> ccList = ppList.contacts.flatten().findAll{ it.contentType?.value in ['E-Mail', 'Mail'] } as List<Contact>

    Map<String, String> ttm_mailStruct = [
            mailto: ccList.collect{ it.content }.sort().join(','),
            subject: 'Fehlerhafte Titel-Daten in der We:kb',
            body: """
Sehr geehrte Damen und Herren,
%0D%0A
%0D%0A
bei einem We:kb-Titel sind mir unvollständige/fehlerhafte Informationen aufgefallen:
%0D%0A
%0D%0A
<bitte ergänzen>
%0D%0A
%0D%0A
Betroffen ist das folgende Objekt:
%0D%0A
%0D%0A
${tipp.name}
%0D%0A
${apiSource.baseUrl + '/resource/show/' + tipp.gokbId}
%0D%0A
%0D%0A
Vielen Dank
"""
    ]
%>

        <ui:msg class="info" showIcon="true">
            Bei den angezeigten Informationen ist Ihnen ein Fehler aufgefallen?
            <br/>
            <a href="${'mailto:' + ttm_mailStruct['mailto'] + '?subject=' +  ttm_mailStruct['subject'] + '&body=' +  ttm_mailStruct['body']}">
                Kontaktieren Sie den Anbieter
            </a>
        </ui:msg>
    </g:if>

</g:if>