package com.splicemachine.derby.impl.sql.execute.actions;

import com.splicemachine.derby.test.framework.SpliceSchemaWatcher;
import com.splicemachine.derby.test.framework.SpliceUnitTest;
import com.splicemachine.derby.test.framework.SpliceWatcher;
import com.splicemachine.test.HBaseTest;
import com.splicemachine.test.SerialTest;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * Created by yxia on 8/21/18.
 */
@Category(value = {SerialTest.class,HBaseTest.class})
public class AddDropPrimaryKeyIT extends SpliceUnitTest {
    private static final SpliceSchemaWatcher schemaWatcher = new SpliceSchemaWatcher(AddDropPrimaryKeyIT.class.getSimpleName().toUpperCase());
    private static final SpliceWatcher classWatcher = new SpliceWatcher();

    @ClassRule
    public static TestRule chain = RuleChain.outerRule(classWatcher)
            .around(schemaWatcher);

    @Test
    public void testAddPrimaryKeyOnNonEmptyTable() throws Exception {
        /*
        String tableName = "testDropPrimaryKey".toUpperCase();
        String tableRef = schemaWatcher.schemaName+"."+tableName;
        tableDAO.drop(schemaWatcher.schemaName, tableName);

        Connection c1 = classWatcher.createConnection();
        c1.setAutoCommit(false);
        Statement s1 = c1.createStatement();

        s1.execute(String.format("create table %s (name char(14) not null primary key, age int)", tableRef));
        c1.commit();

        s1.execute(String.format("insert into %s values('Ralph', 22)", tableRef));
        c1.commit();

        s1.execute(String.format("alter table %s add num char(11) not null default '000001'", tableRef));
        c1.commit();

        s1.execute(String.format("insert into %s (name, age) values('Fred', 23)", tableRef));
        s1.execute(String.format("insert into %s values('Joe', 24, '111111')", tableRef));
        c1.commit();

        try {
            s1.execute(String.format("insert into %s (name, age) values ('Fred', 30)", tableRef));
            Assert.fail("Expected primary key violation");
        } catch (SQLException e) {
            Assert.assertTrue(e.getLocalizedMessage().startsWith("The statement was aborted because it would have " +
                    "caused a " +
                    "duplicate key value in a unique or primary key " +
                    "constraint or unique index " +
                    "identified by 'SQL"));
        }

        s1.execute(String.format("alter table %s drop primary key", tableRef));
        c1.commit();

        // Now should be able to insert violating row
        s1.execute(String.format("insert into %s (name, age) values ('Fred', 30)", tableRef));

        ResultSet rs = s1.executeQuery("select * from " + tableRef);
        int count =0;
        while (rs.next()) {
            String name = rs.getString(1);
            Assert.assertNotNull("NAME is NULL!", name);
            if (name.trim().equals("Fred")) {
                count++;
            }
        }
        Assert.assertEquals("Should see 2 Freds.", 2, count);

        rs = s1.executeQuery("select * from " + tableRef+" where name = 'Fred'");
        count =0;
        while (rs.next()) {
            String name = rs.getString(1);
            Assert.assertNotNull("NAME is NULL!",name);
            count++;
        }
        Assert.assertEquals("Should see 2 Freds.", 2, count);
        */
    }

    @Test
    public void testAddPrimaryKeyOnEmptyTable() throws Exception {

    }

}
