<%@ page import="de.laser.domain.I10nTranslation; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.RefdataValue" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<title>${message(code: 'menu.admin.manageRefdatas')}</title>
	</head>

		<laser:breadcrumbs>
			<laser:crumb message="menu.admin.dash" controller="admin" action="index" />
			<laser:crumb message="menu.admin.manageI10n" class="active"/>
		</laser:breadcrumbs>

		<div class="container">
			<h1><g:message code="menu.admin.manageRefdatas"/></h1>
		</div>

        <div class="container">
            <div class="row">
                <div class="span12">
                    <laser:card class="card-grey">
                        <input class="btn btn-primary" value="${message(code:'refdataValue.create_new.label')}"
                               data-toggle="modal" href="#addRefdataValueModal" type="submit">
                        &nbsp;
                        <input class="btn btn-primary" value="${message(code:'refdataCategory.create_new.label')}"
                               data-toggle="modal" href="#addRefdataCategoryModal" type="submit">
                    </laser:card>
                </div>
            </div>
        </div>

        <laser:flash data="${flash}" />

		<div class="container">
			<div class="row">
				<div class="span12">
					<table class="table table-bordered table-hover">
						<thead>
						<tr>
							<th>Category(Key)</th>
							<th>Value(Key)</th>
							<th>DE</th>
							<th>EN</th>
							<th></th>
						</tr>
						</thead>
						<tbody>
                            <g:each in="${rdCategories}" var="rdc">
                                <g:set var="rdcI10n" value="${I10nTranslation.createI10nOnTheFly(rdc, 'desc')}" />
                                <tr>
                                    <td>${fieldValue(bean: rdc, field: "desc")} (${(RefdataValue.findAllByOwner(rdc)).size()})</td>
                                    <td></td>
                                    <td>
                                        <strong><g:xEditable owner="${rdcI10n}" field="valueDe" /></strong>
                                    </td>
                                    <td>
                                        <strong><g:xEditable owner="${rdcI10n}" field="valueEn" /></strong>
                                    </td>
                                    <td>
                                        <g:if test="${rdc.softData}">&#8252;</g:if>
                                    </td>
                                </tr>

                                    <g:each in="${RefdataValue.findAllByOwner(rdc, [sort: 'value'])}" var="rdv">
                                        <tr>
                                            <td></td>
                                            <td>
                                                ${rdv.value}
                                            </td>
                                            <td>
                                                <g:xEditable owner="${I10nTranslation.createI10nOnTheFly(rdv, 'value')}" field="valueDe" />
                                            </td>
                                            <td>
                                                <g:xEditable owner="${I10nTranslation.createI10nOnTheFly(rdv, 'value')}" field="valueEn" />
                                            </td>
                                            <td>
                                                <g:if test="${rdv.softData}">&#8252;</g:if>
                                            </td>
                                        </tr>
                                    </g:each>

                            </g:each>
						</tbody>
					</table>
				</div><!--.span12-->
			</div><!--.row-->
		</div>

        <div id="addRefdataValueModal" class="modal hide">

            <g:form id="create_cust_prop" url="[controller: 'ajax', action: 'addRefdataValue']" >
                <input type="hidden" name="reloadReferer" value="/admin/manageRefdatas"/>

                <div class="modal-body">
                    <dl>
                        <dt>
                            <label class="control-label">${message(code:'refdataValue.create_new.label')}</label>
                        </dt>
                        <dd>
                            <label class="property-label">Wert:</label> <input type="text" name="refdata_value"/>
                        </dd>

                        <dd>
                            <label class="property-label">Category:</label> <g:select
                                from="${rdCategories}"
                                optionKey="id" optionValue="desc"
                                name="refdata_category_id"
                                id="refdata_modal_select" />
                        </dd>

                    </dl>
                </div>

                <div class="modal-footer">
                    <a href="#" class="btn" data-dismiss="modal">${message(code:'default.button.close.label', default:'Close')}</a>
                    <input class="btn btn-success" name="SaveRefdataValue" value="${message(code:'default.button.create_new.label', default:'Create New')}" type="submit">
                </div>
            </g:form>
        </div>

        <div id="addRefdataCategoryModal" class="modal hide">

            <g:form id="create_cust_prop" url="[controller: 'ajax', action: 'addRefdataCategory']" >
                <input type="hidden" name="reloadReferer" value="/admin/manageRefdatas"/>

                <div class="modal-body">
                    <dl>
                        <dt>
                            <label class="control-label">${message(code:'refdataCategory.create_new.label')}</label>
                        </dt>
                        <dd>
                            <label class="property-label">Beschreibung:</label> <input type="text" name="refdata_category"/>
                        </dd>

                    </dl>
                </div>

                <div class="modal-footer">
                    <a href="#" class="btn" data-dismiss="modal">${message(code:'default.button.close.label', default:'Close')}</a>
                    <input class="btn btn-success" name="SaveRefdataCategory" value="${message(code:'default.button.create_new.label', default:'Create New')}" type="submit">
                </div>
            </g:form>
        </div>

	</body>
</html>
