<%@ page import="de.laser.storage.BeanStore" %>
<laser:htmlStart text="GASCO-Mitglieder" publicLayout="gasco" />

    <g:render template="/layouts/gasco/nav" />

    <h1 class="ui header">
        Liste der GASCO-Mitglieder
    </h1>

    <table class="ui striped fixed table">
        <tbody>
        <tr><th>Mitglieder</th><th>Ansprechpartner</th><th>Institution</th></tr>
        <tr>
            <td>Luxemburg <i>(Gast)</i></td>
            <td><a href="mailto:Patrick.Peiffer@bnl.etat.lu">Patrick Peiffer</a></td>
            <td><span class="la-popup-tooltip" data-content="Bibliothèque nationale de Luxembourg">BNL</span> Luxemburg</td>
        </tr>
        <tr>
            <td>Österreich<br> <a href="http://www.kemoe.at/" target=" _blank">Kooperation E-Medien Österreich</a></td>
            <td><a href="mailto:emedien@obvsg.at">Ute Sondergeld</a></td>
            <td><span data-content="Österreichische Bibliothekenverbund und Service GmbH">OBVSG</span></td>
        </tr>
        <tr>
            <td>Schweiz <br> <a href="http://www.consortium.ch/" target=" _blank">Konsortium der Schweizer Hochschulbibliotheken</a></td>
            <td><a href="mailto:susanne.aerni@slsp.ch">Susanne Aerni</a></td>
            <td><span class="la-popup-tooltip" data-content="care of">c/o</span> <span class="la-popup-tooltip" data-content="Bibliothek der eidgenössischen technischen Hochschule">B ETH</span>Zürich</td>
        </tr>
        </tbody>
    </table>

    <h2 class="ui header">
        Deutschland / Länder
    </h2>

    <table class="ui striped fixed table">
        <tbody>
        <tr><th>Mitglieder</th><th>Ansprechpartner</th><th>Institution</th></tr>
        <tr>
            <td>Baden-Württemberg <br> <a href="http://www.konsortium-bw.de/" target=" _blank">Konsortium Baden-Württemberg</a></td>
            <td><a class="email-link" href="mailto:viktor.kempf@ub.uni-freiburg.de" target="_self" data-content="">Viktor Kempf</a></td>
            <td>UB Freiburg</td>
        </tr>
        <tr>
            <td>Bayern<br><a href="https://www.bsb-muenchen.de/kompetenzzentren-und-landesweite-dienste/landesweite-aufgaben-und-dienste/bayern-konsortium/" target=" _blank">Bayern-Konsortium</a></td>
            <td><a href="mailto:schaeffler@bsb-muenchen.de">Dr. Hildegard Schäffler</a> <br><i>(Vorsitzende der <spnd data-content="German, Austrian and Swiss Consortia Organisation"><span class="la-popup-tooltip" data-content="German, Austrian and Swiss Consortia Organisation">GASCO</span></abbr>)</i></td>
            <td><span class="la-popup-tooltip" data-content="Bayrische Staatsbibliothek">BSB</span> München</td>
        </tr>
        <tr>
            <td>Berlin-Brandenburg&nbsp; <br> <a href="http://althoffkonsortium.wordpress.com" target=" _blank">Friedrich-Althoff-Konsortium <spnd data-content="eingetragener Verein"><span class="la-popup-tooltip" data-content="eingetragener Verein">e.V.</span></abbr></a></td>
            <td><a href="mailto:fak-office@zib.de">Dr. Ursula Stanek</a> <br><i>(Stellvertretende Vorsitzende der <span class="la-popup-tooltip" data-content="German, Austrian and Swiss Consortia Organisation">GASCO</span>)</i></td>
            <td><span class="la-popup-tooltip" data-content=" Staatsbibliothek zu Berlin - Preussischer Kulturbesitz ">SBB-PK</span> Berlin</td>
        </tr>
        <tr>
            <td>Bremen</td>
            <td><a href="mailto:ellis@suub.uni-bremen.de">Rachel Ellis</a></td>
            <td><span class="la-popup-tooltip" data-content="Stadt- und Universitätsbibliothek">SuUB</span> Bremen</td>
        </tr>
        <tr>
            <td><span class="la-popup-tooltip" data-content="Gemeinsamer Bibliotheksverbund"> GBV</span></td>
            <td><a href="mailto:Gerald.steilen@gbv.de">Gerald Steilen</a></td>
            <td>Verbundzentrale GBV</td>
        </tr>
        <tr>
            <td>Hamburg, Schleswig-Holstein&nbsp;</td>
            <td><a class="email-link" href="mailto:sindy.wegner@sub.uni-hamburg.de " target="_self" data-content="">Sindy Wegner</a></td>
            <td><span class="la-popup-tooltip" data-content="Staats- und Universitätsbibliothek">SUB</span> Hamburg</td>
        </tr>
        <tr>
            <td>Hessen&nbsp; <br> <a href="http://www.hebis.de/hebis-konsortium/welcome.php" target=" _blank">hebis-Konsortium</a></td>
            <td><a href="mailto:hebis-konsortium@ub.uni-frankfurt.de">Dr. Roland Wagner</a></td>
            <td>UB <span class="la-popup-tooltip" data-content="Frankfurt am Main"> Frankfurt/M.</span></td>
        </tr>
        <tr>
            <td>Mecklenburg-Vorpommern</td>
            <td><a href="mailto:ub_ggang@uni-greifswald.de">Stefanie Bollin</a></td>
            <td>UB Greifswald</td>
        </tr>
        <tr>
            <td>Niedersachsen<br> <a href="http://nds-konsortium.sub.uni-goettingen.de/" target=" _blank">Niedersachsen-Konsortium</a></td>
            <td>
                <p><a class="email-link" href="mailto:nationallizenzen@sub.uni-goettingen.de" target="_self" data-content="">Kristine Hillenkötter</a></p>
            </td>
            <td><span class="la-popup-tooltip" data-content="Staats- und Universitätsbibliothek">SUB</span> Göttingen</td>
        </tr>
        <tr>
            <td>Nordrhein-Westfalen<br> <a href="https://www.hbz-nrw.de/produkte/digitale-inhalte/" target=" _blank">Nordrhein-Westfalen-Konsortium</a></td>
            <td><a href="mailto:selbach@hbz-nrw.de">Michaela Selbach</a></td>
            <td>hbz – Hochschulbibliothekszentrum NRW</td>
        </tr>
        <tr>
            <td>Rheinland-Pfalz</td>
            <td><a href="mailto:S.Lauer@ub.uni-mainz.de">Sascha Lauer</a></td>
            <td>UB Mainz</td>
        </tr>
        <tr>
            <td>Saarland</td>
            <td><a href="mailto:u.herb@sulb.uni-saarland.de">Dr. Ulrich Herb</a></td>
            <td><span class="la-popup-tooltip" data-content="Universitäts- und Landesbibliothek">ULB</span> Saarbrücken</td>
        </tr>
        <tr>
            <td>Sachsen <br> <a href="http://www.bibag-sachsen.de/" target="_blank">Sachsen-Konsortium</a></td>
            <td><a href="mailto:e-medien@slub-dresden.de">Josephine Hartwig</a></td>
            <td>SLUB Dresden</td>
        </tr>
        <tr>
            <td>Sachsen-Anhalt</td>
            <td><a href="mailto:susann.oezueyaman@bibliothek.uni-halle.de"> Dr. Susann Özüyaman</a></td>
            <td>ULB Sachsen-Anhalt</td>
        </tr>
        <tr>
            <td>Thüringen</td>
            <td><a href="mailto:thomas.mutschler@uni-jena.de">Dr. Thomas Mutschler-Herrmann</a></td>
            <td><span class="la-popup-tooltip" data-content="Thüringische Universitäts- und Landesbibliothek">ThULB</span> Jena</td>
        </tr>
        <tr>
        </tbody>
    </table>
    <h2 class="ui header">
        Deutschland / Sonstige
    </h2>
    <table class="ui striped fixed table">
        <tbody>
        <tr><th>Mitglieder</th><th>Ansprechpartner</th><th>Institution</th></tr>
        <tr>
            <td>AG Bibliotheken privater Hochschulen in der Sektion 4 des dbv (Gast) </td>
            <td><a href="mailto:Lene.Jensen@the-klu.org">Lene Jensen</a></td>
            <td>Kühne Logistics University - KLU - Hamburg</td>
        </tr>
        <tr>
            <td><a href="https://deal-konsortium.de/" target=" _blank">DEAL-Konsortium</a></td>
            <td><a href="mailto:christian.agi@mpdl-services.de">Christian Agi</a></td>
            <td>MPDL Services GmbH</td>
        </tr>
        <tr>
            <td><a href="https://www.zbmed.de/" target=" _blank">Deutsche Zentralbibliothek für Medizin</a></td>
            <td><a href="mailto:labriga@zbmed.de">Petra Labriga</a></td>
            <td><span class="la-popup-tooltip" data-content="Deutsche Zentralbibliothek für Medizin (ZB MED) – Informationszentrum Lebenswissenschaften">ZB MED</span> Köln</td>
        </tr>
        <tr>
            <td><a href="https://www.zbw.eu/de/" target=" _blank">Deutsche Zentralbibliothek für Wirtschaftswissenschaften</a></td>
            <td><a href="mailto:j.lazarus@zbw.eu">Jens Lazarus</a></td>
            <td><span class="la-popup-tooltip" data-content="Deutsche Zentralbibliothek für Wirtschaftswissenschaften">ZBW</span> Kiel, Hamburg</td>
        </tr>
        <tr>
            <td><a href="http://www.fraunhofer.de" target=" _blank">Fraunhofer-Gesellschaft</a></td>
            <td><a href="mailto:stefanie.seeh@zv.fraunhofer.de">Stefanie Seeh</a></td>
            <td>St. Augustin</td>
        </tr>
        <tr>
            <td><a href="http://www.helmholtz.de/" target=" _blank">Helmholtz-Gemeinschaft Deutscher Forschungszentren</a></td>
            <td><a href="mailto:b.mittermaier@fz-juelich.de">Dr. Bernhard Mittermaier</a> <br><br><a href="mailto:k.grosse@gsi.de">Katrin Große</a></td>
            <td>Forschungszentrum Jülich GmbH <br> <span class="la-popup-tooltip" data-content="Gesellschaft für Schwerionenforschung"><br> GSI</span></td>
        </tr>
        <tr>
            <td><a href="http://www.wgl.de/" target=" _blank">Leibniz Gemeinschaft</a></td>
            <td><a href="mailto:alexander.poeche@tib.eu">Dr. Alexander Pöche</a></td>
            <td><span class="la-popup-tooltip" data-content="Technische Informationsbibliothek">TIB</span> Hannover</td>
        </tr>
        <tr>
            <td><a href="http://www.mpdl.mpg.de/" target=" _blank">Max-Planck-Gesellschaft</a></td>
            <td><a href="mailto:planck@mpdl.mpg.de">Tina Planck</a></td>
            <td>München</td>
        </tr>
        <tr>
            <td><a href="http://www.bmelv-forschung.de/" target=" _blank">Ressortforschungseinrichtungen <span class="la-popup-tooltip" data-content="Bundesministerium für Ernährung und Landwirtschaft">BMEL</span> / AG Neue Bibliothekskonzepte</a></td>
            <td><a href="mailto:annette.polly@mri.bund.de"> Annette Polly</a></td>
            <td>Karlsruhe</td>
        </tr>
        <tr>
            <td><a href="https://www.tib.eu/de/" target=" _blank">Technische Informationsbibliothek</a></td>
            <td><a href="mailto:alexander.poeche@tib.eu">Dr. Alexander Pöche</a></td>
            <td><span class="la-popup-tooltip" data-content="Technische Informationsbibliothek">TIB</span> Hannover</td>
        </tr>
        </tbody>
    </table>
<laser:htmlEnd />