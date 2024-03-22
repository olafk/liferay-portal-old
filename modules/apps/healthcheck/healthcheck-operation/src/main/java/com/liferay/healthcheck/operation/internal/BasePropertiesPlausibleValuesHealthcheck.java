/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.util.PropsValues;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This Healthcheck performs the necessary operations for typechecking values
 * set in portal*.properties configurations due to LPS-157829.
 *
 * This can point out configuration errors, where the same file contains a value
 * twice, generating an illegal (and unexpected) configuration value.
 *
 * @author Olaf Kock
 */
public abstract class BasePropertiesPlausibleValuesHealthcheck<T>
	implements Healthcheck {

	public BasePropertiesPlausibleValuesHealthcheck(
		Class<T> clazz, String msg, String errorMsg, Log log) {

		_clazz = clazz;
		_msg = msg;
		_errorMsg = errorMsg;
		_log = log;
	}

	public Collection<HealthcheckItem> check(
		long companyId, PropertyValidator validator) {

		List<HealthcheckItem> result = new LinkedList<>();

		List<String> theProperties = _getProperties();

		if (_log.isInfoEnabled()) {
			_log.info("number of properties found: " + theProperties.size());
		}

		for (String property : theProperties) {
			String value = PropsUtil.get(property);

			if (value != null) {
				if (!validator.isValid(value)) {
					result.add(
						new HealthcheckItem(
							this, false, getClass().getName(), _LINK, _errorMsg,
							property, value));
				}
			}
			else {

				// This is a field defined in PropsValues, but
				// undefined in any portal*.properties file

				if (_log.isWarnEnabled()) {
					_log.warn("null " + property);
				}
			}
		}

		if (result.isEmpty()) {
			result.add(
				new HealthcheckItem(
					this, true, getClass().getName(), _LINK, _msg));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	public interface PropertyValidator {

		public boolean isValid(String string);

	}

	private String _exceptionForFieldName(
		Exception exception, String fieldName) {

		return new StringBundler(
		).append(
			exception.getClass(
			).getName()
		).append(
			" "
		).append(
			exception.getMessage()
		).append(
			" for "
		).append(
			fieldName
		).toString();
	}

	private List<String> _getProperties() {
		ArrayList<String> props = new ArrayList<>(50);

		Field[] fields = PropsValues.class.getFields();

		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) &&
				Objects.equals(field.getType(), _clazz)) {

				String property = _getProperty(field.getName());

				if (property != null) {
					props.add(property);
				}
			}
		}

		return props;
	}

	private String _getProperty(String fieldName) {
		Field field;

		try {
			field = PropsKeys.class.getField(fieldName);

			if ((field != null) && Modifier.isStatic(field.getModifiers()) &&
				Objects.equals(field.getType(), String.class)) {

				return (String)field.get(null);
			}
		}
		catch (NoSuchFieldException noSuchFieldException) {
			if (!_knownMissingProperties.contains(fieldName)) {
				_log.error("No such field: PropsKeys." + fieldName);

				return _exceptionForFieldName(noSuchFieldException, fieldName);
			}
		}
		catch (Exception exception) {
			_log.error(exception);

			return _exceptionForFieldName(exception, fieldName);
		}

		return null;
	}

	private static final String _LINK = new StringBundler(
	).append(
		"/group/control_panel/manage?p_p_id=&"
	).append(
		BasePropertiesPlausibleValuesHealthcheck._SERVER_ADMIN_PORTLET
	).append(
		"&_"
	).append(
		BasePropertiesPlausibleValuesHealthcheck._SERVER_ADMIN_PORTLET
	).append(
		"_mvcRenderCommandName=%2Fserver_admin%2Fview&_"
	).append(
		BasePropertiesPlausibleValuesHealthcheck._SERVER_ADMIN_PORTLET
	).append(
		"_tabs1=properties&_"
	).append(
		BasePropertiesPlausibleValuesHealthcheck._SERVER_ADMIN_PORTLET
	).append(
		"_screenNavigationCategoryKey=portal-properties"
	).toString();

	private static final String _SERVER_ADMIN_PORTLET =
		"com_liferay_server_admin_web_portlet_ServerAdminPortlet";

	/**
	 * Fields that are present in PropsValues, but are known to not be present in
	 * PropsKeys for various reasons (e.g. they might be implemented without
	 * properties, or just convenience lookups derived from other properties)
	 */
	private static final Set<String> _knownMissingProperties = new HashSet<>(
		Arrays.asList(
			"FEATURE_FLAGS_JSON", "LIFERAY_WEB_PORTAL_CONTEXT_TEMPDIR",
			"PORTLET_EVENT_DISTRIBUTION_LAYOUT",
			"PORTLET_EVENT_DISTRIBUTION_LAYOUT_SET",
			"PORTLET_PUBLIC_RENDER_PARAMETER_DISTRIBUTION_LAYOUT",
			"PORTLET_PUBLIC_RENDER_PARAMETER_DISTRIBUTION_LAYOUT_SET"));

	private final Class<T> _clazz;
	private final String _errorMsg;
	private final Log _log;

	// initialized in constructor

	private final String _msg;

}