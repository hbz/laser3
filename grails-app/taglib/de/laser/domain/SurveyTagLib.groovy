package de.laser.domain

import de.laser.ContextService
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.RefdataValue
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyOrg

class SurveyTagLib {

    ContextService contextService
    LinksGenerationService linksGenerationService

    static namespace = 'uiSurvey'

    def status = { attrs, body ->
        def object = attrs.object

        String statusType = object.status?.owner?.desc
        String color = ''
        String tooltip = message(code: 'subscription.details.statusNotSet')
        String startDate = ''
        String endDate = ''
        String dash = ''

        if (object.status) {
            tooltip = object.status.getI10n('value')
            switch (object.status) {
                case RefdataValue.getByValueAndCategory('Survey started', statusType): color = 'la-status-active'
                    break
                case RefdataValue.getByValueAndCategory('Survey completed', statusType): color = 'la-status-inactive'
                    break
                case RefdataValue.getByValueAndCategory('Ready', statusType): color = 'la-status-else'
                    break
                case RefdataValue.getByValueAndCategory('In Evaluation', statusType): color = 'la-status-else'
                    break
                case RefdataValue.getByValueAndCategory('Completed', statusType): color = 'la-status-else'
                    break
                case RefdataValue.getByValueAndCategory('In Processing', statusType): color = 'la-status-else'
                    break

                default: color = 'la-status-else'
                    break
            }
        }
        out << '<div class="ui large label la-annual-rings">'

        if (object.startDate) {
            startDate = g.formatDate(date: object.startDate, format: message(code: 'default.date.format.notime'))
        }
        if (object.endDate) {
            dash = '–'
            endDate = g.formatDate(date: object.endDate, format: message(code: 'default.date.format.notime'))
        }
        out << '<i aria-hidden="true" class="icon"></i>'
        out << '<span class="la-annual-rings-text">' + startDate + dash + endDate + '</span>'

        out << "<a class='ui ${color} circular tiny label la-popup-tooltip la-delay'  data-variation='tiny' data-content='Status: ${tooltip}'>"
        out << '       &nbsp;'
        out << '</a>'
        out << '<i aria-hidden="true" class="icon"></i>'

        out << '</div>'

        if(actionName != 'show'){
            out << '<div class="ui label survey-' + object.type.value + '">'
            out << object.type.getI10n('value')
            out << '</div>'

/*            if(object.isMandatory) {
                out << "<span class='la-long-tooltip la-popup-tooltip la-delay' data-position='right center' data-content='${message(code: "surveyInfo.isMandatory.label.info2")}'>"
                out << "<i class='yellow small icon exclamation triangle'></i>"
                out << "</span>"
            }*/

        }
    }

    def statusWithRings = { attrs, body ->
        def object = attrs.object
        SurveyConfig surveyConfig = attrs.surveyConfig as SurveyConfig

        String statusType = object.status?.owner?.desc
        String color = ''
        String tooltip = message(code: 'subscription.details.statusNotSet')
        String startDate = ''
        String endDate = ''
        String dash = ''
        String prev
        String next

        if(surveyConfig.subSurveyUseForTransfer){
            LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(surveyConfig.subscription)
            prev = links.prevLink ? (SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(links.prevLink[0], true) ?: null) : null
            next = links.nextLink ? (SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(links.nextLink[0], true) ?: null) : null
        }

        if (object.status) {
            tooltip = object.status.getI10n('value')
            switch (object.status) {
                case RefdataValue.getByValueAndCategory('Survey started', statusType): color = 'la-status-active'
                    break
                case RefdataValue.getByValueAndCategory('Survey completed', statusType): color = 'la-status-inactive'
                    break
                case RefdataValue.getByValueAndCategory('Ready', statusType): color = 'la-status-else'
                    break
                case RefdataValue.getByValueAndCategory('In Evaluation', statusType): color = 'la-status-else'
                    break
                case RefdataValue.getByValueAndCategory('Completed', statusType): color = 'la-status-else'
                    break
                case RefdataValue.getByValueAndCategory('In Processing', statusType): color = 'la-status-else'
                    break

                default: color = 'la-status-else'
                    break
            }
        }
        out << "<div class='ui large label la-annual-rings'>"

        if (object.startDate) {
            startDate = g.formatDate(date: object.startDate, format: message(code: 'default.date.format.notime'))
        }
        if (object.endDate) {
            dash = '–'
            endDate = g.formatDate(date: object.endDate, format: message(code: 'default.date.format.notime'))
        }

        if (prev) {
            out << g.link('<i class="arrow left icon"></i>', controller: attrs.controller, action: attrs.action, class: "item", id: prev.surveyInfo.id, params: [surveyConfigID: prev.id])
        } else {
            out << '<i aria-hidden="true" class="arrow left icon disabled"></i>'
        }

        out << '<span class="la-annual-rings-text">' + startDate + dash + endDate + '</span>'

        out << "<a class='ui ${color} circular tiny label la-popup-tooltip la-delay'  data-variation='tiny' data-content='Status: ${tooltip}'>"
        out << '       &nbsp;'
        out << '</a>'

        if (next) {
            out << g.link('<i class="arrow right icon"></i>', controller: attrs.controller, action: attrs.action, class: "item", id: next.surveyInfo.id, params: [surveyConfigID: next.id])
        } else {
            out << '<i aria-hidden="true" class="arrow right icon disabled"></i>'
        }

        out << '</div>'

        if(actionName != 'show'){
            out << '<div class="ui label survey-' + object.type.value + '">'
            out << object.type.getI10n('value')
            out << '</div>'

/*            if(object.isMandatory) {
                out << "<span class='la-long-tooltip la-popup-tooltip la-delay' data-position='right center' data-content='${message(code: "surveyInfo.isMandatory.label.info2")}'>"
                out << "<i class='yellow small icon exclamation triangle'></i>"
                out << "</span>"
            }*/

        }
    }

    def finishIcon = { attrs, body ->
        SurveyConfig surveyConfig = attrs.surveyConfig as SurveyConfig
        Org participant = attrs.participant as Org
        def surveyOwnerView = attrs.surveyOwnerView

            def finishDate = SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant).finishDate

            if (finishDate) {
                if (surveyOwnerView) {
                    out << '<span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right" data-variation="tiny"'
                    out << ' data-content="' + message(code: "surveyResult.finish.info.consortia") + '">'
                    out <<   ' <i class="check big green icon"></i></span>'
                } else {
                    out << '<span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right" data-variation="tiny"'
                    out << ' data-content="' + message(code: "surveyResult.finish.info") + '">'
                    out <<   ' <i class="check big green icon"></i></span>'
                }
            } else {
                if (surveyOwnerView) {
                    out << '<span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right" data-variation="tiny"'
                    out << ' data-content="' + message(code: "surveyResult.noFinish.info.consortia") + '">'
                    out <<   ' <i class="circle red icon"></i></span>'
                } else {
                    out << '<span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right" data-variation="tiny"'
                    out << ' data-content="' + message(code: "surveyResult.noFinish.info") + '">'
                    out <<   ' <i class="circle red icon"></i></span>'
                }
            }
    }

    def finishDate = { attrs, body ->
        SurveyConfig surveyConfig = attrs.surveyConfig as SurveyConfig
        Org participant = attrs.participant as Org

        def finishDate = SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant).finishDate
        if (finishDate) {
            out << g.formatDate(format: message(code: "default.date.format.notime"), date: finishDate)
        }

    }
}
