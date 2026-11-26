import javax.swing.SwingUtilities;
import java.util.*;

public class Main {
   static boolean allDone(List<Process> l){
        for(Process p:l){
            if(p.remaining>0) return false;
        }
        return true;
    }
   static Process pickShortest(List<Process> l, int time) {
       Process best =null;
       for(Process p:l){
           if(time>=p.arrival&& p.remaining!=0){
               if(best==null ) best=p;
               else{
                   if(p.remaining<best.remaining ){
                     best=p;
                 }
               }
           }
       }
    return best;
   }

    static Result run(List<Process> list, int cs) {
        Result r = new Result();
        r.processes = list.toArray(new Process[0]);
        int n = list.size();

        int time=0;
        Process lastP=null;
        while(!allDone(list)){
            Process currP = pickShortest(list,time);
            if(currP==null) {
                r.log("time " + time +"-"+(time+1) + ": No process");
                r.unit(time, "IDLE");
            }
            else {
                if (lastP!=null && lastP.remaining == currP.remaining ) currP=lastP;
                if(lastP!=currP && lastP!=null ){
                    if(cs!=0) {
                        r.log("Context switch time " + time +"-"+(time+cs));
                        for (int k = 0; k < cs; k++) r.unit(time + k, "CS");
                    }
                    time+=cs;
                    lastP=currP;
                }
                if(lastP==null) lastP=currP;

                if(currP.firstResponse==-1) currP.firstResponse=time;
                currP.remaining--;
                if(currP.remaining==0) currP.completion=time+1;

                r.log("time " + time +"-"+(time+1)+ ": P" + currP.id);
                r.unit(time, "P" + currP.id);
            }
            time++;
        }

        double smT=0,smW=0;
        for (Process p : list) {
            p.turnaroundTime=p.completion-p.arrival;
            p.waitingTime=p.turnaroundTime-p.burst;
            p.responseTime=p.firstResponse-p.arrival;
            smT+=p.turnaroundTime;
            smW+=p.waitingTime;
        }
        r.avgTurnaround = smT/n;
        r.avgWaiting = smW/n;
        return r;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SchedulerGUI().setVisible(true));
    }
}