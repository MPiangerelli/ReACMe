package grafo.controllers;

import com.opencsv.CSVWriter;
import grafo.comparators.DictionaryComparator;
import grafo.config.RunProperties;
import grafo.model.LogData;
import grafo.model.Trace;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.*;
import java.nio.file.FileSystems;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Questa classe è il nuovo LogUtils. La differenza principale sta nel fatto
 * che questa classe permette di confrontare i log con l'utilizzo dei dizionari
 */
public class LogsDictionaryController implements Runnable {
    // Costanti
    private static final String OUTPUT_FOLDER = "output";
    private static final Logger logger = Logger.getLogger(LogsDictionaryController.class.getName());


    private final RunProperties runProperties;
    private final String filesDirectory;
    private String noiseFilesDirectory;
    private List<File> listFileNoise = new LinkedList<>();
    private List<File> listFiles = new LinkedList<>();
    private final List<LogData> analyzedLogs;
    private boolean isTreCifre = false;
    private String[][] distanceMatrix;

    private String outputFileName = null;

    @Override
    public void run() {
        logger.info("Program Started");
        analyzeTraces();
        generateDictionaries((int) runProperties.getGrams());
        generateDistanceMatrix(runProperties.getComparator());
        convertToCSV();
        try {
            applyPythonClustering();
        } catch (InterruptedException e) {
            System.out.println("Errore Metodo Apply Python Clustering");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTreCifre(boolean isTreCifre) {
        this.isTreCifre = isTreCifre;
    }

    public LogsDictionaryController(RunProperties runProperties, String filesDirectory) {
        this.runProperties = runProperties;
        this.filesDirectory = filesDirectory;
        analyzedLogs = new LinkedList<>();
        initConfig();
    }

    /**
     * Concettualmente per lavorare con i file .xes e generare la distance matrix abbiamo bisogno
     * di avere in nostro possesso la directory dei file xes e le proprietà dell'esecuzione.
     *
     * @param filesDirectory - la directory di input per i files di log in formato .xes
     * @see RunProperties
     */
    public LogsDictionaryController(RunProperties runProperties, String filesDirectory, String noiseFilesDirectory) {
        this.runProperties = runProperties;
        this.filesDirectory = filesDirectory;
        this.noiseFilesDirectory = noiseFilesDirectory;
        analyzedLogs = new LinkedList<>();
        initConfig();
    }

    public LogsDictionaryController(String filesDirectory, String noiseFilesDirectory) {
        this(new RunProperties(), filesDirectory, noiseFilesDirectory);
    }

    private void initConfig() {
        File directory = new File(filesDirectory);
        if (noiseFilesDirectory != null) {
            File noiseDirectory = new File(noiseFilesDirectory);
            listFileNoise = Arrays.stream(noiseDirectory.listFiles()).toList();
        }
        listFiles = Arrays.stream(directory.listFiles())
                .toList();
    }

    public void analyzeTraces() {
        logger.info("Analyzing traces...");

        var startTime = System.currentTimeMillis();
        listFiles.forEach(this::readFile);
        var endTime = System.currentTimeMillis();
        var elapsedTime = endTime - startTime;
        var seconds = elapsedTime / 1000;
        var minutes = seconds / 60;
        var finalSeconds = seconds % 60;

        logger.info(() -> "Analyzed " + listFiles.size() + " files in " + minutes + "m" + finalSeconds + "s");
    }

    private void readFile(File file) {
        try {
            XLog log = parseXES(file.getAbsolutePath());
            analyzedLogs.add(new LogData(file.getName(),
                    extractTraces(log, file.getName())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Trace> extractTraces(XLog log, String fileName) {
        List<Trace> traces = new LinkedList<>();
        List<Trace> listOfNoiseTraces = getNoiseTraces(fileName);
        log.forEach(trace -> {
            Trace toAdd = extractTraceDataToAnalyzedLogs(trace);
            if (toAdd != null) {
                traces.add(extractTraceDataToAnalyzedLogs(trace));
            }
        });
        traces.addAll(listOfNoiseTraces);

        return traces;
    }

    private List<Trace> getNoiseTraces(String fileName) {
        String path = getNoiseFile(fileName);
        if (path != null) {
            List<Trace> traces = new LinkedList<>();
            try {
                XLog log = parseXES(path);
                log.forEach(trace -> {
                    getTrace(trace);
                    traces.add(getTrace(trace));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return traces;
        }
        return Collections.emptyList();
    }

    private String getNoiseFile(String fileName) {
        for (int i = 0; i < listFileNoise.size(); i++) {
            String file = listFileNoise.get(i).getName();
            if (file.equals(fileName)) {
                return listFileNoise.get(i).getAbsolutePath();
            }
        }
        return null;
    }

    private Trace extractTraceDataToAnalyzedLogs(XTrace trace) {
        if (!trace.getAttributes().containsKey("pdc:isPos")) {
            return getTrace(trace);
        } else if (trace.getAttributes().get("pdc:isPos").toString().equals(Boolean.TRUE.toString())) {
            return getTrace(trace);
        }
        return null;
    }


    private Trace getTrace(XTrace trace) {
        List<String> activitySequence = new LinkedList<>();
        StringBuilder traceLine = new StringBuilder();
        trace.stream().map(xEvent -> xEvent.getAttributes().get("concept:name").toString()).forEach(activity -> addActivityToTraceLineAndSequence(activitySequence, traceLine, activity));
        String idTrace = trace.getAttributes().get("concept:name").toString();
        return new Trace(activitySequence, traceLine.toString(), idTrace);
    }


    private void addActivityToTraceLineAndSequence(List<String> activitySequence, StringBuilder traceLine, String activity) {
        if (isTreCifre) activity = activity.substring(0, 3);
        traceLine.append(activity);
        activitySequence.add(activity);
    }

    private XLog parseXES(String filePath) throws Exception {
        XesXmlParser parser = new XesXmlParser();
        return parser.parse(new File(filePath)).get(0);
    }

    public void generateDictionaries(int gram) {
        logger.info("Generating dictionaries...");
        var startTime = System.currentTimeMillis();
        analyzedLogs.forEach(LogData::generateDictionaryOfTraces);
        analyzedLogs.forEach(LogData::generateDictionaryOfActivities);
        analyzedLogs.forEach(logData -> logData.generateDictionaryOfGramsByValue(gram));
        var endTime = System.currentTimeMillis();
        var elapsedTime = endTime - startTime;
        var seconds = elapsedTime / 1000;
        var minutes = seconds / 60;
        var finalSeconds = seconds % 60;
        logger.info(() -> "Generated dictionaries in " + minutes + "m" + finalSeconds + "s");
    }

    public void generateDistanceMatrix(DictionaryComparator comparator) {
        logger.info("Generating Distance Matrix...");
        var startTime = System.currentTimeMillis();
        var zeroValue = "00.00";
        String[][] matrix = new String[analyzedLogs.size()][analyzedLogs.size()];

        int cores = Runtime.getRuntime().availableProcessors();
        int chunkSize = (int) Math.ceil((double) analyzedLogs.size() / cores);

        ExecutorService executor = Executors.newFixedThreadPool(cores);

        // Generate the chunks to process
        List<Tuple<Integer>> chunksToProcess = new LinkedList<>();
        for (int i = 0; i < cores; i++) {
            chunksToProcess.add(new Tuple<>(i * chunkSize, (i + 1) * chunkSize - 1));
        }

        List<Callable<Void>> tasks = new ArrayList<>();
        for (Tuple<Integer> tuple : chunksToProcess) {

            Callable<Void> task = () -> {
                for (int i = tuple.getFirst(); i <= tuple.getSecond(); i++) {
                    for (int j = 0; j < analyzedLogs.size(); j++) {
                        if (i == j) {
                            matrix[i][j] = zeroValue;
                        } else {
                            // We need to create a new instance of the comparator to avoid concurrency issues
                            DictionaryComparator compare = comparator;
                            Double distance = compare.compare(analyzedLogs.get(i), analyzedLogs.get(j)) * 100;
                            DecimalFormat decimalFormat = new DecimalFormat("#.00");
                            if (distance == 0.0) {
                                matrix[i][j] = zeroValue;
                                matrix[j][i] = zeroValue;
                            } else {
                                matrix[i][j] = decimalFormat.format(distance).replace(",", ".");
                                matrix[j][i] = decimalFormat.format(distance).replace(",", ".");
                            }
                        }
                    }
                }
                return null;
            };
            tasks.add(task);

        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        this.distanceMatrix = matrix;
        addHeaderWithLogFileNames();
        var endTime = System.currentTimeMillis();
        var elapsedTime = endTime - startTime;
        var seconds = elapsedTime / 1000;
        var minutes = seconds / 60;
        var finalSeconds = seconds % 60;
        logger.info(() -> "Time elapsed to create distance Matrix " + minutes + "m" + finalSeconds + "s");
    }


    private void addHeaderWithLogFileNames() {
        String[][] distanceMatrixWithHeader = new String[this.distanceMatrix.length + 1][this.distanceMatrix.length + 1];
        for (int i = 0; i < distanceMatrixWithHeader.length; i++) {
            for (int j = 0; j < distanceMatrixWithHeader.length; j++) {
                manageCells(distanceMatrixWithHeader, i, j);
            }
        }
        this.distanceMatrix = distanceMatrixWithHeader;
    }

    private void manageCells(String[][] distanceMatrix, int i, int j) {
        if (j == i) {
            distanceMatrix[i][j] = "00.00";
        } else {
            if (distanceMatrix[i][j] == null) {
                if (i == 0) {
                    setHeaderName(distanceMatrix, i, j);
                } else {
                    translateCells(distanceMatrix, i, j);
                }
            }
        }
        if (i == 0 && j == 0) {
            distanceMatrix[i][j] = "";
        }
    }

    private void translateCells(String[][] distanceMatrix, int i, int j) {
        distanceMatrix[i][j] = this.distanceMatrix[i - 1][j - 1];
        distanceMatrix[j][i] = this.distanceMatrix[j - 1][i - 1];
    }

    private void setHeaderName(String[][] distanceMatrix, int i, int j) {
        String filename = analyzedLogs.get(j - 1).getLogName();
        int extension = filename.lastIndexOf('.');
        String nameOnly = filename.substring(0, extension);
        distanceMatrix[i][j] = nameOnly;
        distanceMatrix[j][i] = nameOnly;
    }

    public void convertToCSV() {
        logger.info("Saving generated Distance Matrix as .csv file");
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        outputFileName = "distanceMatrix_" + decimalFormat.format(runProperties.getGamma()).replaceAll("[.,]", "_") + "_" + ((int) runProperties.getActivityEqualScore()) + ((int) runProperties.getActivityNotEqualScore()) + ((int) runProperties.getActivitySemiEqualScore()) + "_" + ((int) runProperties.getGramEqualScore()) + ((int) runProperties.getGramNotEqualScore()) + ((int) runProperties.getGramSemiEqualScore()) + ".csv";

        File f = new File(OUTPUT_FOLDER);
        if (!f.exists()) {
            f.mkdir();
        }

        try {
            File csvFile = new File(f.getAbsolutePath() + FileSystems.getDefault().getSeparator() + outputFileName);
            CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
            for (String[] array : distanceMatrix) {
                writer.writeNext(array);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void applyPythonClustering() throws IOException, InterruptedException {
        logger.info("Clustering started");
        var startTime = System.currentTimeMillis();

        File script = new File(Optional.ofNullable(System.getenv("CLUSTERING_SCRIPT_PATH")).orElse("clustering.py"));
        String scriptPath = script.getAbsolutePath();
        scriptPath = scriptPath.replace('\\', '/');
        runProcessSingleCore(scriptPath);
        Thread.sleep(1000);
        List<File> outputList = getFilesFromProcess();
        if (outputList.size() == 3) {
            moveFilesToOutputDirectory(outputList.toArray(new File[0]));
        }
        var endTime = System.currentTimeMillis();
        var elapsedTime = endTime - startTime;
        var seconds = elapsedTime / 1000;
        var minutes = seconds / 60;
        var finalSeconds = seconds % 60;
        logger.info(() -> "Time elapsed to clustering " + minutes + "m" + finalSeconds + "s");
    }

    private void runProcessSingleCore(String scriptPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("python", scriptPath, OUTPUT_FOLDER+FileSystems.getDefault().getSeparator() + outputFileName);
        Process p = pb.start();
        p.waitFor();

        // Read standard output
        BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = bfr.readLine()) != null) {
            logger.info(line);
        }
        bfr.close();

        // Read error output
        BufferedReader errorBfr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String errorLine;
        while ((errorLine = errorBfr.readLine()) != null) {
            logger.severe(errorLine);
        }
        errorBfr.close();
    }


    private void moveFilesToOutputDirectory(File[] winners) {
        for (File win : winners) {
            String parentDir0 = win.getParent();
            parentDir0 = parentDir0 + FileSystems.getDefault().getSeparator() + OUTPUT_FOLDER;
            String winner0name = win.getName();
            if (!win.renameTo(new File(parentDir0 + FileSystems.getDefault().getSeparator() + winner0name)))
                logger.warning("Errore metodo moveFilesToOutputDirectory()");
        }
    }

    private List<File> getFilesFromProcess() {
        File dir = new File("");
        String dirPath = dir.getAbsolutePath();
        dir = new File(dirPath);

        List<File> fileList = new ArrayList<>();
        Collections.addAll(fileList, dir.listFiles());

        List<File> outputList = new ArrayList<>();
        for (File nextFile : fileList) {
            if (nextFile.getName().contains("kmedoids") || nextFile.getName().contains("silhouette"))
                outputList.add(nextFile);
        }
        return outputList;
    }

    public RunProperties getRunProperties() {
        return this.runProperties;
    }

    private static class Tuple<K> {

        private final K v1;
        private final K v2;

        Tuple(K v1, K v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public K getFirst() {
            return v1;
        }

        public K getSecond() {
            return v2;
        }

    }

}