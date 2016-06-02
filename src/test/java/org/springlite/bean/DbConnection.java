package org.springlite.bean;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class DbConnection {

    private String dbName;

    private String password;

    public DbConnection (){

    }

    public void connect(){
        System.out.println("Connect finished."+this.toString());
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "DbConnection{" +
                "dbName='" + dbName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
