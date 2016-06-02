package org.springlite.service.impl;

import org.springlite.service.PrintService;
import org.springlite.service.Printer;

public class PrintServiceImpl implements PrintService {

    private Printer printer;

    @Override
    public void print(String text){
        if(printer.isReady()){
            printer.println(text);
        }else{
            System.err.println("500 - Sorry, service is not available");
        }
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("PrintService is start.");
    }
}
