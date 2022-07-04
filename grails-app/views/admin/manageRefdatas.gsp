<%@ page import="de.laser.RefdataCategory; de.laser.I10nTranslation; de.laser.RefdataValue" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<title>${message(code:'laser')} : ${message(code: 'menu.admin.manageRefdatas')}</title>
	</head>

    <body>
        <semui:debugInfo>
            <laser:render template="/templates/refdata/integrityCheck" model="[result: integrityCheckResult]" />
        </semui:debugInfo>

		<semui:breadcrumbs>
			<semui:crumb message="menu.admin" controller="admin" action="index" />
			<semui:crumb message="menu.admin.manageRefdatas" class="active"/>
		</semui:breadcrumbs>

        <semui:headerWithIcon message="menu.admin.manageRefdatas" />

        <semui:messages data="${flash}" />

            <div class="content ui form">
                <div class="fields">
                    <div class="field">
                        <button class="ui button" value="" data-href="#addRefdataValueModal" data-semui="modal">${message(code:'refdataValue.create_new.label')}</button>
                    </div>
                    <div class="field">
                        <button class="ui button" value="" data-href="#addRefdataCategoryModal" data-semui="modal">${message(code:'refdataCategory.create_new.label')}</button>
                    </div>
                </div>
            </div>

        <div class="ui styled fluid accordion">
            <g:each in="${rdCategories}" var="rdc">

                <div class="title">
                    <i class="dropdown icon"></i>
                    ${rdc.getI10n('desc')}
                </div>
                <div class="content">

                    <table class="ui celled la-js-responsive-table la-table compact table">
                        <thead>
                        <tr>
                            <th>Kategorie (Schlüssel)</th>
                            <th>Wert (Schlüssel)</th>
                            <th>DE</th>
                            <th>EN</th>
                            <th class="la-action-info">${message(code:'default.actions.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    ${fieldValue(bean: rdc, field: "desc")}
                                </td>
                                <td></td>
                                <td>
                                    <g:if test="${!rdc.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                        <strong><semui:xEditable owner="${rdc}" field="desc_de" /></strong>
                                    </g:if>
                                    <g:else>
                                        <strong>${rdc.getI10n('desc', 'de')}</strong>
                                    </g:else>
                                </td>
                                <td>
                                    <g:if test="${!rdc.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                        <strong><semui:xEditable owner="${rdc}" field="desc_en" /></strong>
                                    </g:if>
                                    <g:else>
                                        <strong>${rdc.getI10n('desc', 'en')}</strong>
                                    </g:else>
                                </td>
                                <td>
                                </td>
                            </tr>

                            <g:each in="${RefdataCategory.getAllRefdataValues(rdc.desc)}" var="rdv">
                                <tr>
                                    <td>
                                        <g:if test="${rdv.isHardData}">
                                            <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.hardData.tooltip')}">
                                                <i class="check circle icon green"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${usedRdvList?.contains(rdv.id)}">
                                            <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.dataIsUsed.tooltip', args:[rdv.id])}">
                                                <i class="info circle icon blue"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        ${rdv.value}
                                    </td>
                                    <td>
                                        <g:if test="${!rdv.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <semui:xEditable owner="${rdv}" field="value_de" />
                                        </g:if>
                                        <g:else>
                                            ${rdv.getI10n('value', 'de')}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!rdv.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <semui:xEditable owner="${rdv}" field="value_en" />
                                        </g:if>
                                        <g:else>
                                            ${rdv.getI10n('value', 'en')}
                                        </g:else>
                                    </td>
                                    <td class="x">
                                        <sec:ifAnyGranted roles="ROLE_YODA">
                                            <g:if test="${usedRdvList?.contains(rdv.id)}">
                                                <span data-position="top rightla-popup-tooltip la-delay" data-content="${message(code:'refdataValue.exchange.label')}">
                                                    <button class="ui icon button la-modern-button" data-href="#replaceRefdataValueModal" data-semui="modal"
                                                            data-xcg-rdv="${rdv.class.name}:${rdv.id}"
                                                            data-xcg-rdc="${rdc.class.name}:${rdc.id}"
                                                            data-xcg-debug="${rdv.getI10n('value')} (${rdv.value})"
                                                    ><i class="exchange icon"></i></button>
                                                </span>
                                            </g:if>
                                        </sec:ifAnyGranted>

                                        <g:if test="${! rdv.isHardData && ! usedRdvList?.contains(rdv.id)}">
                                            <g:link controller="admin" action="manageRefdatas"
                                                    params="${[cmd: 'deleteRefdataValue', rdv: RefdataValue.class.name + ':' + rdv.id]}" class="ui icon negative button la-modern-button"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </div>

            </g:each>
        </div>

        <semui:modal id="replaceRefdataValueModal" message="refdataValue.exchange.label" isEditModal="isEditModal">
            <g:form class="ui form" url="[controller: 'admin', action: 'manageRefdatas']">
                <input type="hidden" name="cmd" value="replaceRefdataValue"/>
                <input type="hidden" name="xcgRdvFrom" value=""/>

                Alle Vorkommen von <strong class="xcgInfo"></strong> in der Datenbank durch den unten angegebenen Wert ersetzen.
                <br />
                <br />

                <div class="field">
                    <label for="xcgRdvTo">Innerhalb derselben Kategorie</label>
                    <select id="xcgRdvTo"></select>
                </div>

                <p>oder</p>

                <div class="field">
                    <label for="xcgRdvGlobalTo">Kategorieübergreifend</label>
                    <div class="ui right labeled input">
                        <input id="xcgRdvGlobalTo" name="xcgRdvGlobalTo" value="RefdataCategory_KEY:RefdataValue_KEY" />
                        <div class="ui red label">WARNUNG</div>
                    </div>
                </div>

            </g:form>

            <laser:script file="${this.getGroovyPageFileName()}">
                    $('button[data-xcg-rdv]').on('click', function(){

                        var rdv = $(this).attr('data-xcg-rdv');
                        var rdc = $(this).attr('data-xcg-rdc');

                        $('#replaceRefdataValueModal .xcgInfo').text($(this).attr('data-xcg-debug'));
                        $('#replaceRefdataValueModal input[name=xcgRdvFrom]').attr('value', rdv);

                         $.ajax({
                            url: '<g:createLink controller="ajaxJson" action="refdataSearchByCategory"/>' + '?oid=' + rdc,
                            success: function (data) {
                                var select = '<option></option>';
                                for (var index = 0; index < data.length; index++) {
                                    var option = data[index];
                                    if (option.value != rdv) {
                                        select += '<option value="' + option.value + '">' + option.text + '</option>';
                                    }
                                }
                                select = '<select id="xcgRdvTo" name="xcgRdvTo" class="ui search selection dropdown">' + select + '</select>';

                                $('label[for=xcgRdvTo]').next().replaceWith(select);

                                $('#xcgRdvTo').dropdown({
                                    duration: 150,
                                    transition: 'fade'
                                });

                            }, async: false
                        });
                    })
            </laser:script>

        </semui:modal>

        <semui:modal id="addRefdataValueModal" message="refdataValue.create_new.label">

            <g:form class="ui form" url="[controller: 'ajax', action: 'addRefdataValue']">
                <input type="hidden" name="reloadReferer" value="/admin/manageRefdatas"/>

                <div class="field">
                    <label class="property-label">Wert</label>
                    <input type="text" name="refdata_value"/>
                </div>
                <div class="field">
                    <label class="property-label">Kategorie</label>
                    <g:select
                        from="${rdCategories}"
                        optionKey="id" optionValue="${{it.getI10n('desc')}}"
                        name="refdata_category_id"
                        id="refdata_modal_select" class="ui search selection dropdown" />
                </div>

            </g:form>
        </semui:modal>

        <semui:modal id="addRefdataCategoryModal" message="refdataCategory.create_new.label">

            <g:form class="ui form" url="[controller: 'ajax', action: 'addRefdataCategory']">
                <input type="hidden" name="reloadReferer" value="/admin/manageRefdatas"/>

                <div class="field">
                    <label class="property-label">Beschreibung</label>
                    <input type="text" name="refdata_category"/>
                </div>

            </g:form>
        </semui:modal>

	</body>
</html>
