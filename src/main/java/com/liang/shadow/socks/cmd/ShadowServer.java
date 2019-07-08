package com.liang.shadow.socks.cmd;

import com.liang.shadow.socks.conf.LocalConf;
import com.liang.shadow.socks.service.AccountManageService;
import com.liang.shadow.socks.service.ShadowSocksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * Created by lianglingtao on 2019/2/25.
 */
@CommandLine.Command(name = "Shadow-Server", mixinStandardHelpOptions = true, version = "Shadow Client Version 1.0")
public class ShadowServer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ShadowClient.class);

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Verbose mode. Helpful for troubleshooting. " +
            "Multiple -v options increase the verbosity.")
    private boolean verbose;
    @CommandLine.Option(names = {"-u", "--user"}, required = true, description = "User Name")
    private String userName;
    @CommandLine.Option(names = {"-p", "--password"}, required = true, description = "PassWord")
    private String passWord;
    @CommandLine.Option(names = {"-P", "--port"}, required = false, description = "Port", defaultValue = "1080")
    private Integer port;
    @CommandLine.Option(names = {"-t", "--timeout"}, required = false, description = "Socket Timeout Seconds", defaultValue = "300")
    private Integer socketTimeout;


    public void run() {
        AccountManageService accountManageService = new AccountManageService(userName, passWord);
        ShadowSocksService shadowSocksService = new ShadowSocksService.ShadowSocksServiceBuilder()
                .listen(port).serverMode().accountManageService(accountManageService)
                .timeoutSeconds(socketTimeout)
                .build();
        LocalConf.verbose = verbose;
        shadowSocksService.start();
    }

    public static void main(String[] args) {
        CommandLine.run(new ShadowServer(), args);
    }
}
