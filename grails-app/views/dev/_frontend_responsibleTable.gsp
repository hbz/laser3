<h2 class="ui dividing header">Responsive Tabellen<a class="anchor" id="icons"></a></h2>
<h4 class="ui header">1. Die Tabelle muss sie Klasse 'la-js-responsive-table' bekommen </h4>
<h4 class="ui header">2. Alle Tabellen-Spalten müssen einen Bezeichner bekommen, außer Icons, diese benötigen einen Tooltip</h4>
<h4 class="ui header">3. Tabellen-Header, die aus zwei Reihen bestehen, brauchen </h4>

<table class="ui celled sortable table table-tworow la-table la-js-responsive-table">
    <thead>
    <tr>

        <th scope="col" rowspan="2" class="center aligned">
            Nr.
        </th>

        <th rowspan="2" scope="col" class="sortable"><a href="/myInstitution/currentSubscriptions?status=103&amp;hasPerpetualAccess=1&amp;orgRole=Subscription+Consortia&amp;sort=s.name&amp;order=asc">Lizenz / Vertrag</a></th>
        <th rowspan="2" scope="col">
            Verknüpfte Pakete
        </th>

        <th scope="col" rowspan="2" class="sortable"><a href="/myInstitution/currentSubscriptions?status=103&amp;hasPerpetualAccess=1&amp;orgRole=Subscription+Consortia&amp;sort=providerAgency&amp;order=asc">Anbieter / Lieferant</a></th>

        <th scope="col" class="la-smaller-table-head sortable"><a href="/myInstitution/currentSubscriptions?status=103&amp;hasPerpetualAccess=1&amp;orgRole=Subscription+Consortia&amp;sort=s.startDate&amp;order=asc">Anfangsdatum</a></th>

        <th scope="col" rowspan="2">
            <a href="#" class="la-popup-tooltip la-delay" data-content="Teilnehmer" data-position="top center" aria-label="Teilnehmer" data-jsqtk-id="jsqtk-722">
                <i class="users large icon" aria-hidden="true"></i>
            </a>
        </th>
        <th scope="col" rowspan="2">
            <a href="#" class="la-popup-tooltip la-delay" data-content="Teilnehmerkosten" data-position="top center" aria-label="Teilnehmerkosten" data-jsqtk-id="jsqtk-725">
                <i class="money bill large icon" aria-hidden="true"></i>
            </a>
        </th>

        <th scope="col" rowspan="2" class="two">Optionen</th>
    </tr>
    <tr>
        <th scope="col" class="la-smaller-table-head sortable"><a href="/myInstitution/currentSubscriptions?status=103&amp;hasPerpetualAccess=1&amp;orgRole=Subscription+Consortia&amp;sort=s.endDate&amp;order=asc">Enddatum</a></th>
    </tr>
    </thead>
    <tbody>

    <tr>

        <td class="center aligned" data-label="
                            Nr.
                        :">
            1
        </td>
        <th scope="row" class="la-th-column">
            <a href="/subscription/show/47645" class="la-main-object">

                All You Can Read

            </a>

            <div class="la-flexbox la-minor-object">
                <i class="icon balance scale la-list-icon" aria-hidden="true"></i>
                <a href="/lic/show/54">Herdt Lizenzvertrag</a><br>
            </div>

        </th>
        <td data-label="
                            Verknüpfte Pakete
                        :">
            <!-- packages -->

            <div class="la-flexbox">
                <i class="icon gift la-list-icon" aria-hidden="true"></i>
                <a href="/subscription/index/47645?pkgfilter=440" title="HERDT-Verlag für Bildungsmedien GmbH">
                    All You Can Read
                </a>
            </div>

            <!-- packages -->
        </td>

        <td data-label="Anbieter / Lieferant:">

            <a href="/org/show/25">HERDT-Verlag für Bildungsmedien GmbH

                <br>
                (Herdt)

            </a><br>

        </td>

        <td data-label="Anfangsdatum:">
            01.01.2022<br>
            <span class="la-secondHeaderRow" data-label="Enddatum:">31.12.2022</span>
        </td>

        <td data-label="Teilnehmer:">

            <a href="/subscription/members/47645">2</a>

        </td>
        <td data-label="Teilnehmerkosten:">
            <a href="/subscription/47645/finance">

                1

            </a>
        </td>

        <td class="x" data-label="Optionen:">

            <a href="/subscription/surveysConsortia/47645" class="ui button blue icon la-modern-button">
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="Es ist eine offene Lizenzverlängerungsabfrage vorhanden!" aria-label="Es ist eine offene Lizenzverlängerungsabfrage vorhanden!" data-jsqtk-id="jsqtk-757">
                    <i class="ui icon pie chart" aria-hidden="true"></i>
                </span>
            </a>

        </td>
    </tr>
    </tbody>
</table>
<div class="html ui top attached segment example">

    <div class="ui doubling five column grid">

    </div>

    <div class="ui top attached label">Aussehen der Tabelle</div>
</div>