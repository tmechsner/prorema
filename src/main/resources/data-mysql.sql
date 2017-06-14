
set names utf8;
set @old_foreign_key_checks=@@foreign_key_checks, foreign_key_checks=0;


# export von tabelle auditableentity
# ------------------------------------------------------------

lock tables `auditableentity` write;
/*!40000 alter table `auditableentity` disable keys */;

insert into `auditableentity` (`auditableentity_id`)
values
	(1000),
	(1001),
	(4000),
	(7000);

/*!40000 alter table `auditableentity` enable keys */;
unlock tables;


# export von tabelle employee
# ------------------------------------------------------------

lock tables `employee` write;
/*!40000 alter table `employee` disable keys */;

insert into `employee` (`city`, `country`, `email`, `first_name`, `last_name`, `name_title`, `password`, `position`, `street`, `tel`, `username`, `workentry`, `workexit`, `workschedule`, `zip`, `employee_id`, `organisationunit_id`)
values
	('','Germany','admin1@firma.de','','',' ','$2a$10$WoG5Z4YN9Z37EWyNCkltyeFr6PtrSXSLMeFWOeDUwcanht5CIJgPa','administrator','','','admin1','2000-01-01','2099-01-01',0,'',1000,4000),
	('','Germany','admin2@firma.de','','',' ','$2a$10$WoG5Z4YN9Z37EWyNCkltyeFr6PtrSXSLMeFWOeDUwcanht5CIJgPa','administrator','','','admin2','2000-01-01','2099-01-01',0,'',1001,4000);

/*!40000 alter table `employee` enable keys */;
unlock tables;


# export von tabelle organisationunit
# ------------------------------------------------------------

lock tables `organisationunit` write;
/*!40000 alter table `organisationunit` disable keys */;

insert into `organisationunit` (`description`, `name`, `organisationunit_id`, `first_manager`, `second_manager`)
values
	('Versteckte Administratoren','Admins',4000,1000,1001);

/*!40000 alter table `organisationunit` enable keys */;
unlock tables;


# export von tabelle project
# ------------------------------------------------------------

lock tables `project` write;
/*!40000 alter table `project` disable keys */;

insert into `project` (`city`, `conversion_probability`, `country`, `description`, `end_date`, `man_days`, `name`, `project_volume`, `is_running`, `start_date`, `status`, `street`, `zip`, `project_id`, `contact_id`, `organisationunit_id`, `employee_id`)
values
	(null,0,null,'Kein Status','2099-12-31',0,'Kein Status',0.00,0,'2000-01-01','Won',null,null,7000,null,4000,1000);

/*!40000 alter table `project` enable keys */;
unlock tables;


set foreign_key_checks=@old_foreign_key_checks;