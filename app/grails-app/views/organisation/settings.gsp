<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.OrgSettings; com.k_int.properties.PropertyDefinition" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
            <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'org.confProperties')} &amp; ${message(code:'org.orgSettings')}</title>
            <g:javascript src="properties.js"/>
    </head>
    <body>

        <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

        <%--<semui:controlButtons>
            <g:render template="actions" model="${[ org:orgInstance, user:user ]}"/>
        </semui:controlButtons>--%>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${orgInstance.name} - ${message(code:'org.nav.options')}</h1>

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
                                <r:script>

                                    $('body #oamonitor_server_access').editable({
                                        validate: function (value) {
                                            if (value == "com.k_int.kbplus.RefdataValue:${de.laser.helper.RDStore.YN_YES.id}") {
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
                                </r:script>

                                <g:each in="${settings}" var="os">
                                    <tr>
                                        <td>
                                            ${message(code:"org.setting.${os.key}", default: "${os.key}")}

                                            <g:if test="${'OAMONITOR_SERVER_ACCESS'.equals(os.key.toString())}">
                                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'org.setting.OAMONITOR_SERVER_ACCESS.tooltip')}">
                                                    <i class="question circle icon"></i>
                                                </span>
                                            </g:if>
                                        </td>
                                        <td>

                                            <g:if test="${(inContextOrg || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) && os.key in OrgSettings.getEditableSettings()}">
                                            <%-- Refdata YN --%>
                                                <g:if test="${os?.key?.rdc=='YN'}">
                                                <%-- Validation through user is necessary --%>
                                                    <g:if test="${'OAMONITOR_SERVER_ACCESS'.equals(os.key.toString())}">
                                                        <semui:xEditableRefData owner="${os}" field="rdValue" id="oamonitor_server_access" config="${os.key.rdc}" />
                                                    </g:if>
                                                <%-- Other Refdata YN --%>
                                                    <g:else>
                                                        <semui:xEditableRefData owner="${os}" field="rdValue" config="${os.key.rdc}" />
                                                    </g:else>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${os.key.type=='class com.k_int.kbplus.auth.Role'}">
                                                        ${os.getValue()?.getI10n('authority')} (Editierfunktion deaktiviert) <%-- TODO --%>
                                                    </g:if>
                                                    <g:else>
                                                        <semui:xEditable owner="${os}" field="strValue" />
                                                    </g:else>
                                                </g:else>
                                            </g:if>
                                            <g:else>
                                                <g:if test="${os.rdValue}">
                                                    ${os.getValue()?.getI10n('value')}
                                                </g:if>
                                                <g:elseif test="${os.roleValue}">
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
                                        orphanedProperties: orgInstance.customProperties,
                                        custom_props_div: "custom_props_div_1" ]}"/>
                            </div>
                        </div><!-- .content -->
                    </div><!-- .card -->

                    <r:script language="JavaScript">
                                $(document).ready(function(){
                                    c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_1");
                                });
                    </r:script>


                    <div class="ui card la-dl-no-table la-js-hideable">
                        <div class="content">
                            <h5 class="ui header">
                                ${message(code:'org.customerIdentifier')}
                            </h5>

                            <table class="ui la-table table">
                                <thead>
                                <tr>
                                    <th>Anbieter</th>
                                    <th>Plattform</th>
                                    <th>Kundennummer</th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                    <g:each in="${customerIdentifier}" var="ci">
                                        <tr>
                                            <td>
                                                ${ci.getProvider()}
                                            </td>
                                            <td>
                                                ${ci.platform}
                                            <td>
                                                <g:if test="${editable}">
                                                    <semui:xEditable owner="${ci}" field="value" />
                                                </g:if>
                                                <g:else>
                                                    ${ci.value}
                                                </g:else>
                                            </td>
                                            <td>
                                            <g:if test="${editable}">
                                                <g:link controller="organisation" action="settings" id="${orgInstance.id}"
                                                    params="${[deleteCI:ci.class.name + ':' + ci.id]}"
                                                    class="ui button icon red"><i class="trash alternate icon"></i></g:link>
                                            </g:if>
                                            </td>
                                        </tr>
                                    </g:each>
                                </tbody>
                                <g:if test="${editable}">
                                <tfoot>
                                <tr>
                                    <td colspan="4">
                                        <g:form class="ui form" controller="organisation" action="settings" id="${orgInstance.id}">
                                            <div class="ui grid">
                                                <%--
                                                <g:select id="addCIProvider" name="addCIProvider" class="ui dropdown selection"
                                                          from="${formAllProviders}"
                                                          optionKey="${{'com.k_int.kbplus.Org:' + it.id}}" optionValue="${{'(' + it.sortname +') ' + it.name}}" />
                                                --%>

                                                <div class="eight wide column">
                                                <div class="field">
                                                    <label for="addCIPlatform">Anbieter : Plattform</label>
                                                    <g:select id="addCIPlatform" name="addCIPlatform" class="ui dropdown fluid search selection"
                                                              from="${allPlatforms}"
                                                              optionKey="${{'com.k_int.kbplus.Platform:' + it.id}}"
                                                              optionValue="${{ it.org.name + (it.org.sortname ? " (${it.org.sortname})" : '') + ' : ' + it.name}}" />
                                                </div>
                                            </div>

                                                <div class="four wide column">
                                                    <div class="field">
                                                    <label for="addCIValue">Kundennummer</label>
                                                    <input type="text" id="addCIValue" name="addCIValue" value=""/>
                                                </div>
                                                </div>

                                                <div class="four wide column">
                                                    <div class="field">
                                                    <label>&nbsp;</label>
                                                    <input type="submit" class="ui button" value="${message(code:'default.button.add.label')}" />
                                                </div>
                                                </div>
                                            </div>
                                        </g:form>
                                    </td>
                                </tr>
                                </tfoot>
                                </g:if>
                            </table>
                        </div>
                    </div>

                </div><!-- .la-inline-lists -->

            </div><!-- .twelve -->
        </div><!-- .grid -->

    </body>
</html>
