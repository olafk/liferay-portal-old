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
@ExtendedObjectClassDefinition(
	category = "healthcheck",
	scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	description = "healthcheck-operational-configuration-description",
	id = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration",
	localization = "content/Language",
	name = "healthcheck-operational-configuration-name"
)
public interface HealthcheckOperationalConfiguration {

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