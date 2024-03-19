/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PwdEncryptorException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.comparator.UserEmailAddressComparator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Check if all existing user accounts save passwords with the currently
 * configured hashing algorithm. Danger: Might run a while for large user
 * databases. Currently Healthchecks are a proof of concept, so this aspect is
 * ignored/accepted
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class UserPasswordPBKDF2HashHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		String hashingAlgorithm = PropsUtil.get(
			PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM);

		if (hashingAlgorithm == null) {
			return Arrays.asList(
				new HealthcheckItem(
					this, false, getClass().getName(), _LINK,
					"healthcheck-best-practice-pbkdf2-user-unconfigured-" +
						"algorithm"));
		}
		else if (!hashingAlgorithm.startsWith(_PBKDF2_WITH_HMAC_SHA1)) {
			Object[] info = {hashingAlgorithm};

			return Arrays.asList(
				new HealthcheckItem(
					this, true, getClass().getName(), _LINK,
					"healthcheck-best-practice-pbkdf2-unknown-hashing-" +
						"algorithm-assuming-ok",
					info));
		}

		HashMap<String, Long> algorithms = new HashMap<>();
		LinkedList<HealthcheckItem> result = new LinkedList<>();

		int usersCount = _userLocalService.getUsersCount(
			companyId, WorkflowConstants.STATUS_APPROVED);
		int counted = 0;
		int pageSize = 100;

		for (int i = 0; i <= (usersCount / pageSize); i++) {
			List<User> users;

			try {
				users = _userLocalService.getUsers(
					companyId, WorkflowConstants.STATUS_APPROVED, i * pageSize,
					(i + 1) * pageSize, new UserEmailAddressComparator());

				for (User user : users) {
					_countUp(algorithms, _getAlgorithm(user.getPassword()));

					counted++;
				}
			}
			catch (Throwable throwable) {
				String msg = new StringBundler(
					throwable.getClass(
					).getName()
				).append(
					" "
				).append(
					throwable.getMessage()
				).toString();

				_countUp(algorithms, msg);
			}
		}

		if (counted != usersCount) {
			Object[] info = {_numOfTotal(usersCount - counted, usersCount)};

			result.add(
				new HealthcheckItem(
					this, false, getClass().getName(), _LINK,
					"healthcheck-best-practice-user-count-mismatch-x-uncounted",
					info));
		}

		for (HashMap.Entry<String, Long> entry : algorithms.entrySet()) {
			if (entry.getKey(
				).equalsIgnoreCase(
					hashingAlgorithm
				)) {

				Object[] info = {
					_numOfTotal(entry.getValue(), usersCount), hashingAlgorithm
				};

				result.add(
					new HealthcheckItem(
						this, true, getClass().getName(), _LINK,
						"healthcheck-best-practice-pbkdf2-found-x-entries-" +
							"with-default-algorithm-y",
						info));
			}
			else {
				Object[] info = {
					_numOfTotal(entry.getValue(), usersCount), entry.getKey(),
					hashingAlgorithm
				};

				result.add(
					new HealthcheckItem(
						this, false, getClass().getName(), _LINK,
						"healthcheck-best-practice-pbkdf2-found-x-entries-" +
							"with-nondefault-algorithm-y-looking-for-z",
						info));
			}
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private void _countUp(Map<String, Long> algorithms, String algorithm) {
		Long currentValue = algorithms.get(algorithm);

		if (currentValue == null) {
			currentValue = Long.valueOf(0);
		}

		currentValue++;
		algorithms.put(algorithm, currentValue);
	}

	private String _getAlgorithm(String encryptedPassword)
		throws PwdEncryptorException {

		String alg = "unknown";

		if (encryptedPassword.charAt(0) == '{') {
			int index = encryptedPassword.indexOf('}');

			if (index > 0) {
				alg = encryptedPassword.substring(1, index);
			}
		}

		if (StringUtil.equalsIgnoreCase(alg, _PBKDF2_WITH_HMAC_SHA1)) {
			encryptedPassword = encryptedPassword.substring(
				_PBKDF2_WITH_HMAC_SHA1.length() + 2); // +2 for {}

			ByteBuffer byteBuffer = ByteBuffer.wrap(
				Base64.decode(encryptedPassword));

			StringBundler msgSB = new StringBundler(alg);

			try {
				int keySize = byteBuffer.getInt();
				int rounds = byteBuffer.getInt();

				msgSB.append(
					CharPool.SLASH
				).append(
					keySize
				).append(
					CharPool.SLASH
				).append(
					rounds
				);

				return msgSB.toString();
			}
			catch (BufferUnderflowException bufferUnderflowException) {
				msgSB.append(
					"/?/?/"
				).append(
					bufferUnderflowException.getMessage()
				);

				return msgSB.toString();
			}
		}
		else if (alg.equals("BCRYPT")) {

			// TODO unknown encoding of work factor

		}

		return alg;
	}

	private String _numOfTotal(long num, long total) {
		return new StringBundler(
		).append(
			num
		).append(
			CharPool.SLASH
		).append(
			total
		).toString();
	}

	private static final String _LINK =
		"https://liferay.dev/blogs/-/blogs/hashing-performance";

	private static final String _PBKDF2_WITH_HMAC_SHA1 = "PBKDF2WithHmacSHA1";

	@Reference
	private UserLocalService _userLocalService;

}