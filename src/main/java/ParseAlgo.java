
import model.Molecule;
import model.Server;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import util.ParseUtil;
import util.StringUtils;

import java.io.*;
import java.sql.Struct;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by max on 24.09.2016.
 */
public class ParseAlgo {
    //TODO
    @Deprecated
    public static Molecule recognize(File fileEntry, File file) {
        try {
            boolean isStringHasStructure = false;
            Scanner scanner = new Scanner(fileEntry);
            Molecule molecule = new Molecule();
            molecule.setFileName(getFileNameWithoutFormat(fileEntry.getName()));
            Server server = new Server();
            server.setName(getServerName(file.getName()));
            server.setDate(getDate(file.getName()));
            molecule.setServer(server);
            molecule.fillData();
            return molecule;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getDate(String name) {
        Pattern pattern = Pattern.compile("(node)([1-9]*)(l)(.*)(.tar.*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            String out = matcher.group(4);
            return out;
        } else {
            return name;
        }
    }


    public static Molecule recognizeTar(TarArchiveEntry tarArchiveEntry, File file, TarArchiveInputStream tarArchiveInputStream) {
        boolean isStringHasStructure = false;
        Molecule molecule = new Molecule();
        molecule.setFileName(getFileNameWithoutFormat(tarArchiveEntry.getName()));
        Server server = new Server();
        server.setName(getServerName(file.getName()));
        server.setDate(getDate(file.getName()));
        BufferedReader br = new BufferedReader(new InputStreamReader(tarArchiveInputStream));
        ParseUtil.parseIteration(server, molecule, br, isStringHasStructure);
        molecule.setServer(server);
        molecule.fillData();
        return molecule;
    }

    private static String getServerName(String name) {
        Pattern pattern = Pattern.compile("(node)([1-9]*)(l)(.*)(.tar.*)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            String out = matcher.group(1) + matcher.group(2);
            return out;
        } else {
            return name;
        }

    }

    public static String getFileNameWithoutFormat(String filename) {
        return filename.substring(0, filename.indexOf("."));
    }

    public static Molecule recognizeZip(ZipEntry currentEntry, File file, ZipFile zipFile) {

        try {
            boolean isStringHasStructure = false;

            Molecule molecule = new Molecule();
            molecule.setFileName(getFileNameWithoutFormat(currentEntry.getName()));
            Server server = new Server();
            server.setName(getServerName(file.getName()));
            server.setDate(getDate(file.getName()));
            BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(currentEntry)));
            ParseUtil.parseIteration(server, molecule, br, isStringHasStructure);
            molecule.setServer(server);
            molecule.fillData();
            return molecule;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


}
