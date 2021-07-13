<%@ page import="de.laser.RefdataValue; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : DEMO - Workflows</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb test="DEMO - Workflows" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">DEMO - Workflows</h1>

<!-- -->

<h2 class="ui header">Workflows</h2>

<ul>
    <g:each in="${WfSequencePrototype.executeQuery('select wfsp from WfSequencePrototype wfsp order by id')}" var="wfsp">
        <li><g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: wfsp.id]}">(${wfsp.id}) ${wfsp.title}</g:link></li>
    </g:each>
</ul>

<g:each in="${WfSequencePrototype.executeQuery('select wfsp from WfSequencePrototype wfsp where wfsp.type = :type order by id', [type: RefdataValue.getByValueAndCategory('ready', 'workflow.sequence.type')])}" var="wfsp">
    <p><strong>
            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: wfsp.id]}">(${wfsp.id}) ${wfsp.title}</g:link>
    </strong></p>

    <div class="ui mini steps">
        <g:set var="tasks" value="${wfsp.getStructure()}" />
        <g:each in="${tasks}" var="wftp">
            <div class="step">
                <div class="content">
                    <g:if test="${! wftp.child}">
                        <div class="title">
                            <i class="ui icon exclamation triangle orange"></i>
                            <i class="ui icon hand point right"></i>
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">${wftp.title}</g:link>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="description">
                        <g:set var="children" value="${wftp.child.getStructure()}" />
                            <g:if test="${children}">
                                <div class="ui mini vertical steps">
                                    <g:each in="${children}" var="ch" status="ci">
                                        <g:if test="${ci == 0}">
                                            <div class="step">
                                                <div class="content">
                                                    <div class="title">
                                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">${wftp.title}</g:link>
                                                    </div>
                                                </div>
                                            </div>
                                        </g:if>
                                        <div class="step">
                                            <div class="content">
                                                <div class="title">
                                                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: ch.id]}">${ch.title}</g:link>
                                                </div>
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </g:if>
                        </div>
                    </g:else>
                </div>
            </div>
        </g:each>
    </div>
</g:each>

<!-- -->

<h2 class="ui header">WfSequencePrototype : ${WfSequencePrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
        <tr>
            <th>Sequenz</th>
            <th>Typ</th>
            <th>Has Child</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfSequencePrototype.executeQuery('select sp from WfSequencePrototype sp order by id')}" var="sp">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: sp.id]}">(${sp.id}) ${sp.title}</g:link>
                </td>
                <td>
                    ${sp.type?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${sp.child}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${! sp.inStructure()}">
                        <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfSequencePrototype.KEY}", id: sp.id]}"><i class="trash alternate icon"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfSequencePrototype.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:sequence]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfSequencePrototype.KEY]}">Sequenz (Prototyp) erstellen</g:link>

<!-- -->

<h2 class="ui header">WfTaskPrototype : ${WfTaskPrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
        <tr>
            <th>Task</th>
            <th>Priorität</th>
            <th>Typ</th>
            <th>Is Startnode</th>
            <th>Has Child</th>
            <th>Has Next</th>
            <th>Has Parent</th>
            <th>Has Previous</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp order by id')}" var="tp">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: tp.id]}">(${tp.id}) ${tp.title}</g:link>
                </td>
                <td>
                    ${tp.priority?.getI10n('value')}
                </td>
                <td>
                    ${tp.type?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${WfSequencePrototype.findByChild(tp)}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${tp.child}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${tp.next}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${WfTaskPrototype.findByChild(tp)}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${WfTaskPrototype.findByNext(tp)}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${! tp.inStructure()}">
                        <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfTaskPrototype.KEY}", id: tp.id]}"><i class="trash alternate icon"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfTaskPrototype.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfTaskPrototype.KEY]}">Task (Prototyp) erstellen</g:link>

<!-- -->

<h2 class="ui header">WfSequence : ${WfSequence.findAll().size()}</h2>

<g:each in="${WfSequence.executeQuery('select s from WfSequence s order by id')}" var="s">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequence.KEY, id: s.id]}">(${s.id}) ${s.title}</g:link>
</g:each>

<g:if test="${key == WfSequence.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:sequence]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfSequence.KEY]}">Sequenz erstellen</g:link>

<!-- -->

<h2 class="ui header">WfTask : ${WfTask.findAll().size()}</h2>

<g:each in="${WfTask.executeQuery('select t from WfTask t order by id')}" var="t">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTask.KEY, id: t.id]}">(${t.id}) ${t.title}}</g:link><br/>
</g:each>

<g:if test="${key == WfTask.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfTask.KEY]}">Task erstellen</g:link>

<!-- -->

<div id="wfModal" class="ui modal"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.target).attr('href'), true);
        func();
    });
</laser:script>

</body>
</html>
