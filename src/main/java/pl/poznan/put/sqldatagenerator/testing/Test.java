package pl.poznan.put.sqldatagenerator.testing;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Test {

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Main")
                .defaultHelp(true)
                .description("Calculate checksum of given files.");
//        parser.addArgument("-t", "--type")
//                .choices("SHA-256", "SHA-512", "SHA1").setDefault("SHA-256")
//                .required(true)
//                .help("Specify hash function to use");

        parser.addArgument("--xml")
                .required(true)
                .help("XML file");


        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        System.out.println("ns.getString(\"file\") = " + ns.getString("xml"));


//        MessageDigest digest = null;
//        try {
//            digest = MessageDigest.getInstance(ns.getString("type"));
//        } catch (NoSuchAlgorithmException e) {
//            System.err.printf("Could not get instance of algorithm %s: %s",
//                    ns.getString("type"), e.getMessage());
//            System.exit(1);
//        }
//        for (String name : ns.<String> getList("file")) {
//            Path path = Paths.get(name);
//            try (ByteChannel channel = Files.newByteChannel(path,
//                    StandardOpenOption.READ);) {
//                ByteBuffer buffer = ByteBuffer.allocate(4096);
//                while (channel.read(buffer) > 0) {
//                    buffer.flip();
//                    digest.update(buffer);
//                    buffer.getStateAndClear();
//                }
//            } catch (IOException e) {
//                System.err
//                        .printf("%s: failed to read data: %s", e.getMessage());
//                continue;
//            }
//            byte md[] = digest.digest();
//            StringBuffer sb = new StringBuffer();
//            for (int i = 0, len = md.length; i < len; ++i) {
//                String x = Integer.toHexString(0xff & md[i]);
//                if (x.length() == 1) {
//                    sb.append("0");
//                }
//                sb.append(x);
//            }
//            System.out.printf("%s  %s\n", sb.toString(), name);
//        }
    }
}
