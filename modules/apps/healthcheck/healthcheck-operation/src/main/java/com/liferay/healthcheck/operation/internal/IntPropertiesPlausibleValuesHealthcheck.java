/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;

/**
 * This Healthcheck makes sure that portal*.properties configurations for int
 * values are purely set to numerical values (due to LPS-157829).
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
@SuppressWarnings("rawtypes")
public class IntPropertiesPlausibleValuesHealthcheck
	extends BasePropertiesPlausibleValuesHealthcheck {

	@SuppressWarnings("unchecked")
	public IntPropertiesPlausibleValuesHealthcheck() {
		super(int.class, _MSG, _ERROR_MSG, _log);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<HealthcheckItem> check(long companyId) {
		return check(
			companyId,
			value ->
				!(value.contains(",") ||
				  ((GetterUtil.getInteger(value) == 0) && !value.equals("0"))));
	}

	private static final String _ERROR_MSG =
		"detected-int-property-x-configured-as-x";

	private static final String _MSG =
		"all-int-properties-seem-to-be-configured-to-int-values";

	private static final Log _log = LogFactoryUtil.getLog(
		IntPropertiesPlausibleValuesHealthcheck.class);

}