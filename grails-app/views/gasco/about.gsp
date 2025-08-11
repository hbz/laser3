<%@ page import="de.laser.storage.BeanStore" %>
<laser:htmlStart text="GASCO-Über uns" layout="${BeanStore.getSpringSecurityService().isLoggedIn() ? 'laser':'gasco'}" />

<g:render template="/layouts/gasco/nav" />

<div class="ui grid">
    <div class="ten wide column">
        <br><br><br><br>
        <h1 class="ui header">
            GASCO – Arbeitsgemeinschaft Deutscher, Österreichischer und Schweizer Konsortien
        </h1>
        <p>Die German, Austrian and Swiss Consortia Organisation (GASCO; deutsch Arbeitsgemeinschaft Deutscher, Österreichischer und Schweizer Konsortien) ist eine staatenübergreifende Organisation mit dem Ziel, die Zusammenarbeit öffentlicher Bibliotheken zum Erwerb von elektronischen Zeitschriften, Datenbanken und E-Books im deutschsprachigen Raum zu koordinieren. Die GASCO wurde im Jahr 2000 gegründet.
        </p>
        <h2 class="ui header">
            Starke Partnerschaft
        </h2>
        <p>Die GASCO bündelt die regionalen Konsortialaktivitäten im deutschsprachigen Raum. Sie bildet eine Plattform zur strategischen Zusammenarbeit der Bibliotheken beim gemeinsamen Erwerb von elektronischen Zeitschriften, Datenbanken und E-Books</p>
        <br>
        <p>
            <a href="https://forum13plus.de/"  target="_blank" class="ui red icon button">
                <em>In Zusammenarbeit mit</em> Arbeitskreis Forum 13+ <i class="arrow right icon"></i>
            </a>
        </p>
    </div>
    <div class="six wide column">
        <img class="ui fluid image" alt="Logo Map" src="${resource(dir: 'images', file: 'gasco/gasco-map.png')}"/>
    </div>
</div>

<laser:htmlEnd />
