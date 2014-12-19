/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.jdbc;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.query.*;
import com.opensymphony.workflow.spi.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * JDBC implementation.
 * <p/>
 * <p/>
 * The following properties are all <b>required</b>:
 * <ul>
 * <li><b>datasource</b> - the JNDI location for the DataSource that is to be
 * used.</li>
 * <li><b>entry.sequence</b> - SQL query that returns the next ID for a workflow
 * entry</li>
 * <li><b>entry.table</b> - table name for workflow entry</li>
 * <li><b>entry.id</b> - column name for workflow entry ID field</li>
 * <li><b>entry.name</b> - column name for workflow entry name field</li>
 * <li><b>entry.state</b> - column name for workflow entry state field</li>
 * <li><b>step.sequence</b> - SQL query that returns the next ID for a workflow
 * step</li>
 * <li><b>history.table</b> - table name for steps in history</li>
 * <li><b>current.table</b> - table name for current steps</li>
 * <li><b>step.id</b> - column name for step ID field</li>
 * <li><b>step.entryId</b> - column name for workflow entry ID field (foreign
 * key relationship to [entry.table].[entry.id])</li>
 * <li><b>step.stepId</b> - column name for step workflow definition step field</li>
 * <li><b>step.actionId</b> - column name for step action field</li>
 * <li><b>step.owner</b> - column name for step owner field</li>
 * <li><b>step.caller</b> - column name for step caller field</li>
 * <li><b>step.startDate</b> - column name for step start date field</li>
 * <li><b>step.dueDate</b> - column name for optional step due date field</li>
 * <li><b>step.finishDate</b> - column name for step finish date field</li>
 * <li><b>step.status</b> - column name for step status field</li>
 * <li><b>currentPrev.table</b> - table name for the previous IDs for current
 * steps</li>
 * <li><b>historyPrev.table</b> - table name for the previous IDs for history
 * steps</li>
 * <li><b>step.previousId</b> - column name for step ID field (foreign key
 * relation to [history.table].[step.id] or [current.table].[step.id])</li>
 * </ul>
 *
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class JDBCWorkflowStore implements WorkflowStore {
    // ~ Static fields/initializers
    // /////////////////////////////////////////////

    private static final Log log = LogFactory.getLog(JDBCWorkflowStore.class);

    // ~ Instance fields
    // ////////////////////////////////////////////////////////

    protected DataSource ds;
    protected String currentPrevTable;
    protected String currentTable;
    protected String entryId;
    protected String entryName;
    protected String entrySequence;
    protected String entryState;
    protected String entryTable;
    protected String historyPrevTable;
    protected String historyTable;
    protected String stepActionId;
    protected String stepCaller;
    protected String stepDueDate;
    protected String stepEntryId;
    protected String stepFinishDate;
    protected String stepId;
    protected String stepOwner;
    protected String stepPreviousId;
    protected String stepSequence;
    protected String stepStartDate;
    protected String stepStatus;
    protected String stepStepId;
    protected boolean closeConnWhenDone = false;

    public JDBCWorkflowStore() {
        try {
            init(Collections.EMPTY_MAP);
        } catch (StoreException e) {
        }
    }

    public void setDs(DataSource ds) {
        this.ds = ds;
    }

    public DataSource getDs() {
        return ds;
    }

    // ~ Methods
    // ////////////////////////////////////////////////////////////////

    public void setEntryState(long id, int state) throws StoreException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();

            String sql = "UPDATE " + entryTable + " SET " + entryState + " = ? WHERE " + entryId + " = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, state);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException("Unable to update state for workflow instance #" + id + " to " + state, e);
        } finally {
            cleanup(conn, ps, null);
        }
    }

    public PropertySet getPropertySet(long entryId) {
        HashMap args = new HashMap(1);
        args.put("globalKey", "osff_" + entryId);

        JDBCPropertySet ps = new JDBCPropertySet();
        ps.setDs(getDs());
        ps.init(Collections.EMPTY_MAP, args);

        return ps;
    }

    // //////////METHOD #2 OF 3 //////////////////
    // //////// ...gur; ////////////////////
    // kiz
    public boolean checkIfORExists(NestedExpression nestedExpression) {
        // GURKAN;
        // This method checks if OR exists in any nested query
        // This method is used by doNestedNaturalJoin() to make sure
        // OR does not exist within query
        int numberOfExp = nestedExpression.getExpressionCount();

        if (nestedExpression.getExpressionOperator() == NestedExpression.OR) {
            return true;
        }

        for (int i = 0; i < numberOfExp; i++) {
            Expression expression = nestedExpression.getExpression(i);

            if (expression.isNested()) {
                NestedExpression nestedExp = (NestedExpression) expression;

                return checkIfORExists(nestedExp);
            }
        }

        // System.out.println("!!!...........false is returned ..!!!");
        return false;
    }

    public Step createCurrentStep(long entryId, int wfStepId, String owner, Date startDate, Date dueDate, String status, long[] previousIds) throws StoreException {
        Connection conn = null;

        try {
            conn = getConnection();

            long id = createCurrentStep(conn, entryId, wfStepId, owner, startDate, dueDate, status);
            addPreviousSteps(conn, id, previousIds);

            return new SimpleStep(id, entryId, wfStepId, 0, owner, startDate, dueDate, null, status, previousIds, null);
        } catch (SQLException e) {
            throw new StoreException("Unable to create current step for workflow instance #" + entryId, e);
        } finally {
            cleanup(conn, null, null);
        }
    }

    public WorkflowEntry createEntry(String workflowName) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();

            String sql = "INSERT INTO " + entryTable + " (" + entryId + ", " + entryName + ", " + entryState + ", version) VALUES (?,?,?,?)";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);

            long id = getNextEntrySequence(conn);
            stmt.setLong(1, id);
            stmt.setString(2, workflowName);
            stmt.setInt(3, WorkflowEntry.CREATED);
            stmt.setInt(4, 0);
            stmt.executeUpdate();

            return new SimpleWorkflowEntry(id, workflowName, WorkflowEntry.CREATED);
        } catch (SQLException e) {
            throw new StoreException("Error creating new workflow instance", e);
        } finally {
            cleanup(conn, stmt, null);
        }
    }

    public List findCurrentSteps(long entryId) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        PreparedStatement stmt2 = null;

        try {
            conn = getConnection();

            String sql = "SELECT " + stepId + ", " + stepStepId + ", " + stepActionId + ", " + stepOwner + ", " + stepStartDate + ", " + stepDueDate + ", " + stepFinishDate + ", " + stepStatus + ", "
                    + stepCaller + " FROM " + currentTable + " WHERE " + stepEntryId + " = ?";
            String sql2 = "SELECT " + stepPreviousId + " FROM " + currentPrevTable + " WHERE " + stepId + " = ?";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql2);
            }

            stmt2 = conn.prepareStatement(sql2);
            stmt.setLong(1, entryId);

            rset = stmt.executeQuery();

            ArrayList currentSteps = new ArrayList();

            while (rset.next()) {
                long id = rset.getLong(1);
                int stepId = rset.getInt(2);
                int actionId = rset.getInt(3);
                String owner = rset.getString(4);
                Date startDate = rset.getTimestamp(5);
                Date dueDate = rset.getTimestamp(6);
                Date finishDate = rset.getTimestamp(7);
                String status = rset.getString(8);
                String caller = rset.getString(9);

                ArrayList prevIdsList = new ArrayList();
                stmt2.setLong(1, id);

                ResultSet rs = stmt2.executeQuery();

                while (rs.next()) {
                    long prevId = rs.getLong(1);
                    prevIdsList.add(new Long(prevId));
                }

                long[] prevIds = new long[prevIdsList.size()];
                int i = 0;

                for (Iterator iterator = prevIdsList.iterator(); iterator.hasNext(); ) {
                    Long aLong = (Long) iterator.next();
                    prevIds[i] = aLong.longValue();
                    i++;
                }

                SimpleStep step = new SimpleStep(id, entryId, stepId, actionId, owner, startDate, dueDate, finishDate, status, prevIds, caller);
                currentSteps.add(step);
            }

            return currentSteps;
        } catch (SQLException e) {
            throw new StoreException("Unable to locate current steps for workflow instance #" + entryId, e);
        } finally {
            cleanup(null, stmt2, null);
            cleanup(conn, stmt, rset);
        }
    }

    public WorkflowEntry findEntry(long theEntryId) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            conn = getConnection();

            String sql = "SELECT " + entryName + ", " + entryState + " FROM " + entryTable + " WHERE " + entryId + " = ?";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, theEntryId);

            rset = stmt.executeQuery();
            rset.next();

            String workflowName = rset.getString(1);
            int state = rset.getInt(2);

            return new SimpleWorkflowEntry(theEntryId, workflowName, state);
        } catch (SQLException e) {
            throw new StoreException("Error finding workflow instance #" + entryId);
        } finally {
            cleanup(conn, stmt, rset);
        }
    }

    public List findHistorySteps(long entryId) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rset = null;

        try {
            conn = getConnection();

            String sql = "SELECT " + stepId + ", " + stepStepId + ", " + stepActionId + ", " + stepOwner + ", " + stepStartDate + ", " + stepDueDate + ", " + stepFinishDate + ", " + stepStatus + ", "
                    + stepCaller + " FROM " + historyTable + " WHERE " + stepEntryId + " = ? ORDER BY " + stepId + " DESC";
            String sql2 = "SELECT " + stepPreviousId + " FROM " + historyPrevTable + " WHERE " + stepId + " = ?";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql2);
            }

            stmt2 = conn.prepareStatement(sql2);
            stmt.setLong(1, entryId);

            rset = stmt.executeQuery();

            ArrayList currentSteps = new ArrayList();

            while (rset.next()) {
                long id = rset.getLong(1);
                int stepId = rset.getInt(2);
                int actionId = rset.getInt(3);
                String owner = rset.getString(4);
                Date startDate = rset.getTimestamp(5);
                Date dueDate = rset.getTimestamp(6);
                Date finishDate = rset.getTimestamp(7);
                String status = rset.getString(8);
                String caller = rset.getString(9);

                ArrayList prevIdsList = new ArrayList();
                stmt2.setLong(1, id);

                ResultSet rs = stmt2.executeQuery();

                while (rs.next()) {
                    long prevId = rs.getLong(1);
                    prevIdsList.add(new Long(prevId));
                }

                long[] prevIds = new long[prevIdsList.size()];
                int i = 0;

                for (Iterator iterator = prevIdsList.iterator(); iterator.hasNext(); ) {
                    Long aLong = (Long) iterator.next();
                    prevIds[i] = aLong.longValue();
                    i++;
                }

                SimpleStep step = new SimpleStep(id, entryId, stepId, actionId, owner, startDate, dueDate, finishDate, status, prevIds, caller);
                currentSteps.add(step);
            }

            return currentSteps;
        } catch (SQLException e) {
            throw new StoreException("Unable to locate history steps for workflow instance #" + entryId, e);
        } finally {
            cleanup(null, stmt2, null);
            cleanup(conn, stmt, rset);
        }
    }

    public void init(Map props) throws StoreException {
        entrySequence = getInitProperty(props, "entry.sequence", "SELECT nextVal('seq_os_wfentry')");
        stepSequence = getInitProperty(props, "step.sequence", "SELECT nextVal('seq_os_currentsteps')");
        entryTable = getInitProperty(props, "entry.table", "OS_WFENTRY");
        entryId = getInitProperty(props, "entry.id", "ID");
        entryName = getInitProperty(props, "entry.name", "NAME");
        entryState = getInitProperty(props, "entry.state", "STATE");
        historyTable = getInitProperty(props, "history.table", "OS_HISTORYSTEP");
        currentTable = getInitProperty(props, "current.table", "OS_CURRENTSTEP");
        currentPrevTable = getInitProperty(props, "currentPrev.table", "OS_CURRENTSTEP_PREV");
        historyPrevTable = getInitProperty(props, "historyPrev.table", "OS_HISTORYSTEP_PREV");
        stepId = getInitProperty(props, "step.id", "ID");
        stepEntryId = getInitProperty(props, "step.entryId", "ENTRY_ID");
        stepStepId = getInitProperty(props, "step.stepId", "STEP_ID");
        stepActionId = getInitProperty(props, "step.actionId", "ACTION_ID");
        stepOwner = getInitProperty(props, "step.owner", "OWNER");
        stepCaller = getInitProperty(props, "step.caller", "CALLER");
        stepStartDate = getInitProperty(props, "step.startDate", "START_DATE");
        stepFinishDate = getInitProperty(props, "step.finishDate", "FINISH_DATE");
        stepDueDate = getInitProperty(props, "step.dueDate", "DUE_DATE");
        stepStatus = getInitProperty(props, "step.status", "STATUS");
        stepPreviousId = getInitProperty(props, "step.previousId", "PREVIOUS_ID");
    }

    public Step markFinished(Step step, int actionId, Date finishDate, String status, String caller) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();

            String sql = "UPDATE " + currentTable + " SET " + stepStatus + " = ?, " + stepActionId + " = ?, " + stepFinishDate + " = ?, " + stepCaller + " = ? WHERE " + stepId + " = ?";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, actionId);
            stmt.setTimestamp(3, new Timestamp(finishDate.getTime()));
            stmt.setString(4, caller);
            stmt.setLong(5, step.getId());
            stmt.executeUpdate();

            SimpleStep theStep = (SimpleStep) step;
            theStep.setActionId(actionId);
            theStep.setFinishDate(finishDate);
            theStep.setStatus(status);
            theStep.setCaller(caller);

            return theStep;
        } catch (SQLException e) {
            throw new StoreException("Unable to mark step finished for #" + step.getEntryId(), e);
        } finally {
            cleanup(conn, stmt, null);
        }
    }

    public void moveToHistory(Step step) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();

            String sql = "INSERT INTO " + historyTable + " (" + stepId + ',' + stepEntryId + ", " + stepStepId + ", " + stepActionId + ", " + stepOwner + ", " + stepStartDate + ", " + stepFinishDate
                    + ", " + stepDueDate + ", " + stepStatus + ", " + stepCaller + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, step.getId());
            stmt.setLong(2, step.getEntryId());
            stmt.setInt(3, step.getStepId());
            stmt.setInt(4, step.getActionId());
            stmt.setString(5, step.getOwner());
            stmt.setTimestamp(6, new Timestamp(step.getStartDate().getTime()));

            if (step.getDueDate() != null) {
                stmt.setTimestamp(7, new Timestamp(step.getDueDate().getTime()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            if (step.getFinishDate() != null) {
                stmt.setTimestamp(8, new Timestamp(step.getFinishDate().getTime()));
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }

            stmt.setString(9, step.getStatus());
            stmt.setString(10, step.getCaller());
            stmt.executeUpdate();

            long[] previousIds = step.getPreviousStepIds();

            if ((previousIds != null) && (previousIds.length > 0)) {
                sql = "INSERT INTO " + historyPrevTable + " (" + stepId + ", " + stepPreviousId + ") VALUES (?, ?)";
                log.debug("Executing SQL statement: " + sql);
                cleanup(null, stmt, null);
                stmt = conn.prepareStatement(sql);

                for (int i = 0; i < previousIds.length; i++) {
                    long previousId = previousIds[i];
                    stmt.setLong(1, step.getId());
                    stmt.setLong(2, previousId);
                    stmt.executeUpdate();
                }
            }

            sql = "DELETE FROM " + currentPrevTable + " WHERE " + stepId + " = ?";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            cleanup(null, stmt, null);
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, step.getId());
            stmt.executeUpdate();

            sql = "DELETE FROM " + currentTable + " WHERE " + stepId + " = ?";

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL statement: " + sql);
            }

            cleanup(null, stmt, null);
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, step.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException("Unable to move current step to history step for #" + step.getEntryId(), e);
        } finally {
            cleanup(conn, stmt, null);
        }
    }

    public List query(WorkflowExpressionQuery e) throws StoreException {
        // GURKAN;
        // If it is simple, call buildSimple()
        // SELECT DISTINCT(ENTRY_ID) FROM OS_HISTORYSTEP WHERE FINISH_DATE < ?
        //
        // If it is nested, call doNestedNaturalJoin() if and only if the query
        // is
        // ANDed including nested-nestd queries
        // If OR exists in any query call buildNested()
        //
        // doNestedNaturalJoin()
        // This doNestedNaturalJoin() method improves performance of the queries
        // if and only if
        // the queries including nested queries are ANDed
        //
        // SELECT DISTINCT (a1.ENTRY_ID) AS retrieved
        // FROM OS_CURRENTSTEP AS a1 , OS_CURRENTSTEP AS a2 , OS_CURRENTSTEP AS
        // a3 , OS_CURRENTSTEP AS a4
        // WHERE ((a1.ENTRY_ID = a1.ENTRY_ID AND a1.ENTRY_ID = a2.ENTRY_ID) AND
        // (a2.ENTRY_ID = a3.ENTRY_ID AND a3.ENTRY_ID = a4.ENTRY_ID))
        // AND ( a1.OWNER = ? AND a2.STATUS != ? AND a3.OWNER = ? AND a4.STATUS
        // != ? )
        //
        // doNestedLeftJoin() //not used
        // For this method to work, order of queries is matter
        // This doNestedLeftJoin() method will generate the queries but it works
        // if and only if
        // the query is in correct order -- it is your luck
        // SELECT DISTINCT (a0.ENTRY_ID) AS retrieved FROM OS_CURRENTSTEP AS a0
        // LEFT JOIN OS_CURRENTSTEP a1 ON a0.ENTRY_ID = a1.ENTRY_ID
        //
        // LEFT JOIN OS_CURRENTSTEP a2 ON a1.ENTRY_ID = a2.ENTRY_ID
        // LEFT JOIN OS_CURRENTSTEP a3 ON a2.ENTRY_ID = a3.ENTRY_ID
        // WHERE a1.OWNER = ? AND (a2.STATUS = ? OR a3.OWNER = ?)
        //
        if (log.isDebugEnabled()) {
            log.debug("Starting Query");
        }

        Expression expression = e.getExpression();

        if (log.isDebugEnabled()) {
            log.debug("Have all variables");
        }

        if (expression.isNested()) {
            NestedExpression nestedExp = (NestedExpression) expression;

            StringBuffer sel = new StringBuffer();
            StringBuffer columns = new StringBuffer();
            StringBuffer leftJoin = new StringBuffer();
            StringBuffer where = new StringBuffer();
            StringBuffer whereComp = new StringBuffer();
            StringBuffer orderBy = new StringBuffer();
            List values = new LinkedList();
            List queries = new LinkedList();

            String columnName;
            String selectString;

            // Expression is nested and see if the expresion has OR
            if (checkIfORExists(nestedExp)) {
                // For doNestedLeftJoin() uncomment these -- again order is
                // matter
                // and comment out last two lines where buildNested() is called
                //
                // columns.append("SELECT DISTINCT (");
                // columns.append("a0" + "." + stepEntryId);
                // columnName = "retrieved";
                // columns.append(") AS " + columnName);
                // columns.append(" FROM ");
                // columns.append(currentTable + " AS " + "a0");
                // where.append("WHERE ");
                // doNestedLeftJoin(e, nestedExp, leftJoin, where, values,
                // queries, orderBy);
                // selectString = columns.toString() + " " + leftJoin.toString()
                // + " " + where.toString() + " " + orderBy.toString();
                // System.out.println("LEFT JOIN ...");
                //
                //
                columnName = buildNested(nestedExp, sel, values);
                selectString = sel.toString();
            } else {
                columns.append("SELECT DISTINCT (");
                columns.append("a1" + '.' + stepEntryId);
                columnName = "retrieved";
                columns.append(") AS " + columnName);
                columns.append(" FROM ");
                where.append("WHERE ");

                doNestedNaturalJoin(e, nestedExp, columns, where, whereComp, values, queries, orderBy);
                selectString = columns.toString() + ' ' + leftJoin.toString() + ' ' + where.toString() + " AND ( " + whereComp.toString() + " ) " + ' ' + orderBy.toString();

                // System.out.println("NATURAL JOIN ...");
            }

            // System.out.println("number of queries is      : " +
            // queries.size());
            // System.out.println("values.toString()         : " +
            // values.toString());
            // System.out.println("columnName                : " + columnName);
            // System.out.println("where                     : " + where);
            // System.out.println("whereComp                 : " + whereComp);
            // System.out.println("columns                   : " + columns);
            // System.out.println("Query is : " + selectString + "\n");
            return doExpressionQuery(selectString, columnName, values);
        } else {
            // query is not empty ... it's a SIMPLE query
            // do what the old query did
            StringBuffer qry;
            List values = new LinkedList();

            qry = new StringBuffer();

            String columnName = buildSimple((FieldExpression) expression, qry, values);

            if (e.getSortOrder() != WorkflowExpressionQuery.SORT_NONE) {
                qry.append(" ORDER BY ");

                if (e.getOrderBy() != 0) {
                    String fName = fieldName(e.getOrderBy());

                    qry.append(fName);

                    // To help w/ MySQL and Informix, you have to include the
                    // column in the query
                    String current = qry.toString();
                    String entry = current.substring(0, current.indexOf(columnName)) + columnName + "), " + fName + ' ';
                    entry += current.substring(current.indexOf(columnName) + columnName.length() + 1);

                    qry = new StringBuffer(entry);

                    if (e.getSortOrder() == WorkflowExpressionQuery.SORT_DESC) {
                        qry.append(" DESC");
                    } else {
                        qry.append(" ASC");
                    }
                } else {
                    qry.append(columnName);
                }
            }

            // System.out.println("Query is: " + qry.toString());
            return doExpressionQuery(qry.toString(), columnName, values);
        }
    }

    public List query(WorkflowQuery query) throws StoreException {
        List results = new ArrayList();

        // going to try to do all the comparisons in one query
        String sel;
        String table;

        int qtype = query.getType();

        if (qtype == 0) { // then not set, so look in sub queries
            // todo: not sure if you would have a query that
            // would look in both old and new, if so, i'll have
            // to change this - TR
            // but then again, why are there redundant tables in
            // the first place? the data model should probably
            // change

            if (query.getLeft() != null) {
                qtype = query.getLeft().getType();
            }
        }

        if (qtype == WorkflowQuery.CURRENT) {
            table = currentTable;
        } else {
            table = historyTable;
        }

        sel = "SELECT DISTINCT(" + stepEntryId + ") FROM " + table + " WHERE ";
        sel += queryWhere(query);

        if (log.isDebugEnabled()) {
            log.debug(sel);
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sel);

            while (rs.next()) {
                // get entryIds and add to results list
                Long id = new Long(rs.getLong(stepEntryId));
                results.add(id);
            }
        } catch (SQLException ex) {
            throw new StoreException("SQL Exception in query: " + ex.getMessage());
        } finally {
            cleanup(conn, stmt, rs);
        }

        return results;
    }

    protected Connection getConnection() throws SQLException {
        closeConnWhenDone = true;

        return ds.getConnection();
    }

    protected long getNextEntrySequence(Connection c) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL statement: " + entrySequence);
        }

        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = c.prepareStatement(entrySequence);
            rset = stmt.executeQuery();
            rset.next();

            long id = rset.getLong(1);

            return id;
        } finally {
            cleanup(null, stmt, rset);
        }
    }

    protected long getNextStepSequence(Connection c) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL statement: " + stepSequence);
        }

        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            stmt = c.prepareStatement(stepSequence);
            rset = stmt.executeQuery();
            rset.next();

            long id = rset.getLong(1);

            return id;
        } finally {
            cleanup(null, stmt, rset);
        }
    }

    protected void addPreviousSteps(Connection conn, long id, long[] previousIds) throws SQLException {
        if ((previousIds != null) && (previousIds.length > 0)) {
            if (!((previousIds.length == 1) && (previousIds[0] == 0))) {
                String sql = "INSERT INTO " + currentPrevTable + " (" + stepId + ", " + stepPreviousId + ") VALUES (?, ?)";
                log.debug("Executing SQL statement: " + sql);

                PreparedStatement stmt = conn.prepareStatement(sql);

                for (int i = 0; i < previousIds.length; i++) {
                    long previousId = previousIds[i];
                    stmt.setLong(1, id);
                    stmt.setLong(2, previousId);
                    stmt.executeUpdate();
                }

                cleanup(null, stmt, null);
            }
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

        if ((connection != null) && closeConnWhenDone) {
            try {
                connection.close();
            } catch (SQLException ex) {
                log.error("Error closing connection", ex);
            }
        }
    }

    protected long createCurrentStep(Connection conn, long entryId, int wfStepId, String owner, Date startDate, Date dueDate, String status) throws SQLException {
        String sql = "INSERT INTO " + currentTable + " (" + stepId + ',' + stepEntryId + ", " + stepStepId + ", " + stepActionId + ", " + stepOwner + ", " + stepStartDate + ", " + stepDueDate + ", "
                + stepFinishDate + ", " + stepStatus + ", " + stepCaller + " ) VALUES (?, ?, ?, null, ?, ?, ?, null, ?, null)";

        if (log.isDebugEnabled()) {
            log.debug("Executing SQL statement: " + sql);
        }

        PreparedStatement stmt = conn.prepareStatement(sql);

        long id = getNextStepSequence(conn);
        stmt.setLong(1, id);
        stmt.setLong(2, entryId);
        stmt.setInt(3, wfStepId);
        stmt.setString(4, owner);
        stmt.setTimestamp(5, new Timestamp(startDate.getTime()));

        if (dueDate != null) {
            stmt.setTimestamp(6, new Timestamp(dueDate.getTime()));
        } else {
            stmt.setNull(6, Types.TIMESTAMP);
        }

        stmt.setString(7, status);
        stmt.executeUpdate();
        cleanup(null, stmt, null);

        return id;
    }

    // //////////METHOD #3 OF 3 //////////////////
    // //////// ...gur; ////////////////////
    // kardes
    void doNestedNaturalJoin(WorkflowExpressionQuery e, NestedExpression nestedExpression, StringBuffer columns, StringBuffer where, StringBuffer whereComp, List values, List queries,
                             StringBuffer orderBy) { // throws StoreException {

        Object value;
        int currentExpField;

        int numberOfExp = nestedExpression.getExpressionCount();

        for (int i = 0; i < numberOfExp; i++) { // ori

            // for (i = numberOfExp; i > 0; i--) { //reverse 1 of 3
            Expression expression = nestedExpression.getExpression(i); // ori

            // Expression expression = nestedExpression.getExpression(i - 1);
            // //reverse 2 of 3
            if (!(expression.isNested())) {
                FieldExpression fieldExp = (FieldExpression) expression;

                FieldExpression fieldExpBeforeCurrent;
                queries.add(expression);

                int queryId = queries.size();

                if (queryId > 1) {
                    columns.append(" , ");
                }

                // do; OS_CURRENTSTEP AS a1 ....
                if (fieldExp.getContext() == FieldExpression.CURRENT_STEPS) {
                    columns.append(currentTable + " AS " + 'a' + queryId);
                } else if (fieldExp.getContext() == FieldExpression.HISTORY_STEPS) {
                    columns.append(historyTable + " AS " + 'a' + queryId);
                } else {
                    columns.append(entryTable + " AS " + 'a' + queryId);
                }

                // /////// beginning of WHERE JOINS/s :
                // //////////////////////////////////////////
                // do for first query; a1.ENTRY_ID = a1.ENTRY_ID
                if (queryId == 1) {
                    where.append("a1" + '.' + stepEntryId);
                    where.append(" = ");

                    if (fieldExp.getContext() == FieldExpression.CURRENT_STEPS) {
                        where.append("a" + queryId + '.' + stepEntryId);
                    } else if (fieldExp.getContext() == FieldExpression.HISTORY_STEPS) {
                        where.append("a" + queryId + '.' + stepEntryId);
                    } else {
                        where.append("a" + queryId + '.' + entryId);
                    }
                }

                // do; a1.ENTRY_ID = a2.ENTRY_ID
                if (queryId > 1) {
                    fieldExpBeforeCurrent = (FieldExpression) queries.get(queryId - 2);

                    if (fieldExpBeforeCurrent.getContext() == FieldExpression.CURRENT_STEPS) {
                        where.append("a" + (queryId - 1) + '.' + stepEntryId);
                    } else if (fieldExpBeforeCurrent.getContext() == FieldExpression.HISTORY_STEPS) {
                        where.append("a" + (queryId - 1) + '.' + stepEntryId);
                    } else {
                        where.append("a" + (queryId - 1) + '.' + entryId);
                    }

                    where.append(" = ");

                    if (fieldExp.getContext() == FieldExpression.CURRENT_STEPS) {
                        where.append("a" + queryId + '.' + stepEntryId);
                    } else if (fieldExp.getContext() == FieldExpression.HISTORY_STEPS) {
                        where.append("a" + queryId + '.' + stepEntryId);
                    } else {
                        where.append("a" + queryId + '.' + entryId);
                    }
                }

                // /////// end of LEFT JOIN : "LEFT JOIN OS_CURRENTSTEP a1 ON
                // a0.ENTRY_ID = a1.ENTRY_ID
                //
                // ////// BEGINNING OF WHERE clause
                // //////////////////////////////////////////////////
                value = fieldExp.getValue();
                currentExpField = fieldExp.getField();

                // if the Expression is negated and FieldExpression is "EQUALS",
                // we need to negate that FieldExpression
                if (expression.isNegate()) {
                    // do ; a2.STATUS !=
                    whereComp.append("a" + queryId + '.' + fieldName(fieldExp.getField()));

                    switch (fieldExp.getOperator()) { // WHERE a1.STATUS !=
                        case FieldExpression.EQUALS:

                            if (value == null) {
                                whereComp.append(" IS NOT ");
                            } else {
                                whereComp.append(" != ");
                            }

                            break;

                        case FieldExpression.NOT_EQUALS:

                            if (value == null) {
                                whereComp.append(" IS ");
                            } else {
                                whereComp.append(" = ");
                            }

                            break;

                        case FieldExpression.GT:
                            whereComp.append(" < ");

                            break;

                        case FieldExpression.LT:
                            whereComp.append(" > ");

                            break;

                        default:
                            whereComp.append(" != ");

                            break;
                    }

                    switch (currentExpField) {
                        case FieldExpression.START_DATE:
                        case FieldExpression.FINISH_DATE:
                            values.add(new Timestamp(((java.util.Date) value).getTime()));

                            break;

                        default:

                            if (value == null) {
                                values.add(null);
                            } else {
                                values.add(value);
                            }

                            break;
                    }
                } else {
                    // do; a1.OWNER =
                    whereComp.append("a" + queryId + '.' + fieldName(fieldExp.getField()));

                    switch (fieldExp.getOperator()) { // WHERE a2.FINISH_DATE <
                        case FieldExpression.EQUALS:

                            if (value == null) {
                                whereComp.append(" IS ");
                            } else {
                                whereComp.append(" = ");
                            }

                            break;

                        case FieldExpression.NOT_EQUALS:

                            if (value == null) {
                                whereComp.append(" IS NOT ");
                            } else {
                                whereComp.append(" <> ");
                            }

                            break;

                        case FieldExpression.GT:
                            whereComp.append(" > ");

                            break;

                        case FieldExpression.LT:
                            whereComp.append(" < ");

                            break;

                        default:
                            whereComp.append(" = ");

                            break;
                    }

                    switch (currentExpField) {
                        case FieldExpression.START_DATE:
                        case FieldExpression.FINISH_DATE:
                            values.add(new Timestamp(((java.util.Date) value).getTime()));

                            break;

                        default:

                            if (value == null) {
                                values.add(null);
                            } else {
                                values.add(value);
                            }

                            break;
                    }
                }

                // do; a1.OWNER = ? ... a2.STATUS != ?
                whereComp.append(" ? ");

                // ////// END OF WHERE
                // clause////////////////////////////////////////////////////////////
                if ((e.getSortOrder() != WorkflowExpressionQuery.SORT_NONE) && (e.getOrderBy() != 0)) {
                    System.out.println("ORDER BY ; queries.size() : " + queries.size());
                    orderBy.append(" ORDER BY ");
                    orderBy.append("a1" + '.' + fieldName(e.getOrderBy()));

                    if (e.getSortOrder() == WorkflowExpressionQuery.SORT_ASC) {
                        orderBy.append(" ASC");
                    } else if (e.getSortOrder() == WorkflowExpressionQuery.SORT_DESC) {
                        orderBy.append(" DESC");
                    }
                }
            } else {
                NestedExpression nestedExp = (NestedExpression) expression;

                where.append('(');

                doNestedNaturalJoin(e, nestedExp, columns, where, whereComp, values, queries, orderBy);

                where.append(')');
            }

            // add AND or OR clause between the queries
            if (i < (numberOfExp - 1)) { // ori

                // if (i > 1) { //reverse 3 of 3
                if (nestedExpression.getExpressionOperator() == NestedExpression.AND) {
                    where.append(" AND ");
                    whereComp.append(" AND ");
                } else {
                    where.append(" OR ");
                    whereComp.append(" OR ");
                }
            }
        }
    }

    private String getInitProperty(Map props, String strName, String strDefault) {
        Object o = props.get(strName);

        if (o == null) {
            return strDefault;
        }

        return (String) o;
    }

    private String buildNested(NestedExpression nestedExpression, StringBuffer sel, List values) {
        sel.append("SELECT DISTINCT(");

        // Changed by Anthony on 2 June 2004, to query from OS_CURRENTSTEP
        // instead
        // sel.append(entryId);
        sel.append(stepEntryId);
        sel.append(") FROM ");

        // Changed by Anthony on 2 June 2004, to query from OS_CURRENTSTEP
        // instead
        // sel.append(entryTable);
        sel.append(currentTable);

        if (log.isDebugEnabled()) {
            log.debug("Thus far, query is: " + sel.toString());
        }

        for (int i = 0; i < nestedExpression.getExpressionCount(); i++) {
            Expression expression = nestedExpression.getExpression(i);

            if (i == 0) {
                sel.append(" WHERE ");
            } else {
                if (nestedExpression.getExpressionOperator() == NestedExpression.AND) {
                    sel.append(" AND ");
                } else {
                    sel.append(" OR ");
                }
            }

            if (expression.isNegate()) {
                sel.append(" NOT ");
            }

            // Changed by Anthony on 2 June 2004, to query from OS_CURRENTSTEP
            // instead
            // sel.append(entryId);
            sel.append(stepEntryId);
            sel.append(" IN (");

            if (expression.isNested()) {
                this.buildNested((NestedExpression) nestedExpression.getExpression(i), sel, values);
            } else {
                FieldExpression sub = (FieldExpression) nestedExpression.getExpression(i);
                this.buildSimple(sub, sel, values);
            }

            sel.append(')');
        }

        // Changed by Anthony on 2 June 2004, to query from OS_CURRENTSTEP
        // instead
        // return (entryId);
        return (stepEntryId);
    }

    private String buildSimple(FieldExpression fieldExpression, StringBuffer sel, List values) {
        String table;
        String columnName;

        if (fieldExpression.getContext() == FieldExpression.CURRENT_STEPS) {
            table = currentTable;
            columnName = stepEntryId;
        } else if (fieldExpression.getContext() == FieldExpression.HISTORY_STEPS) {
            table = historyTable;
            columnName = stepEntryId;
        } else {
            table = entryTable;
            columnName = entryId;
        }

        sel.append("SELECT DISTINCT(");
        sel.append(columnName);
        sel.append(") FROM ");
        sel.append(table);
        sel.append(" WHERE ");
        queryComparison(fieldExpression, sel, values);

        return columnName;
    }

    private List doExpressionQuery(String sel, String columnName, List values) throws StoreException {
        if (log.isDebugEnabled()) {
            log.debug(sel);
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List results = new ArrayList();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sel);

            if (!values.isEmpty()) {
                for (int i = 1; i <= values.size(); i++) {
                    stmt.setObject(i, values.get(i - 1));
                }
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                // get entryIds and add to results list
                Long id = new Long(rs.getLong(columnName));
                results.add(id);
            }

            return results;
        } catch (SQLException ex) {
            throw new StoreException("SQL Exception in query: " + ex.getMessage());
        } finally {
            cleanup(conn, stmt, rs);
        }
    }

    private static String escape(String s) {
        StringBuffer sb = new StringBuffer(s);

        char c;
        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            c = chars[i];

            switch (c) {
                case '\'':
                    sb.insert(i, '\'');
                    i++;

                    break;

                case '\\':
                    sb.insert(i, '\\');
                    i++;
            }
        }

        return sb.toString();
    }

    private String fieldName(int field) {
        switch (field) {
            case FieldExpression.ACTION: // actionId
                return stepActionId;

            case FieldExpression.CALLER:
                return stepCaller;

            case FieldExpression.FINISH_DATE:
                return stepFinishDate;

            case FieldExpression.OWNER:
                return stepOwner;

            case FieldExpression.START_DATE:
                return stepStartDate;

            case FieldExpression.STEP: // stepId
                return stepStepId;

            case FieldExpression.STATUS:
                return stepStatus;

            case FieldExpression.STATE:
                return entryState;

            case FieldExpression.NAME:
                return entryName;

            case FieldExpression.DUE_DATE:
                return stepDueDate;

            default:
                return "1";
        }
    }

    private String queryComparison(WorkflowQuery query) {
        Object value = query.getValue();
        int operator = query.getOperator();
        int field = query.getField();

        // int type = query.getType();
        String oper;

        switch (operator) {
            case WorkflowQuery.EQUALS:
                oper = " = ";

                break;

            case WorkflowQuery.NOT_EQUALS:
                oper = " <> ";

                break;

            case WorkflowQuery.GT:
                oper = " > ";

                break;

            case WorkflowQuery.LT:
                oper = " < ";

                break;

            default:
                oper = " = ";
        }

        String left = fieldName(field);
        String right;

        if (value != null) {
            right = '\'' + escape(value.toString()) + '\'';
        } else {
            right = "null";
        }

        return left + oper + right;
    }

    /**
     * Method queryComparison
     *
     * @param expression a FieldExpression
     * @param sel        a StringBuffer
     */
    private void queryComparison(FieldExpression expression, StringBuffer sel, List values) {
        Object value = expression.getValue();
        int operator = expression.getOperator();
        int field = expression.getField();

        String oper;

        switch (operator) {
            case FieldExpression.EQUALS:

                if (value == null) {
                    oper = " IS ";
                } else {
                    oper = " = ";
                }

                break;

            case FieldExpression.NOT_EQUALS:

                if (value == null) {
                    oper = " IS NOT ";
                } else {
                    oper = " <> ";
                }

                break;

            case FieldExpression.GT:
                oper = " > ";

                break;

            case FieldExpression.LT:
                oper = " < ";

                break;

            default:
                oper = " = ";
        }

        String left = fieldName(field);
        String right = "?";

        switch (field) {
            case FieldExpression.FINISH_DATE:
                values.add(new Timestamp(((Date) value).getTime()));

                break;

            case FieldExpression.START_DATE:
                values.add(new Timestamp(((Date) value).getTime()));

                break;

            case FieldExpression.DUE_DATE:
                values.add(new Timestamp(((Date) value).getTime()));

                break;

            default:

                if (value == null) {
                    right = "null";
                } else {
                    values.add(value);
                }
        }

        sel.append(left);
        sel.append(oper);
        sel.append(right);
    }

    private String queryWhere(WorkflowQuery query) {
        if (query.getLeft() == null) {
            // leaf node
            return queryComparison(query);
        } else {
            int operator = query.getOperator();
            WorkflowQuery left = query.getLeft();
            WorkflowQuery right = query.getRight();

            switch (operator) {
                case WorkflowQuery.AND:
                    return '(' + queryWhere(left) + " AND " + queryWhere(right) + ')';

                case WorkflowQuery.OR:
                    return '(' + queryWhere(left) + " OR " + queryWhere(right) + ')';

                case WorkflowQuery.XOR:
                    return '(' + queryWhere(left) + " XOR " + queryWhere(right) + ')';
            }
        }

        return ""; // not sure if we should throw an exception or how this
        // should be handled
    }
}
