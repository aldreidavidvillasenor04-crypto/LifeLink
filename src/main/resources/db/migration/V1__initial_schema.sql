-- Initial LifeLink schema for a new production database. Contains no application records.

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `fullname` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'DONOR',
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `donors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `age` int(11) NOT NULL,
  `blood_type` varchar(255) DEFAULT NULL,
  `contact_number` varchar(255) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `archived` bit(1) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4qgpfd2ct0r5uxwu197g6ruq3` (`user_id`),
  CONSTRAINT `FK7yvkt9vykj2821nmokaph275j` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `appointments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_note` varchar(255) DEFAULT NULL,
  `appointment_at` datetime(6) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `hospital_name` varchar(255) DEFAULT NULL,
  `hospital_type` varchar(255) DEFAULT NULL,
  `previous_appointment_id` bigint(20) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `donor_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjxraluwvogn7awygk3x6qo5ky` (`donor_id`),
  CONSTRAINT `FKjxraluwvogn7awygk3x6qo5ky` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `blood_inventory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bags` int(11) NOT NULL,
  `blood_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKliydr8oh4dopfyyj2ksjtngln` (`blood_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `blood_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_note` varchar(255) DEFAULT NULL,
  `bags_needed` int(11) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `data_use_authorized` bit(1) DEFAULT NULL,
  `deadline_at` datetime(6) DEFAULT NULL,
  `directed_donors_required` varchar(255) DEFAULT NULL,
  `has_deadline` bit(1) DEFAULT NULL,
  `hospital_confirmation` varchar(255) DEFAULT NULL,
  `hospital_location` varchar(255) DEFAULT NULL,
  `hospital_name` varchar(255) DEFAULT NULL,
  `information_confirmed` bit(1) DEFAULT NULL,
  `inventory_deducted` bit(1) NOT NULL,
  `needed_at` datetime(6) DEFAULT NULL,
  `patient_admitted` bit(1) DEFAULT NULL,
  `patient_age` int(11) DEFAULT NULL,
  `patient_blood_type` varchar(255) DEFAULT NULL,
  `patient_name` varchar(255) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `relationship_to_patient` varchar(255) DEFAULT NULL,
  `requested_blood_type` varchar(255) DEFAULT NULL,
  `requester_email` varchar(255) DEFAULT NULL,
  `requester_name` varchar(255) DEFAULT NULL,
  `requester_phone` varchar(255) DEFAULT NULL,
  `specific_blood_type_required` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `supporting_document` longblob DEFAULT NULL,
  `supporting_document_name` varchar(255) DEFAULT NULL,
  `supporting_document_provided` bit(1) DEFAULT NULL,
  `transfusion_status` varchar(255) DEFAULT NULL,
  `urgency` varchar(255) DEFAULT NULL,
  `ward_number` varchar(255) DEFAULT NULL,
  `requester_user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe5byrij0vu5st570bwx0ltb7b` (`requester_user_id`),
  CONSTRAINT `FKe5byrij0vu5st570bwx0ltb7b` FOREIGN KEY (`requester_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `donation_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bags` int(11) NOT NULL,
  `blood_type` varchar(255) DEFAULT NULL,
  `donation_date` date DEFAULT NULL,
  `inventory_applied` bit(1) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `donor_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp4v549bqwtfk0i996meftj2p9` (`donor_id`),
  CONSTRAINT `FKp4v549bqwtfk0i996meftj2p9` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `eligibility_assessments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `age` int(11) NOT NULL,
  `current_illness` bit(1) NOT NULL,
  `donated_before` bit(1) NOT NULL,
  `feeling_well` bit(1) NOT NULL,
  `last_donation_date` date DEFAULT NULL,
  `medical_condition` bit(1) NOT NULL,
  `next_eligible_date` date DEFAULT NULL,
  `pregnant_or_recently_gave_birth` bit(1) NOT NULL,
  `previously_deferred` bit(1) NOT NULL,
  `prior_transfusion_or_transplant` bit(1) NOT NULL,
  `readiness_score` int(11) NOT NULL,
  `recent_dental_procedure` bit(1) NOT NULL,
  `recent_risk_travel` bit(1) NOT NULL,
  `recent_surgery` bit(1) NOT NULL,
  `recent_tattoo_or_piercing` bit(1) NOT NULL,
  `recent_vaccination` bit(1) NOT NULL,
  `result_reasons` varchar(1500) DEFAULT NULL,
  `result_status` varchar(255) DEFAULT NULL,
  `submitted_at` datetime(6) DEFAULT NULL,
  `taking_medication` bit(1) NOT NULL,
  `weight_kg` double NOT NULL,
  `donor_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKoufd6nq5yneip1t23nk459fnw` (`donor_id`),
  CONSTRAINT `FKoufd6nq5yneip1t23nk459fnw` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
