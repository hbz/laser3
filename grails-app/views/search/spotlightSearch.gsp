<%@ page import="de.laser.utils.LocaleUtils; de.laser.utils.DateUtils; de.laser.survey.SurveyConfig; de.laser.I10nTranslation; de.laser.DocContext; de.laser.RefdataValue; de.laser.storage.RDStore; java.text.SimpleDateFormat" %>
<%
    List result = []
    SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMddTHHmmssZ()
    SimpleDateFormat sdfNoTime = DateUtils.getLocalizedSDF_noTime()
    String languageSuffix = LocaleUtils.getCurrentLang()

    hits.each { hit ->
        Map object = hit.getSourceAsMap()
        Map objMap = [
                "title": "${object.name}",
                "ocn": "${object.objectClassName}"
        ]

        String period = object.startDate ? sdfNoTime.format( sdf.parse(object.startDate) )  : ''
        period = object.endDate ? period + ' - ' + sdfNoTime.format( sdf.parse(object.endDate) )  : ''
        period = period ? '('+period+')' : ''
        String statusString = object.status?.getAt('value_'+languageSuffix)

        if (object.rectype == 'License') {
            result << objMap + [
                "url":   g.createLink(controller:"license", action:"show", id:"${object.dbId}"),
                "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                "description": "${statusString + ' ' +period}"
            ]
        }
        else if (object.rectype == 'Org') {
            result << objMap + [
                "url":   g.createLink(controller:"organisation", action:"show", id:"${object.dbId}"),
                "category": (RDStore.OT_PROVIDER.value in object.type?.value || RDStore.OT_AGENCY.value in object.type?.value ) ? "${message(code: 'spotlight.provideragency')}" : "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                "description": ""
            ]
        }
        else if (object.rectype == 'Package') {
            result << objMap + [
                "url":   g.createLink(controller:"package", action:"show", id:"${object.dbId}"),
                "category": "${message(code: 'spotlight.package')}",
                "description": "${message(code: "spotlight.${object.rectype.toLowerCase()}") + ': '+ object.titleCountCurrent}"
            ]
        }
        else if (object.rectype == 'Platform') {
            result << objMap + [
                "url":   g.createLink(controller:"platform", action:"show", id:"${object.dbId}"),
                "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                "description": ""
            ]
        }
        else if (object.rectype == 'Subscription') {
            result << objMap + [
                "url":   g.createLink(controller:"subscription", action:"show", id:"${object.dbId}"),
                "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                "description": "${statusString + ' ' +period}"
            ]
        }
        else if (object.rectype == 'SurveyOrg') {

            SurveyConfig surveyConfig =  SurveyConfig.get(object.dbId)

            result << objMap + [
                    "url":   g.createLink(controller:"myInstitution", action: "surveyInfos", id:"${surveyConfig.surveyInfo.id}", params:[surveyConfigID: "${surveyConfig.id}"]),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": ""
            ]
        }else if (object.rectype == 'SurveyConfig') {
            result << objMap + [
                    "url":   g.createLink(controller:"survey", action:"show", id:"${SurveyConfig.get(object.dbId).surveyInfo.id}", params:"[surveyConfigID: ${object.dbId}]"),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${statusString + ' ' +period}"
            ]
        }else if (object.rectype == 'Note') {
            result << objMap + [
                    "url":   g.createLink(controller:"${object.objectClassName}", action:"notes", id:"${object.objectId}"),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + object.objectClassName)}: ${object.objectName}"
            ]
        }
        else if (object.rectype == 'Document') {
            result << objMap + [
                    "url":   g.createLink(controller:"${object.objectClassName}", action:"documents", id:"${object.objectId}"),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + object.objectClassName)}: ${object.objectName}, ${message(code: 'license.docs.table.type')}: ${DocContext.get(object.dbId)?.owner?.type?.getI10n('value')}"
            ]
        }
        else if (object.rectype == 'IssueEntitlement') {
            result << objMap + [
                    "url":   g.createLink(controller:"${object.objectClassName}", action:"index", id:"${object.objectId}", params:[filter: object.name]),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + object.objectClassName)}: ${object.objectName}"
            ]
        }
        else if (object.rectype == 'SubscriptionProperty') {
            result << objMap + [
                    "url":   g.createLink(controller:"${object.objectClassName}", action:"show", id:"${object.objectId}"),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + object.objectClassName)}: ${object.objectName}"
            ]
        }
        /*
        else if (object.rectype == 'SubscriptionPrivateProperty') {
            result << objMap + [
                    "url":   g.createLink(controller:"${object.objectClassName}", action:"show", id:"${object.objectId}"),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + object.objectClassName)}: ${object.objectName}"
            ]
        }
        */
        else if (object.rectype == 'LicenseProperty') {
            result << objMap + [
                    "url":   g.createLink(controller:"${object.objectClassName}", action:"show", id:"${object.objectId}"),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + object.objectClassName)}: ${object.objectName}"
            ]
        }
        /*
        else if (object.rectype == 'LicensePrivateProperty') {
            result << objMap + [
                    "url":   g.createLink(controller:"${object.objectClassName}", action:"show", id:"${object.objectId}"),
                    "category": "${message(code: "spotlight.${object.rectype.toLowerCase()}")}",
                    "description": "${message(code: 'search.object.' + object.objectClassName)}: ${object.objectName}"
            ]
        }
        */
    }
%>
{
    "results": [
        <g:each in="${result}" var="obj" status="counter">
            <g:if test="${counter > 0}">, </g:if>
            <%
                // workaround: hibernate proxies
                if (obj.ocn) {
                    String subst = obj.ocn.split('\\$hibernateproxy\\$')[0]

                    obj.url = obj.url.replace( URLEncoder.encode(obj.ocn), subst )
                    obj.description = obj.description.replace( 'search.object.' + obj.ocn + ': ', '')
                }
            %>
            {
                "title": "${obj.title}",
                "url":   "${obj.url}",
                "category": "${obj.category}",
                "description" : "${obj.description}"
            }
        </g:each>
    ]
}