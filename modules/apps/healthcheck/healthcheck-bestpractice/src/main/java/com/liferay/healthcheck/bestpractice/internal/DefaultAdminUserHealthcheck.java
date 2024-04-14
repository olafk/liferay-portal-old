/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptorUtil;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * For a long time, the well known default administrator account
 * test@liferay.com was created in new systems. Make sure that this
 * account is not around on this system, especially when still
 * using the default password "test".
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class DefaultAdminUserHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId)
		throws PortalException {

		try {
			User user = _userLocalService.getUserByEmailAddress(
				companyId, "test@liferay.com");

			if (user != null) {
				String hashedPassword = PasswordEncryptorUtil.encrypt(
					"test", user.getPassword());

				String parameterizedLink = StringBundler.concat(
					_LINK, _LINK_PARAMETER, user.getUserId());

				return Arrays.asList(
					new HealthcheckItem(
						Objects.equals(user.getPassword(), hashedPassword),
						parameterizedLink, _MSG));
			}
		}
		catch (PortalException portalException) {
			if (!(portalException instanceof NoSuchUserException)) {
				throw portalException;
			}
		}

		return Arrays.asList(new HealthcheckItem(true, _LINK, _MSG));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private static final String _LINK =
		"/group/control_panel/manage?p_p_id=com_liferay_users_admin_web_" +
			"portlet_UsersAdminPortlet";

	private static final String _LINK_PARAMETER =
		"&_com_liferay_users_admin_web_portlet_UsersAdminPortlet_" +
			"mvcRenderCommandName=%2Fusers_admin%2Fedit_user&_com_liferay_" +
				"users_admin_web_portlet_UsersAdminPortlet_p_u_i_d=";

	private static final String _MSG =
		"default-account-testliferaycom-should-not-exist-with-default-pwd";

	@Reference
	private UserLocalService _userLocalService;

}