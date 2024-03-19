/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class DefaultSiteInitializerUserHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		Collection<HealthcheckItem> result = new LinkedList<>();

		for (String user : _KNOWN_USERS) {
			result.addAll(_checkForUser(companyId, user));
		}

		if (result.isEmpty()) {
			Object[] info = {};

			result.add(
				new HealthcheckItem(
					this, true, getClass().getName(), null, _MSG, info));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private Collection<HealthcheckItem> _checkForUser(
		long companyId, String mailAddress) {

		try {
			User user = _userLocalService.getUserByEmailAddress(
				companyId, mailAddress);

			if (user != null) {
				Object[] info = {mailAddress};

				String link = new StringBundler(
					_LINK
				).append(
					_LINK_PARAMETER
				).append(
					user.getUserId()
				).toString();

				return Arrays.asList(
					new HealthcheckItem(
						this, false, getClass().getName(), link, _MSG_FOUND,
						info));
			}
		}
		catch (NoSuchUserException noSuchUserException) {

			// ignore - this is great and exactly what we're after.

		}
		catch (PortalException portalException) {
			return Arrays.asList(new HealthcheckItem(this, portalException));
		}

		return new LinkedList<>();
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

	private static final String _MSG =
		"healthcheck-bestpractice-siteinitializer-user";

	private static final String _MSG_FOUND =
		"healthcheck-bestpractice-siteinitializer-user-found";

	@Reference
	private UserLocalService _userLocalService;

}