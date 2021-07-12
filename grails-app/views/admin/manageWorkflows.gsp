<%@ page import="de.laser.RefdataValue; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>
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

%{-- <ul>
<g:each in="${WfSequencePrototype.executeQuery('select wfsp from WfSequencePrototype wfsp order by id')}" var="wfsp">
    <li>
        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: wfsp.id]}">Sequenz: (${wfsp.id}) ${wfsp.title}</g:link>
        <g:set var="tasks" value="${wfsp.getWorkflow()}" />
        <g:if test="${tasks}">
            <ul>
                <g:each in="${tasks}" var="wftp">
                    <li>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">Task: (${wftp.id}) ${wftp.title}</g:link>
                        <g:if test="${wftp.head}">
                            <g:set var="children" value="${wftp.head.getWorkflow()}" />
                            <ul>
                                <g:each in="${children}" var="ch">
                                    <li>
                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: ch.id]}">Task: (${ch.id}) ${ch.title}</g:link>
                                    </li>
                                </g:each>
                            </ul>
                        </g:if>
                    </li>
                </g:each>
            </ul>
        </g:if>
    </li>
</g:each>
</ul> --}%

<ul>
    <g:each in="${WfSequencePrototype.executeQuery('select wfsp from WfSequencePrototype wfsp order by id')}" var="wfsp">
        <li><g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: wfsp.id]}">Sequenz: (${wfsp.id}) ${wfsp.title}</g:link></li>
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
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfSequencePrototype.executeQuery('select wf from WfSequencePrototype wf order by id')}" var="wf">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: wf.id]}">Sequenz: (${wf.id}) ${wf.title}</g:link>
                </td>
                <td>
                    ${wf.type.getI10n('value')}
                </td>
                <td>
                    <g:if test="${wf.child}"> <i class="check green icon"></i> </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfSequencePrototype.KEY}">
    <g:if test="${save}">
        <semui:msg class="positive" text="${sequence} wurde erfolgreich gespeichert." />
    </g:if>
    <g:else>
        <semui:errors bean="${sequence}" />
    </g:else>
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
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfTaskPrototype.executeQuery('select wf from WfTaskPrototype wf order by id')}" var="wf">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: wf.id]}">Task: (${wf.id}) ${wf.title}</g:link>
                </td>
                <td>
                    ${wf.priority.getI10n('value')}
                </td>
                <td>
                    ${wf.type.getI10n('value')}
                </td>
                <td>
                    <g:if test="${WfSequencePrototype.findByChild(wf)}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${wf.child}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${wf.next}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${WfTaskPrototype.findByChild(wf)}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${WfTaskPrototype.findByNext(wf)}"> <i class="check green icon"></i> </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfTaskPrototype.KEY}">
    <g:if test="${save}">
        <semui:msg class="positive" text="${task} wurde erfolgreich gespeichert." />
    </g:if>
    <g:else>
        <semui:errors bean="${task}" />
    </g:else>
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfTaskPrototype.KEY]}">Task (Prototyp) erstellen</g:link>

<!-- -->

<h2 class="ui header">WfSequence : ${WfSequence.findAll().size()}</h2>

<g:each in="${WfSequence.executeQuery('select wf from WfSequence wf order by id')}" var="wf">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequence.KEY, id: wf.id]}">Sequenz: (${wf.id}) ${wf.title}</g:link>
</g:each>

<g:if test="${key == WfSequence.KEY}">
    <g:if test="${save}">
        <semui:msg class="positive" text="${sequence} wurde erfolgreich gespeichert." />
    </g:if>
    <g:else>
        <semui:errors bean="${sequence}" />
    </g:else>
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfSequence.KEY]}">Sequenz erstellen</g:link>

<!-- -->

<h2 class="ui header">WfTask : ${WfTask.findAll().size()}</h2>

<g:each in="${WfTask.executeQuery('select wf from WfTask wf order by id')}" var="wf">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTask.KEY, id: wf.id]}">Task: (${wf.id}) ${wf.title}}</g:link><br/>
</g:each>

<g:if test="${key == WfTask.KEY}">
    <g:if test="${save}">
        <semui:msg class="positive" text="${task} wurde erfolgreich gespeichert." />
    </g:if>
    <g:else>
        <semui:errors bean="${task}" />
    </g:else>
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
