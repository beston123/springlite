package org.springlite.service.impl;

import org.springlite.ShareResource;
import org.springlite.beans.InitializingBean;
import org.springlite.service.PrinterDriver;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/5/29
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class ConsolePrinterDriver implements PrinterDriver{

    public void install() {
        System.out.println("ConsolePrinterDriver be installed!");
        ShareResource.getLoadedPrinterDrivers().set(this);
    }
}
