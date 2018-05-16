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

		<h1 class="ui header"><semui:headerIcon /><g:message code="menu.admin.manageRefdatas"/></h1>

        <semui:messages data="${flash}" />

        <semui:card>
            <div class="content ui form">
                <div class="field">
                    <button class="ui button" value="" href="#addRefdataValueModal" data-semui="modal">${message(code:'refdataValue.create_new.label')}</button>
                </div>
                <div class="field">
                    <button class="ui button" value="" href="#addRefdataCategoryModal" data-semui="modal">${message(code:'refdataCategory.create_new.label')}</button>
                </div>
            </div>
        </semui:card>

<%-- TODO: tmp commit
<pre>
<g:each in="${rdvMap}" var="objs">
    ${objs.key}
    <g:each in="${objs.value}" var="attrs">    ${attrs}
    </g:each>
</g:each>
</pre>
--%>

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
                        </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>
                                    ${fieldValue(bean: rdc, field: "desc")}
                                    <g:if test="${rdc.softData}">
                                        <span class="badge" title="${message(code:'default.softData.tooltip')}"> &#8623; </span>
                                    </g:if>
                                </td>
                                <td></td>
                                <td>
                                    <strong><semui:xEditable owner="${rdcI10n}" field="valueDe" /></strong>
                                </td>
                                <td>
                                    <strong><semui:xEditable owner="${rdcI10n}" field="valueEn" /></strong>
                                </td>
                            </tr>

                            <g:each in="${RefdataValue.findAllByOwner(rdc, [sort: 'value'])}" var="rdv">
                                <tr>
                                    <td></td>
                                    <td data-position="top left" data-tooltip="${rdv.getAllDeclarations()}">
                                        ${rdv.value}
                                        <g:if test="${rdv.softData}">
                                            <span class="badge" title="${message(code:'default.softData.tooltip')}"> &#8623; </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${I10nTranslation.createI10nOnTheFly(rdv, 'value')}" field="valueDe" />
                                    </td>
                                    <td>
                                        <semui:xEditable owner="${I10nTranslation.createI10nOnTheFly(rdv, 'value')}" field="valueEn" />
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </div>

            </g:each>
        </div>

        <semui:modal id="addRefdataValueModal" message="refdataValue.create_new.label">

            <g:form class="ui form" id="create_cust_prop" url="[controller: 'ajax', action: 'addRefdataValue']" >
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
                        id="refdata_modal_select" />
                </div>

            </g:form>
        </semui:modal>

        <semui:modal id="addRefdataCategoryModal" message="refdataCategory.create_new.label">

            <g:form class="ui form" id="create_cust_prop" url="[controller: 'ajax', action: 'addRefdataCategory']" >
                <input type="hidden" name="reloadReferer" value="/admin/manageRefdatas"/>

                <div class="field">
                    <label class="property-label">Beschreibung</label>
                    <input type="text" name="refdata_category"/>
                </div>

            </g:form>
        </semui:modal>

	</body>
</html>
