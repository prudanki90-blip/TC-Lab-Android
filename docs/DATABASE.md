TC Lab — Database Schema

1. Purpose

This document defines the initial data model for TC Lab, a native, offline-first Android application for plant tissue culture learning, research, laboratory inventory, experiment management, and analysis.

The database must support beginners and advanced users, preserve experimental history, work without internet, and keep TC Lab separate from SOAP LAB.

2. Database Design Principles

- Use a local relational database, planned around SQLite.
- Use stable unique IDs for records and explicit relationships between related records.
- Store dates as consistent timestamps and display them in the phone's local time zone.
- Store numerical measurements with explicit units.
- Preserve original observations and historical protocol versions.
- Separate published evidence, personal experimental measurements, and educational guidance.
- Record the source of scientific claims and the review status of protocols.
- Validate data before calculations or imports.
- Support schema migrations, export, backup, and restoration.
- Store photographs as files in app-private storage, with their metadata and file references in the database.
- Never silently overwrite experimental data.

3. Core Tables

3.1 species

Stores plant species, cultivars, genotypes, and propagation information.

Fields:

- id: primary key
- botanical_name
- common_name
- family
- cultivar_name
- genotype_or_rootstock
- plant_type
- source_material_notes
- climate_suitability
- propagation_difficulty
- known_challenges
- general_notes
- created_at
- updated_at

Initial priorities include Geneva apple rootstocks, pomegranate, and breadfruit. Cultivar and genotype must remain distinguishable from species.

3.2 protocols

Stores tissue culture protocols and their versions.

Fields:

- id: primary key
- protocol_group_id: stable identifier grouping versions
- title
- species_id: optional foreign key
- cultivar_or_genotype
- culture_stage: establishment, multiplication, rooting, or acclimatization
- protocol_version
- instructions
- medium_formulation_id: optional foreign key
- temperature_value
- temperature_unit
- light_conditions
- photoperiod_hours
- duration_value
- duration_unit
- timing_notes
- evidence_status
- review_status
- limitations
- created_at
- updated_at

Each published or approved version must remain identifiable. Updating a protocol must not silently change an experiment that used an earlier version.

3.3 protocol_sources

Stores source and evidence information for protocols.

Fields:

- id: primary key
- protocol_id: foreign key
- source_type: journal article, book, accessible document, website, or other
- title
- authors
- publication_year
- journal_or_publisher
- DOI
- source_url
- access_date
- page_or_table_reference
- source_excerpt_or_note
- evidence_quality
- access_status
- created_at

Store citations and necessary notes rather than copying entire copyrighted publications without permission.

3.4 protocol_parameters

Stores individual protocol values and their provenance.

Fields:

- id: primary key
- protocol_id: foreign key
- parameter_name
- parameter_value
- unit
- source_id: optional foreign key
- source_location
- value_status: reported, derived, user_measured, or suggested
- review_status
- uncertainty_notes

This table helps identify where concentrations, pH values, temperatures, durations, and other protocol details originated.

3.5 chemicals

Stores laboratory chemical and reagent information.

Fields:

- id: primary key
- chemical_name
- alternate_name
- CAS_number: optional
- chemical_category
- formula
- molar_mass_g_mol: optional
- stock_concentration_value
- stock_concentration_unit
- physical_form
- hazard_notes
- PPE_requirements
- storage_conditions
- waste_handling_notes
- source_reference
- created_at
- updated_at

Stock product concentration must be distinguished from final working concentration.

3.6 chemical_inventory

Tracks actual quantities of chemicals held by the user.

Fields:

- id: primary key
- chemical_id: foreign key
- product_name
- supplier
- lot_number
- stock_concentration_value
- stock_concentration_unit
- quantity_value
- quantity_unit
- received_date
- opened_date
- expiry_date
- storage_location
- safety_document_reference
- notes
- updated_at

Different lots or stock concentrations must be allowed as separate inventory records.

3.7 media_formulations

Stores named culture medium formulations.

Fields:

- id: primary key
- name
- formulation_version
- medium_type
- species_id: optional foreign key
- culture_stage
- final_volume_value
- final_volume_unit
- target_pH
- gelling_agent
- gelling_agent_concentration
- preparation_instructions
- sterilization_method
- sterilization_parameters
- source_protocol_id: optional foreign key
- review_status
- notes
- created_at
- updated_at

A formulation is a planned recipe. Actual preparation measurements belong in a separate preparation record.

3.8 media_ingredients

Stores the ingredients belonging to a formulation.

Fields:

- id: primary key
- formulation_id: foreign key
- chemical_id: optional foreign key
- ingredient_name
- planned_quantity
- quantity_unit
- target_concentration
- concentration_unit
- addition_order
- preparation_notes

Preserve units and distinguish mass, volume, molarity, and mass-per-volume concentration. Do not assume different units are interchangeable.

3.9 equipment

Stores laboratory equipment and its status.

Fields:

- id: primary key
- name
- equipment_category
- specifications
- quantity
- condition
- safety_requirements
- maintenance_schedule
- last_maintenance_date
- estimated_cost
- currency
- supplier
- supplier_url
- purchase_status
- notes

Estimated prices in KES must be marked as estimates and verified before purchase.

3.10 batches

Represents a culture batch or experiment.

Fields:

- id: primary key
- batch_code: unique human-readable identifier
- species_id: foreign key
- cultivar_or_genotype
- source_material_description
- parent_batch_id: optional self-referencing foreign key
- protocol_id: optional foreign key
- protocol_version_used
- formulation_id: optional foreign key
- current_stage
- start_date
- status
- planned_quantity
- quantity_unit
- location
- notes
- created_at
- updated_at

Preserve the actual protocol version used, even if the protocol library changes later.

3.11 media_preparations

Records actual preparation of culture medium.

Fields:

- id: primary key
- batch_id: optional foreign key
- formulation_id: foreign key
- formulation_version_used
- preparation_date
- actual_final_volume
- final_volume_unit
- measured_pH
- pH_adjustment_notes
- actual_ingredient_records
- sterilization_method
- sterilization_record
- prepared_by
- outcome
- notes
- created_at

Planned and actual quantities must remain distinguishable. Record deviations instead of silently replacing planned values.

3.12 batch_tasks

Stores scheduled culture work and reminders.

Fields:

- id: primary key
- batch_id: foreign key
- protocol_id: optional foreign key
- task_type
- title
- instructions
- recommended_interval_value
- recommended_interval_unit
- scheduled_at
- reminder_at
- status: planned, due, completed, skipped, rescheduled, or overdue
- completed_at
- completion_notes
- reschedule_reason
- created_at
- updated_at

Keep the recommended interval separate from the scheduled date. Never assume every plant requires a medium change at the same interval.

3.13 observations

Stores dated observations and measurements.

Fields:

- id: primary key
- batch_id: foreign key
- task_id: optional foreign key
- observed_at
- culture_stage
- observation_type
- measurement_value
- measurement_unit
- contamination_status
- growth_description
- environmental_conditions
- notes
- evidence_type
- created_at

Observations must be appendable without destroying earlier records.

3.14 batch_images

Stores image metadata and links images to culture records.

Fields:

- id: primary key
- batch_id: foreign key
- observation_id: optional foreign key
- file_reference
- capture_date
- caption
- image_category
- checksum
- created_at

Image files should be stored in app-private storage. Database records must not depend on temporary file-picker paths.

3.15 culture_lineage

Tracks transfers and generations of cultured material.

Fields:

- id: primary key
- parent_batch_id: foreign key
- child_batch_id: foreign key
- transfer_date
- generation_number
- transferred_quantity
- quantity_unit
- transfer_notes
- created_at

A batch may produce multiple child batches. Prevent invalid relationships and accidental lineage cycles.

3.16 experiment_results

Stores calculated or measured outcomes.

Fields:

- id: primary key
- batch_id: foreign key
- result_type
- numerator_value
- denominator_value
- result_value
- result_unit
- calculation_method
- observation_period
- evidence_type
- notes
- created_at

Support multiplication rate, rooting percentage, contamination rate, acclimatization survival, and cost per plantlet. Record the formula and relevant counts used for each calculation.

4. Supporting Tables

4.1 task_history

Records task status changes, completions, skips, and rescheduling.

Fields:

- id: primary key
- task_id: foreign key
- previous_status
- new_status
- event_timestamp
- reason
- notes

4.2 app_settings

Stores user preferences and application settings.

Fields:

- setting_key: primary key
- setting_value
- updated_at

Do not store passwords, secret API keys, or other sensitive credentials as ordinary settings.

4.3 schema_migrations

Tracks database schema upgrades.

Fields:

- version: primary key
- migration_name
- applied_at
- checksum

Every database upgrade must preserve existing user data or stop safely and report the problem.

5. Evidence and Review Rules

Every scientific record should distinguish among:

1. Published information: values reported by an identifiable source.
2. User measurements: observations and measurements from the user's own experiments.
3. Derived results: values calculated from recorded inputs using a documented formula.
4. Educational guidance or suggestions: information not yet validated for the user's specific material.

Protocol review status should include draft, needs_review, approved_for_use, and retired.

Approval does not mean a protocol is guaranteed to work for every genotype. Tissue culture alone does not prove that a plant is pathogen-free.

6. Data Integrity and Validation

- Give each record a stable primary key.
- Define foreign keys and enable foreign-key enforcement.
- Use database transactions for related changes.
- Validate required fields, units, dates, and numerical ranges.
- Reject invalid or incomplete calculations rather than inventing missing inputs.
- Preserve original measurements and record corrections with an audit history where appropriate.
- Prevent duplicate batch codes and invalid parent-child relationships.
- Use soft deletion or explicit confirmation where deleting records could break experimental history.
- Never silently overwrite existing data during import or restore.

7. Time and Reminder Rules

Store scheduled task timestamps consistently and display them using the phone's local time zone.

Record scientific intervals separately from scheduled dates. Preserve completed-task history, including the actual completion timestamp and any rescheduling reason.

After a device restart, restore pending reminder scheduling where supported. Notifications depend on Android permissions and operating-system restrictions.

8. Offline Storage and Backups

The local database is the primary working store.

The app must support:

- Creating an export or backup.
- Saving it through the Android file picker.
- Validating imported data before restoration.
- Restoring into an empty or test database.
- Warning about conflicts before merging or replacing records.
- Including referenced photographs in a complete backup.
- Verifying that restored records and image references remain valid.

Explain that uninstalling the app can remove app-private data. Encourage regular backups.

9. Research Extraction Data

Research extraction should create draft protocol records and parameter records linked to citations and source locations.

The extractor must preserve:

- Source document and retrieval date.
- Species and cultivar, when reported.
- Culture stage.
- Medium ingredients and concentrations.
- Units, pH, conditions, and timing.
- Reported outcomes.
- Page, table, section, or passage references where available.
- Missing, ambiguous, or conflicting values.
- Human review status.

Do not transfer unreviewed extracted values automatically into active formulations or experiments.

10. Development and Testing Order

1. Finalize this schema and choose the Android technology stack.
2. Create the native Android project and reproducible build workflow.
3. Implement the local database and schema migrations.
4. Implement plant, protocol, chemical, medium, and equipment records.
5. Implement batches, observations, photographs, and lineage.
6. Implement scheduled tasks, reminders, and history.
7. Implement calculators and experiment results.
8. Implement research extraction with citations and human review.
9. Implement backup, import, restoration, and conflict handling.
10. Test offline behavior, persistence, migrations, notifications, and recovery.
11. Produce a release APK and document installation and backup procedures.

11. Initial Acceptance Tests

- The database can be created on first launch.
- Foreign-key relationships reject invalid references.
- Records remain after the app is closed and reopened.
- Measurements preserve their units and original values.
- Protocol versions remain identifiable in existing batches.
- Tasks preserve recommended intervals, due dates, and completion history.
- Calculated results can be traced to their inputs and formulas.
- Photographs remain linked after app restart.
- Export and restoration preserve records and image references.
- Schema migrations do not silently discard data.
- Published findings, user measurements, derived results, and suggestions remain distinguishable.

12. Implementation Note

This document defines the intended data model, not a completed database. Before implementation, review the relationships, select the Android language and build configuration, and translate the approved schema into versioned database migrations. Test each migration before using it with important experimental data.
