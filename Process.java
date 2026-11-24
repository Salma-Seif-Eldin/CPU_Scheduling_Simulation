
import java.util.List;

public class Process {
    int id;
    int arrival;
    int burst;
    int priority;
 

    int remaining ;     
    int firstResponse=-1; 
    int completion ;
    
   int turnaroundTime;
   int waitingTime;
   int responseTime;

    Process(int id, int arrival, int burst ,int priority) {
        this.id = id;
        this.arrival = arrival;
        this.burst = burst;
        this.priority=priority;
        this.remaining = burst;
    }
    
}
