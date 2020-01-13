databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1576828793575-1") {
		grailsChange {
			change {
				sql.execute("""
update property_definition set pd_name = 'GASCO display name' where pd_name = 'GASCO-Anzeigename' and pd_description ='Subscription Property';
update property_definition set pd_name = 'GASCO negotiator name' where pd_name = 'GASCO-Verhandlername' and pd_description ='Subscription Property';
update property_definition set pd_name = 'GASCO information link' where pd_name = 'GASCO-Information-Link' and pd_description ='Subscription Property';
update property_definition set pd_name = 'EZB tagging (yellow)' where pd_name = 'EZB Gelbschaltung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Pricing advantage by licensing of another product' where pd_name = 'Preisvorteil durch weitere Produktteilnahme' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Product dependency' where pd_name = 'Produktabhängigkeit' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Open country-wide' where pd_name = 'Bundesweit offen' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Billing done by provider' where pd_name = 'Rechnungsstellung durch Anbieter' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Due date for volume discount' where pd_name = 'Mengenrabatt Stichtag' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Time span for testing' where pd_name = 'Testzeitraum' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Joining during the period' where pd_name = 'Unterjähriger Einstieg' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Newcomer discount' where pd_name = 'Neueinsteigerrabatt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Time of billing' where pd_name = 'Rechnungszeitpunkt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Payment target' where pd_name = 'Zahlungsziel' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Price rounded' where pd_name = 'Preis gerundet' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Partial payment' where pd_name = 'Teilzahlung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Statistic' where pd_name = 'Statistik' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Statistic access' where pd_name = 'Statistikzugang' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Statistics Link' where pd_name = 'StatisticsLink' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Admin Access' where pd_name = 'AdminAccess' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Admin Link' where pd_name = 'AdminLink' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Private institutions' where pd_name = 'Private Einrichtungen' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Perennial term' where pd_name = 'Mehrjahreslaufzeit' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Perennial term checked' where pd_name = 'Mehrjahreslaufzeit ausgewählt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Discount' where pd_name = 'Rabatt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Scale of discount' where pd_name = 'Rabattstaffel' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Calculation of discount' where pd_name = 'Rabatt Zählung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Term of notice' where pd_name = 'Kündigungsfrist' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Additional software necessary?' where pd_name = 'Zusätzliche Software erforderlich?' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Price increase' where pd_name = 'Preissteigerung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Price depending on' where pd_name = 'Preis abhängig von' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Cancellation rate' where pd_name = 'Abbestellquote' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Order number in purchasing system' where pd_name = 'Bestellnummer im Erwerbungssystem' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Credentials for users (per journal)' where pd_name = 'Zugangskennungen für Nutzer (pro Zeitschrift)' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Tax exemption' where pd_name = 'TaxExemption' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Subscription number of editor' where pd_name = 'Subscriptionsnummer vom Verlag' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Subscription number of provider' where pd_name = 'Subskriptionsnummer des Lieferanten' and pd_description ='Subscription Property';
update property_definition set pd_name = 'DBIS entry' where pd_name = 'DBIS-Eintrag' and pd_description ='Subscription Property';
update property_definition set pd_name = 'DBIS link' where pd_name = 'DBIS-Link' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Cancellation reason' where pd_name = 'Abbestellgrund' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Hosting fee' where pd_name = 'Hosting-Gebühr' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Pick&Choose package' where pd_name = 'Pick&Choose-Paket' and pd_description ='Subscription Property';
update property_definition set pd_name = 'PDA/EBS model' where pd_name = 'PDA/EBS-Programm' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Specialised statistics / classification' where pd_name = 'Fachstatistik / Klassifikation' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Perpetual access' where pd_name = 'Archivzugriff' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Restricted user group' where pd_name = 'Eingeschränkter Benutzerkreis' and pd_description ='Subscription Property';
update property_definition set pd_name = 'SFX entry' where pd_name = 'SFX-Eintrag' and pd_description ='Subscription Property';
""")
			}
			rollback {}
		}
	}
}
