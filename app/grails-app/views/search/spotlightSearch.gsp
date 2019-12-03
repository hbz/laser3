<%@ page import="com.k_int.kbplus.SurveyConfig; de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue; java.text.SimpleDateFormat;" %>
<%
    def result = []
    SimpleDateFormat sdf = new SimpleDateFormat(message(code: 'default.date.format.notime'))

    hits.each { hit ->

        String period = hit.getSourceAsMap().startDate ? sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().startDate))  : ''
        period = hit.getSourceAsMap().endDate ? period + ' - ' + sdf.format(new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", hit.getSourceAsMap().endDate))  : ''
        period = period ? '('+period+')' : ''
        String statusString = hit.getSourceAsMap().statusId ? RefdataValue.get(hit.getSourceAsMap().statusId).getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

        if (hit.getSourceAsMap().rectype == 'License') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"license", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                "description": "${statusString + ' ' +period}"
            ]
        }
        else if (hit.getSourceAsMap().rectype == 'Organisation') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"organisation", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": (hit.getSourceAsMap().sector == 'Publisher') ? "${message(code: 'spotlight.provideragency')}" : "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
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
        else if (hit.getSourceAsMap().rectype == 'Title') {
            result << [
                "title": "${hit.getSourceAsMap().name}",
                "url":   g.createLink(controller:"title", action:"show", id:"${hit.getSourceAsMap().dbId}"),
                "category": (hit.getSourceAsMap().typTitle == 'Journal') ? "${message(code: 'spotlight.journaltitle')}" :
                                (hit.getSourceAsMap().typTitle == 'Database') ? "${message(code: 'spotlight.databasetitle')}" :
                                        (hit.getSourceAsMap().typTitle == 'EBook') ? "${message(code: 'spotlight.ebooktitle')}" : "${message(code: 'spotlight.title')}",
                "description": ""
            ]
        }else if (hit.getSourceAsMap().rectype == 'ParticipantSurvey') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"myInstitution", action:"surveyInfos", id:"${hit.getSourceAsMap().dbId}"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": ""
            ]
        }else if (hit.getSourceAsMap().rectype == 'Survey') {
            result << [
                    "title": "${hit.getSourceAsMap().name}",
                    "url":   g.createLink(controller:"survey", action:"show", id:"${SurveyConfig.get(hit.getSourceAsMap().dbId).surveyInfo.id}", params:"[surveyConfigID: ${hit.getSourceAsMap().dbId}]"),
                    "category": "${message(code: "spotlight.${hit.getSourceAsMap().rectype.toLowerCase()}")}",
                    "description": "${statusString + ' ' +period}"
            ]
        }
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