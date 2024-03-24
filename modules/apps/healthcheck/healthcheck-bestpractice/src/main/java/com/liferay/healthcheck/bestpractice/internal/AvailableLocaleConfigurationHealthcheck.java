/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.PropsValues;

import java.text.DateFormat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;

/**
 * Some installations simplified the locales to just two letters for the
 * language. In some cases this lead to follow-up problems, as none of the
 * OOTB languages omits a country/locale.
 *
 * This check makes sure that available languages have at least 5 characters
 * (e.g. "en_GB"), and are supported by the underlying JVM
 *
 * (Note: On Java8, this typically triggers a few locales that are used by
 * Liferay, but that the underlying JVM does not provide - with this check,
 * at least this condition can be signalled to the administrator)
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class AvailableLocaleConfigurationHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		LinkedList<HealthcheckItem> result = new LinkedList<>();

		_validateProperties(result, PropsValues.LOCALES, "locales");
		_validateProperties(
			result, PropsValues.LOCALES_ENABLED, "locales.enabled");
		_validateProperties(result, PropsValues.LOCALES_BETA, "locales.beta");

		if (result.isEmpty()) {
			result.add(new HealthcheckItem(true, _LINK, _MSG));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private void _validateProperties(
		LinkedList<HealthcheckItem> result, String[] locales,
		String collectionName) {

		for (String loc : locales) {

			// some JVM naming seems to be different from ours...

			String alternativeLoc = StringUtil.replace(loc, "latin", "#Latn");

			if (loc.length() < 5) {
				result.add(
					new HealthcheckItem(
						false, _LINK, _MSG_SHORT_LOCALE, loc, collectionName));
			}

			if (!(_availableLocales.contains(loc) ||
				  _availableLocales.contains(alternativeLoc))) {

				result.add(
					new HealthcheckItem(
						false, _LINK, _MSG_MISSING_LOCALE, loc,
						collectionName));
			}
		}
	}

	private static final String _LINK =
		"https://docs.liferay.com/portal/7.4-latest/propertiesdoc/portal." +
			"properties.html#Languages%20and%20Time%20Zones";

	private static final String _MSG = "configured-locales-are-provided-by-jvm";

	private static final String _MSG_MISSING_LOCALE =
		"configured-locale-x-from-section-x-not-provided-by-jvm";

	private static final String _MSG_SHORT_LOCALE =
		"configured-locale-x-from-section-x-has-less-than-5-characters-length";

	private static final Set<String> _availableLocales = new TreeSet<>();

	{
		for (Locale locale : DateFormat.getAvailableLocales()) {
			_availableLocales.add(locale.toString());
		}
	}

}