/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;

/**
 * This Healthcheck makes sure that portal*.properties configurations for
 * boolean values are purely set to one of "true" or "false" (due to
 * LPS-157829).
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
@SuppressWarnings("rawtypes")
public class BooleanPropertiesPlausibleValuesHealthcheck
	extends BasePropertiesPlausibleValuesHealthcheck {

	@SuppressWarnings("unchecked")
	public BooleanPropertiesPlausibleValuesHealthcheck() {
		super(boolean.class, _MSG, _ERROR_MSG, _log);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<HealthcheckItem> check(long companyId) {
		return super.check(
			companyId, value -> value.equals("true") || value.equals("false"));
	}

	private static final String _ERROR_MSG =
		"detected-boolean-property-x-configured-as-x";

	private static final String _MSG =
		"all-boolean-properties-seem-to-be-configured-to-boolean-values";

	private static final Log _log = LogFactoryUtil.getLog(
		BooleanPropertiesPlausibleValuesHealthcheck.class);

}