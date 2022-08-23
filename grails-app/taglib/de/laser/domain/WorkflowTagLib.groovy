package de.laser.domain

import de.laser.DocContext
import de.laser.utils.DateUtils
import de.laser.workflow.WorkflowHelper
import de.laser.workflow.WfCondition
import de.laser.workflow.WfTask

class WorkflowTagLib {

    static namespace = 'uiWorkflow'

    def task = { attrs, body ->

        WfTask task = attrs.task as WfTask

        String tooltip = '<p><strong>' + task.title + '</strong></p>' + ( task.description ? '<p>' + task.description + '</p>' : '')

        if (task.condition) {
            WfCondition cnd = task.condition
            tooltip = tooltip + '<div class="ui divider"></div>'

            if (cnd.type != 0) {
                List<String> fields = []
                cnd.getFields().each { f ->
                    String fstr = (cnd.getProperty( f + '_title') ?: message(code:'workflow.field.noTitle.label'))

                    if (f.startsWith('checkbox')) {
                        fstr = ( cnd.getProperty( f ) == true ? '<i class="ui icon check square outline"></i> ' : '<i class="ui icon square outline la-light-grey"></i> ' ) + fstr
                    }
                    else if (f.startsWith('date')) {
                        fstr = ( cnd.getProperty( f )  ?
                                '<i class="icon calendar alternate outline"></i> ' + fstr + ': <strong>' + DateUtils.getLocalizedSDF_noTime().format(cnd.getProperty(f)) + '</strong>' :
                                '<i class="icon calendar alternate outline la-light-grey"></i> ' + fstr  )
                    }
                    else if (f.startsWith('file')) {
                        DocContext docctx = cnd.getProperty( f ) as DocContext
                        String docStr = message(code:'template.documents.missing')

                        if (docctx) {
                            if (docctx.owner?.title) {
                                docStr = docctx.owner.title
                            }
                            else if (docctx.owner?.filename) {
                                docStr = docctx.owner.filename
                            }
                            fstr = '<i class="icon file"></i> ' + fstr + ': <strong>' + docStr + '</strong>'
                        }
                        else {
                            fstr = '<i class="icon file la-light-grey"></i> ' + fstr
                        }
                    }
                    fields.add( fstr )
                }
                tooltip = tooltip + '<p>' + fields.join('<br/>') + '</p>'
            }
            else {
                tooltip = tooltip + '<p><em>' + task.condition.getTypeAsRefdataValue().getI10n('value') + '</em></p>'
            }
        }

        String cssColor = WorkflowHelper.getCssColorByStatus(task.status)
        String cssIcon = WorkflowHelper.getCssIconByTaskPriority( task.priority )

        out << '<span class="la-popup-tooltip la-delay" data-position="top center" data-html="' + tooltip.encodeAsHTML() + '">'
        out <<   '<a href="' + g.createLink( controller:'ajaxHtml', action:'useWfXModal', params:attrs.params ) + '" class="ui icon button wfModalLink">'
        out <<     '<i class="ui icon ' + cssColor + ' ' + cssIcon + '" style="margin-left:0;"></i>'
        out <<   '</a>'
        out << '</span>'
    }

    def taskConditionField = { attrs, body ->

        String field = attrs.field
        WfCondition condition = attrs.condition as WfCondition

        boolean isListItem = attrs.isListItem == 'true'

        if (field && condition) {
            String pTitle = (condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label'))

            if (field.startsWith('checkbox')) {
                if (condition.getProperty(field + '_isTrigger')) {
                    pTitle = '<u>' + pTitle + '</u>'
                }
                if (condition.getProperty(field) == true) {
                    out << (isListItem ? '<div class="item positive">' : '') + '<i class="icon check square outline"></i>'
                }
                else {
                    out << (isListItem ? '<div class="item negative"><i class="icon square outline"></i>' : '<i class="icon square outline la-light-grey"></i>')
                }
                out << (isListItem ? '<div class="middle aligned content">' : '') + pTitle + (isListItem ? '</div></div>' : '')
            }
            else if (field.startsWith('date')) {
                if (condition.getProperty(field)) {
                    out << (isListItem ? '<div class="item positive">' : '') + '<i class="icon calendar alternate outline"></i>'
                    out << (isListItem ? '<div class="middle aligned content">' : '')
                    out << pTitle + ': ' + DateUtils.getLocalizedSDF_noTime().format(condition.getProperty(field))
                }
                else {
                    out << (isListItem ? '<div class="item negative"><i class="icon calendar alternate outline"></i>' : '<i class="icon calendar alternate outline la-light-grey"></i>')
                    out << (isListItem ? '<div class="content">' : '') + pTitle
                }
                out << (isListItem ? '</div></div>' : '')
            }
            else if (field.startsWith('file')) {
                DocContext docctx = condition.getProperty(field) as DocContext
                if (docctx) {
                    String linkBody = message(code:'template.documents.missing')
                    if (docctx.owner?.title) {
                        linkBody = docctx.owner.title
                    }
                    else if (docctx.owner?.filename) {
                        linkBody = docctx.owner.filename
                    }
                    out << (isListItem ? '<div class="item positive">' : '') + '<i class="icon file"></i>'
                    out << (isListItem ? '<div class="middle aligned content">' : '')
                    out << pTitle + ': ' + g.link( [controller: 'docstore', id: docctx.owner.uuid], linkBody + ' (' + docctx.owner?.type?.getI10n('value') + ')')
                }
                else {
                    out << (isListItem ? '<div class="item negative"><i class="icon file"></i>' : '<i class="icon file la-light-grey"></i>')
                    out << (isListItem ? '<div class="content">' : '') + pTitle
                }
                out << (isListItem ? '</div></div>' : '')
            }
            else {
                out << (isListItem ? '<div class="item"><div class="content">' : '')
                out << '[workflow:conditionField ' + field.toString() + '_' + condition.toString() + ']'
                out << (isListItem ? '</div></div>' : '')
            }
        }
        else {
            out << (isListItem ? '<div class="item"><div class="content">' : '') + '[workflow:conditionField]' + (isListItem ? '</div></div>' : '')
        }
    }
}
