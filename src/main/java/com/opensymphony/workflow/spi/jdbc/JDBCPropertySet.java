package com.opensymphony.workflow.spi.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.util.Data;

public class JDBCPropertySet extends AbstractPropertySet
{
	private static final Log log = LogFactory.getLog(JDBCPropertySet.class);

	protected DataSource ds;
	protected String colData;
	protected String colDate;
	protected String colFloat;
	protected String colGlobalKey;
	protected String colItemKey;
	protected String colItemType;
	protected String colNumber;
	protected String colString;
	protected String globalKey;
	protected String tableName;
	protected boolean closeConnWhenDone = false;

	public DataSource getDs() {
		return ds;
	}

	public void setDs(DataSource ds) {
		this.ds = ds;
	}

	public Collection getKeys(String prefix, int type)
			throws PropertyException
	{
		if (prefix == null) {
			prefix = "";
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = getConnection();

			String sql = "SELECT " + this.colItemKey + " FROM " + this.tableName + " WHERE " + this.colItemKey + " LIKE ? AND " + this.colGlobalKey + " = ?";

			if (type == 0) {
				ps = conn.prepareStatement(sql);
				ps.setString(1, prefix + "%");
				ps.setString(2, this.globalKey);
			} else {
				sql = sql + " AND " + this.colItemType + " = ?";
				ps = conn.prepareStatement(sql);
				ps.setString(1, prefix + "%");
				ps.setString(2, this.globalKey);
				ps.setInt(3, type);
			}

			ArrayList list = new ArrayList();
			rs = ps.executeQuery();

			while (rs.next()) {
				list.add(rs.getString(this.colItemKey));
			}

			return list;
		} catch (SQLException e) {
			throw new PropertyException(e.getMessage());
		} finally {
			cleanup(conn, ps, rs);
		}
	}

	public int getType(String key) throws PropertyException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = getConnection();

			String sql = "SELECT " + this.colItemType + " FROM " + this.tableName + " WHERE " + this.colGlobalKey + " = ? AND " + this.colItemKey + " = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, this.globalKey);
			ps.setString(2, key);

			rs = ps.executeQuery();

			int type = 0;

			if (rs.next()) {
				type = rs.getInt(this.colItemType);
			}

			return type;
		} catch (SQLException e) {
			throw new PropertyException(e.getMessage());
		} finally {
			cleanup(conn, ps, rs);
		}
	}

	public boolean exists(String key) throws PropertyException {
		return getType(key) != 0;
	}

	public void init(Map config, Map args)
	{
		this.globalKey = ((String) args.get("globalKey"));

		this.tableName = "OS_PROPERTYENTRY";
		this.colGlobalKey = "GLOBAL_KEY";
		this.colItemKey = "ITEM_KEY";
		this.colItemType = "ITEM_TYPE";
		this.colString = "STRING_VALUE";
		this.colDate = "DATE_VALUE";
		this.colData = "DATA_VALUE";
		this.colFloat = "FLOAT_VALUE";
		this.colNumber = "NUMBER_VALUE";
	}

	public void remove() throws PropertyException {
		Connection conn = null;
		PreparedStatement ps = null;
		try
		{
			conn = getConnection();

			String sql = "DELETE FROM " + this.tableName + " WHERE " + this.colGlobalKey + " = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, this.globalKey);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new PropertyException(e.getMessage());
		} finally {
			cleanup(conn, ps, null);
		}
	}

	public void remove(String key) throws PropertyException {
		Connection conn = null;
		PreparedStatement ps = null;
		try
		{
			conn = getConnection();

			String sql = "DELETE FROM " + this.tableName + " WHERE " + this.colGlobalKey + " = ? AND " + this.colItemKey + " = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, this.globalKey);
			ps.setString(2, key);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new PropertyException(e.getMessage());
		} finally {
			cleanup(conn, ps, null);
		}
	}

	public boolean supportsType(int type) {
		switch (type) {
		case 6:
		case 9:
		case 11:
			return false;
		}

		return true;
	}

	protected Connection getConnection() throws SQLException {
		this.closeConnWhenDone = true;

		return this.ds.getConnection();
	}

	protected void setImpl(int type, String key, Object value) throws PropertyException {
		if (value == null) {
			throw new PropertyException("JDBCPropertySet does not allow for null values to be stored");
		}

		Connection conn = null;
		PreparedStatement ps = null;
		try
		{
			conn = getConnection();

			String sql = "UPDATE " + this.tableName + " SET " + this.colString + " = ?, " + this.colDate + " = ?, " + this.colData + " = ?, " + this.colFloat + " = ?, " + this.colNumber + " = ?, "
					+ this.colItemType + " = ? " + " WHERE " + this.colGlobalKey + " = ? AND " + this.colItemKey + " = ?";
			ps = conn.prepareStatement(sql);
			setValues(ps, type, key, value);

			int rows = ps.executeUpdate();

			if (rows != 1)
			{
				sql = "INSERT INTO " + this.tableName + " (" + this.colString + ", " + this.colDate + ", " + this.colData + ", " + this.colFloat + ", " + this.colNumber + ", " + this.colItemType
						+ ", " + this.colGlobalKey + ", " + this.colItemKey + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				ps.close();
				ps = conn.prepareStatement(sql);
				setValues(ps, type, key, value);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			throw new PropertyException(e.getMessage());
		} finally {
			cleanup(conn, ps, null);
		}
	}

	protected void cleanup(Connection connection, Statement statement, ResultSet result) {
		if (result != null) {
			try {
				result.close();
			} catch (SQLException ex) {
				log.error("Error closing resultset", ex);
			}
		}

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException ex) {
				log.error("Error closing statement", ex);
			}
		}

		if ((connection != null) && (this.closeConnWhenDone))
			try {
				connection.close();
			} catch (SQLException ex) {
				log.error("Error closing connection", ex);
			}
	}

	protected Object get(int type, String key) throws PropertyException
	{
		String sql = "SELECT " + this.colItemType + ", " + this.colString + ", " + this.colDate + ", " + this.colData + ", " + this.colFloat + ", " + this.colNumber + " FROM " + this.tableName
				+ " WHERE " + this.colItemKey + " = ? AND " + this.colGlobalKey + " = ?";

		Object o = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			conn = getConnection();

			ps = conn.prepareStatement(sql);
			ps.setString(1, key);
			ps.setString(2, this.globalKey);

			rs = ps.executeQuery();

			if (rs.next()) {
				int propertyType = rs.getInt(this.colItemType);

				if (propertyType != type) {
					throw new InvalidPropertyTypeException();
				}

				switch (type)
				{
				case 1:
					int boolVal = rs.getInt(this.colNumber);
					o = new Boolean(boolVal == 1);

					break;
				case 10:
					o = rs.getBytes(this.colData);

					break;
				case 7:
					o = rs.getTimestamp(this.colDate);

					break;
				case 8:
					InputStream bis = rs.getBinaryStream(this.colData);
					try
					{
						ObjectInputStream is = new ObjectInputStream(bis);
						o = is.readObject();
					} catch (IOException e) {
						throw new PropertyException("Error de-serializing object for key '" + key + "' from store:" + e);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

				case 4:
					o = new Double(rs.getDouble(this.colFloat));

					break;
				case 2:
					o = new Integer(rs.getInt(this.colNumber));

					break;
				case 3:
					o = new Long(rs.getLong(this.colNumber));

					break;
				case 5:
					o = rs.getString(this.colString);

					break;
				case 6:
					o = rs.getString(this.colString);

					break;
				case 9:
				default:
					throw new InvalidPropertyTypeException("JDBCPropertySet doesn't support this type yet.");
				}
			}
		} catch (SQLException e) {
			throw new PropertyException(e.getMessage());
		} catch (NumberFormatException e) {
			throw new PropertyException(e.getMessage());
		} finally {
			cleanup(conn, ps, rs);
		}

		return o;
	}

	private void setValues(PreparedStatement ps, int type, String key, Object value) throws SQLException, PropertyException {
		ps.setNull(1, 12);
		ps.setNull(2, 93);
		ps.setNull(3, -3);
		ps.setNull(4, 6);
		ps.setNull(5, 2);
		ps.setInt(6, type);
		ps.setString(7, this.globalKey);
		ps.setString(8, key);

		switch (type)
		{
		case 1:
			Boolean boolVal = (Boolean) value;
			ps.setInt(5, boolVal.booleanValue() ? 1 : 0);

			break;
		case 10:
			if ((value instanceof Data)) {
				Data data = (Data) value;
				ps.setBytes(3, data.getBytes());
			}

			if ((value instanceof byte[]))
				ps.setBytes(3, (byte[]) value);
			break;
		case 8:
			if (!(value instanceof Serializable)) {
				throw new PropertyException(value.getClass() + " does not implement java.io.Serializable");
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try
			{
				ObjectOutputStream os = new ObjectOutputStream(bos);
				os.writeObject(value);
				ps.setBytes(3, bos.toByteArray());
			} catch (IOException e) {
				throw new PropertyException("I/O Error when serializing object:" + e);
			}

		case 7:
			Date date = (Date) value;
			ps.setTimestamp(2, new Timestamp(date.getTime()));

			break;
		case 4:
			Double d = (Double) value;
			ps.setDouble(4, d.doubleValue());

			break;
		case 2:
			Integer i = (Integer) value;
			ps.setInt(5, i.intValue());

			break;
		case 3:
			Long l = (Long) value;
			ps.setLong(5, l.longValue());

			break;
		case 5:
			ps.setString(1, (String) value);

			break;
		case 6:
			ps.setString(1, (String) value);

			break;
		case 9:
		default:
			throw new PropertyException("This type isn't supported!");
		}
	}
}