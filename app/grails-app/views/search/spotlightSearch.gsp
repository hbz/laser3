<%@ page import="com.k_int.kbplus.SurveyConfig; de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue; java.text.SimpleDateFormat;" %>
<%
    def result = []
    SimpleDateFormat sdf = new SimpleDateFormat(message(code: 'default.date.format.notime'))
    hits.each { hit ->

        String period = hit.getSource().startDate ? sdf.format(new Date().parse("yyyy-MM-dd hh:MM:SS.S", hit.getSource().startDate))  : ''
        period = hit.getSource().endDate ? period + ' - ' + sdf.format(new Date().parse("yyyy-MM-dd hh:MM:SS.S", hit.getSource().endDate))  : ''
        period = period ? '('+period+')' : ''
        String statusString = hit.getSource().statusId ? RefdataValue.get(hit.getSource().statusId).getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

        if (hit.getSource().rectype == 'License') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"license", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}")}",
                "description": "${statusString + ' ' +period}"
            ]
        }
        else if (hit.getSource().rectype == 'Organisation') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"organisation", action:"show", id:"${hit.getSource().dbId}"),
                "category": (hit.getSource().sector == 'Publisher') ? "${message(code: 'spotlight.provideragency')}" : "${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}")}",
                "description": ""
            ]
        }
        else if (hit.getSource().rectype == 'Package') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"package", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: 'spotlight.package')}",
                "description": "${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}") + ': '+ hit.getSource().titleCountCurrent}"
            ]
        }
        else if (hit.getSource().rectype == 'Platform') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"platform", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}")}",
                "description": ""
            ]
        }
        else if (hit.getSource().rectype == 'Subscription') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"subscription", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}")}",
                "description": "${statusString + ' ' +period}"
            ]
        }
        else if (hit.getSource().rectype == 'Title') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"title", action:"show", id:"${hit.getSource().dbId}"),
                "category": (hit.getSource().typTitle == 'Journal') ? "${message(code: 'spotlight.journaltitle')}" :
                                (hit.getSource().typTitle == 'Database') ? "${message(code: 'spotlight.databasetitle')}" :
                                        (hit.getSource().typTitle == 'EBook') ? "${message(code: 'spotlight.ebooktitle')}" : "${message(code: 'spotlight.title')}",
                "description": ""
            ]
        }else if (hit.getSource().rectype == 'ParticipantSurvey') {
            result << [
                    "title": "${hit.getSource().name}",
                    "url":   g.createLink(controller:"myInstitution", action:"surveyInfos", id:"${hit.getSource().dbId}"),
                    "category": "${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}")}",
                    "description": ""
            ]
        }else if (hit.getSource().rectype == 'Survey') {
            result << [
                    "title": "${hit.getSource().name}",
                    "url":   g.createLink(controller:"survey", action:"show", id:"${SurveyConfig.get(hit.getSource().dbId).surveyInfo.id}", params:"[surveyConfigID: ${hit.getSource().dbId}]"),
                    "category": "${message(code: "spotlight.${hit.getSource().rectype.toLowerCase()}")}",
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