package service;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sys.FISystem;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.lang.invoke.MethodHandles;

@WebListener
public class CtxListener implements ServletContextListener {
    public static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        FISystem.load();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // something on stop
    }
}
