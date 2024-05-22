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

import java.util.Arrays;
import java.util.Collection;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Checks if the host name detector is active. It can be limited in number of inspected requests,
 * or deactivated completely - this will need to a limited ability of a few healthchecks that rely
 * on knowing which virtual host names are actually requested. Limiting the performance impact
 * is the reason for the decision to go with a deactivated detector to begin with, but the fact that
 * these healthchecks won't be running, should be signalled to the user, so that they can make an
 * informed choice.
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class HostnameDetectionHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		return Arrays.asList(
			new HealthcheckItem(
				_hostnameDetector.isActive(), _CONFIGURATION_LINK, _MSG));
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

	protected static final String PID =
		"com.liferay.redirect.internal.configuration.RedirectURLConfiguration";

	private static final String _CONFIGURATION_LINK = StringBundler.concat(
		"/group/control_panel/manage?p_p_id=",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "&_",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_factoryPid=",
		HostnameDetectionHealthcheck._PID, "&_",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS,
		"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_",
		ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_pid=",
		HostnameDetectionHealthcheck._PID);

	private static final String _MSG =
		"optional-hostname-detection-can-provide-more-healthchecks";

	private static final String _PID =
		"com.liferay.healthcheck.operation.internal.configuration." +
			"HealthcheckOperationalSystemConfiguration";

	private static final Log _log = LogFactoryUtil.getLog(
		HostnameDetectionHealthcheck.class);

	@Reference
	private HostnameDetector _hostnameDetector;

}