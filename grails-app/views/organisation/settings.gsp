<%@ page import="de.laser.OrgSetting; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.Org; de.laser.auth.Role; de.laser.helper.RDStore; de.laser.helper.RDConstants" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'org.nav.options')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <g:if test="${!inContextOrg}">
                <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
            </g:if>
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${orgInstance.name}</h1>

        <semui:objectStatus object="${orgInstance}" status="${orgInstance.status}" />

        <g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

        <semui:messages data="${flash}" />


        <div class="ui stackable grid">
            <div class="sixteen wide column">

                <div class="la-inline-lists">

                    <div class="ui card la-dl-no-table la-js-hideable">
                        <div class="content">
                            <h5 class="ui header">
                                ${message(code:'org.orgSettings')}
                            </h5>

                            <table class="ui la-table table">
                                <thead>
                                <tr>
                                    <th>Merkmal</th>
                                    <th>Wert</th>
                                </tr>
                                </thead>
                                <tbody>
                                <%-- Extra Call from editable cause valiation needed only in Case of Selection "Ja" --%>
                                <laser:script file="${this.getGroovyPageFileName()}">

                                    $('body #oamonitor_server_access').editable('destroy').editable({
                                        validate: function (value) {
                                            if (value == "${RefdataValue.class.name}:${RDStore.YN_YES.id}") {
                                                var r = confirm("Mit der Auswahl der Option >>Datenweitergabe an OA-Monitor<< stimmen Sie der Weitergabe der Lizenz- und Kostendaten Ihrer Einrichtung an den OA-Monitor\n- https://open-access-monitor.de -\n zu.\n\n" +
                                                  "Der OA-Monitor wahrt die Vertraulichkeit dieser Informationen und veröffentlicht im frei zugänglichen Bereich nur aggregierte Subskriptionskosten, aus denen nicht auf eine einzelne Einrichtung geschlossen werden kann.\n\n" +
                                                   "Die Einrichtungen selbst haben nach erfolgter Autorisierung im OA-Monitor die Möglichkeit, die eigenen Ausgaben und zugehörige Auswertungen detailliert einzusehen.\n\n" +
                                                    "Ebenso können Konsortialführer die Daten zu den von ihnen betreuten Lizenzen und zugehörigen Teilnehmern sehen.\n\n" +
                                                     "Im Rechtemanagement des OA-Monitors wird dafür die Zugriffsstruktur aus LAS:eR abgebildet." );
                                                if (r == false) {
                                                   return "Sie haben der Weitergabe der Lizenz- und Kostendaten Ihrer Einrichtung an den OA-Monitor nicht zugestimmt"
                                                }
                                            }
                                        },
                                        tpl: '<select class="ui dropdown"></select>'
                                    }).on('shown', function() {
                                        $(".table").trigger('reflow');
                                        $('.ui.dropdown')
                                                .dropdown({
                                            clearable: true
                                        })
                                        ;
                                    }).on('hidden', function() {
                                        $(".table").trigger('reflow')
                                    });
                                </laser:script>

                                <g:each in="${settings}" var="os">
                                    <tr>
                                        <td>
                                            ${message(code:"org.setting.${os.key}", default: "${os.key}")}

                                            <g:if test="${OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS == os.key}">
                                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'org.setting.OAMONITOR_SERVER_ACCESS.tooltip')}">
                                                    <i class="question circle icon"></i>
                                                </span>
                                            </g:if>
                                        </td>
                                        <td>

                                            <g:if test="${editable && os.key in OrgSetting.getEditableSettings()}">

                                                <g:if test="${OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS == os.key}">
                                                    <semui:xEditableRefData owner="${os}" field="rdValue" id="oamonitor_server_access" config="${os.key.rdc}" />
                                                </g:if>
                                                <g:elseif test="${os.key.type == RefdataValue}">
                                                    <semui:xEditableRefData owner="${os}" field="rdValue" config="${os.key.rdc}" />
                                                </g:elseif>
                                                <g:elseif test="${os.key.type == Role}">
                                                    ${os.getValue()?.getI10n('authority')} (Editierfunktion deaktiviert) <%-- TODO --%>
                                                </g:elseif>
                                                <g:else>
                                                    <semui:xEditable owner="${os}" field="strValue" />
                                                </g:else>

                                            </g:if>
                                            <g:else>

                                                <g:if test="${OrgSetting.KEYS.GASCO_ENTRY == os.key}">
                                                    <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                                                        <semui:xEditableRefData owner="${os}" field="rdValue" config="${os.key.rdc}" />
                                                    </g:if>
                                                    <g:else>
                                                        ${os.getValue()?.getI10n('value')}
                                                    </g:else>
                                                </g:if>
                                                <g:elseif test="${os.key.type == RefdataValue}">
                                                    ${os.getValue()?.getI10n('value')}
                                                </g:elseif>
                                                <g:elseif test="${os.key.type == Role}">
                                                    ${os.getValue()?.getI10n('authority')}
                                                </g:elseif>
                                                <g:else>
                                                    ${os.getValue()}
                                                </g:else>

                                            </g:else>

                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                        </table>
                        </div><!-- .content -->
                    </div>


                    <div class="ui card la-dl-no-table la-js-hideable">
                        <div class="content">
                            <h5 class="ui header">
                                ${message(code:'org.confProperties')}
                            </h5>

                            <div id="custom_props_div_1">
                                <g:render template="/templates/properties/custom" model="${[
                                        prop_desc: PropertyDefinition.ORG_CONF,
                                        ownobj: orgInstance,
                                        orphanedProperties: orgInstance.propertySet,
                                        custom_props_div: "custom_props_div_1" ]}"/>
                            </div>
                        </div><!-- .content -->
                    </div><!-- .card -->

                    <laser:script file="${this.getGroovyPageFileName()}">
                        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#custom_props_div_1");
                    </laser:script>

                </div><!-- .la-inline-lists -->

            </div><!-- .twelve -->
        </div><!-- .grid -->

    </body>
</html>
