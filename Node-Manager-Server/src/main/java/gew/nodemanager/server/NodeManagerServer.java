package gew.nodemanager.server;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;


/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@SpringBootApplication
@ComponentScan(basePackages = {"gew.nodemanager.server.repository", "gew.nodemanager.server.config",
        "gew.nodemanager.server.controller", "gew.nodemanager.server.service", "gew.nodemanager.common"})
@EntityScan("gew.nodemanager.common.entity")
public class NodeManagerServer {

    public static final String VERSION = "1.0.0";

    private static final String BANNER = "    _   __          __        __  ___            \n" +
            "   / | / /___  ____/ /__     /  |/  /___ _____  ____ _____ ____  _____\n" +
            "  /  |/ / __ \\/ __  / _ \\   / /|_/ / __ `/ __ \\/ __ `/ __ `/ _ \\/ ___/    \n" +
            " / /|  / /_/ / /_/ /  __/  / /  / / /_/ / / / / /_/ / /_/ /  __/ /    \n" +
            "/_/ |_/\\____/\\__,_/\\___/  /_/  /_/\\__,_/_/ /_/\\__,_/\\__, /\\___/_/    {}\n" +
            "                                                   /____/             ";

    private static final Logger log = LogManager.getLogger(NodeManagerServer.class);

    public static void main(String[] args) {
        SpringApplication.run(NodeManagerServer.class, args);
        log.info("\n" + BANNER, " -> Initializing...");
    }
}
