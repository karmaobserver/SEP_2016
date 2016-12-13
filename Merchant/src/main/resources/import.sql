--DROP TABLE age;
--CREATE TABLE age (age_id VARCHAR(20), age_risk_value VARCHAR(20));
--
--insert into age(AGE_ID, AGE_RISK_VALUE) values (1,2.1);
--insert into age(AGE_ID, AGE_RISK_VALUE) values (2,2.4);
--insert into age(AGE_ID, AGE_RISK_VALUE) values (3,2.5);
--insert into age(AGE_ID, AGE_RISK_VALUE) values (4,1.1);

--DROP TABLE date_category;
--CREATE TABLE date_category (date_category_id INTEGER, end_date DATE, start_date DATE);

INSERT INTO date_category (end_date, start_date) VALUES ('2017-10-11 00:00:00', '2014-10-11 00:00:00');
INSERT INTO date_category (end_date, start_date) VALUES ('2016-10-11 00:00:00', '2013-10-11 00:00:00');

INSERT INTO risk_category (name, date_category) VALUES ('age', 1);
INSERT INTO risk_category (name, date_category) VALUES ('region', 2);
INSERT INTO risk_category (name, date_category) VALUES ('sumTo', 2);
INSERT INTO risk_category (name, date_category) VALUES ('sport', 2);
INSERT INTO risk_category (name, date_category) VALUES ('towing', 2);

INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (2, 'to 18', 1);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (4, '18-60', 1);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (6, 'over 60', 1);

INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (1, 'Europe', 2);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (14, 'Asia', 2);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (6, 'North America', 2);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (22, 'South America', 2);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (10, 'Australia', 2);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (30, 'Africa', 2);


INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (5, '10.000 EUR', 3);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (10, '50.000 EUR', 3);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (15, '150.000 EUR', 3);

INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (15, 'Rugby', 4);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (1, 'Golf', 4);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (8, 'Biking', 4);

INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (3, '20km', 5);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (8, '100km', 5);
INSERT INTO risk_subcategory (coefficient, name, risk_category) VALUES (15, '300km', 5);

INSERT INTO insurance_type (insurance_name,  insurance) VALUES ( 'Road Insurance', NULL);
INSERT INTO insurance_type (insurance_name, insurance) VALUES ('Home Insurance', NULL);

INSERT INTO vehicle (registration_number, make, owner_first_name, owner_jmbg, owner_last_name, type,vin_number, year_of_production, insurance_type_id) VALUES ('SO232-JL', 'Ford', 'Pera', '1323241543312', 'Peric', 'Kola', '2132', 2005, 1);


