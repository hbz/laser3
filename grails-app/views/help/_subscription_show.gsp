<%@ page import="de.laser.ui.Icon" %>
<div class="ui wide flyout" id="help-content" style="padding:50px 0 10px 0;overflow:scroll">

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
                <i class="${Icon.CMD.EDIT} green" aria-hidden="true"></i>
            </td>
            <td>
                Aktuell: Inhalte editierbar
            </td>
        </tr>
        <tr>
            <td>
                <i class="${Icon.CMD.EDIT} slash" aria-hidden="true"></i>
            </td>
            <td>
                Aktuell: Inhalte nicht editierbar
            </td>
        </tr>
        <tr>
            <td>
                <i class="${Icon.CMD.EDIT} blue" aria-hidden="true"></i>
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
                <i aria-hidden="true" class="${Icon.SIG.INHERITANCE}"></i>
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
                <i class="${Icon.CMD.UNLINK}"></i>
            </td>
            <td>
                Objekt wird entknüpft
            </td>
        </tr>
        <tr>
            <td>
                <i class="${Icon.SIG.SHARED_OBJECT_ON}" aria-hidden="true"></i>
            </td>
            <td>
                Objekt wird geteilt
            </td>
        </tr>
        <tr>
            <td>
                <i class="${Icon.SIG.SHARED_OBJECT_OFF}" aria-hidden="true"></i>
            </td>
            <td>
                Objekt wird nicht geteilt
            </td>
        </tr>

        <tr>
            <td>
                <i class="${Icon.CMD.SHOW_MORE}" aria-hidden="true"></i>
            </td>
            <td>
                Anzeige der Vertragsmerkmale
            </td>
        </tr>




        </tbody>
    </table>

    </div>

</div>