/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Several SiteInitializers create default users - once such a SiteInitializer
 * has been used, those might be present in production systems. Check for
 * their existence and trigger admins to delete them or ignore, if this is a
 * dev system, and if it's acceptable to have an account where _everybody_
 * can reset the password (e.g. through mailinator.com).
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class DefaultSiteInitializerUserHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		Collection<HealthcheckItem> result = new LinkedList<>();

		for (String user : _KNOWN_USERS) {
			_checkForUser(companyId, result, user);
		}

		if (result.isEmpty()) {
			result.add(new HealthcheckItem(true, null, _MSG));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private void _checkForUser(
		long companyId, Collection<HealthcheckItem> result,
		String mailAddress) {

		User user = _userLocalService.fetchUserByEmailAddress(
			companyId, mailAddress);

		if (user != null) {
			String link = StringBundler.concat(
				_LINK, _LINK_PARAMETER, user.getUserId());

			result.add(
				new HealthcheckItem(false, link, _MSG_FOUND, mailAddress));
		}
	}

	private static final String[] _KNOWN_USERS = {
		"test.user1@liferay.com", "test.user2.update@liferay.com",
		"test.user3@liferay.com", "test.user1@liferay.com",
		"test.user2@liferay.com", "scott.producer@mailinator.com",
		"marie.producer@mailinator.com", "ryan.underwriter@mailinator.com",
		"clark.insured@mailinator.com", "administrator@testray.com",
		"analyst@testray.com", "lead@testray.com", "user@testray.com",
		"j@acme.com", "s@acme.com", "john.developer@mailinator.com",
		"marie.developer@mailinator.com", "ryan.administrator@mailinator.com",
		"clark.customer@mailinator.com", "pm@partner.com", "pmu@partner.com",
		"psu@partner.com", "ptu@partner.com", "cam@liferaytest.com",
		"com@liferaytest.com", "cmm@liferaytest.com", "cmd@liferaytest.com",
		"cfm@liferaytest.com", "cem@liferaytest.com", "test@liferay.com",
		"employee@liferay.com", "manager@liferay.com", "finance@liferay.com"
	};

	private static final String _LINK =
		"/group/control_panel/manage?p_p_id=com_liferay_users_admin_web_" +
			"portlet_UsersAdminPortlet";

	private static final String _LINK_PARAMETER =
		"&_com_liferay_users_admin_web_portlet_UsersAdminPortlet_" +
			"mvcRenderCommandName=%2Fusers_admin%2Fedit_user&_com_liferay_" +
				"users_admin_web_portlet_UsersAdminPortlet_p_u_i_d=";

	private static final String _MSG = "no-default-siteinitializer-user-found";

	private static final String _MSG_FOUND =
		"found-well-known-siteinitializer-user-account-x";

	@Reference
	private UserLocalService _userLocalService;

}