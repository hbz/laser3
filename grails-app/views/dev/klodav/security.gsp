<%@ page import="org.grails.io.support.GrailsResourceUtils; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Security" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="dev"/>

<g:render template="klodav/nav" />

%{--<%@page expressionCodec="none" %>--}%
%{--<%@page scriptletCodec="html" %>--}%

<div class="ui fluid card">
    <div class="content">
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
</div>

<div class="ui fluid card">
    <div class="content">
        ${'<%@page expressionCodec="html" %> // ${}'} <br />
        ${'<%@page scriptletCodec="none" %> // <% %>'}
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
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
</div>

<div class="ui segment">
    <div class="content">
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
</div>

<laser:htmlEnd />
