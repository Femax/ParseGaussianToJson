package util;


import com.google.gson.Gson;
import model.JsonCollection;
import model.Molecule;
import model.Server;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fedosov on 4/18/17.
 */
public class ParseUtil {
    public static void saveFileInJson(Map<String, TreeMap<String, Molecule>> data) {
        Gson gson = new Gson();
        for (Map.Entry<String, TreeMap<String, Molecule>> task : data.entrySet()) {

            String moleculename = task.getValue().firstEntry().getValue().getMoleculeName();
            ArrayList<Molecule> list = new ArrayList<>(task.getValue().values());
            JsonCollection jsonCollection = new JsonCollection();
            jsonCollection.setMolecules(list);

            String json = gson.toJson((jsonCollection));
            try {
                FileWriter fw = new FileWriter("./output/" + moleculename + ".txt");
                BufferedWriter out = new BufferedWriter(fw);
                out.write(json);
                out.close();

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public static void parseIteration(Server server, Molecule molecule, BufferedReader br, boolean isStringHasStructure) {
        String str;
        try {
            while ((str = br.readLine()) != null) {

                if (str.contains(" 16                    2.32214   3.60726   1.03062 ")) {
                    System.out.println(str);
                }
                if (str.contains("%mem")) {
                    Pattern pattern = Pattern.compile(" (%mem=)([0-9])([0-9])([0-9])(MW)");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.matches()) {
                        server.setMemory(Integer.parseInt(matcher.group(2) + matcher.group(3) + matcher.group(4)));
                    } else {
                    }
                } else if (str.matches(" (%nproc=)([1-9)])")) {
                    Pattern pattern = Pattern.compile(" (%nproc=)([1-9)])");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.matches()) {
                        server.setProcCount(Byte.parseByte(matcher.group(2)));
                    } else {
                    }
                } else if (str.matches("(.*)(Step number)( *)([0-9]*)(.*)")) {
                    Pattern pattern = Pattern.compile("(.*)(Step number)( *)([0-9]*)(.*)");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        if (Integer.parseInt(matcher.group(4)) > molecule.getStepCount()) {
                            molecule.setStepCount(Integer.parseInt(matcher.group(4)));
                        }
                    } else {
                    }
                } else if (str.matches("(.*)(time:)( *)(.*)")) {
                    Pattern pattern = Pattern.compile("(.*)(time:)( *)(.*)(.)");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.matches()) {
                        molecule.increaseTime(StringUtils.stringTimeToSeconds(matcher.group(4)));
                    } else {

                    }
                } else if (str.matches("(.*)(#)( )(.*)( )(opt freq)")) {
                    Pattern pattern = Pattern.compile("(.*)(#)( )(.*)( )(opt freq)");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        molecule.setMethod(matcher.group(4));
                    } else {

                    }
                } else if (str.matches("(.*)(Charge =)( *)([0-9]*)( )(Multiplicity =)( *)([0-9]*)(.*)")) {
                    isStringHasStructure = true;
                } else if (isStringHasStructure && !str.equals("") && str.matches("( ?)([0-9]+)(.*)")) {
                    Pattern pattern = Pattern.compile("( ?)([0-9]*)(.*)");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        molecule.addAtomToStructure(StringUtils.numberToElement(matcher.group(2)));
                    } else {

                    }
                } else if (isStringHasStructure && !str.matches("( ?)(Grad*)(.*)") && !str.equals("") && str.matches("( ?)([A-Z]+)(.*)")) {
                    Pattern pattern = Pattern.compile("( ?)([A-Z]*)(.*)");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.find()) {
                        molecule.addAtomToStructure(matcher.group(2));
                    } else {

                    }
                } else if (isStringHasStructure && str.matches("( ?)(Grad*)(.*)")) {
                    isStringHasStructure = false;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
