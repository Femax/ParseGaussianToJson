
import com.google.gson.Gson;
import model.JsonCollection;
import model.Molecule;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import util.ParseUtil;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by fedosovmax on 10.01.17.
 */
public class Main {
    public static void main(String args[]) {
        //fedosov.ParseAlgo
        String pathToFolder = args[0];
        final File folder = new File(pathToFolder);
        Map<String, TreeMap<String, Molecule>> data = new HashMap<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            } else {
                System.out.println(fileEntry.getName());
                if (fileEntry.getName().contains(".zip")) {
                    parseFileFromZip(fileEntry, data);
                } else {
                    parseFileFromTar(fileEntry, data);
                }

            }
        }
        ParseUtil.saveFileInJson(data);
    }

    private static void parseFileFromTar(File file, Map<String, TreeMap<String, Molecule>> data) {
        try {
            TarArchiveInputStream tarInput = new TarArchiveInputStream(new BZip2CompressorInputStream(new FileInputStream(file)));

            TarArchiveEntry currentEntry = (TarArchiveEntry) tarInput.getNextTarEntry();
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (currentEntry != null) {
                // Read directly from tarInput
                try {
                    if (currentEntry.getName().contains(".out")) {
                        System.out.println(currentEntry.getName());
                        Molecule molecule;
                        molecule = ParseAlgo.recognizeTar(currentEntry, file, tarInput);
                        String formFactor = molecule.getMoleculeName();
                        recognize(currentEntry.getName(), data, formFactor, molecule);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                currentEntry = tarInput.getNextTarEntry(); // You forgot to iterate to the next file

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void parseFileFromZip(File file, Map<String, TreeMap<String, Molecule>> data) {

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry currentEntry = entries.nextElement();

                try {
                    if (currentEntry.getName().contains(".out")) {
                        System.out.println(currentEntry.getName());
                        Molecule molecule = ParseAlgo.recognizeZip(currentEntry, file, zipFile);
                        String formFactor = molecule.getMoleculeName();
                        recognize(currentEntry.getName(), data, formFactor, molecule);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void recognize(String fileName, Map<String, TreeMap<String, Molecule>> data, String formFactor, Molecule molecule) {
        /***
         * .cut маркеруется файл который при попытке расчет завершился ошибкой
         */
        if (fileName.contains(".cut")) {
            /***
             * если файл завершился ошибкой передаем данный в массив данных
             * увеличиваем счетчик ошибок на один
             */
            if (data.get(formFactor) != null) {
                /***
                 * .cut маркеруется файл который при попытке расчет завершился ошибкой
                 */
                if (data.get(formFactor).get(ParseAlgo.getFileNameWithoutFormat(fileName)) != null) {
                    /**
                     * case при котором текущий массив содержит данную молеклуы
                     * увеличиваем количество времени для найденной молекулы
                     * увеличиваем количество шагов найдейной молекулы
                     * */
                    data.get(formFactor).get(ParseAlgo.getFileNameWithoutFormat(fileName)).increaseTime(molecule.getTime());
                    data.get(formFactor).get(ParseAlgo.getFileNameWithoutFormat(fileName)).increaseStepTime(molecule.getStepTime());
                } else {
                    /**
                     * case при котором текущий массив не содержит данную молеклуы
                     * передаем инстанс молекулы в массив
                     * */
                    data.get(formFactor).put(ParseAlgo.getFileNameWithoutFormat(fileName), molecule);
                }
            } else {
                TreeMap<String, Molecule> moleculesList = new TreeMap<>();
                moleculesList.put(ParseAlgo.getFileNameWithoutFormat(fileName), molecule);
                data.put(formFactor, moleculesList);
            }
        } else if (data.get(formFactor) != null) {
            data.get(formFactor).put(ParseAlgo.getFileNameWithoutFormat(fileName), molecule);
        } else {
            TreeMap<String, Molecule> moleculesList = new TreeMap<>();
            moleculesList.put(ParseAlgo.getFileNameWithoutFormat(fileName), molecule);
            data.put(formFactor, moleculesList);
        }
    }


}
