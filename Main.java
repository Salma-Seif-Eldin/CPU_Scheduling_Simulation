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
    public static void main(String[] args) {
        Scanner inp = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = inp.nextInt();

        List<Process> list = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.println("Process " + (i + 1) + ":");
            System.out.print("Enter the arrival time of P"+(i+1)+": ");
            int arrivalTime= inp.nextInt();
            System.out.print("Enter the burst time of P"+(i+1)+": ");
            int burstTime= inp.nextInt();
            Process p = new Process(i+1,arrivalTime,burstTime);
            list.add(p);
           
    
        }

        System.out.print("Context switch time (CS): ");
        int cs = inp.nextInt();
        int time=0;
        Process lastP=null;
        while(!allDone(list)){
            Process currP = pickShortest(list,time);
            if(currP==null) System.out.println("time " + time +"-"+(time+1) + ": No process");
            else {
                if (lastP!=null && lastP.remaining == currP.remaining ) currP=lastP;
                if(lastP!=currP && lastP!=null ){
                
                    if(cs!=0)System.out.println("Context switch time " + time +"-"+(time+cs));
                    time+=cs;
                    lastP=currP;
                }
                if(lastP==null) lastP=currP;
                
                if(currP.firstResponse==-1) currP.firstResponse=time;
                currP.remaining--;
                if(currP.remaining==0) currP.completion=time+1;
                
                System.out.println("time " + time +"-"+(time+1)+ ": P" + currP.id);
            }
            
            time++;
            
           
            
             
           
           
        }
      
        for (Process p : list) {
           p.turnaroundTime=p.completion-p.arrival;
           p.waitingTime=p.turnaroundTime-p.burst;
           p.responseTime=p.firstResponse-p.arrival;
          
        }
       System.out.printf("%-20s %-20s %-20s %-20s%n", "Process", "Turnaround Time", "Waiting Time", "Response Time");
       double smT=0,smW=0;
        for(Process p:list) {
           System.out.printf("%-20s %-20d %-20d %-20d%n", "P" + (p.id), p.turnaroundTime, p.waitingTime, p.responseTime);
           smT+= p.turnaroundTime;
           smW+=p.waitingTime;
        }
        double averageTT=smT/n;
        double averageWT=smW/n;
        System.out.print("Average Turnaround Time = (");
        for(int i=0;i<n;i++) {
            System.out.print(list.get(i).turnaroundTime);
            if(i<n-1) System.out.print(" + ");
        }
        System.out.printf(") / %d = %.2f %n",n,averageTT);
        System.out.print("Average Waiting Time = (");
        for(int i=0;i<n;i++) {
            System.out.print(list.get(i).waitingTime);
            if(i<n-1) System.out.print(" + ");
        }
        System.out.printf(") / %d = %.2f %n",n,averageWT);
        
        
        
                
    }
}