package com.tsystems.wsdldoc;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.WSDLParserContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point.
 * CLI for using as "java -jar jar_name params"
 * <p/>
 * By: Alexey Matveev
 * Date: 25.08.2016
 * Time: 16:38
 */
public class Main {


    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        // parse input parameters
        Options options = setupOptions();

        CommandLineParser cmdParser = new DefaultParser();
        CommandLine cmd = cmdParser.parse(options, args);

        if (args.length == 0 || cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("wsdldoc", options);
            return;
        }
        String template;
        if (!cmd.hasOption('T')) {
            System.out.println("You must provide a template to use.");
            return;
        } else {
            template = cmd.getOptionValue('T');
        }

        // current directory by default
        Path destination = Paths.get(".");
        if (!cmd.hasOption("d")) {
            System.out.println("# The documentation will be generated in current directory.");
        } else {
            Path dPath = Paths.get(cmd.getOptionValue("d"));
            if (!Files.exists(dPath)) {
                Files.createDirectories(dPath);
            }
            destination = dPath;
        }
        // default filename is "index.html"
        String filename = "index.html";
        if (!cmd.hasOption("o")) {
            System.out.println("# The default filename \"index.html\" will be used");
        } else {
            filename = cmd.getOptionValue("o");
        }
        // wsdl locations is a mandatory parameter
        String[] sourceWsdlLocations = null;
        String[] sourceWsdlFileNames = null;
        if (!cmd.hasOption("s") && !cmd.hasOption("f")) {
            System.out.println("# You have provided no source WSDL's to process");
            return;
        } else {
            if (cmd.hasOption("s")) {
                sourceWsdlLocations = cmd.getOptionValues("s");
            }
            if (cmd.hasOption("f")) {
                sourceWsdlFileNames = cmd.getOptionValues("f");
            }
        }
        // title is "WSDL documentation" by default
        String title = "WSDL";
        if (!cmd.hasOption("t")) {
            System.out.println("# default title \"WSDL documentation\" will be used");
        } else {
            title = cmd.getOptionValue("t");
        }

        File outputFile = new File(destination + File.separator + filename);

        // generate the documentation
        WSDLParser parser = new WSDLParser();
        List<Definitions> defsList = List.of();
        if (sourceWsdlLocations != null) {
            defsList = Arrays.stream(sourceWsdlLocations)
                             .map(parser::parse)
                             .toList();
        } else if (sourceWsdlFileNames != null) {
            defsList = Arrays.stream(sourceWsdlFileNames)
                    .map(File::new)
                    .map(f -> {
                        WSDLParserContext ctx = new WSDLParserContext();
                        ctx.setBaseDir(f.getParent());
                        ctx.setInput(f);
                        return ctx;
                    })
                    .map(parser::parse)
                    .toList();
        }

        DocGenerator.generateDoc(defsList, template, outputFile, title);

        if (template.equals("wsdl2html")) {
            // copy files in "static" folder
            copyStaticFiles("bootstrap.min.css", destination.toString());
            copyStaticFiles("wsdl_style.css", destination.toString());
            copyStaticFiles("complex-icon.png", destination.toString());
            copyStaticFiles("complex-icon-orange.png", destination.toString());
            copyStaticFiles("simple-icon.png", destination.toString());
            copyStaticFiles("simple-icon-cube.png", destination.toString());
            copyStaticFiles("simple-icon-orange.png", destination.toString());
        }
        System.out.println("--");
        long end = System.currentTimeMillis();
        System.out.println("Documentation generated in " + (end - start) / 1000 + "s");
        System.out.println("The output location is: " + destination.toAbsolutePath());
    }

    private static Options setupOptions() {
        Options options = new Options();
        options.addOption("d",
                          "destination",
                          true,
                          "the destination folder of documentation (absolute or relative), it will generate index.html and statics in this folder (there should be write access to the folder)");
        options.addOption("o", "output", true, "the destination file name (default is index.html)");
        options.addOption("h", "help", false, "shows this help output");
        options.addOption("s",
                          "source",
                          true,
                          "one or multiple URLs with source WSDLs location; the schemas in WSDL's should have schemaLocation in order to correctly generate all the types");
        options.addOption("f",
                          "file",
                          true,
                          "filename source WSDLs location; the schemas in WSDL's should have schemaLocation in order to correctly generate all the types");
        options.addOption("t", "title", true, "the title of the documentation, like \"eCompany\" (WSDL by default)");
        options.addOption("T", "template", true, "the template to use");
        return options;
    }

    /**
     * Copies the static files from jar (IDE) to target bundle.
     *
     * @param fileName name of file to copy
     * @param destination directory to copy the file to
     * @throws Exception if something goes bad
     */
    public static void copyStaticFiles(String fileName, String destination) throws Exception {
//        String jarFolder = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
//        System.out.println("Jar folder: " + jarFolder);
        // for IDE usage
        // TODO: Fix better handling
        Path fileLocation = Paths.get("src\\main\\resources\\static" + File.separator + fileName);
        if (Files.exists(fileLocation)) {
            Files.copy(fileLocation, new FileOutputStream(destination + File.separator + fileName));
        } else {
            // for usage as a JAR
//            System.out.println(Main.class.getClassLoader().getResourceAsStream("wsdl2html.ftl")); // ok
//            System.out.println(Main.class.getClassLoader().getResourceAsStream("static/complex-icon.png")); // ok
            try (InputStream stream = Main.class.getClassLoader().getResourceAsStream("static/" + fileName);
                 OutputStream resStreamOut = new FileOutputStream(destination + File.separator + fileName)) {
                if (stream == null) {
                    throw new Exception("Cannot get resource \"" + fileName + "\" from Jar file.");
                }
                int readBytes;
                byte[] buffer = new byte[4096];
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }
            }
        }
    }

}
