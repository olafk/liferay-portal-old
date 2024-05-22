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
	category = "healthcheck", scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
	description = "healthcheck-operational-system-configuration-description",
	id = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration",
	localization = "content/Language",
	name = "healthcheck-operational-system-configuration-name"
)
public interface HealthcheckOperationalSystemConfiguration {

	@Meta.AD(
		deflt = "1000",
		description = "healthcheck-operational-max-filter-executions-description",
		name = "healthcheck-operational-max-filter-executions-name",
		required = false
	)
	public int maxFilterExecutions();

}