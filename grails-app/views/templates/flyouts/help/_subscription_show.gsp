<%@ page import="de.laser.helper.Icons" %>
<div class="ui flyout" id="help-content" style="padding:50px 0 10px 0;overflow:scroll">

    <h1 class="ui header">Erklärung der Icons</h1>

    <div class="content">

    <table class="ui la-ignore-fixed compact table">
        <thead>
        <tr>
            <th>Icon</th>
            <th>Erklärung</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>
                <i class="circular la-subscription icon" aria-hidden="true"></i>
            </td>
            <td>
                Lizenz-Symbol
            </td>
        </tr>
        <tr>
            <td>
                <i class="${Icons.CMD_EDIT} green" aria-hidden="true"></i>
            </td>
            <td>
                Aktuell: Inhalte editierbar
            </td>
        </tr>
        <tr>
            <td>
                <i class="${Icons.CMD_EDIT} slash" aria-hidden="true"></i>
            </td>
            <td>
                Aktuell: Inhalte nicht editierbar
            </td>
        </tr>
        <tr>
            <td>
                <i class="${Icons.CMD_EDIT} blue" aria-hidden="true"></i>
            </td>
            <td>
                Verknüpfung bearbeiten
            </td>
        </tr>
        <tr>
            <td>
                <i aria-hidden="true" class="icon la-thumbtack slash"></i>
            </td>
            <td>
                Wert wird nicht vererbt
            </td>
        </tr>
        <tr>
            <td>
                <i aria-hidden="true" class="icon thumbtack"></i>
            </td>
            <td>
                Wert wird vererbt
            </td>
        </tr>
        <tr>
            <td>
                <i aria-hidden="true" class="icon thumbtack la-thumbtack-regular"></i>
            </td>
            <td>
                Wert wird automatisch geerbt
            </td>
        </tr>

        <tr>
            <td>
                <i class="${Icons.CMD_UNLINK}"></i>
            </td>
            <td>
                Objekt wird entknüpft
            </td>
        </tr>
        <tr>
            <td>
                <i class="la-share icon" aria-hidden="true"></i>
            </td>
            <td>
                Objekt wird geteilt
            </td>
        </tr>
        <tr>
            <td>
                <i class="la-share slash icon" aria-hidden="true"></i>
            </td>
            <td>
                Objekt wird nicht geteilt
            </td>
        </tr>

        <tr>
            <td>
                <i class="ui angle double down icon" aria-hidden="true"></i>
            </td>
            <td>
                Anzeige der Vertragsmerkmale
            </td>
        </tr>




        </tbody>
    </table>

    </div>

</div>