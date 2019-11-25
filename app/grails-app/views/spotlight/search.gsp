<%@ page import="com.k_int.kbplus.SurveyConfig" %>
<%
    def result = []
    hits.each { hit ->

        if (hit.getSource().rectype == 'License') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"license", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: 'spotlight.license')}"
            ]
        }
        else if (hit.getSource().rectype == 'Organisation') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"organisation", action:"show", id:"${hit.getSource().dbId}"),
                "category": (hit.getSource().sector == 'Publisher') ? "${message(code: 'spotlight.ProviderAgency')}" : "${message(code: 'spotlight.organisation')}"
            ]
        }
        else if (hit.getSource().rectype == 'Package') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"package", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: 'spotlight.package')}"
            ]
        }
        else if (hit.getSource().rectype == 'Platform') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"platform", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: 'spotlight.platform')}"
            ]
        }
        else if (hit.getSource().rectype == 'Subscription') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"subscription", action:"show", id:"${hit.getSource().dbId}"),
                "category": "${message(code: 'spotlight.subscription')}"
            ]
        }
        else if (hit.getSource().rectype == 'Title') {
            result << [
                "title": "${hit.getSource().name}",
                "url":   g.createLink(controller:"title", action:"show", id:"${hit.getSource().dbId}"),
                "category": (hit.getSource().typTitle == 'Journal') ? "${message(code: 'spotlight.journaltitle')}" :
                                (hit.getSource().typTitle == 'Database') ? "${message(code: 'spotlight.databasetitle')}" :
                                        (hit.getSource().typTitle == 'EBook') ? "${message(code: 'spotlight.ebooktitle')}" : "${message(code: 'spotlight.title')}"
            ]
        }else if (hit.getSource().rectype == 'ParticipantSurveys') {
            result << [
                    "title": "${hit.getSource().name}",
                    "url":   g.createLink(controller:"myInstitution", action:"surveyInfos", id:"${hit.getSource().dbId}"),
                    "category": "${message(code: 'spotlight.Survey')}"
            ]
        }else if (hit.getSource().rectype == 'Surveys') {
            result << [
                    "title": "${hit.getSource().name}",
                    "url":   g.createLink(controller:"survey", action:"show", id:"${com.k_int.kbplus.SurveyConfig.get(hit.getSource().dbId).surveyInfo.id}", params:"[surveyConfigID: ${hit.getSource().dbId}]"),
                    "category": "${message(code: 'spotlight.Survey')}"
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
                "category": "${hit.category}"
            }
        </g:each>
    ]
}