/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;

/**
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class DatabaseEncodingHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		Collection<HealthcheckItem> result = new LinkedList<>();

		DB db = DBManagerUtil.getDB();

		try (Connection connection = DataAccess.getConnection()) {
			DBType dbType = db.getDBType();
			String encoding = "not-yet-implemented";
			boolean correctEncoding = false;
			String connectionURL = "no URL detected";

			if (DBType.MYSQL.equals(dbType) || DBType.MARIADB.equals(dbType)) {
				encoding = "undetected";

				connectionURL = connection.getMetaData(
				).getURL();

				String schema = _extractSchema(connectionURL);

				if (schema != null) {
					PreparedStatement preparedStatement =
						connection.prepareStatement(
							"SELECT default_character_set_name FROM " +
								"information_schema.SCHEMATA WHERE " +
									"schema_name = ?");

					preparedStatement.setString(1, schema);

					ResultSet resultSet = preparedStatement.executeQuery();

					if (resultSet.next()) {
						encoding = resultSet.getString(1);

						if ((encoding != null) &&
							encoding.toLowerCase(
							).startsWith(
								"utf8"
							)) {

							correctEncoding = true;
						}
					}
				}
				else {
					result.add(
						new HealthcheckItem(
							this, false, getClass().getName(),
							_LINK_DB_CONNECTION, _MSG_SCHEMA_UNDETECTED,
							connectionURL));
				}
			}
			else if (DBType.ORACLE.equals(dbType)) {

				// TODO

			}
			else if (DBType.POSTGRESQL.equals(dbType)) {

				// TODO

			}
			else if (DBType.DB2.equals(dbType)) {

				// TODO

			}
			else if (DBType.SQLSERVER.equals(dbType)) {

				// TODO

			}
			else if (DBType.SYBASE.equals(dbType)) {

				// TODO

			}
			else if (DBType.HYPERSONIC.equals(dbType)) {
				result.add(
					new HealthcheckItem(
						this, false, getClass().getName(), _LINK_DB_CONNECTION,
						_MSG_HSQL));

				return result;
			}
			else {
				result.add(
					new HealthcheckItem(
						this, false, getClass().getName(), _LINK_DB_CONNECTION,
						_MSG_DB_UNDETECTED));

				return result;
			}

			result.add(
				new HealthcheckItem(
					this, correctEncoding, getClass().getName(),
					_LINK_DB_CONNECTION, _MSG, encoding, dbType,
					connectionURL));
		}
		catch (SQLException sqlException) {
			result.add(new HealthcheckItem(this, sqlException));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private String _extractSchema(String connectionURL) {

		// assume something like jdbc:mariadb://localhost/lportal?something
		// very crude deciphering, the initial regexp-try looked like
		// write-only code...

		int pos = connectionURL.indexOf("//");

		if (pos >= 0) {
			pos = connectionURL.indexOf("/", pos + 2);

			if (pos > -1) {
				String result = connectionURL.substring(pos + 1);

				pos = result.indexOf("?");

				if (pos > -1) {
					result = result.substring(0, pos);
				}

				return result;
			}
		}

		return null;
	}

	private static final String _LINK_DB_CONNECTION =
		"https://learn.liferay.com/w/dxp/installation-and-upgrades" +
			"/installing-liferay/configuring-a-database";

	private static final String _MSG = "healthcheck-database-encoding";

	private static final String _MSG_DB_UNDETECTED =
		"healthcheck-database-undetected";

	private static final String _MSG_HSQL =
		"healthcheck-database-hsql-for-demo";

	private static final String _MSG_SCHEMA_UNDETECTED =
		"healthcheck-database-schema-undetected";

}