<%@ page import="de.laser.domain.I10nTranslation; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.RefdataValue" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<title>${message(code: 'menu.admin.manageRefdatas')}</title>
	</head>

		<semui:breadcrumbs>
			<semui:crumb message="menu.admin.dash" controller="admin" action="index" />
			<semui:crumb message="menu.admin.manageI10n" class="active"/>
		</semui:breadcrumbs>

		<h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.admin.manageRefdatas"/></h1>

        <semui:messages data="${flash}" />


            <div class="content ui form">
                <div class="fields">
                    <div class="field">
                        <button class="ui button" value="" href="#addRefdataValueModal" data-semui="modal">${message(code:'refdataValue.create_new.label')}</button>
                    </div>
                    <div class="field">
                        <button class="ui button" value="" href="#addRefdataCategoryModal" data-semui="modal">${message(code:'refdataCategory.create_new.label')}</button>
                    </div>
                </div>
            </div>

<%--<pre>
${usedRdvList.join(", ")}

<g:each in="${attrMap}" var="objs">
    ${objs.key}
    <g:each in="${objs.value}" var="attrs">    ${attrs}
    </g:each>
</g:each>
</pre>--%>


        <div class="ui styled fluid accordion">
            <g:each in="${rdCategories}" var="rdc">
                <g:set var="rdcI10n" value="${I10nTranslation.createI10nOnTheFly(rdc, 'desc')}" />

                <div class="title">
                    <i class="dropdown icon"></i>
                    ${fieldValue(bean: rdc, field: "desc")}
                </div>
                <div class="content">

                    <table class="ui celled la-table la-table-small table">
                        <thead>
                        <tr>
                            <th>Category (Key)</th>
                            <th>Value (Key)</th>
                            <th>DE</th>
                            <th>EN</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    ${fieldValue(bean: rdc, field: "desc")}
                                </td>
                                <td></td>
                                <td>
                                    <strong><semui:xEditable owner="${rdcI10n}" field="valueDe" /></strong>
                                </td>
                                <td>
                                    <strong><semui:xEditable owner="${rdcI10n}" field="valueEn" /></strong>
                                </td>
                                <td>
                                    <g:if test="${rdc.softData}">
                                        <span data-position="top right" data-tooltip="${message(code:'default.softData.tooltip')}">
                                            <i class="tint icon teal"></i>
                                        </span>
                                    </g:if>
                                </td>
                            </tr>

                            <g:each in="${RefdataValue.findAllByOwner(rdc).toSorted()}" var="rdv">
                                <tr>
                                    <td>
                                        <g:if test="${rdv.hardData}">
                                            <span data-position="top left" data-tooltip="${message(code:'default.hardData.tooltip')}">
                                                <i class="check circle icon green"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${! usedRdvList?.contains(rdv.id)}">
                                            <span data-position="top left" data-tooltip="Dieser Wert wird bisher nicht verwendet (ID:${rdv.id})">
                                                <i class="info circle icon blue"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        ${rdv.value}
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${I10nTranslation.createI10nOnTheFly(rdv, 'value')}" field="valueDe" />
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${I10nTranslation.createI10nOnTheFly(rdv, 'value')}" field="valueEn" />
                                    </td>
                                    <td class="x">
                                        <g:if test="${rdv.softData}">
                                            <span data-position="top right" data-tooltip="${message(code:'default.softData.tooltip')}">
                                                <i class="tint icon teal"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${usedRdvList?.contains(rdv.id)}">
                                            <span data-position="top right" data-tooltip="${message(code:'refdataValue.exchange.label')}">
                                                <button class="ui icon button" href="#replaceRefdataValueModal" data-semui="modal"
                                                        data-xcg-rdv="${rdv.class.name}:${rdv.id}"
                                                        data-xcg-rdc="${rdc.class.name}:${rdc.id}"
                                                        data-xcg-debug="${rdv.getI10n('value')} (${rdv.value})"
                                                ><i class="exchange icon"></i></button>
                                            </span>
                                        </g:if>

                                        <g:if test="${rdv.softData && ! usedRdvList?.contains(rdv.id)}">
                                            <g:link controller="admin" action="manageRefdatas"
                                                    params="${[cmd: 'deleteRefdataValue', rdv: 'com.k_int.kbplus.RefdataValue:' + rdv.id]}" class="ui icon negative button">
                                                <i class="trash alternate icon"></i>
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

        <semui:modal id="replaceRefdataValueModal" message="refdataValue.exchange.label" editmodal="editmodal">
            <g:form class="ui form" url="[controller: 'admin', action: 'manageRefdatas']">
                <input type="hidden" name="cmd" value="replaceRefdataValue"/>
                <input type="hidden" name="xcgRdvFrom" value=""/>

                <p>
                    <strong>WARNUNG</strong>
                </p>

                <p>
                    Alle Vorkommen von <strong class="xcgInfo"></strong> in der Datenbank durch folgenden Wert ersetzen:
                </p>

                <div class="field">
                    <label for="xcgRdvTo">&nbsp;</label>
                    <select id="xcgRdvTo"></select>
                </div>

            </g:form>

            <r:script>
                    $('button[data-xcg-rdv]').on('click', function(){

                        var rdv = $(this).attr('data-xcg-rdv');
                        var rdc = $(this).attr('data-xcg-rdc');

                        $('#replaceRefdataValueModal .xcgInfo').text($(this).attr('data-xcg-debug'));
                        $('#replaceRefdataValueModal input[name=xcgRdvFrom]').attr('value', rdv);

                         $.ajax({
                            url: '<g:createLink controller="ajax" action="refdataSearchByOID"/>' + '?oid=' + rdc + '&format=json',
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
            </r:script>

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
                        optionKey="id" optionValue="desc"
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
