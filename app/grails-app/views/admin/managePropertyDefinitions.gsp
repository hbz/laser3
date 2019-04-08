<%@ page import="de.laser.domain.I10nTranslation; com.k_int.properties.PropertyDefinition" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.admin.managePropertyDefinitions')}</title>
	</head>

		<semui:breadcrumbs>
			<semui:crumb message="menu.admin.dash" controller="admin" action="index" />
			<semui:crumb message="menu.admin.manageI10n" class="active"/>
		</semui:breadcrumbs>

		<h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.admin.managePropertyDefinitions"/></h1>

        <h3 class="ui header">${message(code:'license.properties')}</h3>

		<semui:messages data="${flash}" />

            <div class="content ui form">
                <div class="fields">
                    <div class="field">
                        <button class="ui button" value="" href="#addPropertyDefinitionModal" data-semui="modal" >${message(code:'propertyDefinition.create_new.label')}</button>
                    </div>
                </div>
            </div>

		<div class="ui styled fluid accordion">
			<g:each in="${propertyDefinitions}" var="entry">
                <div class="title">
                    <i class="dropdown icon"></i>
                    <g:message code="propertyDefinitions.${entry.key}.label" default="${entry.key}" />
                </div>
                <div class="content">
                    <table class="ui celled la-table la-table-small table">
                        <thead>
                        <tr>
                            <th></th>
                            <th>${message(code:'propertyDefinition.name.label', default:'Name')}</th>
                            <th>DE</th>
                            <th>EN</th>
                            <th>Erklärung</th>
                            <th>Explanation</th>
                            <th></th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                            <g:each in="${entry.value}" var="pd">
                                <g:set var="pdI10nName"  value="${I10nTranslation.createI10nOnTheFly(pd, 'name')}" />
                                <g:set var="pdI10nExpl" value="${I10nTranslation.createI10nOnTheFly(pd, 'expl')}" />
                                <tr>
                                    <td>
                                        <g:if test="${pd.hardData}">
                                            <span data-position="top left" data-tooltip="${message(code:'default.hardData.tooltip')}">
                                                <i class="check circle icon green"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.multipleOccurrence}">
                                            <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                                                <i class="redo icon orange"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${usedPdList?.contains(pd.id)}">
                                            <span data-position="top left" data-tooltip="${message(code:'default.dataIsUsed.tooltip', args:[pd.id])}">
                                                <i class="info circle icon blue"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.isUsedForLogic}">
                                            <span data-position="top left" data-tooltip="${message(code:'default.isUsedForLogic.tooltip')}">
                                                <i class="ui icon orange cube"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${pd.isUsedForLogic}">
                                            <span style="color:orange">${fieldValue(bean: pd, field: "name")}</span>
                                        </g:if>
                                        <g:else>
                                            ${fieldValue(bean: pd, field: "name")}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <semui:xEditable owner="${pdI10nName}" field="valueDe" />
                                        </g:if>
                                        <g:else>
                                            ${pdI10nName?.valueDe}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <semui:xEditable owner="${pdI10nName}" field="valueEn" />
                                        </g:if>
                                        <g:else>
                                            ${pdI10nName?.valueEn}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <semui:xEditable owner="${pdI10nExpl}" field="valueDe" type="textarea" />
                                        </g:if>
                                        <g:else>
                                            ${pdI10nExpl?.valueDe}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.hardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <semui:xEditable owner="${pdI10nExpl}" field="valueEn" type="textarea" />
                                        </g:if>
                                        <g:else>
                                            ${pdI10nExpl?.valueEn}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                        <g:if test="${'RefdataValue'.equals(pdRdc)}">
                                            <span data-position="top right" data-tooltip="${pd.refdataCategory}">
                                                <small>${pd.type?.split('\\.').last()}</small>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <small>${pd.type?.split('\\.').last()}</small>
                                        </g:else>
                                    </td>
                                    <td class="x">

                                        <sec:ifAnyGranted roles="ROLE_YODA">
                                            <g:if test="${usedPdList?.contains(pd.id)}">
                                                <span data-position="top right" data-tooltip="${message(code:'propertyDefinition.exchange.label')}">
                                                    <button class="ui icon button" href="#replacePropertyDefinitionModal" data-semui="modal"
                                                            data-xcg-pd="${pd.class.name}:${pd.id}"
                                                            data-xcg-type="${pd.type}"
                                                            data-xcg-rdc="${pd.refdataCategory}"
                                                            data-xcg-debug="${pd.getI10n('name')} (${pd.name})"
                                                    ><i class="exchange icon"></i></button>
                                                </span>
                                            </g:if>
                                        </sec:ifAnyGranted>

                                    </td>

                                </tr>
                            </g:each>

                        </tbody>
                    </table>
                </div>
			</g:each>
        </div>


        <semui:modal id="replacePropertyDefinitionModal" message="propertyDefinition.exchange.label" editmodal="editmodal">
            <g:form class="ui form" url="[controller: 'admin', action: 'managePropertyDefinitions']">
                <input type="hidden" name="cmd" value="replacePropertyDefinition"/>
                <input type="hidden" name="xcgPdFrom" value=""/>

                <p>
                    <strong>WARNUNG</strong>
                </p>

                <p>
                    Alle Vorkommen von <strong class="xcgInfo"></strong> in der Datenbank durch folgende Eigenschaft ersetzen:
                </p>

                <div class="field">
                    <label for="xcgPdTo">&nbsp;</label>
                    <select id="xcgPdTo"></select>
                </div>

                <p>
                    Die gesetzten Werte bleiben erhalten!
                </p>

            </g:form>

            <r:script>
                        $('button[data-xcg-pd]').on('click', function(){

                            var pd = $(this).attr('data-xcg-pd');
                            //var type = $(this).attr('data-xcg-type');
                            //var rdc = $(this).attr('data-xcg-rdc');

                            $('#replacePropertyDefinitionModal .xcgInfo').text($(this).attr('data-xcg-debug'));
                            $('#replacePropertyDefinitionModal input[name=xcgPdFrom]').attr('value', pd);

                            $.ajax({
                                url: '<g:createLink controller="ajax" action="propertyAlternativesSearchByOID"/>' + '?oid=' + pd + '&format=json',
                                success: function (data) {
                                    var select = '<option></option>';
                                    for (var index = 0; index < data.length; index++) {
                                        var option = data[index];
                                        if (option.value != pd) {
                                            select += '<option value="' + option.value + '">' + option.text + '</option>';
                                        }
                                    }
                                    select = '<select id="xcgPdTo" name="xcgPdTo" class="ui search selection dropdown">' + select + '</select>';

                                    $('label[for=xcgPdTo]').next().replaceWith(select);

                                    $('#xcgPdTo').dropdown({
                                        duration: 150,
                                        transition: 'fade'
                                    });

                                }, async: false
                            });
                        })
            </r:script>

        </semui:modal>


        <semui:modal id="addPropertyDefinitionModal" message="propertyDefinition.create_new.label">

            <g:form class="ui form" id="create_cust_prop" url="[controller: 'ajax', action: 'addCustomPropertyType']" >
                <input type="hidden" name="reloadReferer" value="/admin/managePropertyDefinitions"/>
                <input type="hidden" name="ownerClass" value="${this.class}"/>

				<div class="field">
                	<label class="property-label">Name</label>
                	<input type="text" name="cust_prop_name"/>
                </div>

                <div class="fields">
                    <div class="field five wide">
                        <label class="property-label">Context:</label>
                        <%--<g:select name="cust_prop_desc" from="${PropertyDefinition.AVAILABLE_CUSTOM_DESCR}" />--%>
                        <select name="cust_prop_desc" id="cust_prop_desc" class="ui dropdown">
                            <g:each in="${PropertyDefinition.AVAILABLE_CUSTOM_DESCR}" var="pd">
                                <option value="${pd}"><g:message code="propertyDefinition.${pd}.label" default="${pd}"/></option>
                            </g:each>
                        </select>
                    </div>
                    <div class="field five wide">
                        <label class="property-label"><g:message code="propertyDefinition.type.label" /></label>
                        <g:select class="ui dropdown"
                            from="${PropertyDefinition.validTypes2.entrySet()}"
                            optionKey="key" optionValue="${{PropertyDefinition.getLocalizedValue(it.key)}}"
                            name="cust_prop_type"
                            id="cust_prop_modal_select" />
                    </div>
                    <div class="field five wide">
                        <label class="property-label">${message(code:'propertyDefinition.expl.label', default:'Explanation')}</label>
                        <textarea name="cust_prop_expl" id="eust_prop_expl" class="ui textarea"></textarea>
                    </div>

                    <div class="field six wide hide" id="cust_prop_ref_data_name">
                        <label class="property-label"><g:message code="refdataCategory.label" /></label>
                        <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
                    </div>
                </div>

                <div class="fields">
                    <div class="field five wide">
                        <label class="property-label">${message(code:'default.multipleOccurrence.tooltip')}</label>
                        <g:checkBox type="text" name="cust_prop_multiple_occurence" />
                    </div>
                </div>

            </g:form>

        </semui:modal>

		<g:javascript>

			   if( $( "#cust_prop_modal_select option:selected" ).val() == "class com.k_int.kbplus.RefdataValue") {
					$("#cust_prop_ref_data_name").show();
			   } else {
                     $("#cust_prop_ref_data_name").hide();
                }

			$('#cust_prop_modal_select').change(function() {
				var selectedText = $( "#cust_prop_modal_select option:selected" ).val();
				if( selectedText == "class com.k_int.kbplus.RefdataValue") {
					$("#cust_prop_ref_data_name").show();
				}else{
					$("#cust_prop_ref_data_name").hide();
				}
			});

			$("#cust_prop_refdatacatsearch").select2({
				placeholder: "Kategorie eintippen...",
                minimumInputLength: 1,

                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note', default:'Please enter 1 or more character')}";
                },
                formatNoMatches: function() {
                    return "${message(code:'select2.noMatchesFound')}";
                },
                formatSearching:  function() {
                    return "${message(code:'select2.formatSearching')}";
                },
				ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
					url: '${createLink(controller:'ajax', action:'lookup')}',
					dataType: 'json',
					data: function (term, page) {
						return {
							q: term, // search term
							page_limit: 10,
							baseClass:'com.k_int.kbplus.RefdataCategory'
						};
					},
					results: function (data, page) {
						return {results: data.values};
					}
				}
			});

		</g:javascript>

	</body>
</html>
