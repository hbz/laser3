<%@ page import="de.laser.storage.BeanStore" %>
<laser:htmlStart text="GASCO-Über uns" publicLayout="gasco" />

<g:render template="/layouts/gasco/nav" />

<div class="ui grid">
    <div class="ten wide column">
        <br><br><br><br>
        <h1 class="ui header">
            GASCO – Arbeitsgemeinschaft Deutscher, Österreichischer und Schweizer Konsortien
        </h1>
        <p>
            Die German, Austrian, Swiss Consortia Organisation (GASCO – Arbeitsgemeinschaft Deutscher, Österreichischer und Schweizer Konsortien) wurde im Jahr 2000 gegründet. Zu den Zielen der GASCO zählen insbesondere die Verbesserung des internen Informationsaustauschs unter den Konsortien, die Verständigung über gemeinsame Verhandlungsstrategien sowie ein (bundes)länderübergreifendes Vorgehen und die Stärkung der Position der Bibliotheken gegenüber den Anbietern elektronischer Medien. Begleitet hat die GASCO nicht zuletzt eine Reihe von Modellumbrüchen bis hin zur aktuell dominierenden Diskussion über Open-Access-Transformationsverträge und deren Zukunft.
        </p>
        <p>
            In der GASCO sind derzeit 28 Einrichtungen organisiert, die hunderte Angebote von wissenschaftlichen Verlagen verhandeln und betreuen. Sie leisten damit einen wesentlichen Beitrag zur Literaturversorgung des Wissenschaftssektors, aber auch zunehmend zur Publikationsunterstützung im Rahmen von Open-Access-Transformationsverträgen.
        </p>
        <h2 class="ui header">
            Starke Partnerschaft
        </h2>
        <p>Die GASCO bündelt die regionalen Konsortialaktivitäten im deutschsprachigen Raum. Sie bildet eine Plattform zur strategischen Zusammenarbeit der Bibliotheken beim gemeinsamen Erwerb von elektronischen Zeitschriften, Datenbanken und E-Books</p>
        <br>
        <p>
            <a href="https://forum13plus.de/"  target="_blank" class="ui red icon button">
                <em>Zusammenarbeit mit</em> Arbeitskreis Forum 13+ <i class="arrow right icon"></i>
            </a>
        </p>
    </div>
    <div class="six wide column">
        <img class="ui fluid image" alt="Logo Map" src="${resource(dir: 'images', file: 'gasco/gasco-map.png')}"/>
    </div>
</div>

<laser:htmlEnd />
