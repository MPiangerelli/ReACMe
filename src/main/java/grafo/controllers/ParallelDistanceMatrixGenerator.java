package grafo.controllers;

import grafo.config.RunProperties;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ParallelDistanceMatrixGenerator {
    public static void main(String[] args) throws InterruptedException {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        RunProperties runProp1 = new RunProperties(1, 0, 0, 1, 0, 0, 0,2);
        RunProperties runProp2 = new RunProperties(1, 0, 0, 1, 0, 1, 0, 2);
        RunProperties runProp3 = new RunProperties(1, 0, 0, 1, 0, 0, 0.5, 2);
        RunProperties runProp4 = new RunProperties(1, 0, 0, 1, 0, 1, 0.5, 2);
        RunProperties runProp5 = new RunProperties(1,0,1,1,0,0,0.5,2);
        RunProperties runProp6 = new RunProperties(1,0,1,1,0,1,0.5,2);
        RunProperties runProp7 = new RunProperties(1,0,0,1,0,0,1,2);
        RunProperties runProp8 = new RunProperties(1,0,1,1,0,0,1,2);

        String filePathGround = "C:\\Users\\Riccardo\\Desktop\\Stage ++\\ReACMe_final\\data\\training_ground_without_1";
        String filePathInput = "C:\\Users\\Riccardo\\Desktop\\Stage ++\\ReACMe_final\\data\\training_without_1";
        String filePathNoise = "C:\\Users\\Riccardo\\Desktop\\Stage ++\\ReACMe_final\\noise\\1perc\\01";


        LogsDictionaryController it1 = new LogsDictionaryController(runProp1, filePathGround,filePathNoise);
        LogsDictionaryController it2 = new LogsDictionaryController(runProp2, filePathGround,filePathNoise);
        LogsDictionaryController it3 = new LogsDictionaryController(runProp3, filePathGround,filePathNoise);
        LogsDictionaryController it4 = new LogsDictionaryController(runProp4, filePathGround,filePathNoise);

        LogsDictionaryController it7 = new LogsDictionaryController(runProp7, filePathGround,filePathNoise);
        LogsDictionaryController it8 = new LogsDictionaryController(runProp8, filePathGround,filePathNoise);
        LogsDictionaryController it5 = new LogsDictionaryController(runProp5, filePathGround,filePathNoise);
        LogsDictionaryController it6 = new LogsDictionaryController(runProp6, filePathGround,filePathNoise);


//        executor.submit(it1);
//        executor.submit(it2);
//        executor.submit(it3);
//        executor.submit(it4);

        executor.submit(it5);
        executor.submit(it6);
        executor.submit(it7);
        executor.submit(it8);

        executor.shutdown();
        while (!executor.isTerminated())
            Thread.sleep(1000);

    }
}
