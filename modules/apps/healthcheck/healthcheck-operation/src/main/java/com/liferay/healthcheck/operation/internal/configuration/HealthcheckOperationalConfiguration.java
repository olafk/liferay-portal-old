/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Olaf Kock
 */
@ExtendedObjectClassDefinition(category = "healthcheck")
@Meta.OCD(
	description = "healthcheck-operational-configuration-description",
	id = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration",
	localization = "content/Language",
	name = "healthcheck-operational-configuration-name"
)
public interface HealthcheckOperationalConfiguration {

	@Meta.AD(
		deflt = "90",
		description = "healthcheck-operational-remaining-activation-period-description",
		name = "healthcheck-operational-remaining-activation-period-name",
		required = false
	)
	public Integer remainingActivationPeriod();

	@Meta.AD(
		deflt = "22",
		description = "healthcheck-operational-acceptable-missing-updates-description",
		name = "healthcheck-operational-acceptable-missing-updates-name",
		required = false
	)
	public Integer acceptableMissingUpdates();

	@Meta.AD(
		deflt = "3",
		description = "healthcheck-operational-acceptable-age-in-quarters-description",
		name = "healthcheck-operational-acceptable-age-in-quarters-name",
		required = false
	)
	public Integer acceptableAgeInQuarters();

	@Meta.AD(
		description = "healthcheck-operational-client-extension-host-whitelist-description",
		name = "healthcheck-operational-client-extension-host-whitelist-name",
		required = false
	)
	public String[] clientExtensionHostWhitelist();

	@Meta.AD(
		description = "healthcheck-operational-dataprovider-host-whitelist-description",
		name = "healthcheck-operational-dataprovider-host-whitelist-name",
		required = false
	)
	public String[] dataProviderHostWhitelist();

}