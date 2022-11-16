<laser:htmlStart text="Manage ES Sources" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="ES Sources" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="ES Sources" />

    <ui:messages data="${flash}" />

      <table class="ui celled la-js-responsive-table la-table table">
        <thead>
          <tr>
              <th>${message(code:'default.identifier.label')}</th>
              <th>${message(code:'default.name.label')}</th>
            <th>Host</th>
            <th>Active</th>
            <th>Port</th>
            <th>Index</th>
            <th>Cluster</th>
            <th>Laser ES</th>
            <th>we:kb ES</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${sources}" var="source">
            <tr>
              <td>${source.identifier}</td>
              <td>
                  <ui:xEditable owner="${source}" field="name"/>
              </td>
              <td>
                  <ui:xEditable owner="${source}" field="host"/>
              </td>
              <td>
                  <ui:xEditableBoolean owner="${source}" field="active" />
              </td>
              <td>
                  <ui:xEditable owner="${source}" field="port"/>
              </td>
              <td>
                  <ui:xEditable owner="${source}" field="index"/>
              </td>
              <td>
                    <ui:xEditable owner="${source}" field="cluster"/>
              </td>
              <td>
                  <ui:xEditableBoolean owner="${source}" field="laser_es" />
              </td>
              <td>
                  <ui:xEditableBoolean owner="${source}" field="gokb_es" />
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <br />

    <ui:form controller="yoda" action="newESSource">

            <div class="field">
                <label for="host">ES Source Host</label>
                <input type="text" id="host" name="host" placeholder="" value=""/>
            </div>

            <div class="fields two">
                <div class="field">
                    <label for="identifier">ES Source Identifier</label>
                    <input type="text" id="identifier" name="identifier" placeholder=""/>
                </div>
                <div class="field">
                    <label for="name">ES Source Name</label>
                    <input type="text" id="name" name="name" placeholder=""/>
                </div>
            </div>

            <div class="fields two">
                <div class="field">
                    <label for="index">ES Index</label>
                    <input type="text" id="index" name="index" placeholder=""/>
                </div>
                <div class="field">
                    <label for="cluster">ES Cluster</label>
                    <input type="text" id="cluster" name="cluster" placeholder=""/>
                </div>
            </div>

            <div class="fields two">
                <div class="field">
                    <label for="laser_es">LAS:eR ES</label>
                    <select id="laser_es" name="laser_es">
                        <option value="0">No</option>
                        <option value="1">Yes</option>
                    </select>
                </div>
                <div class="field">
                    <label for="gokb_es">we:kb ES</label>
                    <select id="gokb_es" name="gokb_es">
                        <option value="0">No</option>
                        <option value="1">Yes</option>
                    </select>
                </div>
            </div>

            <div class="field">
                <label>&nbsp;</label>
                <input type="submit" value="Submit" class="ui button"/>
            </div>

    </ui:form>

<laser:htmlEnd />
