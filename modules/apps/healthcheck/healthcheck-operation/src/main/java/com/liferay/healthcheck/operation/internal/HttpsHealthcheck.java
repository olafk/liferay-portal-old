/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.HostnameDetector;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HtmlUtil;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * It's 2024 (when I write this check). Make sure we're accessed through https
 * only. Unless we're on localhost
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class HttpsHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		String year = String.valueOf(
			Calendar.getInstance(
			).get(
				Calendar.YEAR
			)); // just for rubbing it in in the message
		Set<String> urls = _hostnameDetector.getAccessedUrls(companyId);
		Collection<HealthcheckItem> result = new LinkedList<>();

		if (urls.isEmpty()) {
			result.add(
				new HealthcheckItem(
					false, _CONFIGURATION_LINK, _MSG, year, "----"));
		}

		for (String requestedUrl : urls) {
			String scheme = _extractScheme(requestedUrl);
			String host = _extractHost(requestedUrl);

			if ((host != null) &&
				(StringUtil.equalsIgnoreCase(host, "localhost") ||
				 host.toLowerCase(
				 ).startsWith(
					 "localhost:"
				 ))) {

				result.add(
					new HealthcheckItem(
						true, _CONFIGURATION_LINK, _MSG_LOCALHOST, year,
						scheme));
			}
			else {
				result.add(
					new HealthcheckItem(
						(scheme != null) &&
						StringUtil.equalsIgnoreCase(scheme, "https"),
						_CONFIGURATION_LINK, _MSG, year, requestedUrl));
			}
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Activate
	protected void activate() {
		if (_log.isDebugEnabled()) {
			_log.debug("Activating");
		}
	}

	@Deactivate
	protected void deactivate() {
		if (_log.isDebugEnabled()) {
			_log.debug("Deactivating");
		}
	}

	private String _extractHost(String url) {
		if (url == null) {
			return "null";
		}

		int separatorIndex = url.indexOf("://");

		if (separatorIndex < 1) { // not found, and should have a scheme leading up to it

			return "???";
		}

		return HtmlUtil.escape(url.substring(separatorIndex + 3));
	}

	private String _extractScheme(String url) {
		if (url == null) {
			return "null";
		}

		int separatorIndex = url.indexOf("://");

		if (separatorIndex < 1) { // not found, and should have a scheme leading up to it

			return "???";
		}

		return HtmlUtil.escape(url.substring(0, separatorIndex));
	}

	private static final String _CONFIGURATION_LINK = StringBundler.concat(
		"/group/control_panel/manage?p_p_id=",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "&_",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_factoryPid=",
		HttpsHealthcheck._PID, "&_",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS,
		"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_pid=",
		HttpsHealthcheck._PID);

	private static final String _MSG =
		"in-x-a-webserver-should-be-accessed-through-https-detected-x";

	private static final String _MSG_LOCALHOST =
		"in-x-a-webserver-should-be-accessed-through-https-detected-x" +
			"[localhost]";

	private static final String _PID =
		"com.liferay.healthcheck.operation.internal.configuration." +
			"HealthcheckOperationalSystemConfiguration";

	private static final Log _log = LogFactoryUtil.getLog(
		HttpsHealthcheck.class);

	@Reference
	private HostnameDetector _hostnameDetector;

}