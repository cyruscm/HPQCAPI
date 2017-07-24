package alm;

/**
 * Created by MARTINCORB on 7/14/2017.
 */

import org.apache.commons.cli.CommandLine;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import infrastructure.Constants;

public class HPQCAPI {
    public static void main(String[] args) {
        // https://stackoverflow.com/questions/45152242/hp-alm-rest-api-qcsession-411-authentication/45153033#45153033
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        CommandLine commandLine;
        Option opt_help = Option.builder("h")
                .required(false)
                .desc("Help for cli")
                .longOpt("help")
                .build();
        Option opt_host = Option.builder("ho")
                .required(false)
                .desc("The host domain to connect to (defaults "+ Constants.HOST +")")
                .longOpt("host")
                .build();
        Option opt_port = Option.builder("po")
                .required(false)
                .desc("The port to connect to (defaults "+ Constants.PORT +")")
                .longOpt("port")
                .build();
        Option opt_domain = Option.builder("d")
                .required(false)
                .desc("The domain in ALM to connect to. (defaults to " + Constants.DOMAIN + ")" )
                .longOpt("domain")
                .build();
        Option opt_project = Option.builder("pr")
                .required(false)
                .desc("The project name to connect to (defaults to " + Constants.PROJECT + ")" )
                .longOpt("project")
                .build();
        Option opt_username = Option.builder("u")
                .required(false)
                .desc("The username to authenticate with. (defaults to " + Constants.USERNAME)
                .longOpt("username")
                .build();
        Option opt_password = Option.builder("p")

                .required(false)
                .desc("The password to authenticate with. (defaults to password for " + Constants.USERNAME + ")")
                .longOpt("password")
                .build();
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption(opt_help);
        options.addOption(opt_host);
        options.addOption(opt_port);
        options.addOption(opt_domain);
        options.addOption(opt_project);
        options.addOption(opt_username);
        options.addOption(opt_password);

        String[] parameters = new String[6];
        try {
            commandLine = parser.parse(options, args);

            if (commandLine.hasOption("ho")) {
                parameters[0] = commandLine.getOptionValue("ho");
            }
            if (commandLine.hasOption("po")) {
                parameters[1] = commandLine.getOptionValue("po");
            }
            if (commandLine.hasOption("d")) {
                parameters[2] = commandLine.getOptionValue("d");
            }
            if (commandLine.hasOption("pr")) {
                parameters[3] = commandLine.getOptionValue("pr");
            }
            if (commandLine.hasOption("u")) {
                parameters[4] = commandLine.getOptionValue("u");
            }
            if (commandLine.hasOption("p")) {
                parameters[5] = commandLine.getOptionValue("p");
            }

            Config.initConfigs(parameters);
            run();


        } catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public static void run() {
    	JUnitPoster poster = new JUnitPoster();
    	poster.init();
    	poster.getTestID("bla");
    }
}