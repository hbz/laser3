package de.laser.domain

import de.laser.ContextService
import de.laser.DocContext
import de.laser.RefdataValue
import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.workflow.WfWorkflow
import de.laser.workflow.WorkflowHelper
import de.laser.workflow.WfCondition
import de.laser.workflow.WfTask
import de.laser.workflow.WfChecklist
import de.laser.workflow.WfCheckpoint

class WorkflowTagLib {

    ContextService contextService

    static namespace = 'uiWorkflow'

    // --- workflows AND checklists ---

    def statusIcon = { attrs, body ->

        WfWorkflow workflow
        WfChecklist checklist

        Map<String, Object> info = [:]
        String iconSize = attrs.get('size') ?: 'large'

        if (attrs.workflow) {
            workflow = attrs.workflow as WfWorkflow
            info = workflow.getInfo()
            out << '<i class="icon ' + iconSize + ' ' + WorkflowHelper.getCssIconAndColorByStatus(workflow.status) + '"></i>'
        }
        else if (attrs.checklist) {
            checklist = attrs.checklist as WfChecklist
            info = checklist.getInfo()
            out << '<i class="icon ' + iconSize + ' ' + WorkflowHelper.getCssIconAndColorByStatus(info.status as RefdataValue) + '"></i>'
        }

        if (workflow) {
            if (workflow.status == RDStore.WF_WORKFLOW_STATUS_DONE) {
                if (info.tasksImportantBlocking) {
                    out << '<span data-position="top left" class="la-popup-tooltip la-delay" data-content="' + message(code:'workflow.blockingTasks.important') + '">'
                    out << '<i class="icon ' + iconSize + ' red frown outline"></i>'
                    out << '</span>'
                }
                else if (info.tasksNormalBlocking) {
                    out << '<span data-position="top left" class="la-popup-tooltip la-delay" data-content="' + message(code:'workflow.blockingTasks.normal') + '">'
                    out << '<i class="icon ' + iconSize + ' yellow frown outline"></i>'
                    out << '</span>'
                }
            }
        }
        else if (checklist) {
        }
    }

    // --- workflows ---

    def usageIconLinkButton = { attrs, body ->

        WfWorkflow workflow = attrs.workflow as WfWorkflow
        User user           = contextService.getUser()
        String link         = g.createLink(controller: 'ajaxHtml', action: 'useWfXModal', params: attrs.params)

        if (workflow.user) {
//            if (workflow.user.id == user.id) {
//                out <<  '<a href="' + link + '" class="ui icon button blue la-modern-button wfModalLink"><i class="icon user"></i></a>'
//            }
//            else {
                out <<  '<a href="' + link + '" class="ui icon button blue la-modern-button la-popup-tooltip la-delay wfModalLink" '
                out <<          'data-position="top right" data-content="' + message(code:'workflow.user.currentUser', args: [workflow.user.displayName]) + '">'
                if (workflow.user.id == user.id) {
                    out <<      '<i class="icon user"></i>'
                } else {
                    out <<      '<i class="icon user outline"></i>'
                }
                out <<  '</a>'
//            }
        }
        else {
            out <<  '<a href="' + link + '" class="ui icon button blue la-modern-button la-popup-tooltip la-delay wfModalLink" '
            out <<          'data-position="top right" data-content="' + message(code:'workflow.user.noCurrentUser') + '">'
            out <<      '<i class="icon users"></i>'
            out <<  '</a>'
        }
    }

    // --- workflows ---

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
                            if (docctx.status == RDStore.DOC_CTX_STATUS_DELETED) {
                                docStr = '<span class="sc_red">' + message(code:'workflow.condition.file.deleted') + '</span>'
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

    // --- workflows ---

    def taskConditionField = { attrs, body ->

        String field = attrs.field
        WfCondition condition = attrs.condition as WfCondition

        boolean isListItem = attrs.isListItem == 'true'

        if (field && condition) {
            String pTitle = (condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label'))
            String posMark = '<div class="item positive"><i class="icon check"></i> '
            String negMark = '<div class="item negative"><i class="icon caret right"></i> '

            if (field.startsWith('checkbox')) {
                if (condition.getProperty(field + '_isTrigger')) {
                    pTitle = '<u>' + pTitle + '</u>'
                }
                if (condition.getProperty(field) == true) {
                    out << (isListItem ? posMark : '') + '<i class="icon check square outline"></i>'
                }
                else {
                    out << (isListItem ? negMark + '<i class="icon square outline"></i>' : '<i class="icon square outline la-light-grey"></i>')
                }
                out << (isListItem ? '<div class="middle aligned content">' : '') + pTitle + (isListItem ? '</div></div>' : '')
            }
            else if (field.startsWith('date')) {
                if (condition.getProperty(field)) {
                    out << (isListItem ? posMark : '') + '<i class="icon calendar alternate outline"></i>'
                    out << (isListItem ? '<div class="middle aligned content">' : '')
                    out << pTitle + ': ' + DateUtils.getLocalizedSDF_noTime().format(condition.getProperty(field))
                }
                else {
                    out << (isListItem ? negMark + '<i class="icon calendar alternate outline"></i>' : '<i class="icon calendar alternate outline la-light-grey"></i>')
                    out << (isListItem ? '<div class="content">' : '') + pTitle
                }
                out << (isListItem ? '</div></div>' : '')
            }
            else if (field.startsWith('file')) {
                DocContext docctx = condition.getProperty(field) as DocContext
                if (docctx) {
                    String linkBody = message(code:'template.documents.missing')
                    if (docctx.owner.title) {
                        linkBody = docctx.owner.title
                    }
                    else if (docctx.owner.filename) {
                        linkBody = docctx.owner.filename
                    }
                    out << (isListItem ? posMark : '') + '<i class="icon file"></i>'
                    out << (isListItem ? '<div class="middle aligned content">' : '')

                    if (docctx.status == RDStore.DOC_CTX_STATUS_DELETED) {
                        out << pTitle + ': [ <span class="sc_red">' + message(code:'workflow.condition.file.deleted') + '</span> ]'
                    }
                    else {
                        out << pTitle + ': <a href="#documentPreview" data-documentKey="' + docctx.owner.uuid + ':' + docctx.id + '">' + linkBody + '</a>'
                        if (docctx.getDocType()) {
                            out << ' (' + docctx.getDocType().getI10n('value') + ')'
                        }
                        if (docctx.getDocConfid()) {
                            out << ' (' + docctx.getDocConfid().getI10n('value') + ')'
                        }
                        out << ' &nbsp; [ <a href="docstore/index/' + docctx.owner.uuid + '" target="_blank">' + message(code: 'default.download.label') + '</a> ]'

//                    out << pTitle + ': ' + g.link( [controller: 'docstore', id: docctx.owner.uuid], linkBody + ' (' + docctx.owner?.type?.getI10n('value') + ')')
//                    out << ' &nbsp; <a href="docstore/index/' + docctx.owner.uuid + '" class="ui icon small blue button la-modern-button" target="_blank"><i class="download icon"></i></a>'
                    }
                }
                else {
                    out << (isListItem ? negMark + '<i class="icon file"></i>' : '<i class="icon file la-light-grey"></i>')
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

    // --- checklists ---

    def checkpoint = { attrs, body ->

        WfCheckpoint cpoint = attrs.checkpoint as WfCheckpoint

        String tooltip = '<p><strong>' + cpoint.title + '</strong></p>' + ( cpoint.comment ? '<p>' + cpoint.comment + '</p>' : '')
        tooltip = tooltip + '<div class="ui divider"></div>'

        List<String> fields = []

        fields.add( (cpoint.date ?
                '<i class="icon calendar alternate outline"></i> ' + message(code:'workflow.checkpoint.date') + ': <strong>' + DateUtils.getLocalizedSDF_noTime().format(cpoint.date) + '</strong>' :
                '<i class="icon calendar alternate outline la-light-grey"></i> ' + message(code:'workflow.checkpoint.noDate')
        ))
        fields.add( (cpoint.done == true ?
                '<i class="ui icon check square outline"></i> ' + message(code:'workflow.checkpoint.done') :
                '<i class="ui icon square outline la-light-grey"></i> ' + message(code:'workflow.checkpoint.open')
        ))

        tooltip = tooltip + '<p>' + fields.join('<br/>') + '</p>'

        String cssColor = WorkflowHelper.getCssColorByStatus( cpoint.done ? RDStore.WF_TASK_STATUS_DONE : RDStore.WF_TASK_STATUS_OPEN )
//        String cssIcon = WorkflowHelper.getCssIconByTaskPriority( RDStore.WF_TASK_PRIORITY_NORMAL )
        String cssIcon = cpoint.done ? 'check' : 'circle'

        out << '<span class="la-popup-tooltip la-delay" data-position="top center" data-html="' + tooltip.encodeAsHTML() + '">'
        out <<   '<a href="' + g.createLink( controller:'ajaxHtml', action:'workflowModal', params:attrs.params ) + '" class="ui icon button wfModalLink">'
        out <<     '<i class="ui icon ' + cssColor + ' ' + cssIcon + '" style="margin-left:0;"></i>'
        out <<   '</a>'
        out << '</span>'
    }
}
