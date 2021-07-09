<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*;" %>
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

<h3 class="ui header">Prototypen</h3>

<ul>
<g:each in="${WfSequencePrototype.executeQuery('select wfsp from WfSequencePrototype wfsp order by id')}" var="wfsp">
    <li>
        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: wfsp.id]}">Sequenz: (${wfsp.id}) ${wfsp.title}</g:link>
        <g:set var="tasks" value="${wfsp.getWorkflow()}" />
        <g:if test="${tasks}">
            <ul>
                <g:each in="${tasks}" var="wftp">
                    <li>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">Task: (${wfsp.id}) ${wfsp.title}</g:link>
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
</ul>

<hr />

<h2 class="ui header">Prototypen</h2>

<h3 class="ui header">WfSequencePrototype : ${WfSequencePrototype.findAll().size()}</h3>

<g:set var="prefix" value="${WfSequencePrototype.KEY}" />
<g:render template="/templates/workflow/forms/wfSequence" model="${[cmd:'create', prefix:prefix]}" />

<g:each in="${WfSequencePrototype.executeQuery('select wf from WfSequencePrototype wf order by id')}" var="wf">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequencePrototype.KEY, id: wf.id]}">Sequenz: (${wf.id}) ${wf.title}</g:link><br/>
</g:each>

<h3 class="ui header">WfTaskPrototype : ${WfTaskPrototype.findAll().size()}</h3>

<g:set var="prefix" value="${WfTaskPrototype.KEY}" />
<g:render template="/templates/workflow/forms/wfTask" model="${[cmd:'create', prefix:prefix]}" />

<g:each in="${WfTaskPrototype.executeQuery('select wf from WfTaskPrototype wf order by id')}" var="wf">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTaskPrototype.KEY, id: wf.id]}">Task: (${wf.id}) ${wf.title}</g:link><br/>
</g:each>

<!-- -->

<hr />

<h2 class="ui header">Instanzen</h2>

<h3 class="ui header">WfSequence : ${WfSequence.findAll().size()}</h3>

<g:set var="prefix" value="${WfSequence.KEY}" />
<g:render template="/templates/workflow/forms/wfSequence" model="${[cmd:'create', prefix:prefix]}" />

<g:each in="${WfSequence.executeQuery('select wf from WfSequence wf order by id')}" var="wf">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfSequence.KEY, id: wf.id]}">Sequenz: (${wf.id}) ${wf.title}</g:link>
</g:each>

<h3 class="ui header">WfTask : ${WfTask.findAll().size()}</h3>

<g:set var="prefix" value="${WfTask.KEY}" />
<g:render template="/templates/workflow/forms/wfTask" model="${[cmd:'create', prefix:prefix]}" />

<g:each in="${WfTask.executeQuery('select wf from WfTask wf order by id')}" var="wf">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTask.KEY, id: wf.id]}">Task: (${wf.id}) ${wf.title}}</g:link><br/>
</g:each>

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
