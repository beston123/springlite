package org.springlite.bean;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/31
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class CyclicRefA {

    private CyclicRefB cyclicRefB;

    public CyclicRefA(){}

    public CyclicRefA(CyclicRefB cyclicRefB){
        this.cyclicRefB = cyclicRefB;
    }

    public CyclicRefB getCyclicRefB() {
        return cyclicRefB;
    }

    public void setCyclicRefB(CyclicRefB cyclicRefB) {
        this.cyclicRefB = cyclicRefB;
    }
}
