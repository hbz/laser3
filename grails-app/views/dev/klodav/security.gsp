<%@ page import="org.grails.io.support.GrailsResourceUtils; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Security" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item">Various</g:link>
    <g:link controller="dev" action="klodav" id="icons" class="item"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Icons</g:link>
    <g:link controller="dev" action="klodav" id="buttons" class="item"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Buttons</g:link>
    <g:link controller="dev" action="klodav" id="markdown" class="item"><i class="${Icon.SIG.NEW_OBJECT} orange"></i> Markdown</g:link>
    <g:link controller="dev" action="klodav" id="security" class="item active"><i class="${Icon.SIG.NEW_OBJECT} red"></i> Security</g:link>
</nav>

%{--<%@page expressionCodec="none" %>--}%

<div class="ui segment">
    <table class="ui table celled striped very compact">
        <tbody>
        <g:each in="${['default.codec', 'gsp.codecs.expression', 'gsp.codecs.scriptlet', 'gsp.codecs.scriptlets', 'gsp.codecs.staticparts', 'gsp.codecs.taglib', 'gsp.encoding', 'gsp.htmlcodec']}" var="cfg">
            <tr>
                <td>grails.views.${cfg}</td>
                <td>${grailsApplication.config.getProperty('grails.views.' + cfg)}</td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>

<div class="ui segment">
    ${'<%@page expressionCodec="none" %>'}
</div>

<div class="ui segment">
    <%
        String t1 = '<span style="color:green">Lorem ipsum dolor sit amet</span>'

        println '<p>1: ${t1}, <strong style="color:red">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>'
        println "<p>2: ${t1}, <strong style=\"color:red\">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>"

        println raw('<p>3: ${t1}, <strong style="color:red">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>')
        println raw("<p>4: ${t1}, <strong style=\"color:red\">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>")
    %>

    <%= '<p>5: ${t1}, <strong style="color:red">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>' %>
    <%= "<p>6: ${t1}, <strong style=\"color:red\">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>" %>

    <%= raw('<p>7: ${t1}, <strong style="color:red">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>') %>
    <%= raw("<p>8: ${t1}, <strong style=\"color:red\">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>") %>

    ${'<p>9: ${t1}, <strong style="color:red">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>'}
    <br />
    ${"<p>10: ${t1}, <strong style=\"color:red\">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>"}
    <br />

    ${raw('<p>11: ${t1}, <strong style="color:red">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>')}
    ${raw("<p>12: ${t1}, <strong style=\"color:red\">consetetur sadipscing elitr</strong>, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.</p>")}

</div>

<div class="ui segment">
    <div id="js_output"></div>

    <script>$('#js_output').append('<p>1: ${t1}</p>')</script>
    <%
        println '<script>$("#js_output").append("<p>2: ${t1}</p>")</script>'
        println "<script>\$('#js_output').append('<p>3: ${t1}</p>')</script>"

        println raw('<script>$("#js_output").append("<p>4: ${t1}</p>")</script>')
        println raw("<script>\$('#js_output').append('<p>5: ${t1}</p>')</script>")
    %>
    <%= '<script>$("#js_output").append("<p>6: ${t1}</p>")</script>' %>
    <%= "<script>\$('#js_output').append('<p>7: ${t1}</p>')</script>" %>

    <%= raw('<script>$("#js_output").append("<p>8: ${t1}</p>")</script>') %>
    <%= raw("<script>\$('#js_output').append('<p>9: ${t1}</p>')</script>") %>

    ${'<script>$("#js_output").append("<p>10: ${t1}</p>")</script>'}
    <br />
    ${"<script>\$('#js_output').append('<p>11: ${t1}</p>')</script>"}
    <br />

    ${raw('<script>$("#js_output").append("<p>12: ${t1}</p>")</script>')}
    ${raw("<script>\$('#js_output').append('<p>13: ${t1}</p>')</script>")}
</div>

<laser:htmlEnd />
