/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.auxiliary.HostNameExtractingFilter;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.CompanyLocalService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.servlet.Filter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Healthcheck for relaxed-security Demo Systems Demo systems, especially on
 * LXC-SM, are often restored from systems that are configured for a different
 * virtual host name. Make sure that the currently configured virtual host name
 * has at least been accessed once during the current uptime of the system
 * (typically this happens at least when the healthcheck report page is
 * accessed)
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class VirtualHostHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId)
		throws PortalException {

		String configuredHostname = _companyLocalService.getCompany(
			companyId
		).getVirtualHostname();

		HostNameExtractingFilter hostNameExtractingFilter =
			(HostNameExtractingFilter)_filter;

		Set<String> requestedHostnames =
			hostNameExtractingFilter.getAccessedUrls(companyId);

		if (requestedHostnames.contains("https://" + configuredHostname) ||
			requestedHostnames.contains("http://" + configuredHostname)) {

			return Arrays.asList(
				new HealthcheckItem(
					this, true, getClass().getName(), _LINK, _MSG,
					configuredHostname));
		}

		return Arrays.asList(
			new HealthcheckItem(
				this, false, getClass().getName(), _LINK, _MSG_ERROR,
				configuredHostname));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private static final String _LINK = new StringBundler(
	).append(
		"/group/control_panel/manage?p_p_id="
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_mvcRenderCommandName="
	).append(
		"%2Fconfiguration_admin%2Fview_configuration_screen&_"
	).append(
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS
	).append(
		"_configurationScreenKey=general"
	).toString();

	private static final String _MSG =
		"healthcheck-configured-virtualhost-has-been-accessed";

	private static final String _MSG_ERROR =
		"healthcheck-configured-virtualhost-has-not-been-accessed-yet-check-c" +
			"ompany-virtual-host";

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference(
		target = "(servlet-filter-name=Healthcheck Hostname Extracting Filter)"
	)
	private Filter _filter;

}