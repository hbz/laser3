<%@ page import="de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<laser:htmlStart message="myinst.financeImport.pageTitle" />

  <ui:breadcrumbs>
      <ui:crumb controller="org" action="show" id="${institution.id}" text="${institution.getDesignation()}"/>
    <ui:crumb message="menu.institutions.financeImport" class="active"/>
  </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.institutions.financeImport" type="finance" />

    <ui:messages data="${flash}" />

    <div class="ui flyout" id="help-content" style="padding:50px 0 10px 0;overflow:scroll">
        <h1 class="ui header">
            Erläuterungen zu den Tabelleninhalten
        </h1>
        <div class="content">
            <table class="ui la-ignore-fixed compact table">
                <thead>
                    <tr>
                        <%-- <th>tsv column name</th>
                        <th>Description</th>
                        <th>maps to</th> --%>
                        <th>${message(code:'myinst.financeImport.tsvColumnName')}</th>
                        <%--<th>${message(code:'myinst.financeImport.descriptionColumnName')}</th>--%>
                        <th>${message(code:'myinst.financeImport.necessaryFormat')}</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${mappingCols}" var="mpg">
                        <%
                            List args = []
                            boolean mandatory = false
                            switch(mpg) {
                                case 'status': List<RefdataValue> costItemStatus = RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS)
                                    costItemStatus.remove(RDStore.COST_ITEM_DELETED)
                                    args.addAll(costItemStatus.collect { it -> it.getI10n('value') })
                                    break
                                case 'currency': mandatory = true
                                    break
                                case 'element': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT).collect { it -> it.getI10n('value') })
                                    break
                                case 'elementSign': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_CONFIGURATION).collect { it -> it.getI10n('value') })
                                    break
                                    //as of December 3rd '20, Micha said that no reverse charge should be made possible by tax type in order to avoid confusion with users of the interface
                                case 'taxType': List<RefdataValue> taxTypes = RefdataCategory.getAllRefdataValues(RDConstants.TAX_TYPE)
                                    taxTypes.remove(RDStore.TAX_TYPE_REVERSE_CHARGE)
                                    args.addAll(taxTypes.collect { it -> it.getI10n('value') })
                                    break
                                case 'taxRate': args.addAll([0,5,7,16,19])
                                    break
                            }
                        %>
                        <tr <g:if test="${mandatory}">class="negative"</g:if>>
                            <td>${message(code:"myinst.financeImport.${mpg}")}<g:if test="${mandatory}"><span style="color: #BB1600">*</span></g:if></td>
                            <%--<td>${message(code:"myinst.financeImport.description.${mpg}") ?: ''}</td>--%>
                            <td>${message(code:"myinst.financeImport.format.${mpg}",args:[raw("<ul><li>${args.join('</li><li>')}</li></ul>")]) ?: ''}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
    </div>

    <p>
        Im Folgenden können Sie Kosten in Form einer Tabelle hochladen. Eine Vorlage finden Sie im unten angegebenen Link; Erläuterungen darüber, wie Sie die Spalten ausfüllen müssen
        finden Sie im Ausklappfenster, das Sie mit dem Fragezeichen oben rechts öffnen können.
        Wenn Sie die Tabelle mit Excel bearbeiten, müssen Sie folgende Dinge beachten, damit das System die Angaben korrekt verarbeitet:
        <ol>
            <li>Speichern Sie die Datei als "Text (Tabstopp-getrennt)" ab. Die Dateiendung .txt stört dabei nicht.</li>
            <li>Wenn Sie die Datei speichern, müssen Sie die Zeichenkodierung korrekt einstellen. Hierzu klicken Sie auf "Tools" neben dem Knopf "Speichern", dann "Weboptionen".</li>
            <li>Im nun sich öffnenden Fenster den Reiter "Codierung" anwählen</li>
            <li>Dort in der Auswahlliste "Dokument speichern als" "UTF-8 (Unicode)" auswählen</li>
            <li>Mit "OK" bestätigen</li>
            <li>Datei speichern</li>
        </ol>
        Anschließend können Sie die Datei hochladen.
    </p>
          <g:if test="${params.id}">
              <g:link action="generateFinanceImportWorksheet" params="${[id:params.id]}">
                  <p>${message(code:'myinst.financeImport.subscription.template')}</p>
              </g:link>
          </g:if>
          <g:else>
              <a href="${resource(dir: 'files', file: 'bulk_load_cost_item_records_template_02.csv')}" download="template_bulk_load_cost_item_records.csv">
                  <p>${message(code:'myinst.financeImport.template')}</p>
              </a>
          </g:else>


          <g:uploadForm action="processFinanceImport" method="POST">
              <g:if test="${params.id}">
                  <g:hiddenField name="subId" value="${params.id}"/>
              </g:if>
              <label for="tsvFile">${message(code:'myinst.financeImport.upload')}</label>
              <input class="ui input" type="file" name="tsvFile" id="tsvFile"/>
              <input class="ui button" type="submit" value="${message(code:"myinst.financeImport.upload")}"/>
          </g:uploadForm>

<laser:htmlEnd />
