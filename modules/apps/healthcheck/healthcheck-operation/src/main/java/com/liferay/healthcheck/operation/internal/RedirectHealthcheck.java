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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Portal;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Checks if redirection to all accessed hosts is allowed. If it isn't allowed,
 * the UI sometimes behaves in unexpected ways, and it's easy to fix once
 * you know where to look for it.
 *
 * @author Olaf Kock
 */
@Component(
	configurationPid = RedirectHealthcheck.PID,
	property = Constants.SERVICE_PID + "=" + RedirectHealthcheck.PID + ".scoped",
	service = {Healthcheck.class, ManagedServiceFactory.class}
)
public class RedirectHealthcheck implements Healthcheck, ManagedServiceFactory {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		Set<String> urls = _hostnameDetector.getAccessedUrls(companyId);
		Collection<HealthcheckItem> result = new LinkedList<>();

		for (String requestedUrl : urls) {
			String url = _portal.escapeRedirect(requestedUrl);

			result.add(
				new HealthcheckItem(
					url != null, _CONFIGURATION_LINK, _MSG,
					_extractHost(requestedUrl)));
		}

		return result;
	}

	@Override
	public void deleted(String pid) {
		Set<Map.Entry<Long, String>> es =
			_companyToRedirectConfigPid.entrySet();

		for (Map.Entry<Long, String> entry : es) {
			if (Objects.equals(entry.getValue(), pid)) {
				Long companyId = entry.getKey();

				_companyToRedirectConfigPid.remove(companyId);

				if (_log.isDebugEnabled()) {
					_log.debug("removing redirection config for " + companyId);
				}

				break;
			}
		}
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Override
	public String getName() {
		return PID + ".scoped";
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties)
		throws ConfigurationException {

		long companyId = (Long)properties.get("companyId");

		_companyToRedirectConfigPid.put(companyId, pid);

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"adding redirection config for ", companyId, " as ", pid));
		}
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

	protected static final String PID =
		"com.liferay.redirect.internal.configuration.RedirectURLConfiguration";

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

	private static final String _CONFIGURATION_LINK = new StringBundler(
		"/group/control_panel/manage?p_p_id="
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_factoryPid="
	).append(
		PID
	).append(
		"&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_pid="
	).append(
		PID
	).toString();

	private static final String _MSG = "redirection-to-x-should-be-allowed";

	private static final Log _log = LogFactoryUtil.getLog(
		RedirectHealthcheck.class);

	private final HashMap<Long, String> _companyToRedirectConfigPid =
		new HashMap<>();

	@Reference
	private HostnameDetector _hostnameDetector;

	@Reference
	private Portal _portal;

}