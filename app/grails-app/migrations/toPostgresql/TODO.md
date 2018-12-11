# PostgreSQL Migration

- bootstrap application; init table generation by GORM
- migrate data with pgLoader using migrate2postgresql.load


### Refactorings

#### /myInstitution/currentTitles

Message
Der in SQL für eine Instanz von java.util.Date zu verwendende Datentyp kann nicht abgeleitet werden. Benutzen Sie 'setObject()' mit einem expliziten Typ, um ihn festzulegen.

Message
    Error processing GroovyPageView: Error executing tag <g:form>: Error evaluating expression [ti.getInstitutionalCoverageSummary(institution, session.sessionPreferences?.globalDateFormat, date_restriction)] on line [163]: Hibernate operation: could not extract ResultSet; SQL [n/a]; FEHLER: ungültige Eingabesyntax für ganze Zahl: »Deleted« Position: 1774; nested exception is org.postgresql.util.PSQLException: FEHLER: ungültige Eingabesyntax für ganze Zahl: »Deleted« Position: 1774
Caused by
    FEHLER: ungültige Eingabesyntax für ganze Zahl: »Deleted« Position: 1774

#### /laser/myInstitution/processEmptySubscription
 Class
 java.util.ConcurrentModificationException
 
 Message
 null
         
#### /myInstitution/emptySubscription

[submit] ->

Message
Error applying layout : semanticUI

Caused by
FEHLER: doppelter Schlüsselwert verletzt Unique-Constraint »audit_log_pkey« Detail: Schlüssel »(id)=(16095)« existiert bereits.

#### /packageDetails/history

Message
Error applying layout : semanticUI

Caused by
FEHLER: Operator existiert nicht: bigint = character varying Hinweis: Kein Operator stimmt mit dem angegebenen Namen und den Argumenttypen überein. Sie müssen möglicherweise ausdrückliche Typumwandlungen hinzufügen. Position: 775