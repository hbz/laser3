import de.laser.License
import de.laser.Subscription

databaseChangeLog = {

	changeSet(author: "klober (modified)", id: "1592202212497-1") {
		grailsChange {
			change {
				sql.execute("alter table system_message rename column sm_text to sm_content_de")
				sql.execute("alter table system_message rename column sm_show_now to sm_is_active")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1592202212497-2") {
		modifyDataType(columnName: "sm_content_de", newDataType: "text", tableName: "system_message")
	}

	changeSet(author: "klober (generated)", id: "1592202212497-3") {
		addColumn(schemaName: "public", tableName: "system_message") {
			column(name: "sm_content_en", type: "text")
		}
	}

	changeSet(author: "klober (generated)", id: "1592202212497-4") {
		addColumn(schemaName: "public", tableName: "system_message") {
			column(name: "sm_type", type: "varchar(255)")
		}
	}

	changeSet(author: "klober (generated)", id: "1592202212497-5") {
		dropColumn(columnName: "sm_org_fk", tableName: "system_message")
	}

	changeSet(author: "klober (modified)", id: "1592202212497-6") {
		grailsChange {
			change {
				sql.execute("""insert into system_message(sm_content_de, sm_content_en, sm_type, sm_is_active, sm_version, sm_date_created, sm_last_updated) values (
'Das System wird in den nächsten Minuten aktualisiert. Bitte pflegen Sie keine Daten mehr ein!', 
'The system will be updated in the next few minutes. Please do not enter any more data!',
'TYPE_ATTENTION', false, 0, now(), now()
)""")

				sql.execute("""insert into system_message(sm_content_de, sm_content_en, sm_type, sm_is_active, sm_version, sm_date_created, sm_last_updated) values (
'<strong>LAS:eR ist neue hbz-Dienstleistung</strong>
<br>
<br>
Ab dem 01.10.2019 ist das Electronic Resource Management System LAS:eR eine Dienstleistung des hbz. Der Dienst wird bereits bundesweit von Hochschulbibliotheken und Konsortialführern zur Lizenzverwaltung eingesetzt, die durch das Software as a Service (SaaS)-Modell webbasiert Zugriff auf die jeweils aktuelle Version der Software haben und somit auch von neu hinzukommenden Features, Komfort-Funktionen und Verbesserungen ohne zusätzlichen Installationsaufwand profitieren.
<br>
<br>
Das LAS:eR-Support-Team (<a href="mailto:laser@hbz-nrw.de">laser@hbz-nrw.de</a>) unterstützt die teilnehmenden Einrichtungen durch Schulungen und Dokumentationen und ist erster Ansprechpartner bei technischen Problemen.
<br>
<br>
Falls Sie weiterführende Fragen zur LAS:eR-Dienstleistung haben oder LAS:eR an Ihrer Einrichtung zur lokalen Lizenzverwaltung einsetzen möchten, kontaktieren Sie uns: <a href="mailto:laser@hbz-nrw.de">laser@hbz-nrw.de</a>
', 
'<strong>LAS:eR is new hbz-service</strong>
<br>
<br>
From the 1st October 2019, the electronic resource management system LAS:eR is a service of hbz. The service is already used country-wide for subscription management by high school libraries and consortial leaders who have web-based access by the Software as a Servce (SaaS) model to the current version of the software and thus benefit from newly added features, comfort functions and ameliorations without additional installation effort.
<br>
<br>
The LAS:eR-Support-Team (<a href="mailto:laser@hbz-nrw.de">laser@hbz-nrw.de</a>) supports the participating institutions by trainings and documentations and is first contact by technical issues.
<br>
<br>
If you have further questions to the LAS:eR service or would like to use LAS:eR at your institution for local subscription management, contact us: <a href="mailto:laser@hbz-nrw.de">laser@hbz-nrw.de</a>
',
'TYPE_STARTPAGE_NEWS', true, 0, now(), now()
)""")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1592202212497-7") {
		addColumn(schemaName: "public", tableName: "license") {
			column(name: "lic_open_ended_rv_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (modified)", id: "1592202212497-8") {
		grailsChange {
			change {
				sql.execute("update license set lic_open_ended_rv_fk = (select rdv_id from refdata_value left join refdata_category on rdv_owner = rdc_id where rdc_description = 'y.n.u' and rdv_value = 'Unknown') where lic_open_ended_rv_fk is null;")
			}
		}
	}

	changeSet(author: "galffy (modified)", id: "1592202212497-9") {
		addNotNullConstraint(columnDataType: "int8", columnName: "lic_open_ended_rv_fk", tableName: "license")
	}

	changeSet(author: "galffy (generated)", id: "1592202212497-10") {
		addForeignKeyConstraint(baseColumnNames: "lic_open_ended_rv_fk", baseTableName: "license", baseTableSchemaName: "public", constraintName: "FK9F084417F199EB0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
