package org.springlite.service.impl;

import org.springlite.ShareResource;
import org.springlite.service.Printer;
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
public class ConsolePrinter implements Printer {

    private boolean isReady = false;

    @Override
    public void println(String text) {
        System.out.println(text+" [Print by ConsolePrinter] ");
    }

    @Override
    public boolean isReady() {
        return this.isReady;
    }

    @Override
    public void afterPropertiesSet() {
        PrinterDriver printerDriver = ShareResource.getLoadedPrinterDrivers().get();
        if(printerDriver != null && printerDriver instanceof ConsolePrinterDriver){
            this.isReady = true;
            System.out.println(">>>>>> ConsolePrinter has prepared. ");
        }else{
            System.out.println(">>>>>> ConsolePrinter has not installed driver. ");
        }

    }
}
