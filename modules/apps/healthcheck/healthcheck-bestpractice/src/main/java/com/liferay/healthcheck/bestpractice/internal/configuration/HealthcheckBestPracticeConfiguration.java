/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Olaf Kock
 */
@ExtendedObjectClassDefinition(category = "healthcheck")
@Meta.OCD(
	description = "healthcheck-bestpractice-configuration-description",
	id = "com.liferay.healthcheck.bestpractice.internal.configuration.HealthcheckBestPracticeConfiguration",
	localization = "content/Language",
	name = "healthcheck-bestpractice-configuration-name"
)
public interface HealthcheckBestPracticeConfiguration {

	@Meta.AD(
		deflt = "500",
		description = "healthcheck-bestpractice-maximum-simple-store-files-description",
		name = "healthcheck-bestpractice-maximum-simple-store-files-name",
		required = false
	)
	public Integer maximumSimpleStoreFiles();

	@Meta.AD(
		deflt = "5000000",
		description = "healthcheck-bestpractice-doclib-minimum-usable-space-description",
		name = "healthcheck-bestpractice-doclib-minimum-usable-space-name",
		required = false
	)
	public Long minimumUsableSpace();

	@Meta.AD(
		deflt = "1300000",
		description = "healthcheck-bestpractice-owasp-pbkdf2withhmacsha1-hashing-recommendation-description",
		name = "healthcheck-bestpractice-owasp-pbkdf2withhmacsha1-hashing-recommendation-name",
		required = false
	)
	public Long owaspHashingRecommendation();

}