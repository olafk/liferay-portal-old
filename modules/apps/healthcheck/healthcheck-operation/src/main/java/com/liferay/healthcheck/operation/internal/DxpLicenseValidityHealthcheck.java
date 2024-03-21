/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.license.util.LicenseManagerUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Olaf Kock
 */
@Component(
	configurationPid = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration",
	service = Healthcheck.class
)
public class DxpLicenseValidityHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		if (ReleaseInfo.isDXP()) {
			Map<String, String> licenseProperties =
				LicenseManagerUtil.getLicenseProperties("Portal");

			long expires = Long.valueOf(
				licenseProperties.get("expirationDate"));

			long now = new Date(
			).getTime();

			long remainingMillis = expires - now;

			long remainingDays = remainingMillis / (1000 * 60 * 60 * 24);

			return Arrays.asList(
				new HealthcheckItem(
					this, remainingDays > _warningPeriod,
					getClass().getName() + "-" + (int)(remainingDays / 7), null,
					"healthcheck-license-key-validity-period", remainingDays,
					_warningPeriod));
		}

		return Collections.emptyList();
	}

	@Override
	public String getCategory() {
		return "health-check-category-operation";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_warningPeriod = ConfigurableUtil.createConfigurable(
			HealthcheckOperationalConfiguration.class, properties
		).remainingActivationPeriod();
	}

	private volatile int _warningPeriod = 90;

}