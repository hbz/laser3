<%@ page import="de.laser.domain.I10nTranslation; com.k_int.properties.PropertyDefinition" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="mmbootstrap">
		<title>${message(code: 'menu.admin.managePropertyDefinitions')}</title>
	</head>

		<laser:breadcrumbs>
			<laser:crumb message="menu.admin.dash" controller="admin" action="index" />
			<laser:crumb message="menu.admin.manageI10n" class="active"/>
		</laser:breadcrumbs>

		<div class="container">
			<h1><g:message code="menu.admin.managePropertyDefinitions"/></h1>
		</div>

		<div class="container">
			<div class="row">
				<div class="span12">
					<laser:card class="card-grey">
						<input class="btn btn-primary" value="${message(code:'propertyDefinition.create_new.label')}"
							   data-toggle="modal" href="#addPropertyDefinitionModal" type="submit">
					</laser:card>
				</div>
			</div>
		</div>

		<laser:flash data="${flash}" />

		<div class="container">
			<div class="row">
				<div class="span12">
					<g:each in="${propertyDefinitions}" var="entry">
						<h3>${entry.key}</h3>
                        <table class="table table-striped table-hover table-bordered">
							<thead>
							<tr>
								<th>${message(code:'propertyDefinition.name.label', default:'Name')}</th>
								<th>Name (DE)</th>
								<th>Name (EN)</th>
								<!--<th>DE: Description</th>
								<th>EN: Description</th>-->
							</tr>
							</thead>
							<tbody>
								<g:each in="${entry.value}" var="pd">
									<g:set var="pdI10nName"  value="${I10nTranslation.createI10nOnTheFly(pd, 'name')}" />
									<!--<g:set var="pdI10nDescr" value="${I10nTranslation.createI10nOnTheFly(pd, 'descr')}" />-->
									<tr>
										<td>
                                            ${fieldValue(bean: pd, field: "name")}
											<g:if test="${pd.multipleOccurrence}">
												<span class="badge badge-info" title="${message(code:'default.multipleOccurrence.tooltip')}"> &#9733; </span>
											</g:if>
											<g:if test="${pd.softData}">
												<span class="badge" title="${message(code:'default.softData.tooltip')}"> &#8623; </span>
											</g:if>
										</td>
										<td><g:xEditable owner="${pdI10nName}"  field="valueDe" /></td>
										<td><g:xEditable owner="${pdI10nName}"  field="valueEn" /></td>
										<!--<td><g:xEditable owner="${pdI10nDescr}" field="valueDe" /></td>
										<td><g:xEditable owner="${pdI10nDescr}" field="valueEn" /></td>-->
									</tr>
								</g:each>

							</tbody>
						</table>
					</g:each>
				</div><!--.span12-->
			</div><!--.row-->
		</div>

		<div id="addPropertyDefinitionModal" class="modal hide">

			<g:form id="create_cust_prop" url="[controller: 'ajax', action: 'addCustomPropertyType']" >
				<input type="hidden" name="reloadReferer" value="/admin/managePropertyDefinitions"/>
				<input type="hidden" name="ownerClass" value="${this.class}"/>

				<div class="modal-body">
					<dl>
						<dt>
							<label class="control-label">${message(code:'propertyDefinition.create_new.label')}</label>
						</dt>
						<dd>
							<label class="property-label">Name:</label> <input type="text" name="cust_prop_name"/>
						</dd>

						<dd>
							<label class="property-label">Type:</label> <g:select
								from="${PropertyDefinition.validTypes.entrySet()}"
								optionKey="value" optionValue="key"
								name="cust_prop_type"
								id="cust_prop_modal_select" />
						</dd>

						<div class="hide" id="cust_prop_ref_data_name">
							<dd>
								<label class="property-label">Refdata Category:</label>
								<input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
							</dd>
						</div>

						<dd>
							<label class="property-label">Context:</label>
							<g:select name="cust_prop_desc" from="${PropertyDefinition.AVAILABLE_CUSTOM_DESCR}"/>
						</dd>

						<dd>
							<label class="property-label">${message(code:'default.multipleOccurrence.tooltip')}:</label>
                            <g:checkBox type="text" name="cust_prop_multiple_occurence" />
						</dd>
					</dl>
				</div>

				<div class="modal-footer">
					<a href="#" class="btn" data-dismiss="modal">${message(code:'default.button.close.label', default:'Close')}</a>
					<input class="btn btn-success" name="SavePropertyDefinition" value="${message(code:'default.button.create_new.label', default:'Create New')}" type="submit">
				</div>
			</g:form>
		</div>

		<g:javascript>

			   if( $( "#cust_prop_modal_select option:selected" ).val() == "class com.k_int.kbplus.RefdataValue") {
					$("#cust_prop_ref_data_name").show();
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
				placeholder: "Type category...",
				minimumInputLength: 1,
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
