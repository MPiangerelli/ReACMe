package grafo.utils;

import grafo.model.LogData;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.*;

import static grafo.utils.XesFileManager.removeTraceFrom;
import static grafo.utils.XesFileManager.toLogData;

public class XesDirectoryManager {
    private XesDirectoryManager() {
    }

    private static final Logger logger = Logger.getLogger(XesDirectoryManager.class.getName());
    private static final Random random = new Random();

    /**
     * Questo metodo permette di leggere i file <code>.xes</code> in una directory, e restituirli come una lista di LogData
     *
     * @param path dal quale prendere i file <code>.xes</code>
     * @return lista dei <code>LogData</code> rappresentati i file
     */
    public static List<LogData> extractLogDataFromPath(String path) {
        logger.info("Extracting LogData...");
        List<File> fileList = getListOfFiles(path);
        List<LogData> logDataList = new LinkedList<>();
        fileList.forEach(file -> {
            try {
                logDataList.add(toLogData(file));
            } catch (ReadingException e) {
                logger.severe(e.getMessage());
            }
        });
        return logDataList;
    }

    /**
     * Questo metodo permette di leggere i file <code>.xes</code> in una directory, e restituirli come una lista di XLog
     *
     * @param path dal quale prendere i file <code>.xes</code>
     * @return lista dei <code>XLog</code> rappresentati i file
     */
    public static List<XLog> extractXLogFromPath(String path) {
        logger.log(Level.INFO, () -> "Extracting XLogs from " + path);
        List<File> fileList = getListOfFiles(path);
        List<XLog> xLogList = new LinkedList<>();
        fileList.forEach(file -> {
            try {
                xLogList.add(XesFileManager.toXLog(file));
            } catch (ReadingException e) {
                logger.severe(e.getMessage());
            }
        });
        return xLogList;
    }

    /**
     * Questo metodo introdurre del rumore nei file xes
     *
     * @param firstDirectory  la prima directory in input
     * @param secondDirectory la seconda directory in input
     */
    public static void combineDirectoriesWithSameSize(String firstDirectory, String secondDirectory) throws IOException, ReadingException {
        logger.info("Combining two directories");
        List<XLog> firstList = extractXLogFromPath(firstDirectory);
        List<XLog> secondList = extractXLogFromPath(secondDirectory);
        if (firstList.size() != secondList.size()) {
            throw new IllegalArgumentException("Le cartelle non hanno lo stesso numero di file");
        }
        for (int i = 0; i < firstList.size(); i++) {
            String firstName = firstList.get(i).getAttributes().get(XConceptExtension.KEY_NAME).toString().subSequence(42, 58).toString();
            String secondName = secondList.get(i).getAttributes().get(XConceptExtension.KEY_NAME).toString().subSequence(0, 16).toString();
            if (!firstName.equals(secondName)) {
                throw new ReadingException("File con nomi differenti");
            }
            XLog combinedLog = new XLogImpl(new XAttributeMapImpl());
            combinedLog.addAll(firstList.get(i));
            combinedLog.addAll(secondList.get(i));
            combinedLog.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, firstName + ".xes"));

            FileOutputStream outputStream = new FileOutputStream(firstName + ".xes");
            XesXmlSerializer serializer = new XesXmlSerializer();
            serializer.serialize(combinedLog, outputStream);
            outputStream.close();
        }
    }

    public static void combineDirectoriesWithDifferentSize(String firstDirectory, String secondDirectory) throws IOException {
        logger.info("Combining two directories with different size");
        List<XLog> firstList = extractXLogFromPath(firstDirectory);
        List<XLog> secondList = extractXLogFromPath(secondDirectory);
        for (XLog log : secondList) {
            // Prendo in input i nomi dei file
            String secondName = log.getAttributes().get(XConceptExtension.KEY_NAME).toString().subSequence(0, 16).toString();
            XLog logWithSameNameFromList = XesFileManager.findXLogsWithNameFrom(firstList, secondName).get(0);
            if (logWithSameNameFromList != null) {
                logger.log(Level.WARNING, () -> "Size of first: " + logWithSameNameFromList.size());
                logger.log(Level.WARNING, () -> "Size of second: " + log.size());
                XLog combinedLog = new XLogImpl(new XAttributeMapImpl());
                combinedLog.addAll(log);
                combinedLog.addAll(logWithSameNameFromList);
                combinedLog.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, secondName + ".xes"));
                logger.log(Level.WARNING, () -> "Size of combined log: " + combinedLog.size());
                FileOutputStream outputStream = new FileOutputStream(secondName + ".xes");
                XesXmlSerializer serializer = new XesXmlSerializer();
                serializer.serialize(combinedLog, outputStream);
                outputStream.close();
            } else {
                throw new FileNotFoundException("File diversi");
            }

        }
    }

    public static void introduceNoise(String inPath, String fromPath, int newPerc) throws IOException {
        logger.log(Level.INFO, () -> "Introducing noise in " + inPath + " from " + fromPath);
        List<XLog> inLogs = extractXLogFromPath(inPath);
        List<XLog> fromLogs = extractXLogFromPath(fromPath);

        for (XLog inLog : inLogs) {

            XLog fromLog = XesFileManager.findXLogsWithNameFrom(fromLogs, XesFileManager.getXLogName(inLog)).get(0);
            List<XTrace> traces = XesFileManager.extractTracesByBoolean(fromLog, "false");
            List<XTrace> tracesInFalse = XesFileManager.extractTracesByBoolean(inLog, "false");
            List<XTrace> tracesInTrue = XesFileManager.extractTracesByBoolean(inLog, "true");
            fromLog.clear();
            fromLog.addAll(traces);
            inLog.clear();
            inLog.addAll(tracesInFalse);
            int sizeOfAllTraces = fromLog.size();

            inLog.forEach(trace -> removeTraceFrom(fromLog, trace));

            double actualPerc = (double) (inLog.size() * 100) / sizeOfAllTraces;
            if (actualPerc == 0.0) {
                XesFileManager.insertNewTraceFromAnotherLog(inLog, fromLog);
            }
            while (actualPerc - newPerc <= getThreshold(inLog.size(), sizeOfAllTraces, newPerc)) {
                XesFileManager.insertNewTraceFromAnotherLog(inLog, fromLog);
                actualPerc = (double) (inLog.size() * 100) / sizeOfAllTraces;
            }
            inLog.addAll(tracesInTrue);
            XesFileManager.saveAsFile(inLog, ".xes");
        }
    }

    public static void createNoiseFile(String fromPath, int perc) throws IOException {
        logger.info("Creating noise files");
        List<XLog> extractedLogs = extractXLogFromPath(fromPath);
        for (XLog xLog : extractedLogs) {
            List<XTrace> falseTraces = XesFileManager.extractTracesByBoolean(xLog, "false");
            XLog toSave = new XLogImpl(new XAttributeMapImpl());
            toSave.getAttributes().put("concept:name", new XAttributeLiteralImpl("concept:name", XesFileManager.getXLogName(xLog)));
            int i = 0;
            while (i < Math.ceil(((double) falseTraces.size() / 100) * perc)) {
                XTrace toAdd = falseTraces.get(random.nextInt(falseTraces.size()));
                if (!toSave.contains(toAdd)) {
                    toSave.add(toAdd);
                    i++;
                } else {
                    i--;
                }
            }
            XesFileManager.saveAsFile(toSave, "_g.xes");
        }
    }


    private static double getThreshold(int actualTraces, int sizeOfAllTraces, int newPerc) {
        return newPerc - ((double) (actualTraces * 100) / sizeOfAllTraces);
    }


    private static List<File> getListOfFiles(String directory) {
        File dir = new File(directory);
        File[] list = Arrays.stream(dir.listFiles())
                .toList().toArray(new File[0]);

        assert list != null;
        return Arrays.asList(list);
    }
}
