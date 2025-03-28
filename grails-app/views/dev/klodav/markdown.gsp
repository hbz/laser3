<%@ page import="org.grails.io.support.GrailsResourceUtils; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Markdown" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="dev"/>

<g:render template="klodav/nav" />

<div class="ui segment">
    <p class="ui header small">Parser</p>
    <ul>
        <g:each in="${helpService.getMarkdownParser().getOptions().getAll()}" var="opt">
            <li>${opt}</li>
        </g:each>
    </ul>

    <p class="ui header small">Renderer</p>
    <ul>
        <g:each in="${helpService.getMarkdownHtmlRenderer().getOptions().getAll()}" var="opt">
            <li>${opt}</li>
        </g:each>
    </ul>

    <p class="ui header small">Token</p>
    <ul>
        <g:each in="${helpService.getTokenMap()}" var="tk">
            <li>{{${tk.key}}} -> ${tk.value}</li>
        </g:each>
    </ul>
</div>

<div class="ui segment la-markdown">
    <ui:renderMarkdown file="test.md" />
    <hr />
    Source (modified) : <a href="https://gist.github.com/allysonsilva/85fff14a22bbdf55485be947566cc09e" target="_blank">allysonsilva/Full-Markdown.md @ GitHub</a>
</div>

<laser:htmlEnd />
