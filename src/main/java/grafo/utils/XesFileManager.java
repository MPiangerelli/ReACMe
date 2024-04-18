package grafo.utils;

import grafo.model.LogData;
import grafo.model.Trace;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XesFileManager {
    private static final Logger logger = Logger.getLogger(XesFileManager.class.getName());

    private XesFileManager() {
    }

    private static final Random random = new Random();

    public static LogData toLogData(File file) throws ReadingException {
        XesXmlParser parser = new XesXmlParser();
        XLog log;
        try {
            log = parser.parse(file.getAbsoluteFile()).get(0);
        } catch (Exception e) {
            throw new ReadingException("Xes File Couldn't be read or different format");
        }
        return new LogData(file.getName(), extractTraces(log));
    }

    public static XLog toXLog(File file) throws ReadingException {
        XesXmlParser parser = new XesXmlParser();
        XLog log;
        try {
            log = parser.parse(file.getAbsoluteFile()).get(0);
        } catch (Exception e) {
            throw new ReadingException("Xes File Couldn't be read or different format");
        }
        return log;
    }

    public static void insertNewTraceFromAnotherLog(XLog inLog, XLog fromLog) {
        List<XTrace> inTraces = new LinkedList<>(inLog);
        List<XTrace> fromTraces = new LinkedList<>(fromLog);
        boolean passed = false;
        while (!passed) {
            XTrace toInsert = fromTraces.get(random.nextInt(fromTraces.size()));
            if (!inTraces.contains(toInsert)) {
                inTraces.add(toInsert);
                passed = true;
            }
        }
        inLog.clear();
        inLog.addAll(inTraces);
    }

    public static void removeTraceFrom(XLog log, XTrace toRemove) {
        log.removeIf(trace -> trace.getAttributes().get(XConceptExtension.KEY_NAME).toString().equals(toRemove.getAttributes().get(XConceptExtension.KEY_NAME).toString()));

    }

    public static void saveAsFile(XLog log, String extension) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(getXLogName(log) + extension);
        XesXmlSerializer serializer = new XesXmlSerializer();
        serializer.serialize(log, outputStream);
        outputStream.close();
    }

    public static List<XLog> findXLogsWithNameFrom(List<XLog> list, String nameToFind) {
        List<XLog> toReturn = new LinkedList<>();
        for (XLog xLog : list) {
            nameToFind = nameToFind.substring(0, 16);
            if (xLog.getAttributes().get(XConceptExtension.KEY_NAME).toString().subSequence(42, 58).toString().equals(nameToFind)) {
                toReturn.add(xLog);
            }
        }
        return toReturn;
    }

    public static void saveXesByBoolean(String directoryLocation, String booleanValue) throws IOException {
        logger.log(Level.INFO, () -> "Extracting " + booleanValue + " traces from " + directoryLocation);
        List<XLog> logs = XesDirectoryManager.extractXLogFromPath(directoryLocation);

        for (XLog log : logs) {
            List<XTrace> trueTraces = XesFileManager.extractTracesByBoolean(log, booleanValue);
            XLog logWithOnlyTrueTraces = new XLogImpl(new XAttributeMapImpl());
            logWithOnlyTrueTraces.addAll(trueTraces);
            logWithOnlyTrueTraces.getAttributes().put("concept:name", new XAttributeLiteralImpl("concept:name", log.getAttributes().get(XConceptExtension.KEY_NAME).toString()));
            try {
                FileOutputStream outputStream = new FileOutputStream(XesFileManager.getXLogName(log) + ".xes");
                XesXmlSerializer serializer = new XesXmlSerializer();
                serializer.serialize(logWithOnlyTrueTraces, outputStream);
                outputStream.close();
            } catch (IOException e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    public static List<XTrace> extractTracesByBoolean(XLog log, String booleanValue) {
        List<XTrace> toReturn = new LinkedList<>();
        for (XTrace xTrace : log) {
            if (xTrace.getAttributes().get("pdc:isPos").toString().equals(booleanValue)) toReturn.add(xTrace);
        }
        return toReturn;
    }

    public static String getXLogName(XLog log) {
        if (log.getAttributes().get(XConceptExtension.KEY_NAME).toString().length() == 22) {
            return log.getAttributes().get(XConceptExtension.KEY_NAME).toString().substring(0, 18);
        } else if (log.getAttributes().get(XConceptExtension.KEY_NAME).toString().length() == 16) {
            return log.getAttributes().get(XConceptExtension.KEY_NAME).toString();
        } else {
            return log.getAttributes().get(XConceptExtension.KEY_NAME).toString().subSequence(42, 58).toString();
        }
    }


    private static List<Trace> extractTraces(XLog log) {
        List<Trace> traces = new LinkedList<>();
        for (XTrace xTrace : log) {
            traces.add(convertXTraceToTrace(xTrace));
        }
        return traces;
    }

    private static Trace convertXTraceToTrace(XTrace trace) {
        List<String> activitySequence = new LinkedList<>();
        StringBuilder traceLine = new StringBuilder();
        trace.stream().map(xEvent -> xEvent.getAttributes().get("concept:name").toString()).forEach(activity -> addActivityToTraceLineAndSequence(activitySequence, traceLine, activity));
        return new Trace(activitySequence, traceLine.toString(), trace.getAttributes().get("concept:name").toString());
    }

    private static void addActivityToTraceLineAndSequence(List<String> activitySequence, StringBuilder traceLine, String activity) {
        traceLine.append(activity);
        activitySequence.add(activity);
    }
}