-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>

-- 2019-12-19
-- ERMS-1992: translations provided for subscription custom properties
update property_definition set pd_name = 'GASCO display name' where pd_name = 'GASCO-Anzeigename';
update property_definition set pd_name = 'GASCO negotiator name' where pd_name = 'GASCO-Verhandlername';
update property_definition set pd_name = 'GASCO information link' where pd_name = 'GASCO-Informations-Link';
update property_definition set pd_name = 'EZB tagging (yellow)' where pd_name = 'EZB Gelbschaltung';
update property_definition set pd_name = 'Pricing advantage by licensing of another product' where pd_name = 'Preisvorteil durch weitere Produktteilnahme';
update property_definition set pd_name = 'Product dependency' where pd_name = 'Produktabhängigkeit';
update property_definition set pd_name = 'Open country-wide' where pd_name = 'Bundesweit offen';
update property_definition set pd_name = 'Billing done by provider' where pd_name = 'Rechnungsstellung durch Anbieter';
update property_definition set pd_name = 'Due date for volume discount' where pd_name = 'Mengenrabatt Stichtag';
update property_definition set pd_name = 'Time span for testing' where pd_name = 'Testzeitraum';
update property_definition set pd_name = 'Joining during the period' where pd_name = 'Unterjähriger Einstieg';
update property_definition set pd_name = 'Newcomer discount' where pd_name = 'Neueinsteigerrabatt';
update property_definition set pd_name = 'Time of billing' where pd_name = 'Rechnungszeitpunkt';
update property_definition set pd_name = 'Payment target' where pd_name = 'Zahlungsziel';
update property_definition set pd_name = 'Price rounded' where pd_name = 'Preis gerundet';
update property_definition set pd_name = 'Partial payment' where pd_name = 'Teilzahlung';
update property_definition set pd_name = 'Statistic' where pd_name = 'Statistik';
update property_definition set pd_name = 'Statistic access' where pd_name = 'Statistikzugang';
update property_definition set pd_name = 'Statistics Link' where pd_name = 'StatisticsLink';
update property_definition set pd_name = 'Admin Access' where pd_name = 'AdminAccess';
update property_definition set pd_name = 'Admin Link' where pd_name = 'AdminLink';
update property_definition set pd_name = 'Private institutions' where pd_name = 'Private Einrichtungen';
update property_definition set pd_name = 'Perennial term' where pd_name = 'Mehrjahreslaufzeit';
update property_definition set pd_name = 'Perennial term checked' where pd_name = 'Mehrjahreslaufzeit ausgewählt';
update property_definition set pd_name = 'Discount' where pd_name = 'Rabatt';
update property_definition set pd_name = 'Scale of discount' where pd_name = 'Rabattstaffel';
update property_definition set pd_name = 'Calculation of discount' where pd_name = 'Rabatt Zählung';
update property_definition set pd_name = 'Term of notice' where pd_name = 'Kündigungsfrist';
update property_definition set pd_name = 'Additional software necessary?' where pd_name = 'Zusätzliche Software erforderlich?';
update property_definition set pd_name = 'Price increase' where pd_name = 'Preissteigerung';
update property_definition set pd_name = 'Price depending on' where pd_name = 'Preis abhängig von';
update property_definition set pd_name = 'Cancellation rate' where pd_name = 'Abbestellquote';
update property_definition set pd_name = 'Order number in purchasing system' where pd_name = 'Bestellnummer im Erwerbungssystem';
update property_definition set pd_name = 'Credentials for users (per journal)' where pd_name = 'Zugangskennungen für Nutzer (pro Zeitschrift)';
update property_definition set pd_name = 'Tax exemption' where pd_name = 'TaxExemption';
update property_definition set pd_name = 'Subscription number of editor' where pd_name = 'Subscriptionsnummer vom Verlag';
update property_definition set pd_name = 'Subscription number of provider' where pd_name = 'Subskriptionsnummer des Lieferanten';
update property_definition set pd_name = 'DBIS entry' where pd_name = 'DBIS-Eintrag';
update property_definition set pd_name = 'DBIS link' where pd_name = 'DBIS-Link';
update property_definition set pd_name = 'Cancellation reason' where pd_name = 'Abbestellgrund';
update property_definition set pd_name = 'Hosting fee' where pd_name = 'Hosting-Gebühr';
update property_definition set pd_name = 'Pick&Choose package' where pd_name = 'Pick&Choose-Paket';
update property_definition set pd_name = 'PDA/EBS model' where pd_name = 'PDA/EBS-Programm';
update property_definition set pd_name = 'Specialised statistics / classification' where pd_name = 'Fachstatistik / Klassifikation';
update property_definition set pd_name = 'Perpetual access' where pd_name = 'Archivzugriff';
update property_definition set pd_name = 'Restricted user group' where pd_name = 'Eingeschränkter Benutzerkreis';
update property_definition set pd_name = 'SFX entry' where pd_name = 'SFX-Eintrag';