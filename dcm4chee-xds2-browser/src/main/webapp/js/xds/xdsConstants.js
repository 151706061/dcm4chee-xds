angular.module('xds.common').factory('xdsConstants', function() {
	return {
		"APND" : "urn:ihe:iti:2007:AssociationType:APND",
		"HAS_MEMBER" : "urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember",

		"QRY_ASSOCIATION_TYPES" : "$AssociationTypes",
		"QRY_DOCUMENT_ENTRY_AUTHOR_PERSON" : "$XDSDocumentEntryAuthorPerson",
		"QRY_DOCUMENT_ENTRY_CLASS_CODE" : "$XDSDocumentEntryClassCode",
		"QRY_DOCUMENT_ENTRY_CONFIDENTIALITY_CODE" : "$XDSDocumentEntryConfidentialityCode",
		"QRY_DOCUMENT_ENTRY_CREATION_TIME_FROM" : "$XDSDocumentEntryCreationTimeFrom",
		"QRY_DOCUMENT_ENTRY_CREATION_TIME_TO" : "$XDSDocumentEntryCreationTimeTo",
		"QRY_DOCUMENT_ENTRY_EVENT_CODE_LIST" : "$XDSDocumentEntryEventCodeList",
		"QRY_DOCUMENT_ENTRY_FORMAT_CODE" : "$XDSDocumentEntryFormatCode",
		"QRY_DOCUMENT_ENTRY_HEALTHCARE_FACILITY_TYPE_CODE" : "$XDSDocumentEntryHealthcareFacilityTypeCode",
		"QRY_DOCUMENT_ENTRY_PATIENT_ID" : "$XDSDocumentEntryPatientId",
		"QRY_DOCUMENT_ENTRY_PRACTICE_SETTING_CODE" : "$XDSDocumentEntryPracticeSettingCode",
		"QRY_DOCUMENT_ENTRY_SERVICE_START_TIME_FROM" : "$XDSDocumentEntryServiceStartTimeFrom",
		"QRY_DOCUMENT_ENTRY_SERVICE_START_TIME_TO" : "$XDSDocumentEntryServiceStartTimeTo",
		"QRY_DOCUMENT_ENTRY_SERVICE_STOP_TIME_FROM" : "$XDSDocumentEntryServiceStopTimeFrom",
		"QRY_DOCUMENT_ENTRY_SERVICE_STOP_TIME_TO" : "$XDSDocumentEntryServiceStopTimeTo",
		"QRY_DOCUMENT_ENTRY_STATUS" : "$XDSDocumentEntryStatus",
		"QRY_DOCUMENT_ENTRY_TYPE_CODE" : "$XDSDocumentEntryTypeCode",
		"QRY_DOCUMENT_ENTRY_UUID" : "$XDSDocumentEntryEntryUUID",
		"QRY_DOCUMENT_UNIQUE_ID" : "$XDSDocumentEntryUniqueId",
		"QRY_FOLDER_CODE_LIST" : "$XDSFolderCodeList",
		"QRY_FOLDER_ENTRY_UUID" : "$XDSFolderEntryUUID",
		"QRY_FOLDER_LAST_UPDATE_TIME_FROM" : "$XDSFolderLastUpdateTimeFrom",
		"QRY_FOLDER_LAST_UPDATE_TIME_TO" : "$XDSFolderLastUpdateTimeTo",
		"QRY_FOLDER_PATIENT_ID" : "$XDSFolderPatientId",
		"QRY_FOLDER_STATUS" : "$XDSFolderStatus",
		"QRY_FOLDER_UNIQUE_ID" : "$XDSFolderUniqueId",
		"QRY_HOME_COMMUNITY_ID" : "$homeCommunityId",
		"QRY_PATIENT_ID" : "$patientId",
		"QRY_SUBMISSIONSET_AUTHOR_PERSON" : "$XDSSubmissionSetAuthorPerson",
		"QRY_SUBMISSIONSET_CONTENT_TYPE" : "$XDSSubmissionSetContentType",
		"QRY_SUBMISSIONSET_ENTRY_UUID" : "$XDSSubmissionSetEntryUUID",
		"QRY_SUBMISSIONSET_PATIENT_ID" : "$XDSSubmissionSetPatientId",
		"QRY_SUBMISSIONSET_SOURCE_ID" : "$XDSSubmissionSetSourceId",
		"QRY_SUBMISSIONSET_STATUS" : "$XDSSubmissionSetStatus",
		"QRY_SUBMISSIONSET_SUBMISSION_TIME_FROM" : "$XDSSubmissionSetSubmissionTimeFrom",
		"QRY_SUBMISSIONSET_SUBMISSION_TIME_TO" : "$XDSSubmissionSetSubmissionTimeTo",
		"QRY_SUBMISSIONSET_UNIQUE_ID" : "$XDSSubmissionSetUniqueId",
		"QRY_UUID" : "$uuid",
		"QUERY_RETURN_TYPE_LEAF" : "LeafClass",
		"QUERY_RETURN_TYPE_OBJREF" : "ObjectRef",
		"RELATED_TO" : "urn:oasis:names:tc:ebxml-regrep:AssociationType:relatedTo",
		"RPLC" : "urn:ihe:iti:2007:AssociationType:RPLC",
		"SIGNS" : "urn:ihe:iti:2007:AssociationType:signs",
		"SLOT_NAME_AUTHOR_PERSON" : "authorPerson",
		"SLOT_NAME_CREATION_TIME" : "creationTime",
		"SLOT_NAME_HASH" : "hash",
		"SLOT_NAME_INTENDED_RECIPIENT" : "intendedRecipient",
		"SLOT_NAME_LANGUAGE_CODE" : "languageCode",
		"SLOT_NAME_LAST_UPDATE_TIME" : "lastUpdateTime",
		"SLOT_NAME_LEGAL_AUTHENTICATOR" : "legalAuthenticator",
		"SLOT_NAME_REPOSITORY_UNIQUE_ID" : "repositoryUniqueId",
		"SLOT_NAME_SERVICE_START_TIME" : "serviceStartTime",
		"SLOT_NAME_SERVICE_STOP_TIME" : "serviceStopTime",
		"SLOT_NAME_SIZE" : "size",
		"SLOT_NAME_SOURCE_PATIENT_ID" : "sourcePatientId",
		"SLOT_NAME_SOURCE_PATIENT_INFO" : "sourcePatientInfo",
		"SLOT_NAME_SUBMISSIONSET_STATUS" : "SubmissionSetStatus",
		"SLOT_NAME_SUBMISSION_TIME" : "submissionTime",
		"STATUS_APPROVED" : "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved",
		"STATUS_DEPRECATED" : "urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated",
		"STATUS_SUBMITTED" : "urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted",
		B_Statuses : [ {
			name : "Any status",
			val : "('urn:oasis:names:tc:ebxml-regrep:StatusType:Approved', " +
					"'urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated', " +
					"'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted')"
		}, {
			name : "Status APPROVED",
			val : "('urn:oasis:names:tc:ebxml-regrep:StatusType:Approved')"
		}, {
			name : "Status DEPRECATED",
			val : "('urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated')"
		}, {
			name : "Status SUBMITTED",
			val : "('urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted')"
		} ]

		,
		"UUID_Association_Documentation" : "urn:uuid:abd807a3-4432-4053-87b4-fd82c643d1f3",
		"UUID_XDSDocumentEntry" : "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1",
		"UUID_XDSDocumentEntryStub" : "urn:uuid:10aa1a4b-715a-4120-bfd0-9760414112c8",
		"UUID_XDSDocumentEntry_author" : "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d",
		"UUID_XDSDocumentEntry_classCode" : "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a",
		"UUID_XDSDocumentEntry_confidentialityCode" : "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f",
		"UUID_XDSDocumentEntry_eventCodeList" : "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4",
		"UUID_XDSDocumentEntry_formatCode" : "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d",
		"UUID_XDSDocumentEntry_healthCareFacilityTypeCode" : "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1",
		"UUID_XDSDocumentEntry_limitedMetadata" : "urn:uuid:ab9b591b-83ab-4d03-8f5d-f93b1fb92e85",
		"UUID_XDSDocumentEntry_patientId" : "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427",
		"UUID_XDSDocumentEntry_practiceSettingCode" : "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead",
		"UUID_XDSDocumentEntry_typeCode" : "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983",
		"UUID_XDSDocumentEntry_uniqueId" : "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab",
		"UUID_XDSFolder" : "urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2",
		"UUID_XDSFolder_codeList" : "urn:uuid:1ba97051-7806-41a8-a48b-8fce7af683c5",
		"UUID_XDSFolder_limitedMetadata" : "urn:uuid:2c144a76-29a9-4b7c-af54-b25409fe7d03",
		"UUID_XDSFolder_patientId" : "urn:uuid:f64ffdf0-4b97-4e06-b79f-a52b38ec2f8a",
		"UUID_XDSFolder_uniqueId" : "urn:uuid:75df8f67-9973-4fbe-a900-df66cefecc5a",
		"UUID_XDSSubmissionSet" : "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd",
		"UUID_XDSSubmissionSet_autor" : "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d",
		"UUID_XDSSubmissionSet_contentTypeCode" : "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500",
		"UUID_XDSSubmissionSet_limitedMetadata" : "urn:uuid:5003a9db-8d8d-49e6-bf0c-990e34ac7707",
		"UUID_XDSSubmissionSet_patientId" : "urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446",
		"UUID_XDSSubmissionSet_sourceId" : "urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832",
		"UUID_XDSSubmissionSet_uniqueId" : "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8",
		"WS_ADDRESSING_ANONYMOUS" : "http://www.w3.org/2005/08/addressing/anonymous",
		"WS_ADDRESSING_NS" : "http://www.w3.org/2005/08/addressing",
		"XDS_B_STATUS_FAILURE" : "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure",
		"XDS_B_STATUS_PARTIAL_SUCCESS" : "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:PartialSuccess",
		"XDS_B_STATUS_SUCCESS" : "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success",

		B_FindEntities : {
			"XDS_FindDocuments" : "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d",
			"XDS_FindFolders" : "urn:uuid:958f3006-baad-4929-a4de-ff1114824431",
			"XDS_FindSubmissionSets" : "urn:uuid:f26abbcb-ac74-4422-8a30-edb644bbc1a9",
		},
		"XDS_GetAll" : "urn:uuid:10b545ea-725c-446d-9b95-8aeb444eddf3",
		"XDS_GetAssociations" : "urn:uuid:a7ae438b-4bc2-4642-93e9-be891f7bb155",
		"XDS_GetDocuments" : "urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4",
		"XDS_GetDocumentsAndAssociations" : "urn:uuid:bab9529a-4a10-40b3-a01f-f68a615d247a",
		"XDS_GetFolderAndContents" : "urn:uuid:b909a503-523d-4517-8acf-8e5834dfc4c7",
		"XDS_GetFolders" : "urn:uuid:5737b14c-8a1a-4539-b659-e03a34a5e1e4",
		"XDS_GetSubmissionSetAndContents" : "urn:uuid:e8e3cb2c-e39c-46b9-99e4-c12f57260b83",
		"XDS_GetSubmissionSets" : "urn:uuid:51224314-5390-4169-9b91-b1980040715a",

		"XDS_Association" : "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association",

		"XDS_GetFoldersForDocument" : "urn:uuid:10cae35a-c7f9-4cf5-b61e-fc3278ffb578",
		"XDS_GetRelatedDocuments" : "urn:uuid:d90e5407-b356-4d91-a89f-873917b4b0e6",

		"XFRM" : "urn:ihe:iti:2007:AssociationType:XFRM",
		"XFRM_RPLC" : "urn:ihe:iti:2007:AssociationType:XFRM_RPLC"
	};
});