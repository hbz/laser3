<%@ page import="de.laser.I10nTranslation; org.springframework.context.i18n.LocaleContextHolder; de.laser.DocContext; de.laser.RefdataValue; de.laser.SurveyConfig; de.laser.storage.RDStore; java.text.SimpleDateFormat" %>
<%
    def result = []
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    String languageSuffix = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())

    hits.each { hit ->

        String period = hit.getSourceAsMap().startDate ? sdf.parse(hit.getSourceAsMap().startDate).format(message(code: 'default.date.format.notime'))  : ''
        period = hit.getSourceAsMap().endDate ? period + ' - ' + sdf.parse(hit.getSourceAsMap().endDate).format(message(code: 'default.date.format.notime'))  : ''
        period = period ? '('+period+')' : ''
        String statusString = hit.getSourceAsMap().status?.getAt('value_'+languageSuffix)

        if (hit.getSourceAsMap().rectype == 'License') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"license", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                "description": "${statusString + ' ' +period}"
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'Org') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"organisation", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": (RDStore.OT_PROVIDER.value in hit.getSourceAsMap().type?.value || RDStore.OT_AGENCY.value in hit.getSourceAsMap().type?.value ) ? "${message(code: 'spotlight.provideragency')}" : "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                "description": ""
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'Package') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"package", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": "${message(code: 'spotlight.package')}",
                "description": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}") + ': '+ hit.getSourceAsMap().titleCountCurrent}"
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'Platform') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"platform", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                "description": ""
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'Subscription') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"subscription", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                "description": "${statusString + ' ' +period}"
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'TitleInstance') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"title", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": (hit.getSourceAsMap().typTitle == 'Journal') ? "${message(code: 'spotlight.journaltitle')}" :
                                (hit.getSourceAsMap().typTitle == 'Database') ? "${message(code: 'spotlight.databasetitle')}" :
                                        (hit.getSourceAsMap().typTitle == 'EBook') ? "${message(code: 'spotlight.ebooktitle')}" : "${message(code: 'spotlight.title')}",
                "description": ""
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'BookInstance') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"title", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                    "category": "${message(code: 'spotlight.ebooktitle')}",
                    "description": ""
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'DatabaseInstance') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"title", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                    "category": "${message(code: 'spotlight.databasetitle')}",
                    "description": ""
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'JournalInstance') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"title", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                    "category":  "${message(code: 'spotlight.journaltitle')}",
                    "description": ""
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'SurveyOrg') {

            SurveyConfig surveyConfig =  SurveyConfig.get(hit.getSourceAsMap().dbId)

            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"myInstitution", action: "surveyInfos", id:"${surveyConfig.surveyInfo.id}", params:[surveyConfigID: "${surveyConfig.id}"]),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": ""
            ]
        }else if (hit.getSourceAsMap().rectype == 'SurveyConfig') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"survey", action:"show", id:"${SurveyConfig.get(hit.getSourceAsMap().dbId).surveyInfo.id}", params:"[surveyConfigID: ${hit.getSourceAsMap().dbId}]"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${statusString + ' ' +period}"
            ]
        }else if (hit.getSourceAsMap().rectype == 'Note') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"${hit.getSourceAsMap().objectClassName}", action:"notes", id:"${hit.getSourceAsMap().objectId}"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + hit.getSourceAsMap().objectClassName)}: ${hit.getSourceAsMap().objectName}"
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'Document') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"${hit.getSourceAsMap().objectClassName}", action:"documents", id:"${hit.getSourceAsMap().objectId}"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + hit.getSourceAsMap().objectClassName)}: ${hit.getSourceAsMap().objectName}, ${message(code: 'license.docs.table.type')}: ${DocContext.get(hit.getSourceAsMap().dbId)?.owner?.type?.getI10n('value')}"
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'IssueEntitlement') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"${hit.getSourceAsMap().objectClassName}", action:"index", id:"${hit.getSourceAsMap().objectId}", params:[filter: hit.getSourceAsMap().name]),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + hit.getSourceAsMap().objectClassName)}: ${hit.getSourceAsMap().objectName}"
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'SubscriptionProperty') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"${hit.getSourceAsMap().objectClassName}", action:"show", id:"${hit.getSourceAsMap().objectId}"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + hit.getSourceAsMap().objectClassName)}: ${hit.getSourceAsMap().objectName}"
            ]
        }
        /*
        else if (hit.getSourceAsMap().rectype == 'SubscriptionPrivateProperty') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"${hit.getSourceAsMap().objectClassName}", action:"show", id:"${hit.getSourceAsMap().objectId}"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + hit.getSourceAsMap().objectClassName)}: ${hit.getSourceAsMap().objectName}"
            ]
        }
        */
        else if (hit.getSourceAsMap().rectype == 'LicenseProperty') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"${hit.getSourceAsMap().objectClassName}", action:"show", id:"${hit.getSourceAsMap().objectId}"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + hit.getSourceAsMap().objectClassName)}: ${hit.getSourceAsMap().objectName}"
            ]
        }
        /*
        else if (hit.getSourceAsMap().rectype == 'LicensePrivateProperty') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"${hit.getSourceAsMap().objectClassName}", action:"show", id:"${hit.getSourceAsMap().objectId}"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + hit.getSourceAsMap().objectClassName)}: ${hit.getSourceAsMap().objectName}"
            ]
        }
        */
    }
%>
{
    "results": [
        <g:each in="${result}" var="hit" status="counter">
            <g:if test="${counter > 0}">, </g:if>
            {
                "title": "${hit.title}",
                "url":   "${hit.url}",
                "category": "${hit.category}",
                "description" : "${hit.description}"
            }
        </g:each>
    ]
}