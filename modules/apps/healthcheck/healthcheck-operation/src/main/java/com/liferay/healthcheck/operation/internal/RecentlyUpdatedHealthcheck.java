/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;
import com.liferay.portal.kernel.util.StringUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class RecentlyUpdatedHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		String version = ReleaseInfo.getVersionDisplayName();
		String term;
		String message;

		if (ReleaseInfo.isDXP()) {
			term = "Update ";
			message = "healthcheck-recently-updated-dxp";
		}
		else {
			term = "CE GA";
			message = "healthcheck-recently-updated-ce";
		}

		int updatePos = version.indexOf(term);

		if (updatePos > 0) {
			int update = GetterUtil.getInteger(
				version.substring(updatePos + term.length()));
			int expectedActualUpdate =
				_guessCurrentlyExpectedUpdate() - _acceptableMissingUpdates;

			return Arrays.asList(
				new HealthcheckItem(
					this, update > expectedActualUpdate,
					StringUtil.merge(
						new String[] {
							getClass().getName(),
							String.valueOf(expectedActualUpdate)
						},
						"-"),
					null, message, update, expectedActualUpdate));
		}

		// might be a quarterly release, e.g. "2023.Q4.1"

		Pattern pattern = Pattern.compile(_QUARTERLY_PATTERN);

		Matcher matcher = pattern.matcher(version);

		if (matcher.matches()) {
			int year = Integer.valueOf(matcher.group(1));
			int quarter = Integer.valueOf(matcher.group(2));
			//			int patch = Integer.valueOf(matcher.group(3));
			message = "healthcheck-recent-quarterly-dxp";

			int ageInQuarters = _getAgeInQuarters(year, quarter);

			return Arrays.asList(
				new HealthcheckItem(
					this, ageInQuarters <= _acceptableAgeInQuarters,
					getClass().getName(), null, message, version,
					_acceptableAgeInQuarters, ageInQuarters));
		}

		return Arrays.asList(
			new HealthcheckItem(
				this, false, getClass().getName(), null,
				"healthcheck-recently-updated-couldnt-compute", version));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		HealthcheckOperationalConfiguration
			healthcheckOperationalConfiguration =
				ConfigurableUtil.createConfigurable(
					HealthcheckOperationalConfiguration.class, properties);

		_acceptableMissingUpdates =
			healthcheckOperationalConfiguration.acceptableMissingUpdates();
		_acceptableAgeInQuarters =
			healthcheckOperationalConfiguration.acceptableAgeInQuarters();
	}

	private int _getAgeInQuarters(int releaseYear, int releaseQuarter) {
		LocalDate now = LocalDate.now();

		int currentYear = now.getYear();

		int month = now.getMonthValue();

		int currentQuarter = 1 + (int)((month - 1) / 3);

		return ((currentYear - releaseYear) * 4) +
			(currentQuarter - releaseQuarter - 1);
	}

	private int _guessCurrentlyExpectedUpdate() {

		// Update 15 was released on 11.March 2022 - assuming
		// weekly releases since then, which held true until U69
		// at the time of writing this code.
		// Ignore all timezone magic and Date/Time Math Elegance:
		// We're calculating in the granularity of _weeks_

		LocalDate u15rel = LocalDate.of(2022, 3, 11);
		LocalDate now = LocalDate.now();

		long timePassed = ChronoUnit.DAYS.between(u15rel, now);

		return (int)(timePassed / 7) + 15;
	}

	private static final String _QUARTERLY_PATTERN =
		"^(\\d{4})\\.Q(\\d)\\.(\\d)$";

	private volatile int _acceptableAgeInQuarters;
	private volatile int _acceptableMissingUpdates;

}