package org.springlite;

import org.springlite.core.NamedThreadLocal;
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
public class ShareResource {

    private static NamedThreadLocal<PrinterDriver> loadedPrinterDrivers = new NamedThreadLocal<PrinterDriver>("loadedPrinterDrivers");

    public static NamedThreadLocal<PrinterDriver> getLoadedPrinterDrivers() {
        return loadedPrinterDrivers;
    }

}
