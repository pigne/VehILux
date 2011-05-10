/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lu.uni.routegeneration.generation;
import java.util.*;
import java.io.*;

/**
 *
 * @author Masoud
 */
public class EvaluationData {
    
    ArrayList<EvaluationPoint> points;
    int sumOfDifference = 0;
    int meanOfDifferencePercent = 0;
    int sumOfDifferenceF1 = 0;

    public EvaluationData(){
        points = new ArrayList<EvaluationPoint>();
        EvaluationPoint ep;
        
        ep = new EvaluationPoint("22964079#7");
        int r1[] = {0,53,28,21,34,52,194,480,733,643,547,551};
        ep.setRealFlows(r1,1,11);
        points.add(ep);

        ep = new EvaluationPoint("80015627#0");
        int r2[] = {0,32,14,8,9,21,89,317,659,561,382,346};
        ep.setRealFlows(r2,1,11);
        points.add(ep);

        ep = new EvaluationPoint("30143811#2");
        int r4[] = {0,83,70,53,59,94,355,1112,3131,3325,1782,1248};
        ep.setRealFlows(r4,1,11);
        points.add(ep);

        ep = new EvaluationPoint("8599689#1");
        int r6[] = {0,27,21,14,17,43,173,531,712,532,418,321};
        ep.setRealFlows(r6,1,11);
        points.add(ep);

        ep = new EvaluationPoint("8138897#3");
        int r7[] = {0,9,8,6,2,7,31,119,285,305,237,226};
        ep.setRealFlows(r7,1,11);
        points.add(ep);

        ep = new EvaluationPoint("26585595#0");
        int r8[] = {0,67,41,17,14,25,71,165,366,428,409,442};
        ep.setRealFlows(r8,1,11);
        points.add(ep);

        ep = new EvaluationPoint("71121158");
        int r9[] = {0,159,106,64,93,137,318,475,976,1165,1312,1295};
        ep.setRealFlows(r9,1,11);
        points.add(ep);

        ep = new EvaluationPoint("56760938#1");
        int r10[] = {0,29,19,13,12,20,85,339,594,517,443,337};
        ep.setRealFlows(r10,1,11);
        points.add(ep);

        ep = new EvaluationPoint("83063543#3");
        int r11[] = {0,55,38,20,21,35,134,354,953,1007,750,663};
        ep.setRealFlows(r11,1,11);
        points.add(ep);

        ep = new EvaluationPoint("54436866#12");
        int r12[] = {0,33,20,16,13,33,143,460,848,922,633,536};
        ep.setRealFlows(r12,1,11);
        points.add(ep);

        ep = new EvaluationPoint("28961985");
        int r13[] = {0,28,17,9,7,27,48,143,500,613,512,604};
        ep.setRealFlows(r13,1,11);
        points.add(ep);

        ep = new EvaluationPoint("34425561");
        int r14[] = {0,18,13,5,5,12,42,172,687,738,412,373};
        ep.setRealFlows(r14,1,11);
        points.add(ep);

        ep = new EvaluationPoint("75627896#1");
        int r17[] = {0,22,11,8,6,11,32,143,418,474,346,327};
        ep.setRealFlows(r17,1,11);
        points.add(ep);

    }

    public void updateFitness(){
        sumOfDifference = 0;
        meanOfDifferencePercent = 0;
        sumOfDifferenceF1 = 0;
        for (EvaluationPoint ep:points){
            ep.updateFitness(1, 11);
            sumOfDifference+=ep.sumOfDifference;
            sumOfDifferenceF1+=ep.sumOfDifferenceF1;
            meanOfDifferencePercent+=ep.meanOfDifferencePercent;
        }
        meanOfDifferencePercent/=points.size();

    }


    public void resetPoints(){
        for (EvaluationPoint ev:points){
            ev.reset();
        }
    }



    public void writeResultsToFile(){
        try {
              /* Open the file */
              FileOutputStream fos   = new FileOutputStream("bestRoute.txt")     ;
              OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
              BufferedWriter bw      = new BufferedWriter(osw)        ;

              for(EvaluationPoint e:points) {
                bw.write(e.toString());
                bw.newLine();
                bw.write(e.RealtoString());
                bw.newLine();
              }

              /* Close the file */
              bw.close();
            }catch (IOException e) {
                System.out.println("ioexception");
            }

    }

}
